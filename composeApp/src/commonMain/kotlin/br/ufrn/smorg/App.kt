package br.ufrn.smorg

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navDeepLink
import androidx.navigation.toRoute
import br.ufrn.smorg.data.NfcHardwareBridge
import br.ufrn.smorg.domain.Equipment
import br.ufrn.smorg.domain.EquipmentStatus
import br.ufrn.smorg.domain.UserRole
import br.ufrn.smorg.domain.isEquipmentCategoryValid
import br.ufrn.smorg.domain.isEquipmentNameValid
import br.ufrn.smorg.presentation.CatalogScreenState
import br.ufrn.smorg.presentation.CatalogUiState
import br.ufrn.smorg.presentation.CatalogViewModel
import br.ufrn.smorg.presentation.DetailScreenState
import br.ufrn.smorg.presentation.LoginViewModel
import br.ufrn.smorg.presentation.components.EquipmentCard
import br.ufrn.smorg.presentation.components.EquipmentDetailContent
import br.ufrn.smorg.presentation.screens.LoginScreen
import br.ufrn.smorg.presentation.theme.SmOrgTheme
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
fun App(
    dataDirectory: String = "build/smorg-cache",
    appModule: AppModule = remember(dataDirectory) { AppModule(dataDirectory = dataDirectory) },
    catalogViewModel: CatalogViewModel = remember(appModule) { appModule.createCatalogViewModel() },
    loginViewModel: LoginViewModel = remember(appModule) { appModule.createLoginViewModel() },
) {
    val systemInDark = isSystemInDarkTheme()
    var isDarkTheme by remember { mutableStateOf(systemInDark) }

    SmOrgTheme(darkTheme = isDarkTheme) {
        val navController = rememberNavController()
        val catalogState by catalogViewModel.state.collectAsStateWithLifecycle()
        val loginState by loginViewModel.state.collectAsStateWithLifecycle()

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
                        viewModel = loginViewModel,
                        onLoginSuccess = {
                            navController.navigate(HomeRoute) {
                                popUpTo<LoginRoute> { inclusive = true }
                            }
                        },
                    )
                }

                composable<HomeRoute> {
                    val snackbarHostState = remember { SnackbarHostState() }

                    EquipmentCatalogScreen(
                        state = catalogState,
                        snackbarHostState = snackbarHostState,
                        userName = loginState.displayName,
                        userRole = loginState.selectedRole,
                        isDarkTheme = isDarkTheme,
                        onToggleTheme = { isDarkTheme = !isDarkTheme },
                        onRefresh = catalogViewModel::loadEquipments,
                        onFilterSelect = catalogViewModel::selectFilter,
                        onSearchQueryChange = catalogViewModel::onSearchQueryChange,
                        onClearFilters = catalogViewModel::clearFiltersAndSearch,
                        onCreateEquipment = catalogViewModel::createEquipment,
                        onNfcTagScanned = { tagId ->
                            catalogViewModel.handleScannedNfcTag(tagId) { resolved ->
                                navController.navigate(EquipmentDetailsRoute(id = resolved.id))
                            }
                        },
                        onBorrowEquipment = { eq ->
                            catalogViewModel.borrowEquipment(
                                id = eq.id,
                                borrowerName = loginState.displayName,
                                userId = loginState.userUuid,
                                userRole = loginState.selectedRole,
                            )
                        },
                        onReturnEquipment = { eq ->
                            catalogViewModel.returnEquipment(eq.id)
                        },
                        onUndoLastAction = {
                            catalogViewModel.undoLastAction(
                                userId = loginState.userUuid,
                                borrowerName = loginState.displayName,
                                userRole = loginState.selectedRole,
                            )
                        },
                        onLogout = {
                            loginViewModel.clearFields()
                            navController.navigate(LoginRoute) {
                                popUpTo<HomeRoute> { inclusive = true }
                            }
                        },
                        onEquipmentClick = { eq ->
                            navController.navigate(EquipmentDetailsRoute(id = eq.id))
                        },
                    )
                }

                composable<EquipmentDetailsRoute>(
                    deepLinks =
                        listOf(
                            navDeepLink<EquipmentDetailsRoute>(basePath = "smorg://equipamento"),
                        ),
                ) { backStackEntry ->
                    val details: EquipmentDetailsRoute = backStackEntry.toRoute()

                    LaunchedEffect(details.id) {
                        catalogViewModel.loadEquipmentDetail(details.id)
                    }

                    EquipmentDetailScreen(
                        detailState = catalogState.detailScreen,
                        userRole = loginState.selectedRole,
                        feedbackMessage = catalogState.feedbackMessage,
                        canUndo = catalogState.canUndoLastAction,
                        onBorrowEquipment = { eq ->
                            catalogViewModel.borrowEquipment(
                                id = eq.id,
                                borrowerName = loginState.displayName,
                                userId = loginState.userUuid,
                                userRole = loginState.selectedRole,
                            )
                        },
                        onReturnEquipment = { eq ->
                            catalogViewModel.returnEquipment(eq.id)
                        },
                        onUndoLastAction = {
                            catalogViewModel.undoLastAction(
                                userId = loginState.userUuid,
                                borrowerName = loginState.displayName,
                                userRole = loginState.selectedRole,
                            )
                        },
                        onRetry = { catalogViewModel.loadEquipmentDetail(details.id) },
                        onBack = {
                            catalogViewModel.dismissFeedback()
                            navController.popBackStack()
                        },
                    )
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

@Composable
fun EquipmentDetailScreen(
    detailState: DetailScreenState,
    onRetry: () -> Unit,
    onBack: () -> Unit,
    userRole: UserRole = UserRole.STUDENT,
    onBorrowEquipment: ((Equipment) -> Unit)? = null,
    onReturnEquipment: ((Equipment) -> Unit)? = null,
    feedbackMessage: String? = null,
    canUndo: Boolean = false,
    onUndoLastAction: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        when (detailState) {
            DetailScreenState.Loading -> {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(
                        modifier =
                            Modifier.semantics {
                                contentDescription = "Carregando detalhes do equipamento"
                            },
                    )
                    Spacer(Modifier.height(16.dp))
                    Text("Buscando detalhes do equipamento...")
                }
            }
            DetailScreenState.Empty -> {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(24.dp),
                ) {
                    Text(
                        text = "Nenhum equipamento selecionado.",
                        style = MaterialTheme.typography.titleMedium,
                        textAlign = TextAlign.Center,
                    )
                    Spacer(Modifier.height(16.dp))
                    Button(onClick = onBack) {
                        Text("Voltar")
                    }
                }
            }
            is DetailScreenState.Success -> {
                EquipmentDetailContent(
                    equipment = detailState.equipment,
                    onBackOrClose = onBack,
                    closeLabel = "Voltar",
                    userRole = userRole,
                    onBorrowEquipment = onBorrowEquipment,
                    onReturnEquipment = onReturnEquipment,
                    feedbackMessage = feedbackMessage,
                    canUndo = canUndo,
                    onUndoLastAction = onUndoLastAction,
                )
            }
            is DetailScreenState.Error -> {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(24.dp),
                ) {
                    Text(
                        text = detailState.message,
                        color = MaterialTheme.colorScheme.error,
                        textAlign = TextAlign.Center,
                    )
                    Spacer(Modifier.height(16.dp))
                    OutlinedButton(onClick = onRetry) {
                        Text("Tentar Novamente")
                    }
                    Spacer(Modifier.height(8.dp))
                    Button(onClick = onBack) {
                        Text("Voltar")
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EquipmentCatalogScreen(
    state: CatalogUiState,
    snackbarHostState: SnackbarHostState,
    userName: String = "Estudante",
    userRole: UserRole = UserRole.STUDENT,
    isDarkTheme: Boolean = false,
    onToggleTheme: () -> Unit = {},
    onRefresh: () -> Unit,
    onFilterSelect: (EquipmentStatus) -> Unit,
    onSearchQueryChange: (String) -> Unit = {},
    onClearFilters: () -> Unit = {},
    onCreateEquipment: ((String, String, String?) -> Unit)? = null,
    onNfcTagScanned: ((String) -> Unit)? = null,
    onBorrowEquipment: ((Equipment) -> Unit)? = null,
    onReturnEquipment: ((Equipment) -> Unit)? = null,
    onUndoLastAction: (() -> Unit)? = null,
    onLogout: (() -> Unit)? = null,
    onEquipmentClick: (Equipment) -> Unit,
    modifier: Modifier = Modifier,
) {
    var selectedEquipmentIdForWide by remember { mutableStateOf<String?>(null) }
    var showNfcSimulatorDialog by remember { mutableStateOf(false) }
    var showCreateEquipmentDialog by remember { mutableStateOf(false) }
    var prefilledTagForCreation by remember { mutableStateOf<String?>(null) }

    val isNfcHardwareSupported by NfcHardwareBridge.isHardwareSupported.collectAsStateWithLifecycle()
    val isNfcHardwareEnabled by NfcHardwareBridge.isHardwareEnabled.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        NfcHardwareBridge.scannedTags.collect { physicalTagHex ->
            if (showCreateEquipmentDialog) {
                prefilledTagForCreation = physicalTagHex
            } else {
                showNfcSimulatorDialog = false
                onNfcTagScanned?.invoke(physicalTagHex)
            }
        }
    }

    val selectedEquipmentForWide = state.items.find { it.id == selectedEquipmentIdForWide }

    val cacheBannerMessage =
        state.notice ?: if ((state.screen as? CatalogScreenState.Success)?.fromCache == true) {
            "Sem conexão: mostrando a última lista salva no aparelho."
        } else {
            null
        }

    if (showNfcSimulatorDialog) {
        NfcReaderAndSimulatorDialog(
            equipments = state.items,
            isHardwareSupported = isNfcHardwareSupported,
            isHardwareEnabled = isNfcHardwareEnabled,
            onScanTag = { tagId ->
                showNfcSimulatorDialog = false
                if (onNfcTagScanned != null) {
                    onNfcTagScanned(tagId)
                } else {
                    state.items.find { it.nfcTagId.equals(tagId, ignoreCase = true) }?.let(onEquipmentClick)
                }
            },
            onDismiss = { showNfcSimulatorDialog = false },
        )
    }

    if (showCreateEquipmentDialog && onCreateEquipment != null) {
        CreateEquipmentDialog(
            initialTagId = prefilledTagForCreation ?: state.lastScannedUnregisteredTag.orEmpty(),
            isHardwareEnabled = isNfcHardwareEnabled,
            onConfirmCreate = { name, category, tag ->
                showCreateEquipmentDialog = false
                prefilledTagForCreation = null
                onCreateEquipment(name, category, tag)
            },
            onDismiss = {
                showCreateEquipmentDialog = false
                prefilledTagForCreation = null
            },
        )
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "SmOrg Mobile",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            modifier = Modifier.semantics { heading() },
                        )
                        Text(
                            text = "Controle de Ativos e Empréstimos NFC",
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }
                },
                actions = {
                    if (onCreateEquipment != null && userRole.canManageInventory) {
                        TextButton(
                            onClick = { showCreateEquipmentDialog = true },
                            modifier =
                                Modifier.semantics {
                                    contentDescription = "Cadastrar novo equipamento"
                                },
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            )
                            Text(
                                text = "Novo",
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                style = MaterialTheme.typography.labelLarge,
                            )
                        }
                    }
                    IconButton(
                        onClick = onToggleTheme,
                        modifier =
                            Modifier.semantics {
                                contentDescription =
                                    if (isDarkTheme) "Alternar para tema claro" else "Alternar para tema escuro"
                            },
                    ) {
                        Text(
                            text = if (isDarkTheme) "☀️" else "🌙",
                            style = MaterialTheme.typography.titleMedium,
                        )
                    }
                    TextButton(onClick = onRefresh) {
                        Text(
                            text = "Atualizar",
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            style = MaterialTheme.typography.labelLarge,
                        )
                    }
                    if (onLogout != null) {
                        TextButton(onClick = onLogout) {
                            Text(
                                text = "Sair",
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                style = MaterialTheme.typography.labelLarge,
                            )
                        }
                    }
                },
                colors =
                    TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    ),
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showNfcSimulatorDialog = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
            ) {
                Text("📳 Ler Tag NFC")
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { innerPadding ->
        BoxWithConstraints(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
        ) {
            val isWide = maxWidth >= 600.dp

            if (isWide) {
                Row(modifier = Modifier.fillMaxSize()) {
                    Column(
                        modifier =
                            Modifier
                                .width(390.dp)
                                .fillMaxHeight(),
                    ) {
                        WelcomeAndSearchHeader(
                            userName = userName,
                            userRole = userRole,
                            searchQuery = state.searchQuery,
                            onSearchQueryChange = onSearchQueryChange,
                            feedbackMessage = if (selectedEquipmentForWide == null) state.feedbackMessage else null,
                            canUndo = state.canUndoLastAction,
                            onUndo = onUndoLastAction,
                        )

                        FilterSection(
                            selectedFilter = state.selectedFilter,
                            hasActiveSearchOrFilter = state.selectedFilter != null || state.searchQuery.isNotBlank(),
                            onFilterSelect = onFilterSelect,
                            onClearFilters = onClearFilters,
                        )

                        if (cacheBannerMessage != null) {
                            Text(
                                text = cacheBannerMessage,
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                            )
                        }

                        CatalogListContent(
                            screenState = state.screen,
                            searchQuery = state.searchQuery,
                            onRefresh = onRefresh,
                            onEquipmentClick = { eq ->
                                selectedEquipmentIdForWide = eq.id
                            },
                        )
                    }

                    VerticalDivider()

                    Box(
                        modifier =
                            Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .padding(16.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        if (selectedEquipmentForWide != null) {
                            EquipmentDetailContent(
                                equipment = selectedEquipmentForWide,
                                onBackOrClose = { selectedEquipmentIdForWide = null },
                                closeLabel = "Fechar Painel",
                                userRole = userRole,
                                onBorrowEquipment = onBorrowEquipment,
                                onReturnEquipment = onReturnEquipment,
                                feedbackMessage = state.feedbackMessage,
                                canUndo = state.canUndoLastAction,
                                onUndoLastAction = onUndoLastAction,
                            )
                        } else {
                            Text(
                                text = "Selecione um equipamento da lista ao lado para ver seus detalhes",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center,
                            )
                        }
                    }
                }
            } else {
                Column(modifier = Modifier.fillMaxSize()) {
                    WelcomeAndSearchHeader(
                        userName = userName,
                        userRole = userRole,
                        searchQuery = state.searchQuery,
                        onSearchQueryChange = onSearchQueryChange,
                        feedbackMessage = state.feedbackMessage,
                        canUndo = state.canUndoLastAction,
                        onUndo = onUndoLastAction,
                    )

                    FilterSection(
                        selectedFilter = state.selectedFilter,
                        hasActiveSearchOrFilter = state.selectedFilter != null || state.searchQuery.isNotBlank(),
                        onFilterSelect = onFilterSelect,
                        onClearFilters = onClearFilters,
                    )

                    if (cacheBannerMessage != null) {
                        Text(
                            text = cacheBannerMessage,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                        )
                    }

                    CatalogListContent(
                        screenState = state.screen,
                        searchQuery = state.searchQuery,
                        onRefresh = onRefresh,
                        onEquipmentClick = onEquipmentClick,
                    )
                }
            }
        }
    }
}

@Composable
private fun CreateEquipmentDialog(
    initialTagId: String,
    isHardwareEnabled: Boolean,
    onConfirmCreate: (String, String, String?) -> Unit,
    onDismiss: () -> Unit,
) {
    var name by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("") }
    var nfcTagId by remember(initialTagId) { mutableStateOf(initialTagId) }
    var nameError by remember { mutableStateOf<String?>(null) }
    var categoryError by remember { mutableStateOf<String?>(null) }

    val suggestedCategories = listOf("Bancada e Medição", "Instrumentação", "Sistemas Embarcados")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Cadastrar Novo Equipamento") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Adicione um novo ativo ao inventário do laboratório:",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                OutlinedTextField(
                    value = name,
                    onValueChange = {
                        name = it
                        nameError = null
                    },
                    label = { Text("Nome do Equipamento") },
                    placeholder = { Text("Ex.: Fonte de Bancada Digital 30V") },
                    isError = nameError != null,
                    supportingText =
                        nameError?.let { error ->
                            { Text(error, color = MaterialTheme.colorScheme.error) }
                        },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )

                OutlinedTextField(
                    value = category,
                    onValueChange = {
                        category = it
                        categoryError = null
                    },
                    label = { Text("Categoria") },
                    placeholder = { Text("Ex.: Bancada e Medição") },
                    isError = categoryError != null,
                    supportingText =
                        categoryError?.let { error ->
                            { Text(error, color = MaterialTheme.colorScheme.error) }
                        },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    suggestedCategories.forEach { suggestion ->
                        SuggestionChip(
                            onClick = {
                                category = suggestion
                                categoryError = null
                            },
                            label = {
                                Text(
                                    text = suggestion.substringBefore(" "),
                                    style = MaterialTheme.typography.labelSmall,
                                )
                            },
                        )
                    }
                }

                OutlinedTextField(
                    value = nfcTagId,
                    onValueChange = { nfcTagId = it },
                    label = { Text("Tag NFC (Opcional)") },
                    placeholder = { Text("Ex.: 04:A2:8B:1A:5C") },
                    supportingText = {
                        Text(
                            if (isHardwareEnabled) {
                                "Encoste uma tag NFC física no celular para preencher automaticamente"
                            } else {
                                "Informe o código hexadecimal da tag ou gere um código de teste"
                            },
                        )
                    },
                    trailingIcon = {
                        TextButton(
                            onClick = {
                                val randomByte = (10..99).random()
                                nfcTagId = "04:F1:A9:$randomByte:3C"
                            },
                        ) {
                            Text("Gerar Tag", style = MaterialTheme.typography.labelSmall)
                        }
                    },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val validName = isEquipmentNameValid(name)
                    val validCategory = isEquipmentCategoryValid(category)
                    nameError = if (!validName) "Informe um nome com ao menos 3 caracteres." else null
                    categoryError = if (!validCategory) "A categoria do equipamento é obrigatória." else null

                    if (validName && validCategory) {
                        onConfirmCreate(name, category, nfcTagId.ifBlank { null })
                    }
                },
            ) {
                Text("Salvar Equipamento")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        },
    )
}

@Composable
private fun NfcReaderAndSimulatorDialog(
    equipments: List<Equipment>,
    isHardwareSupported: Boolean,
    isHardwareEnabled: Boolean,
    onScanTag: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    var manualTagInput by remember { mutableStateOf("") }

    val hardwareStatusText =
        when {
            isHardwareSupported && isHardwareEnabled ->
                "Sensor NFC físico ATIVO: encoste qualquer tag NFC real na traseira do celular " +
                    "ou selecione uma tag cadastrada abaixo."
            isHardwareSupported && !isHardwareEnabled ->
                "Sensor NFC físico detectado, porém DESATIVADO nas configurações do Android. " +
                    "Você pode ativar o NFC ou simular a leitura abaixo."
            else ->
                "Modo Simulador (Desktop / Emulador sem hardware NFC): selecione uma tag abaixo " +
                    "ou digite um código hexadecimal para simular a aproximação."
        }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Aproximação de Tag NFC") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(
                        text = hardwareStatusText,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(10.dp),
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    OutlinedTextField(
                        value = manualTagInput,
                        onValueChange = { manualTagInput = it },
                        label = { Text("Código da Tag NFC") },
                        placeholder = { Text("Ex.: 04:A2:8B:1A:5C") },
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                    )
                    Button(
                        onClick = { onScanTag(manualTagInput) },
                        enabled = manualTagInput.isNotBlank(),
                    ) {
                        Text("Ler")
                    }
                }

                if (equipments.isNotEmpty()) {
                    Text(
                        text = "Ou toque em um equipamento para simular sua Tag NFC:",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    equipments.take(4).forEach { eq ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            onClick = { onScanTag(eq.nfcTagId) },
                            colors =
                                CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                ),
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text(
                                    text = eq.name,
                                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                                )
                                Text(
                                    text = "Tag: ${eq.nfcTagId}",
                                    style = MaterialTheme.typography.bodySmall,
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        },
    )
}

@Composable
private fun WelcomeAndSearchHeader(
    userName: String,
    userRole: UserRole,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    feedbackMessage: String?,
    canUndo: Boolean,
    onUndo: (() -> Unit)?,
) {
    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "Olá, $userName!",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onBackground,
            )
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.secondaryContainer,
            ) {
                Text(
                    text = userRole.badgeText,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                )
            }
        }
        Text(
            text =
                if (userRole.canManageInventory) {
                    "Modo de Gestão (${userRole.label}): consulte o acervo, cadastre itens ou dê baixa em devoluções."
                } else {
                    "Consulte o acervo abaixo ou use o botão 'Ler Tag NFC' para retirada rápida."
                },
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        if (feedbackMessage != null) {
            Spacer(Modifier.height(8.dp))
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp).fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = feedbackMessage,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.weight(1f),
                    )
                    if (canUndo && onUndo != null) {
                        TextButton(onClick = onUndo) {
                            Text("Desfazer")
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(8.dp))

        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchQueryChange,
            label = { Text("Buscar equipamento") },
            placeholder = { Text("Nome, categoria ou tag NFC...") },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Ícone de busca",
                )
            },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(
                        onClick = { onSearchQueryChange("") },
                        modifier =
                            Modifier.semantics {
                                contentDescription = "Limpar busca"
                            },
                    ) {
                        Icon(
                            imageVector = Icons.Default.Clear,
                            contentDescription = "Limpar texto da busca",
                        )
                    }
                }
            },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
fun CatalogListContent(
    screenState: CatalogScreenState,
    onRefresh: () -> Unit,
    onEquipmentClick: (Equipment) -> Unit,
    searchQuery: String = "",
    modifier: Modifier = Modifier,
) {
    when (screenState) {
        CatalogScreenState.Loading -> {
            Box(
                modifier = modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(
                        modifier =
                            Modifier.semantics {
                                contentDescription = "Carregando lista de equipamentos"
                            },
                    )
                    Spacer(Modifier.height(16.dp))
                    Text(
                        text = "Buscando dados da nuvem...",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
        is CatalogScreenState.Error -> {
            Box(
                modifier = modifier.fillMaxSize().padding(24.dp),
                contentAlignment = Alignment.Center,
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Erro de conexão",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.semantics { heading() },
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = screenState.message,
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
        CatalogScreenState.Empty -> {
            EmptyCatalogState(modifier = modifier)
        }
        is CatalogScreenState.Success -> {
            val visibleItems =
                if (searchQuery.isBlank()) {
                    screenState.items
                } else {
                    br.ufrn.smorg.domain
                        .filterEquipments(screenState.items, searchQuery)
                }

            if (visibleItems.isEmpty()) {
                EmptyCatalogState(modifier = modifier)
            } else {
                val listState = rememberLazyListState()
                LaunchedEffect(visibleItems.firstOrNull()?.id) {
                    listState.scrollToItem(0)
                }

                LazyColumn(
                    state = listState,
                    modifier = modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 84.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    items(visibleItems, key = { it.id }) { equipment ->
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
    hasActiveSearchOrFilter: Boolean = false,
    onClearFilters: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
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
        if (hasActiveSearchOrFilter) {
            TextButton(onClick = onClearFilters) {
                Text("Limpar", style = MaterialTheme.typography.labelSmall)
            }
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
                modifier = Modifier.semantics { heading() },
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
