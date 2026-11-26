package br.ufrn.smorg

import br.ufrn.smorg.data.CachedEquipmentRepository
import br.ufrn.smorg.data.EquipmentCache
import br.ufrn.smorg.data.InMemoryEquipmentRepository
import br.ufrn.smorg.data.RemoteEquipmentRepository
import br.ufrn.smorg.data.createDataStore
import br.ufrn.smorg.data.createHttpClient
import br.ufrn.smorg.data.defaultApiBaseUrl
import br.ufrn.smorg.domain.EquipmentRepository
import br.ufrn.smorg.presentation.CatalogViewModel
import br.ufrn.smorg.presentation.LoginViewModel
import io.ktor.client.HttpClient

/**
 * Raiz de composição (Injeção de Dependência): o único ponto que conhece as camadas
 * de dados e apresentação e as conecta através das interfaces de domínio.
 * O diretório de dados vem de cada plataforma (filesDir no Android, pasta do usuário no Desktop).
 */
class AppModule(
    val apiBaseUrl: String? = defaultApiBaseUrl,
    val dataDirectory: String = "build/smorg-cache",
) {
    val httpClient: HttpClient by lazy {
        createHttpClient()
    }

    val equipmentCache: EquipmentCache by lazy {
        EquipmentCache(createDataStore(dataDirectory))
    }

    val equipmentRepository: EquipmentRepository by lazy {
        if (apiBaseUrl == null) {
            InMemoryEquipmentRepository()
        } else {
            CachedEquipmentRepository(
                remote =
                    RemoteEquipmentRepository(
                        client = httpClient,
                        baseUrl = apiBaseUrl,
                    ),
                cache = equipmentCache,
            )
        }
    }

    fun createCatalogViewModel(): CatalogViewModel =
        CatalogViewModel(
            repository = equipmentRepository,
        )

    fun createLoginViewModel(): LoginViewModel = LoginViewModel()
}
