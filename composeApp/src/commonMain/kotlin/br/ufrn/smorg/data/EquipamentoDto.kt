package br.ufrn.smorg.data

import br.ufrn.smorg.domain.Equipment
import br.ufrn.smorg.domain.EquipmentStatus
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
    val responsavel: String? = null,
    @SerialName("prazo_devolucao")
    val prazoDevolucao: String? = null,
)

@Serializable
data class NovoEquipamentoDto(
    val nome: String,
    val categoria: String,
    @SerialName("nfc_tag_id")
    val nfcTagId: String? = null,
    val status: String = "DISPONIVEL",
)

@Serializable
data class AtualizarEquipamentoDto(
    val status: String,
)

@Serializable
data class NovoEmprestimoDto(
    @SerialName("usuario_id")
    val usuarioId: String,
    @SerialName("equipamento_id")
    val equipamentoId: String,
    @SerialName("perfil_usuario")
    val perfilUsuario: String,
)

@Serializable
data class RegistrarDevolucaoDto(
    @SerialName("equipamento_id")
    val equipamentoId: String,
)

@Serializable
data class EmprestimoResponseDto(
    val id: String,
    @SerialName("equipamento_id")
    val equipamentoId: String,
    @SerialName("data_devolucao_prevista")
    val dataDevolucaoPrevista: String? = null,
    val status: String? = null,
    val equipamento: EquipamentoDto? = null,
)

fun EquipamentoDto.toDomain(): Equipment {
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
        borrowerName = responsavel,
        returnDueDate = prazoDevolucao,
    )
}

fun Equipment.toDto(): EquipamentoDto =
    EquipamentoDto(
        id = id,
        nome = name,
        nfcTagId = nfcTagId,
        status = status.toBackendStatus(),
        categoria = category,
        responsavel = borrowerName,
        prazoDevolucao = returnDueDate,
    )

fun EquipmentStatus.toBackendStatus(): String =
    when (this) {
        EquipmentStatus.AVAILABLE -> "DISPONIVEL"
        EquipmentStatus.BORROWED -> "EMPRESTADO"
        EquipmentStatus.MAINTENANCE -> "MANUTENCAO"
    }
