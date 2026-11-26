package br.ufrn.smorg.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import br.ufrn.smorg.domain.Equipment
import br.ufrn.smorg.domain.EquipmentListing
import br.ufrn.smorg.domain.EquipmentRepository
import br.ufrn.smorg.domain.EquipmentStatus
import br.ufrn.smorg.domain.RepositoryFailure
import br.ufrn.smorg.domain.UserRole
import br.ufrn.smorg.domain.replacingEquipment
import kotlinx.coroutines.flow.first
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import okio.Path.Companion.toPath

/**
 * Armazena a última lista de equipamentos recebida da API em um arquivo local do aparelho
 * usando DataStore Preferences. Sobrevive ao fechamento do app.
 */
class EquipmentCache(
    private val dataStore: DataStore<Preferences>,
) {
    private val lastListKey = stringPreferencesKey("ultima_lista_equipamentos")

    suspend fun save(items: List<Equipment>) {
        val dtos = items.map { it.toDto() }
        dataStore.edit { prefs ->
            prefs[lastListKey] = Json.encodeToString(dtos)
        }
    }

    suspend fun read(): List<Equipment>? =
        dataStore.data.first()[lastListKey]?.let { json ->
            Json.decodeFromString<List<EquipamentoDto>>(json).map { it.toDomain() }
        }
}

/**
 * Padrão Decorator: envolve o repositório remoto, salvando cada resposta no cache local
 * e devolvendo os dados salvos (com `fromCache = true`) quando não há conexão com o servidor.
 */
class CachedEquipmentRepository(
    private val remote: EquipmentRepository,
    private val cache: EquipmentCache,
) : EquipmentRepository {
    override suspend fun listEquipments(status: EquipmentStatus?): EquipmentListing =
        try {
            val previousCachedById = cache.read().orEmpty().associateBy { it.id }
            val remoteListing = remote.listEquipments(status)
            val mergedItems =
                remoteListing.items.map { remoteItem ->
                    val cachedItem = previousCachedById[remoteItem.id]
                    if (remoteItem.status == EquipmentStatus.BORROWED && cachedItem != null) {
                        remoteItem.copy(
                            borrowerName = remoteItem.borrowerName ?: cachedItem.borrowerName,
                            returnDueDate = remoteItem.returnDueDate ?: cachedItem.returnDueDate,
                        )
                    } else {
                        remoteItem
                    }
                }
            if (status == null || previousCachedById.isEmpty()) {
                cache.save(mergedItems)
            }
            remoteListing.copy(items = mergedItems)
        } catch (e: RepositoryFailure.NoConnection) {
            val saved = cache.read() ?: throw e
            val filtered =
                if (status == null) {
                    saved
                } else {
                    saved.filter { it.status == status }
                }
            EquipmentListing(items = filtered, fromCache = true)
        }

    override suspend fun getEquipmentById(id: String): Equipment =
        try {
            val remoteItem = remote.getEquipmentById(id)
            val cachedItem = cache.read()?.find { it.id == id }
            if (remoteItem.status == EquipmentStatus.BORROWED && cachedItem != null) {
                remoteItem.copy(
                    borrowerName = remoteItem.borrowerName ?: cachedItem.borrowerName,
                    returnDueDate = remoteItem.returnDueDate ?: cachedItem.returnDueDate,
                )
            } else {
                remoteItem
            }
        } catch (e: RepositoryFailure.NoConnection) {
            cache.read()?.find { it.id == id } ?: throw e
        }

    override suspend fun getEquipmentByTag(tag: String): Equipment =
        try {
            remote.getEquipmentByTag(tag)
        } catch (e: RepositoryFailure.NoConnection) {
            cache.read()?.find { it.nfcTagId.equals(tag, ignoreCase = true) } ?: throw e
        }

    override suspend fun createEquipment(
        name: String,
        category: String,
        nfcTagId: String?,
    ): Equipment =
        remote.createEquipment(name, category, nfcTagId).also { created ->
            cache.save(listOf(created) + cache.read().orEmpty())
        }

    override suspend fun borrowEquipment(
        equipmentId: String,
        userId: String,
        borrowerName: String,
        userRole: UserRole,
    ): Equipment =
        remote.borrowEquipment(equipmentId, userId, borrowerName, userRole).also { updated ->
            val currentCached = cache.read().orEmpty()
            if (currentCached.isNotEmpty()) {
                cache.save(currentCached.replacingEquipment(updated))
            }
        }

    override suspend fun returnEquipment(equipmentId: String): Equipment =
        remote.returnEquipment(equipmentId).also { updated ->
            val currentCached = cache.read().orEmpty()
            if (currentCached.isNotEmpty()) {
                cache.save(currentCached.replacingEquipment(updated))
            }
        }
}

fun createDataStore(directory: String): DataStore<Preferences> =
    PreferenceDataStoreFactory.createWithPath(
        produceFile = { "$directory/smorg_equipments.preferences_pb".toPath() },
    )
