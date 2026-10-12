package br.ufrn.smorg.data.remote

import br.ufrn.smorg.model.Equipment
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

object SmorgApiClient {
    private const val BASE_URL = "https://smorg-api.onrender.com/api/v1"

    val client: HttpClient by lazy {
        HttpClient {
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
                requestTimeoutMillis = 60_000
                connectTimeoutMillis = 60_000
                socketTimeoutMillis = 60_000
            }
        }
    }

    suspend fun listarEquipamentos(status: String? = null): List<Equipment> {
        val url =
            if (!status.isNullOrBlank()) {
                "$BASE_URL/equipamentos?status=$status"
            } else {
                "$BASE_URL/equipamentos"
            }

        val dtos: List<EquipamentoDto> = client.get(url).body()
        return dtos.map { it.toEquipment() }
    }

    suspend fun buscarPorId(id: String): Equipment {
        val dto: EquipamentoDto = client.get("$BASE_URL/equipamentos/$id").body()
        return dto.toEquipment()
    }

    suspend fun buscarPorTag(tag: String): Equipment {
        val dto: EquipamentoDto = client.get("$BASE_URL/equipamentos/tag/$tag").body()
        return dto.toEquipment()
    }
}
