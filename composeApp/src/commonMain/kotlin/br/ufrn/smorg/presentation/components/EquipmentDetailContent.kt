package br.ufrn.smorg.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import br.ufrn.smorg.domain.DEFAULT_RETURN_DUE_DATE
import br.ufrn.smorg.domain.Equipment
import br.ufrn.smorg.domain.EquipmentStatus
import br.ufrn.smorg.domain.UserRole

private enum class PendingConfirmationAction {
    BORROW,
    RETURN,
}

@Composable
fun EquipmentDetailContent(
    equipment: Equipment,
    onBackOrClose: () -> Unit,
    closeLabel: String = "Voltar",
    userRole: UserRole = UserRole.STUDENT,
    onBorrowEquipment: ((Equipment) -> Unit)? = null,
    onReturnEquipment: ((Equipment) -> Unit)? = null,
    feedbackMessage: String? = null,
    canUndo: Boolean = false,
    onUndoLastAction: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    var pendingAction by remember { mutableStateOf<PendingConfirmationAction?>(null) }

    val statusLabel =
        when (equipment.status) {
            EquipmentStatus.AVAILABLE -> "Disponível"
            EquipmentStatus.BORROWED -> "Emprestado"
            EquipmentStatus.MAINTENANCE -> "Em Manutenção"
        }

    val statusColor =
        when (equipment.status) {
            EquipmentStatus.AVAILABLE -> MaterialTheme.colorScheme.primary
            EquipmentStatus.BORROWED -> MaterialTheme.colorScheme.error
            EquipmentStatus.MAINTENANCE -> MaterialTheme.colorScheme.outline
        }

    if (pendingAction == PendingConfirmationAction.BORROW) {
        AlertDialog(
            onDismissRequest = { pendingAction = null },
            title = { Text("Confirmar Empréstimo") },
            text = {
                Text(
                    "Deseja registrar a retirada de '${equipment.name}' com o perfil ${userRole.label}? " +
                        "O prazo de devolução será calculado pelo servidor (estimativa: $DEFAULT_RETURN_DUE_DATE).",
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        pendingAction = null
                        onBorrowEquipment?.invoke(equipment)
                    },
                ) {
                    Text("Confirmar Retirada")
                }
            },
            dismissButton = {
                TextButton(onClick = { pendingAction = null }) {
                    Text("Cancelar")
                }
            },
        )
    } else if (pendingAction == PendingConfirmationAction.RETURN) {
        AlertDialog(
            onDismissRequest = { pendingAction = null },
            title = { Text("Confirmar Devolução") },
            text = {
                Text(
                    "Confirma que o equipamento '${equipment.name}' foi devolvido à bancada do laboratório " +
                        "em perfeitas condições de uso?",
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        pendingAction = null
                        onReturnEquipment?.invoke(equipment)
                    },
                ) {
                    Text("Confirmar Devolução")
                }
            },
            dismissButton = {
                TextButton(onClick = { pendingAction = null }) {
                    Text("Cancelar")
                }
            },
        )
    }

    Card(
        modifier =
            modifier
                .padding(16.dp)
                .fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors =
            CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
            ),
    ) {
        Column(
            modifier =
                Modifier
                    .padding(24.dp)
                    .fillMaxWidth(),
            horizontalAlignment = Alignment.Start,
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "Catálogo > Ficha do Equipamento",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                )
                Text(
                    text = userRole.badgeText,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            Spacer(Modifier.height(6.dp))

            Text(
                text = equipment.name,
                style =
                    MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Bold,
                    ),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.semantics { heading() },
            )

            Spacer(Modifier.height(8.dp))

            Text(
                text = "Categoria: ${equipment.category}",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Spacer(Modifier.height(6.dp))

            Text(
                text = "Tag NFC: ${equipment.nfcTagId} • Patrimônio: ${equipment.id}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.outline,
            )

            Spacer(Modifier.height(12.dp))

            SuggestionChip(
                onClick = {},
                label = { Text("Status: $statusLabel") },
                colors =
                    SuggestionChipDefaults.suggestionChipColors(
                        containerColor = statusColor.copy(alpha = 0.12f),
                    ),
                border =
                    SuggestionChipDefaults.suggestionChipBorder(
                        enabled = true,
                        borderColor = statusColor.copy(alpha = 0.5f),
                    ),
            )

            if (equipment.status == EquipmentStatus.BORROWED && equipment.borrowerName != null) {
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "Responsável Atual: ${equipment.borrowerName}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error,
                )
                if (equipment.returnDueDate != null) {
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "Prazo Limite de Devolução: ${equipment.returnDueDate}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            if (feedbackMessage != null) {
                Spacer(Modifier.height(14.dp))
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp).fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = feedbackMessage,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.weight(1f),
                        )
                        if (canUndo && onUndoLastAction != null) {
                            TextButton(onClick = onUndoLastAction) {
                                Text("Desfazer")
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))
            HorizontalDivider()
            Spacer(Modifier.height(16.dp))

            when (equipment.status) {
                EquipmentStatus.AVAILABLE -> {
                    if (onBorrowEquipment != null) {
                        Button(
                            onClick = { pendingAction = PendingConfirmationAction.BORROW },
                            modifier = Modifier.fillMaxWidth().height(48.dp),
                        ) {
                            Text("Solicitar Empréstimo", style = MaterialTheme.typography.labelLarge)
                        }
                        Spacer(Modifier.height(10.dp))
                    }
                }
                EquipmentStatus.BORROWED -> {
                    if (onReturnEquipment != null && userRole.canRegisterReturn) {
                        Button(
                            onClick = { pendingAction = PendingConfirmationAction.RETURN },
                            colors =
                                ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.secondary,
                                ),
                            modifier = Modifier.fillMaxWidth().height(48.dp),
                        ) {
                            Text(
                                "Registrar Devolução (${userRole.label})",
                                style = MaterialTheme.typography.labelLarge,
                            )
                        }
                        Spacer(Modifier.height(10.dp))
                    } else if (onReturnEquipment != null) {
                        Text(
                            text =
                                "A conferência e baixa oficial de devolução no inventário " +
                                    "é realizada pelo Monitor ou Professor na bancada.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Spacer(Modifier.height(8.dp))
                        OutlinedButton(
                            onClick = { pendingAction = PendingConfirmationAction.RETURN },
                            modifier = Modifier.fillMaxWidth().height(48.dp),
                        ) {
                            Text("Devolver Equipamento na Bancada", style = MaterialTheme.typography.labelLarge)
                        }
                        Spacer(Modifier.height(10.dp))
                    }
                }
                EquipmentStatus.MAINTENANCE -> {
                    Text(
                        text = "Este item está em manutenção preventiva e bloqueado para retirada.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline,
                    )
                    Spacer(Modifier.height(10.dp))
                }
            }

            OutlinedButton(
                onClick = onBackOrClose,
                modifier = Modifier.fillMaxWidth().height(48.dp),
            ) {
                Text(closeLabel, style = MaterialTheme.typography.labelLarge)
            }
        }
    }
}
