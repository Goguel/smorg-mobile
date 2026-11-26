package br.ufrn.smorg.presentation

import app.cash.turbine.test
import br.ufrn.smorg.domain.DEFAULT_RETURN_DUE_DATE
import br.ufrn.smorg.domain.Equipment
import br.ufrn.smorg.domain.EquipmentListing
import br.ufrn.smorg.domain.EquipmentRepository
import br.ufrn.smorg.domain.EquipmentStatus
import br.ufrn.smorg.domain.RepositoryFailure
import br.ufrn.smorg.domain.UserRole
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

private const val VIRTUAL_NETWORK_DELAY_MS = 500L

class FakeEquipmentRepository(
    var onList: (EquipmentStatus?) -> EquipmentListing = {
        EquipmentListing(
            items =
                listOf(
                    Equipment(
                        id = "EQ-1",
                        name = "Osciloscópio Digital",
                        category = "Bancada",
                        nfcTagId = "04:AA:BB:CC",
                        status = EquipmentStatus.AVAILABLE,
                    ),
                ),
        )
    },
    var onGetById: (String) -> Equipment = { id ->
        Equipment(
            id = id,
            name = "Multímetro True RMS",
            category = "Medição",
            nfcTagId = "04:11:22:33",
            status = EquipmentStatus.AVAILABLE,
        )
    },
) : EquipmentRepository {
    override suspend fun listEquipments(status: EquipmentStatus?): EquipmentListing {
        delay(VIRTUAL_NETWORK_DELAY_MS)
        return onList(status)
    }

    override suspend fun getEquipmentById(id: String): Equipment {
        delay(VIRTUAL_NETWORK_DELAY_MS)
        return onGetById(id)
    }

    override suspend fun getEquipmentByTag(tag: String): Equipment {
        delay(VIRTUAL_NETWORK_DELAY_MS)
        return onGetById(tag)
    }

    override suspend fun createEquipment(
        name: String,
        category: String,
        nfcTagId: String?,
    ): Equipment {
        delay(VIRTUAL_NETWORK_DELAY_MS)
        return Equipment(
            id = "EQ-NEW",
            name = name,
            category = category,
            nfcTagId = nfcTagId ?: "Sem Tag NFC",
            status = EquipmentStatus.AVAILABLE,
        )
    }

    override suspend fun borrowEquipment(
        equipmentId: String,
        userId: String,
        borrowerName: String,
        userRole: UserRole,
    ): Equipment {
        delay(VIRTUAL_NETWORK_DELAY_MS)
        return onGetById(equipmentId).copy(
            status = EquipmentStatus.BORROWED,
            borrowerName = borrowerName,
            returnDueDate = DEFAULT_RETURN_DUE_DATE,
        )
    }

    override suspend fun returnEquipment(equipmentId: String): Equipment {
        delay(VIRTUAL_NETWORK_DELAY_MS)
        return onGetById(equipmentId).copy(
            status = EquipmentStatus.AVAILABLE,
            borrowerName = null,
            returnDueDate = null,
        )
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
class CatalogViewModelTest {
    private fun TestScope.useVirtualMainDispatcher() {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
    }

    @AfterTest
    fun resetMainDispatcher() {
        Dispatchers.resetMain()
    }

    @Test
    fun loadPassesThroughLoadingAndReachesSuccess() =
        runTest {
            useVirtualMainDispatcher()
            val viewModel = CatalogViewModel(FakeEquipmentRepository())

            viewModel.state.test {
                assertEquals(CatalogScreenState.Loading, awaitItem().screen)
                val success = assertIs<CatalogScreenState.Success>(awaitItem().screen)
                assertEquals("Osciloscópio Digital", success.items.single().name)
            }
        }

    @Test
    fun emptyListTransitionsToEmptyState() =
        runTest {
            useVirtualMainDispatcher()
            val repo = FakeEquipmentRepository(onList = { EquipmentListing(emptyList()) })
            val viewModel = CatalogViewModel(repo)

            viewModel.state.test {
                assertEquals(CatalogScreenState.Loading, awaitItem().screen)
                assertEquals(CatalogScreenState.Empty, awaitItem().screen)
            }
        }

    @Test
    fun noConnectionFailureTransitionsToErrorWithConnectionMessage() =
        runTest {
            useVirtualMainDispatcher()
            val repo = FakeEquipmentRepository(onList = { throw RepositoryFailure.NoConnection() })
            val viewModel = CatalogViewModel(repo)

            viewModel.state.test {
                assertEquals(CatalogScreenState.Loading, awaitItem().screen)
                val error = assertIs<CatalogScreenState.Error>(awaitItem().screen)
                assertTrue("Sem conexão" in error.message)
            }
        }

    @Test
    fun invalidResponseFailureTransitionsToErrorWithServerMessage() =
        runTest {
            useVirtualMainDispatcher()
            val repo =
                FakeEquipmentRepository(
                    onList = { throw RepositoryFailure.InvalidResponse("status 500") },
                )
            val viewModel = CatalogViewModel(repo)

            viewModel.state.test {
                assertEquals(CatalogScreenState.Loading, awaitItem().screen)
                val error = assertIs<CatalogScreenState.Error>(awaitItem().screen)
                assertTrue("inesperada" in error.message)
            }
        }

    @Test
    fun retryAfterErrorLoadsEquipmentsSuccessfully() =
        runTest {
            useVirtualMainDispatcher()
            val repo = FakeEquipmentRepository(onList = { throw RepositoryFailure.NoConnection() })
            val viewModel = CatalogViewModel(repo)

            viewModel.state.test {
                skipItems(1) // Loading inicial
                assertIs<CatalogScreenState.Error>(awaitItem().screen)

                repo.onList = {
                    EquipmentListing(
                        listOf(
                            Equipment(
                                id = "EQ-2",
                                name = "Fonte de Bancada",
                                category = "Energia",
                                nfcTagId = "04:FF:EE:DD",
                                status = EquipmentStatus.AVAILABLE,
                            ),
                        ),
                    )
                }

                viewModel.loadEquipments()
                assertEquals(CatalogScreenState.Loading, awaitItem().screen)
                val recovered = assertIs<CatalogScreenState.Success>(awaitItem().screen)
                assertEquals("Fonte de Bancada", recovered.items.single().name)
            }
        }

    @Test
    fun cachedListArrivesMarkedWithFromCacheTrue() =
        runTest {
            useVirtualMainDispatcher()
            val repo =
                FakeEquipmentRepository(
                    onList = {
                        EquipmentListing(
                            items =
                                listOf(
                                    Equipment(
                                        id = "EQ-CACHE",
                                        name = "Item Salvo Offline",
                                        category = "Cache",
                                        nfcTagId = "04:00:11:22",
                                        status = EquipmentStatus.AVAILABLE,
                                    ),
                                ),
                            fromCache = true,
                        )
                    },
                )
            val viewModel = CatalogViewModel(repo)

            viewModel.state.test {
                skipItems(1)
                val success = assertIs<CatalogScreenState.Success>(awaitItem().screen)
                assertTrue(success.fromCache)
            }
        }

    @Test
    fun selectFilterUpdatesSelectedFilterAndReloadsEquipments() =
        runTest {
            useVirtualMainDispatcher()
            val viewModel = CatalogViewModel(FakeEquipmentRepository())

            viewModel.state.test {
                skipItems(2) // Loading, Success inicial
                viewModel.selectFilter(EquipmentStatus.BORROWED)

                val filterUpdated = awaitItem()
                assertEquals(EquipmentStatus.BORROWED, filterUpdated.selectedFilter)

                val loadingWithFilter = awaitItem()
                assertEquals(CatalogScreenState.Loading, loadingWithFilter.screen)

                val reloaded = awaitItem()
                assertEquals(EquipmentStatus.BORROWED, reloaded.selectedFilter)
                assertIs<CatalogScreenState.Success>(reloaded.screen)
            }
        }

    @Test
    fun createEquipmentAddsNewItemToCatalogList() =
        runTest {
            useVirtualMainDispatcher()
            val viewModel = CatalogViewModel(FakeEquipmentRepository())

            viewModel.state.test {
                skipItems(2) // Loading, Success inicial
                viewModel.createEquipment(
                    name = "Gerador de Funções 20MHz",
                    category = "Instrumentação",
                    nfcTagId = "04:99:88:77",
                )

                val updated = awaitItem()
                assertEquals("Gerador de Funções 20MHz", updated.items.first().name)
            }
        }
}
