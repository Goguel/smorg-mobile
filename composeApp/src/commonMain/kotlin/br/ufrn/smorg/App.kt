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
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import br.ufrn.smorg.data.remote.SmorgApiClient
import br.ufrn.smorg.model.Equipment
import br.ufrn.smorg.model.EquipmentStatus
import br.ufrn.smorg.ui.components.EquipmentCard
import br.ufrn.smorg.ui.screens.LoginScreen
import kotlinx.coroutines.launch
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
                    var items by remember { mutableStateOf<List<Equipment>>(emptyList()) }
                    var isLoading by remember { mutableStateOf(true) }
                    var errorMessage by remember { mutableStateOf<String?>(null) }
                    var selectedStatus by remember { mutableStateOf<EquipmentStatus?>(null) }
                    var refreshCounter by remember { mutableStateOf(0) }
                    val snackbarHostState = remember { SnackbarHostState() }
                    val coroutineScope = rememberCoroutineScope()

                    LaunchedEffect(selectedStatus, refreshCounter) {
                        isLoading = true
                        errorMessage = null
                        try {
                            val backendStatus =
                                when (selectedStatus) {
                                    EquipmentStatus.AVAILABLE -> "DISPONIVEL"
                                    EquipmentStatus.BORROWED -> "EMPRESTADO"
                                    EquipmentStatus.MAINTENANCE -> "MANUTENCAO"
                                    null -> null
                                }
                            items = SmorgApiClient.listarEquipamentos(backendStatus)
                        } catch (e: Exception) {
                            val msg = "Não foi possível carregar os equipamentos da nuvem: ${e.message}"
                            errorMessage = msg
                            coroutineScope.launch {
                                snackbarHostState.showSnackbar(msg)
                            }
                        } finally {
                            isLoading = false
                        }
                    }

                    EquipmentCatalogScreen(
                        items = items,
                        selectedFilter = selectedStatus,
                        isLoading = isLoading,
                        errorMessage = errorMessage,
                        snackbarHostState = snackbarHostState,
                        onRefresh = { refreshCounter++ },
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
                    var equipment by remember { mutableStateOf<Equipment?>(null) }
                    var isLoadingDetails by remember { mutableStateOf(true) }
                    var detailError by remember { mutableStateOf<String?>(null) }

                    LaunchedEffect(details.id) {
                        isLoadingDetails = true
                        detailError = null
                        try {
                            equipment = SmorgApiClient.buscarPorId(details.id)
                        } catch (e: Exception) {
                            detailError = "Erro ao buscar equipamento: ${e.message}"
                        } finally {
                            isLoadingDetails = false
                        }
                    }

                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        when {
                            isLoadingDetails -> {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    CircularProgressIndicator()
                                    Spacer(Modifier.height(16.dp))
                                    Text("Buscando detalhes do equipamento...")
                                }
                            }
                            equipment != null -> {
                                val eq = equipment!!
                                Card(
                                    modifier = Modifier.padding(24.dp).fillMaxWidth(),
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                    ),
                                ) {
                                    Column(
                                        modifier = Modifier.padding(20.dp),
                                        horizontalAlignment = Alignment.Start,
                                    ) {
                                        Text(
                                            text = eq.name,
                                            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                                        )
                                        Spacer(Modifier.height(8.dp))
                                        Text(
                                            text = "Categoria: ${eq.category}",
                                            style = MaterialTheme.typography.bodyMedium,
                                        )
                                        Spacer(Modifier.height(8.dp))
                                        Text(
                                            text = "Tag NFC: ${eq.nfcTagId}",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.outline,
                                        )
                                        Spacer(Modifier.height(8.dp))
                                        SuggestionChip(
                                            onClick = {},
                                            label = { Text("Status: ${eq.status}") },
                                        )
                                        Spacer(Modifier.height(24.dp))
                                        Button(
                                            onClick = { navController.popBackStack() },
                                            modifier = Modifier.fillMaxWidth(),
                                        ) {
                                            Text("Voltar")
                                        }
                                    }
                                }
                            }
                            else -> {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier.padding(24.dp),
                                ) {
                                    Text(
                                        text = detailError ?: "Equipamento não encontrado",
                                        color = MaterialTheme.colorScheme.error,
                                        textAlign = TextAlign.Center,
                                    )
                                    Spacer(Modifier.height(16.dp))
                                    Button(onClick = { navController.popBackStack() }) {
                                        Text("Voltar")
                                    }
                                }
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
    isLoading: Boolean,
    errorMessage: String?,
    snackbarHostState: SnackbarHostState,
    onRefresh: () -> Unit,
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
                actions = {
                    TextButton(onClick = onRefresh) {
                        Text(
                            text = "Atualizar",
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            style = MaterialTheme.typography.labelLarge,
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

            when {
                isLoading && items.isEmpty() -> {
                    Box(
                        modifier = Modifier.weight(1f).fillMaxWidth(),
                        contentAlignment = Alignment.Center,
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator()
                            Spacer(Modifier.height(16.dp))
                            Text(
                                text = "Buscando dados da nuvem...",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
                errorMessage != null && items.isEmpty() -> {
                    Box(
                        modifier = Modifier.weight(1f).fillMaxWidth().padding(24.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "Erro de conexão",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.error,
                            )
                            Spacer(Modifier.height(8.dp))
                            Text(
                                text = errorMessage,
                                style = MaterialTheme.typography.bodySmall,
                                textAlign = TextAlign.Center,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            Spacer(Modifier.height(16.dp))
                            OutlinedButton(onClick = onRefresh) {
                                Text("Tentar Novamente")
                            }
                        }
                    }
                }
                items.isEmpty() -> {
                    EmptyCatalogState(modifier = Modifier.weight(1f))
                }
                else -> {
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
                text = "Tente selecionar outro filtro de status ou cadastre itens na API.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.outline,
            )
        }
    }
}
