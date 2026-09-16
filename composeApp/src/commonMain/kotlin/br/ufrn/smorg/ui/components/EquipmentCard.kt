package br.ufrn.smorg.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import br.ufrn.smorg.model.Equipment
import br.ufrn.smorg.model.EquipmentStatus

@Composable
fun EquipmentCard(
    equipment: Equipment,
    onClick: (Equipment) -> Unit,
    modifier: Modifier = Modifier,
) {
    val statusLabel = when (equipment.status) {
        EquipmentStatus.AVAILABLE -> "Disponível"
        EquipmentStatus.BORROWED -> "Emprestado"
        EquipmentStatus.MAINTENANCE -> "Em Manutenção"
    }

    val statusColor = when (equipment.status) {
        EquipmentStatus.AVAILABLE -> MaterialTheme.colorScheme.primary
        EquipmentStatus.BORROWED -> MaterialTheme.colorScheme.error
        EquipmentStatus.MAINTENANCE -> MaterialTheme.colorScheme.outline
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick(equipment) }
            .semantics {
                contentDescription = "Equipamento ${equipment.name}, status $statusLabel"
            },
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
        ),
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = equipment.name,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        text = equipment.category,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f),
                    )
                }

                SuggestionChip(
                    onClick = { onClick(equipment) },
                    label = {
                        Text(
                            text = statusLabel,
                            style = MaterialTheme.typography.labelSmall,
                            color = statusColor,
                        )
                    },
                    colors = SuggestionChipDefaults.suggestionChipColors(
                        containerColor = statusColor.copy(alpha = 0.12f),
                    ),
                    border = SuggestionChipDefaults.suggestionChipBorder(
                        enabled = true,
                        borderColor = statusColor.copy(alpha = 0.4f),
                    ),
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "NFC Tag: ${equipment.nfcTagId}",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.outline,
            )

            if (equipment.status == EquipmentStatus.BORROWED && equipment.borrowerName != null) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Responsável: ${equipment.borrowerName}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                )
            }
        }
    }
}
