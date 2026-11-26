package br.ufrn.smorg.presentation

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.runComposeUiTest
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.compose.LocalLifecycleOwner
import br.ufrn.smorg.App
import br.ufrn.smorg.AppModule
import br.ufrn.smorg.EquipmentCatalogScreen
import br.ufrn.smorg.data.InMemoryEquipmentRepository
import br.ufrn.smorg.domain.EquipmentListing
import br.ufrn.smorg.domain.EquipmentRepository
import br.ufrn.smorg.domain.EquipmentStatus
import br.ufrn.smorg.presentation.screens.LoginScreen
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import kotlin.random.Random
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.time.Duration

@OptIn(ExperimentalTestApi::class, ExperimentalCoroutinesApi::class)
class ScreensUiTest {
    @BeforeTest
    fun setUpMainDispatcher() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @AfterTest
    fun tearDownMainDispatcher() {
        Dispatchers.resetMain()
    }

    private fun ComposeUiTest.setContentWithLifecycle(content: @Composable () -> Unit) {
        val lifecycleOwner =
            object : LifecycleOwner {
                private val registry =
                    LifecycleRegistry(this).apply {
                        currentState = Lifecycle.State.RESUMED
                    }
                override val lifecycle: Lifecycle = registry
            }
        setContent {
            CompositionLocalProvider(LocalLifecycleOwner provides lifecycleOwner) {
                content()
            }
        }
    }

    @Test
    fun loginScreenShowsValidationErrorsOnInvalidInput() =
        runComposeUiTest {
            var loginCalled = false
            val loginViewModel = LoginViewModel()

            setContentWithLifecycle {
                LoginScreen(
                    viewModel = loginViewModel,
                    onLoginSuccess = { loginCalled = true },
                )
            }

            onNodeWithText("Entrar").performClick()

            onNodeWithText("A matrícula ou e-mail é obrigatória.").assertIsDisplayed()
            onNodeWithText("A senha deve ter no mínimo 6 caracteres.").assertIsDisplayed()
            assertFalse(loginCalled)
        }

    @Test
    fun loginScreenAcceptsValidCredentialsAndCallsOnLoginSuccess() =
        runComposeUiTest {
            var loginCalled = false
            val loginViewModel = LoginViewModel()

            setContentWithLifecycle {
                LoginScreen(
                    viewModel = loginViewModel,
                    onLoginSuccess = { loginCalled = true },
                )
            }

            onNodeWithText("Matrícula ou E-mail").performTextInput("20240027427")
            onNodeWithText("Senha").performTextInput("senha123")
            onNodeWithText("Entrar").performClick()

            assertTrue(loginCalled)
        }

    @Test
    fun appNavigatesFromLoginToCatalogAndOpensEquipmentDetails() =
        runComposeUiTest {
            val repo = InMemoryEquipmentRepository(delayDuration = Duration.ZERO)
            val catalogViewModel = CatalogViewModel(repo)
            val loginViewModel = LoginViewModel()
            val module = AppModule(apiBaseUrl = null, dataDirectory = "build/tmp/ui-test-${Random.nextLong()}")

            setContentWithLifecycle {
                App(
                    appModule = module,
                    catalogViewModel = catalogViewModel,
                    loginViewModel = loginViewModel,
                )
            }

            onNodeWithText("Matrícula ou E-mail").performTextInput("aluno@ufrn.edu.br")
            onNodeWithText("Senha").performTextInput("123456")
            onNodeWithText("Entrar").performClick()

            onNodeWithText("SmOrg Mobile").assertIsDisplayed()
            onNodeWithText("Osciloscópio Digital 100MHz").assertIsDisplayed()

            onNodeWithText("Osciloscópio Digital 100MHz").performClick()
            onNodeWithText("Categoria: Bancada e Medição").assertIsDisplayed()
        }

    @Test
    fun catalogScreenShowsEmptyStateWhenRepositoryReturnsNoItems() =
        runComposeUiTest {
            setContentWithLifecycle {
                EquipmentCatalogScreen(
                    state = CatalogUiState(screen = CatalogScreenState.Empty),
                    snackbarHostState = SnackbarHostState(),
                    onRefresh = {},
                    onFilterSelect = {},
                    onEquipmentClick = {},
                )
            }

            onNodeWithText("Nenhum equipamento encontrado").assertIsDisplayed()
            onNodeWithText("Tente selecionar outro filtro de status ou cadastre itens na API.").assertIsDisplayed()
        }

    @Test
    fun catalogScreenShowsErrorAndRetriesWhenClickingButton() =
        runComposeUiTest {
            val repo = InMemoryEquipmentRepository(delayDuration = Duration.ZERO, shouldFail = true)
            val viewModel = CatalogViewModel(repo)
            val loginViewModel = LoginViewModel()
            val module = AppModule(apiBaseUrl = null, dataDirectory = "build/tmp/ui-test-${Random.nextLong()}")

            setContentWithLifecycle {
                App(
                    appModule = module,
                    catalogViewModel = viewModel,
                    loginViewModel = loginViewModel,
                )
            }

            onNodeWithText("Matrícula ou E-mail").performTextInput("20240027427")
            onNodeWithText("Senha").performTextInput("123456")
            onNodeWithText("Entrar").performClick()

            onNodeWithText("Sem conexão com o servidor. Confira a rede e tente novamente.").assertIsDisplayed()

            repo.shouldFail = false
            onNodeWithText("Tentar Novamente").performClick()

            onNodeWithText("Osciloscópio Digital 100MHz").assertIsDisplayed()
        }

    @Test
    fun catalogScreenShowsOfflineCacheNoticeWhenDataIsFromCache() =
        runComposeUiTest {
            val delegate = InMemoryEquipmentRepository(delayDuration = Duration.ZERO)
            val cachedRepo =
                object : EquipmentRepository by delegate {
                    override suspend fun listEquipments(status: EquipmentStatus?): EquipmentListing =
                        delegate.listEquipments(status).copy(fromCache = true)
                }
            val viewModel = CatalogViewModel(cachedRepo)
            val loginViewModel = LoginViewModel()
            val module = AppModule(apiBaseUrl = null, dataDirectory = "build/tmp/ui-test-${Random.nextLong()}")

            setContentWithLifecycle {
                App(
                    appModule = module,
                    catalogViewModel = viewModel,
                    loginViewModel = loginViewModel,
                )
            }

            onNodeWithText("Matrícula ou E-mail").performTextInput("20240027427")
            onNodeWithText("Senha").performTextInput("123456")
            onNodeWithText("Entrar").performClick()

            onNodeWithText("Sem conexão: mostrando a última lista salva no aparelho.").assertIsDisplayed()
        }

    @Test
    fun detailScreenAllowsBorrowingWithConfirmationDialogAndUndo() =
        runComposeUiTest {
            val repo = InMemoryEquipmentRepository(delayDuration = Duration.ZERO)
            val catalogViewModel = CatalogViewModel(repo)
            val loginViewModel = LoginViewModel()
            val module = AppModule(apiBaseUrl = null, dataDirectory = "build/tmp/ui-test-${Random.nextLong()}")

            setContentWithLifecycle {
                App(
                    appModule = module,
                    catalogViewModel = catalogViewModel,
                    loginViewModel = loginViewModel,
                )
            }

            onNodeWithText("Matrícula ou E-mail").performTextInput("miguel@ufrn.edu.br")
            onNodeWithText("Senha").performTextInput("123456")
            onNodeWithText("Entrar").performClick()

            onNodeWithText("Olá, Miguel!").assertIsDisplayed()
            onNodeWithText("Osciloscópio Digital 100MHz").performClick()

            onNodeWithText("Solicitar Empréstimo").performClick()
            onNodeWithText("Confirmar Empréstimo").assertIsDisplayed()
            onNodeWithText("Confirmar Retirada").performClick()

            onNodeWithText("Status: Emprestado").assertIsDisplayed()
            onNodeWithText("Desfazer").performClick()
            onNodeWithText("Status: Disponível").assertIsDisplayed()
        }

    @Test
    fun catalogScreenAllowsCreatingNewEquipmentViaDialog() =
        runComposeUiTest {
            val repo = InMemoryEquipmentRepository(delayDuration = Duration.ZERO)
            val catalogViewModel = CatalogViewModel(repo)
            val loginViewModel = LoginViewModel()
            val module = AppModule(apiBaseUrl = null, dataDirectory = "build/tmp/ui-test-${Random.nextLong()}")

            setContentWithLifecycle {
                App(
                    appModule = module,
                    catalogViewModel = catalogViewModel,
                    loginViewModel = loginViewModel,
                )
            }

            onNodeWithText("Monitor").performClick()
            onNodeWithText("Matrícula ou E-mail").performTextInput("miguel@ufrn.edu.br")
            onNodeWithText("Senha").performTextInput("123456")
            onNodeWithText("Entrar").performClick()

            onNodeWithText("🛠️ Perfil: Monitor").assertIsDisplayed()
            onNodeWithText("Novo").performClick()
            onNodeWithText("Cadastrar Novo Equipamento").assertIsDisplayed()

            onNodeWithText("Nome do Equipamento").performTextInput("Analisador Lógico 8 Canais")
            onNodeWithText("Categoria").performTextInput("Sistemas Embarcados")
            onNodeWithText("Salvar Equipamento").performClick()

            onNodeWithText("Analisador Lógico 8 Canais").assertIsDisplayed()
        }
}
