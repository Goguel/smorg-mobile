package br.ufrn.smorg

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import br.ufrn.smorg.model.Equipment
import br.ufrn.smorg.model.EquipmentStatus
import br.ufrn.smorg.ui.components.EquipmentCard
import br.ufrn.smorg.ui.screens.LoginScreen
import kotlinx.serialization.Serializable

@Serializable
object LoginRoute

@Serializable
object HomeRoute

@Serializable
data class EquipmentDetailsRoute(
    val id: String,
)

@Serializable
object ReturnEquipmentRoute

@Composable
fun App() {
    MaterialTheme {
        val navController = rememberNavController()

        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background,
        ) {
            NavHost(
                navController = navController,
                startDestination = LoginRoute,
            ) {
                composable<LoginRoute> {
                    LoginScreen(
                        onLoginSuccess = {
                            navController.navigate(HomeRoute) {
                                popUpTo<LoginRoute> { inclusive = true }
                            }
                        },
                    )
                }

                composable<HomeRoute> {
                    val sampleItems =
                        remember {
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
                                    borrowerName = "Miguel Morais (20240027427)",
                                    returnDueDate = "18/09/2026",
                                ),
                                Equipment(
                                    id = "EQ-003",
                                    name = "Kit Didático ESP32 IoT",
                                    category = "Microcontroladores",
                                    nfcTagId = "04:7F:3D:E4:90",
                                    status = EquipmentStatus.AVAILABLE,
                                ),
                                Equipment(
                                    id = "EQ-004",
                                    name = "Fonte de Alimentação DC 30V 5A",
                                    category = "Bancada e Medição",
                                    nfcTagId = "04:18:22:9C:AA",
                                    status = EquipmentStatus.MAINTENANCE,
                                ),
                            )
                        }

                    var selectedStatus by remember { mutableStateOf<EquipmentStatus?>(null) }
                    val snackbarHostState = remember { SnackbarHostState() }

                    val filteredItems =
                        remember(selectedStatus, sampleItems) {
                            if (selectedStatus == null) {
                                sampleItems
                            } else {
                                sampleItems.filter { it.status == selectedStatus }
                            }
                        }

                    EquipmentCatalogScreen(
                        items = filteredItems,
                        selectedFilter = selectedStatus,
                        snackbarHostState = snackbarHostState,
                        onFilterSelect = { newFilter ->
                            selectedStatus = if (selectedStatus == newFilter) null else newFilter
                        },
                        onEquipmentClick = { eq ->
                            navController.navigate(EquipmentDetailsRoute(id = eq.id))
                        },
                    )
                }

                composable<EquipmentDetailsRoute> { backStackEntry ->
                    val details: EquipmentDetailsRoute = backStackEntry.toRoute()
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Detalhes do Equipamento", style = MaterialTheme.typography.headlineMedium)
                            Text("ID Solicitado: ${details.id}")
                            Spacer(Modifier.height(16.dp))
                            androidx.compose.material3.Button(onClick = { navController.popBackStack() }) {
                                Text("Voltar")
                            }
                        }
                    }
                }

                composable<ReturnEquipmentRoute> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Tela de Devolução", style = MaterialTheme.typography.headlineMedium)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EquipmentCatalogScreen(
    items: List<Equipment>,
    selectedFilter: EquipmentStatus?,
    snackbarHostState: SnackbarHostState,
    onFilterSelect: (EquipmentStatus) -> Unit,
    onEquipmentClick: (Equipment) -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "SmOrg Mobile",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        )
                        Text(
                            text = "Controle de Ativos e Empréstimos NFC",
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }
                },
                colors =
                    TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    ),
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { innerPadding ->
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
        ) {
            FilterSection(
                selectedFilter = selectedFilter,
                onFilterSelect = onFilterSelect,
            )

            if (items.isEmpty()) {
                EmptyCatalogState(modifier = Modifier.weight(1f))
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    items(items, key = { it.id }) { equipment ->
                        EquipmentCard(
                            equipment = equipment,
                            onClick = onEquipmentClick,
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun FilterSection(
    selectedFilter: EquipmentStatus?,
    onFilterSelect: (EquipmentStatus) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        EquipmentStatus.entries.forEach { status ->
            val label =
                when (status) {
                    EquipmentStatus.AVAILABLE -> "Disponíveis"
                    EquipmentStatus.BORROWED -> "Emprestados"
                    EquipmentStatus.MAINTENANCE -> "Manutenção"
                }
            FilterChip(
                selected = selectedFilter == status,
                onClick = { onFilterSelect(status) },
                label = { Text(label) },
            )
        }
    }
}

@Composable
fun EmptyCatalogState(modifier: Modifier = Modifier) {
    Box(
        modifier =
            modifier
                .fillMaxSize()
                .padding(32.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "Nenhum equipamento encontrado",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Tente selecionar outro filtro de status.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.outline,
            )
        }
    }
}
