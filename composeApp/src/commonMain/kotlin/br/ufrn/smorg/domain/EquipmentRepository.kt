package br.ufrn.smorg.domain

interface EquipmentRepository {
    suspend fun listEquipments(status: EquipmentStatus? = null): EquipmentListing

    suspend fun getEquipmentById(id: String): Equipment

    suspend fun getEquipmentByTag(tag: String): Equipment

    suspend fun createEquipment(
        name: String,
        category: String,
        nfcTagId: String?,
    ): Equipment

    suspend fun borrowEquipment(
        equipmentId: String,
        userId: String,
        borrowerName: String,
        userRole: UserRole,
    ): Equipment

    suspend fun returnEquipment(equipmentId: String): Equipment
}

sealed class RepositoryFailure(
    message: String,
) : Exception(message) {
    class NoConnection : RepositoryFailure("Sem conexão com o servidor")

    class InvalidResponse(
        detail: String,
    ) : RepositoryFailure("Resposta inválida: $detail")
}
