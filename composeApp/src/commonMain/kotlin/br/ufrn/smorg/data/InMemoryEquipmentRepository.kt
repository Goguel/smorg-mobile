package br.ufrn.smorg.data

import br.ufrn.smorg.domain.DEFAULT_RETURN_DUE_DATE
import br.ufrn.smorg.domain.Equipment
import br.ufrn.smorg.domain.EquipmentListing
import br.ufrn.smorg.domain.EquipmentRepository
import br.ufrn.smorg.domain.EquipmentStatus
import br.ufrn.smorg.domain.RepositoryFailure
import br.ufrn.smorg.domain.UserRole
import br.ufrn.smorg.domain.initialSampleEquipments
import kotlinx.coroutines.delay
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

class InMemoryEquipmentRepository(
    private val delayDuration: Duration = 300.milliseconds,
    var shouldFail: Boolean = false,
    initialItems: List<Equipment> = initialSampleEquipments,
) : EquipmentRepository {
    private val equipments = initialItems.toMutableList()
    private var nextSequence = equipments.size + 1

    override suspend fun listEquipments(status: EquipmentStatus?): EquipmentListing {
        delay(delayDuration)
        if (shouldFail) throw RepositoryFailure.NoConnection()
        val filtered =
            if (status == null) {
                equipments.toList()
            } else {
                equipments.filter { it.status == status }
            }
        return EquipmentListing(items = filtered)
    }

    override suspend fun getEquipmentById(id: String): Equipment {
        delay(delayDuration)
        if (shouldFail) throw RepositoryFailure.NoConnection()
        return equipments.find { it.id == id }
            ?: throw RepositoryFailure.InvalidResponse("Equipamento $id não encontrado")
    }

    override suspend fun getEquipmentByTag(tag: String): Equipment {
        delay(delayDuration)
        if (shouldFail) throw RepositoryFailure.NoConnection()
        return equipments.find { it.nfcTagId.equals(tag, ignoreCase = true) }
            ?: throw RepositoryFailure.InvalidResponse("Tag NFC $tag não encontrada")
    }

    override suspend fun createEquipment(
        name: String,
        category: String,
        nfcTagId: String?,
    ): Equipment {
        delay(delayDuration)
        if (shouldFail) throw RepositoryFailure.NoConnection()
        val formattedId = "EQ-${nextSequence.toString().padStart(3, '0')}"
        nextSequence++
        val created =
            Equipment(
                id = formattedId,
                name = name.trim(),
                category = category.trim(),
                nfcTagId = nfcTagId?.trim()?.takeIf { it.isNotEmpty() } ?: "Sem Tag NFC",
                status = EquipmentStatus.AVAILABLE,
            )
        equipments.add(0, created)
        return created
    }

    override suspend fun borrowEquipment(
        equipmentId: String,
        userId: String,
        borrowerName: String,
        userRole: UserRole,
    ): Equipment {
        delay(delayDuration)
        if (shouldFail) throw RepositoryFailure.NoConnection()
        val index =
            equipments
                .indexOfFirst { it.id == equipmentId }
                .takeIf { it >= 0 }
                ?: throw RepositoryFailure.InvalidResponse("Equipamento $equipmentId não encontrado")
        val updated =
            equipments[index].copy(
                status = EquipmentStatus.BORROWED,
                borrowerName = borrowerName,
                returnDueDate = DEFAULT_RETURN_DUE_DATE,
            )
        equipments[index] = updated
        return updated
    }

    override suspend fun returnEquipment(equipmentId: String): Equipment {
        delay(delayDuration)
        if (shouldFail) throw RepositoryFailure.NoConnection()
        val index =
            equipments
                .indexOfFirst { it.id == equipmentId }
                .takeIf { it >= 0 }
                ?: throw RepositoryFailure.InvalidResponse("Equipamento $equipmentId não encontrado")
        val updated =
            equipments[index].copy(
                status = EquipmentStatus.AVAILABLE,
                borrowerName = null,
                returnDueDate = null,
            )
        equipments[index] = updated
        return updated
    }
}
