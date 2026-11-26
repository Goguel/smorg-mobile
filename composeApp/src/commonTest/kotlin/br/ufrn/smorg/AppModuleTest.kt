package br.ufrn.smorg

import br.ufrn.smorg.data.CachedEquipmentRepository
import br.ufrn.smorg.data.InMemoryEquipmentRepository
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertIs
import kotlin.test.assertNotNull

class AppModuleTest {
    @Test
    fun moduleCreatesViewModelWithInMemoryRepository() {
        val module = AppModule(apiBaseUrl = null, dataDirectory = "build/test-cache-${Random.nextLong()}")
        assertIs<InMemoryEquipmentRepository>(module.equipmentRepository)
        val viewModel = module.createCatalogViewModel()
        assertNotNull(viewModel)
    }

    @Test
    fun moduleCreatesViewModelWithCachedRemoteRepository() {
        val module =
            AppModule(
                apiBaseUrl = "http://localhost:8080/api/v1",
                dataDirectory = "build/test-cache-${Random.nextLong()}",
            )
        assertIs<CachedEquipmentRepository>(module.equipmentRepository)
        val viewModel = module.createCatalogViewModel()
        assertNotNull(viewModel)
    }
}
