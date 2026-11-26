package br.ufrn.smorg.data

import br.ufrn.smorg.domain.RepositoryFailure
import kotlinx.coroutines.test.runTest
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.time.Duration

class CachedEquipmentRepositoryTest {
    private fun createTempCache(): EquipmentCache =
        EquipmentCache(
            createDataStore("build/tmp/datastore-test-${Random.nextLong()}"),
        )

    @Test
    fun withConnectionReturnsRemoteDataAndSavesToLocalCache() =
        runTest {
            val remote = InMemoryEquipmentRepository(delayDuration = Duration.ZERO)
            val cache = createTempCache()
            val repository = CachedEquipmentRepository(remote = remote, cache = cache)

            val listing = repository.listEquipments()

            assertFalse(listing.fromCache)
            assertEquals(listing.items, cache.read())
        }

    @Test
    fun withoutConnectionReturnsLastCachedListMarkedAsFromCache() =
        runTest {
            val remote = InMemoryEquipmentRepository(delayDuration = Duration.ZERO)
            val repository = CachedEquipmentRepository(remote = remote, cache = createTempCache())

            val initialListing = repository.listEquipments()
            remote.shouldFail = true

            val offlineListing = repository.listEquipments()

            assertTrue(offlineListing.fromCache)
            assertEquals(initialListing.items.size, offlineListing.items.size)
        }

    @Test
    fun withoutConnectionAndNothingCachedPropagatesNoConnectionFailure() =
        runTest {
            val remote = InMemoryEquipmentRepository(delayDuration = Duration.ZERO, shouldFail = true)
            val repository = CachedEquipmentRepository(remote = remote, cache = createTempCache())

            assertFailsWith<RepositoryFailure.NoConnection> {
                repository.listEquipments()
            }
        }
}
