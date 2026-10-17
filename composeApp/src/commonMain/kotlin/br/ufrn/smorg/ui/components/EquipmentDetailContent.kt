package br.ufrn.smorg.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import br.ufrn.smorg.model.Equipment
import br.ufrn.smorg.model.EquipmentStatus

@Composable
fun EquipmentDetailContent(
    equipment: Equipment,
    onBackOrClose: () -> Unit,
    closeLabel: String = "Voltar",
    modifier: Modifier = Modifier,
) {
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

            Spacer(Modifier.height(8.dp))

            Text(
                text = "Tag NFC: ${equipment.nfcTagId}",
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
            }

            Spacer(Modifier.height(28.dp))

            Button(
                onClick = onBackOrClose,
                modifier = Modifier.fillMaxWidth().height(48.dp),
            ) {
                Text(closeLabel, style = MaterialTheme.typography.labelLarge)
            }
        }
    }
}
