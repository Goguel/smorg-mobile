package br.ufrn.smorg.model

enum class EquipmentStatus {
    AVAILABLE,
    BORROWED,
    MAINTENANCE,
}

data class Equipment(
    val id: String,
    val name: String,
    val category: String,
    val nfcTagId: String,
    val status: EquipmentStatus,
    val borrowerName: String? = null,
    val returnDueDate: String? = null,
)
