package br.ufrn.smorg.ui.preview

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import br.ufrn.smorg.EmptyCatalogState
import br.ufrn.smorg.EquipmentCatalogScreen
import br.ufrn.smorg.model.Equipment
import br.ufrn.smorg.model.EquipmentStatus
import br.ufrn.smorg.ui.components.EquipmentCard
import org.jetbrains.compose.ui.tooling.preview.Preview

@Preview
@Composable
fun EquipmentCardPreview() {
    MaterialTheme {
        EquipmentCard(
            equipment =
                Equipment(
                    id = "PREV-001",
                    name = "Osciloscópio Digital 100MHz",
                    category = "Bancada e Medição",
                    nfcTagId = "04:A2:8B:1A:5C",
                    status = EquipmentStatus.AVAILABLE,
                ),
            onClick = {},
        )
    }
}

@Preview
@Composable
fun EquipmentCatalogEmptyPreview() {
    MaterialTheme {
        EmptyCatalogState()
    }
}

@Preview
@Composable
fun EquipmentCatalogFullPreview() {
    MaterialTheme {
        val sampleList =
            listOf(
                Equipment(
                    id = "PREV-001",
                    name = "Osciloscópio Digital 100MHz",
                    category = "Bancada e Medição",
                    nfcTagId = "04:A2:8B:1A:5C",
                    status = EquipmentStatus.AVAILABLE,
                ),
                Equipment(
                    id = "PREV-002",
                    name = "Multímetro Digital True RMS",
                    category = "Instrumentação",
                    nfcTagId = "04:C5:91:2B:7E",
                    status = EquipmentStatus.BORROWED,
                    borrowerName = "Miguel Morais",
                ),
            )
        val snackbarHostState = remember { SnackbarHostState() }
        EquipmentCatalogScreen(
            items = sampleList,
            selectedFilter = null,
            snackbarHostState = snackbarHostState,
            onFilterSelect = {},
            onEquipmentClick = {},
        )
    }
}
