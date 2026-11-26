package br.ufrn.smorg.data

import br.ufrn.smorg.domain.EquipmentStatus
import br.ufrn.smorg.domain.RepositoryFailure
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.test.runTest
import kotlinx.io.IOException
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class RemoteEquipmentRepositoryTest {
    private val jsonHeaders = headersOf(HttpHeaders.ContentType, "application/json")

    private fun createRepository(mockEngine: MockEngine): RemoteEquipmentRepository {
        val client =
            HttpClient(mockEngine) {
                expectSuccess = true
                install(ContentNegotiation) {
                    json(
                        Json {
                            ignoreUnknownKeys = true
                            isLenient = true
                        },
                    )
                }
            }
        return RemoteEquipmentRepository(
            client = client,
            baseUrl = "http://api.local/api/v1",
        )
    }

    @Test
    fun convertsApiJsonToDomainEquipmentIgnoringUnknownKeys() =
        runTest {
            val payload =
                """
                [
                    {
                        "id": "EQ-10",
                        "nome": "Osciloscópio Digital",
                        "nfc_tag_id": "04:AA:BB:CC",
                        "status": "DISPONIVEL",
                        "categoria": "Bancada",
                        "campo_novo_desconhecido": 123
                    }
                ]
                """.trimIndent()
            val repo = createRepository(MockEngine { respond(payload, headers = jsonHeaders) })

            val listing = repo.listEquipments()
            val item = listing.items.single()

            assertEquals("EQ-10", item.id)
            assertEquals("Osciloscópio Digital", item.name)
            assertEquals("04:AA:BB:CC", item.nfcTagId)
            assertEquals(EquipmentStatus.AVAILABLE, item.status)
            assertEquals("Bancada", item.category)
        }

    @Test
    fun serverOfflineThrowsNoConnection() =
        runTest {
            val repo = createRepository(MockEngine { throw IOException("Conexão recusada") })

            assertFailsWith<RepositoryFailure.NoConnection> {
                repo.listEquipments()
            }
        }

    @Test
    fun serverError500ThrowsInvalidResponse() =
        runTest {
            val repo =
                createRepository(
                    MockEngine {
                        respond("Erro interno", HttpStatusCode.InternalServerError)
                    },
                )

            assertFailsWith<RepositoryFailure.InvalidResponse> {
                repo.listEquipments()
            }
        }

    @Test
    fun malformedJsonThrowsInvalidResponse() =
        runTest {
            val repo =
                createRepository(
                    MockEngine {
                        respond("""{"invalido": "nao_e_lista"}""", headers = jsonHeaders)
                    },
                )

            assertFailsWith<RepositoryFailure.InvalidResponse> {
                repo.listEquipments()
            }
        }

    @Test
    fun getEquipmentByIdReturnsMappedDomainEntity() =
        runTest {
            val payload =
                """
                {
                    "id": "EQ-20",
                    "nome": "Multímetro True RMS",
                    "nfc_tag_id": null,
                    "status": "EMPRESTADO",
                    "categoria": "Medição"
                }
                """.trimIndent()
            val repo = createRepository(MockEngine { respond(payload, headers = jsonHeaders) })

            val item = repo.getEquipmentById("EQ-20")

            assertEquals("EQ-20", item.id)
            assertEquals("Sem Tag NFC", item.nfcTagId)
            assertEquals(EquipmentStatus.BORROWED, item.status)
        }
}
