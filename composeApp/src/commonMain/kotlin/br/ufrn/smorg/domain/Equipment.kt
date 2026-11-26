package br.ufrn.smorg.domain

enum class EquipmentStatus {
    AVAILABLE,
    BORROWED,
    MAINTENANCE,
}

enum class UserRole(
    val backendCode: String,
    val label: String,
    val badgeText: String,
    val description: String,
    val canManageInventory: Boolean,
    val canRegisterReturn: Boolean,
) {
    STUDENT(
        backendCode = "ALUNO",
        label = "Aluno",
        badgeText = "🎓 Perfil: Aluno",
        description = "Consulta o acervo, lê tags NFC e retira equipamentos para aulas práticas.",
        canManageInventory = false,
        canRegisterReturn = false,
    ),
    MONITOR(
        backendCode = "MONITOR",
        label = "Monitor",
        badgeText = "🛠️ Perfil: Monitor",
        description = "Realiza conferência de bancada, registra devoluções e cadastra equipamentos.",
        canManageInventory = true,
        canRegisterReturn = true,
    ),
    PROFESSOR(
        backendCode = "PROFESSOR",
        label = "Professor",
        badgeText = "👨‍🏫 Perfil: Professor",
        description = "Supervisiona o laboratório, gerencia ativos e possui prazo estendido de retirada.",
        canManageInventory = true,
        canRegisterReturn = true,
    ),
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

data class EquipmentListing(
    val items: List<Equipment>,
    val fromCache: Boolean = false,
)

fun isIdentifierValid(identifier: String): Boolean = identifier.isNotBlank()

fun isPasswordValid(password: String): Boolean = password.length >= MIN_PASSWORD_LENGTH

fun isEquipmentNameValid(name: String): Boolean = name.trim().length >= MIN_EQUIPMENT_NAME_LENGTH

fun isEquipmentCategoryValid(category: String): Boolean = category.trim().isNotBlank()

fun deterministicUserUuid(identifier: String): String {
    val clean = identifier.trim().lowercase().ifEmpty { "default-user" }
    val hash =
        clean
            .hashCode()
            .toUInt()
            .toString(HEX_RADIX)
            .padStart(HEX_PAD_LENGTH, '0')
    val lenHex = (clean.length % MAX_HEX_BYTE).toString(HEX_RADIX).padStart(4, '0')
    return "00000000-0000-4000-8000-$lenHex$hash"
}

fun formatIsoDateToBr(isoString: String?): String {
    if (isoString.isNullOrBlank()) return DEFAULT_RETURN_DUE_DATE
    val datePart = isoString.substringBefore("T")
    val pieces = datePart.split("-")
    return if (pieces.size == ISO_DATE_PARTS) {
        "${pieces[2]}/${pieces[1]}/${pieces[0]} às 18:00"
    } else {
        isoString
    }
}

fun formatDisplayName(identifier: String): String {
    val clean = identifier.trim()
    if (clean.isEmpty()) return "Estudante"
    val beforeAt = clean.substringBefore("@")
    return if (beforeAt.all { it.isDigit() }) {
        "Estudante ($beforeAt)"
    } else {
        beforeAt
            .replace(".", " ")
            .replace("_", " ")
            .split(" ")
            .filter { it.isNotBlank() }
            .joinToString(" ") { part ->
                part.lowercase().replaceFirstChar { it.uppercase() }
            }
    }
}

fun filterEquipments(
    items: List<Equipment>,
    query: String,
): List<Equipment> {
    val trimmed = query.trim()
    if (trimmed.isEmpty()) return items
    return items.filter { eq ->
        eq.name.contains(trimmed, ignoreCase = true) ||
            eq.category.contains(trimmed, ignoreCase = true) ||
            eq.nfcTagId.contains(trimmed, ignoreCase = true) ||
            eq.id.contains(trimmed, ignoreCase = true)
    }
}

fun List<Equipment>.borrowingEquipment(
    id: String,
    borrowerName: String,
    returnDueDate: String = DEFAULT_RETURN_DUE_DATE,
): List<Equipment> =
    map { item ->
        if (item.id == id) {
            item.copy(
                status = EquipmentStatus.BORROWED,
                borrowerName = borrowerName,
                returnDueDate = returnDueDate,
            )
        } else {
            item
        }
    }

fun List<Equipment>.returningEquipment(id: String): List<Equipment> =
    map { item ->
        if (item.id == id) {
            item.copy(
                status = EquipmentStatus.AVAILABLE,
                borrowerName = null,
                returnDueDate = null,
            )
        } else {
            item
        }
    }

fun List<Equipment>.replacingEquipment(updated: Equipment): List<Equipment> =
    map { item -> if (item.id == updated.id) updated else item }

private const val MIN_PASSWORD_LENGTH = 6
private const val MIN_EQUIPMENT_NAME_LENGTH = 3
private const val HEX_RADIX = 16
private const val HEX_PAD_LENGTH = 8
private const val MAX_HEX_BYTE = 65535
private const val ISO_DATE_PARTS = 3
const val DEFAULT_RETURN_DUE_DATE = "02/12/2026 às 18:00"

val initialSampleEquipments =
    listOf(
        Equipment(
            id = "EQ-001",
            name = "Osciloscópio Digital 100MHz",
            category = "Bancada e Medição",
            nfcTagId = "04:A2:8B:1A:5C",
            status = EquipmentStatus.AVAILABLE,
        ),
        Equipment(
            id = "EQ-002",
            name = "Multímetro Digital True RMS",
            category = "Instrumentação",
            nfcTagId = "04:C5:91:2B:7E",
            status = EquipmentStatus.BORROWED,
            borrowerName = "Miguel Morais",
            returnDueDate = "28/11/2026 às 18:00",
        ),
        Equipment(
            id = "EQ-003",
            name = "Kit Arduino Mega 2560",
            category = "Sistemas Embarcados",
            nfcTagId = "04:D1:44:8F:12",
            status = EquipmentStatus.MAINTENANCE,
        ),
    )
