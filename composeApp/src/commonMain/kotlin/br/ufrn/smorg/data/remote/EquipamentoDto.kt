package br.ufrn.smorg.data.remote

import br.ufrn.smorg.model.Equipment
import br.ufrn.smorg.model.EquipmentStatus
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class EquipamentoDto(
    val id: String,
    val nome: String,
    @SerialName("nfc_tag_id")
    val nfcTagId: String? = null,
    val status: String,
    val categoria: String,
)

fun EquipamentoDto.toEquipment(): Equipment {
    val mappedStatus =
        when (status.uppercase()) {
            "DISPONIVEL", "AVAILABLE" -> EquipmentStatus.AVAILABLE
            "EMPRESTADO", "BORROWED" -> EquipmentStatus.BORROWED
            "MANUTENCAO", "MAINTENANCE" -> EquipmentStatus.MAINTENANCE
            else -> EquipmentStatus.AVAILABLE
        }

    return Equipment(
        id = id,
        name = nome,
        category = categoria,
        nfcTagId = nfcTagId ?: "Sem Tag NFC",
        status = mappedStatus,
    )
}
