package br.ufrn.smorg.data

import br.ufrn.smorg.domain.Equipment
import br.ufrn.smorg.domain.EquipmentListing
import br.ufrn.smorg.domain.EquipmentRepository
import br.ufrn.smorg.domain.EquipmentStatus
import br.ufrn.smorg.domain.RepositoryFailure
import br.ufrn.smorg.domain.UserRole
import br.ufrn.smorg.domain.formatIsoDateToBr
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.ResponseException
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.ContentConvertException
import io.ktor.serialization.kotlinx.json.json
import kotlinx.io.IOException
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json

private const val DEFAULT_TIMEOUT_MILLIS = 60_000L

class RemoteEquipmentRepository(
    private val client: HttpClient,
    private val baseUrl: String,
) : EquipmentRepository {
    override suspend fun listEquipments(status: EquipmentStatus?): EquipmentListing =
        translateFailures {
            val backendStatus = status?.toBackendStatus()
            val url =
                if (!backendStatus.isNullOrBlank()) {
                    "$baseUrl/equipamentos?status=$backendStatus"
                } else {
                    "$baseUrl/equipamentos"
                }

            val dtos: List<EquipamentoDto> = client.get(url).body()
            EquipmentListing(items = dtos.map { it.toDomain() })
        }

    override suspend fun getEquipmentById(id: String): Equipment =
        translateFailures {
            val dto: EquipamentoDto = client.get("$baseUrl/equipamentos/$id").body()
            dto.toDomain()
        }

    override suspend fun getEquipmentByTag(tag: String): Equipment =
        translateFailures {
            val dto: EquipamentoDto = client.get("$baseUrl/equipamentos/tag/$tag").body()
            dto.toDomain()
        }

    override suspend fun createEquipment(
        name: String,
        category: String,
        nfcTagId: String?,
    ): Equipment =
        translateFailures {
            val dto: EquipamentoDto =
                client
                    .post("$baseUrl/equipamentos") {
                        contentType(ContentType.Application.Json)
                        setBody(
                            NovoEquipamentoDto(
                                nome = name.trim(),
                                categoria = category.trim(),
                                nfcTagId = nfcTagId?.trim()?.ifBlank { null },
                            ),
                        )
                    }.body()
            dto.toDomain()
        }

    override suspend fun borrowEquipment(
        equipmentId: String,
        userId: String,
        borrowerName: String,
        userRole: UserRole,
    ): Equipment =
        translateFailures {
            val response: EmprestimoResponseDto =
                client
                    .post("$baseUrl/emprestimos") {
                        contentType(ContentType.Application.Json)
                        setBody(
                            NovoEmprestimoDto(
                                usuarioId = userId,
                                equipamentoId = equipmentId,
                                perfilUsuario = userRole.backendCode,
                            ),
                        )
                    }.body()

            val baseEquipment =
                response.equipamento?.toDomain()
                    ?: getEquipmentById(equipmentId)

            baseEquipment.copy(
                status = EquipmentStatus.BORROWED,
                borrowerName = borrowerName,
                returnDueDate = formatIsoDateToBr(response.dataDevolucaoPrevista),
            )
        }

    override suspend fun returnEquipment(equipmentId: String): Equipment =
        translateFailures {
            try {
                val response: EmprestimoResponseDto =
                    client
                        .post("$baseUrl/emprestimos/devolucao") {
                            contentType(ContentType.Application.Json)
                            setBody(RegistrarDevolucaoDto(equipamentoId = equipmentId))
                        }.body()

                val baseEquipment =
                    response.equipamento?.toDomain()
                        ?: getEquipmentById(equipmentId)

                baseEquipment.copy(
                    status = EquipmentStatus.AVAILABLE,
                    borrowerName = null,
                    returnDueDate = null,
                )
            } catch (_: ResponseException) {
                val updatedDto: EquipamentoDto =
                    client
                        .put("$baseUrl/equipamentos/$equipmentId") {
                            contentType(ContentType.Application.Json)
                            setBody(AtualizarEquipamentoDto(status = "DISPONIVEL"))
                        }.body()
                updatedDto.toDomain().copy(
                    status = EquipmentStatus.AVAILABLE,
                    borrowerName = null,
                    returnDueDate = null,
                )
            }
        }

    @Suppress("SwallowedException")
    private suspend fun <T> translateFailures(call: suspend () -> T): T =
        try {
            call()
        } catch (e: IOException) {
            throw RepositoryFailure.NoConnection()
        } catch (e: ResponseException) {
            throw RepositoryFailure.InvalidResponse("status ${e.response.status.value}")
        } catch (e: ContentConvertException) {
            throw RepositoryFailure.InvalidResponse("JSON fora do formato")
        } catch (e: SerializationException) {
            throw RepositoryFailure.InvalidResponse("JSON fora do formato")
        }
}

fun createHttpClient(): HttpClient =
    HttpClient {
        expectSuccess = true

        install(ContentNegotiation) {
            json(
                Json {
                    prettyPrint = true
                    isLenient = true
                    ignoreUnknownKeys = true
                    explicitNulls = false
                },
            )
        }

        install(HttpTimeout) {
            requestTimeoutMillis = DEFAULT_TIMEOUT_MILLIS
            connectTimeoutMillis = DEFAULT_TIMEOUT_MILLIS
            socketTimeoutMillis = DEFAULT_TIMEOUT_MILLIS
        }
    }
