package br.ufrn.smorg.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.ufrn.smorg.domain.DEFAULT_RETURN_DUE_DATE
import br.ufrn.smorg.domain.Equipment
import br.ufrn.smorg.domain.EquipmentRepository
import br.ufrn.smorg.domain.EquipmentStatus
import br.ufrn.smorg.domain.RepositoryFailure
import br.ufrn.smorg.domain.UserRole
import br.ufrn.smorg.domain.borrowingEquipment
import br.ufrn.smorg.domain.deterministicUserUuid
import br.ufrn.smorg.domain.filterEquipments
import br.ufrn.smorg.domain.replacingEquipment
import br.ufrn.smorg.domain.returningEquipment
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

sealed interface CatalogScreenState {
    data object Loading : CatalogScreenState

    data object Empty : CatalogScreenState

    data class Success(
        val items: List<Equipment>,
        val fromCache: Boolean = false,
    ) : CatalogScreenState

    data class Error(
        val message: String,
    ) : CatalogScreenState
}

sealed interface DetailScreenState {
    data object Loading : DetailScreenState

    data object Empty : DetailScreenState

    data class Success(
        val equipment: Equipment,
    ) : DetailScreenState

    data class Error(
        val message: String,
    ) : DetailScreenState
}

data class CatalogUiState(
    val screen: CatalogScreenState = CatalogScreenState.Loading,
    val detailScreen: DetailScreenState = DetailScreenState.Empty,
    val selectedFilter: EquipmentStatus? = null,
    val searchQuery: String = "",
    val notice: String? = null,
    val feedbackMessage: String? = null,
    val previousEquipmentSnapshot: Equipment? = null,
    val lastScannedUnregisteredTag: String? = null,
) {
    val items: List<Equipment>
        get() = (screen as? CatalogScreenState.Success)?.items.orEmpty()

    val filteredItems: List<Equipment>
        get() = filterEquipments(items, searchQuery)

    val canUndoLastAction: Boolean
        get() = previousEquipmentSnapshot != null
}

class CatalogViewModel(
    private val repository: EquipmentRepository,
) : ViewModel() {
    private val _state = MutableStateFlow(CatalogUiState())
    val state: StateFlow<CatalogUiState> = _state.asStateFlow()

    init {
        loadEquipments()
    }

    fun loadEquipments() {
        val currentFilter = _state.value.selectedFilter
        _state.update { it.copy(screen = CatalogScreenState.Loading, notice = null) }
        viewModelScope.launch {
            val nextScreen =
                try {
                    val listing = repository.listEquipments(currentFilter)
                    if (listing.items.isEmpty()) {
                        CatalogScreenState.Empty
                    } else {
                        CatalogScreenState.Success(items = listing.items, fromCache = listing.fromCache)
                    }
                } catch (e: RepositoryFailure.NoConnection) {
                    CatalogScreenState.Error("Sem conexão com o servidor. Confira a rede e tente novamente.")
                } catch (e: RepositoryFailure.InvalidResponse) {
                    CatalogScreenState.Error("O servidor respondeu de forma inesperada (${e.message}).")
                }
            _state.update { it.copy(screen = nextScreen) }
        }
    }

    fun selectFilter(status: EquipmentStatus) {
        val nextFilter = if (_state.value.selectedFilter == status) null else status
        _state.update { it.copy(selectedFilter = nextFilter) }
        loadEquipments()
    }

    fun clearFiltersAndSearch() {
        val hadFilter = _state.value.selectedFilter != null
        _state.update { it.copy(selectedFilter = null, searchQuery = "") }
        if (hadFilter) {
            loadEquipments()
        }
    }

    fun onSearchQueryChange(newQuery: String) {
        _state.update { it.copy(searchQuery = newQuery) }
    }

    fun loadEquipmentDetail(id: String) {
        if (id.isBlank()) {
            _state.update { it.copy(detailScreen = DetailScreenState.Empty) }
            return
        }
        val cachedInList = _state.value.items.find { it.id == id }
        if (cachedInList != null) {
            _state.update { it.copy(detailScreen = DetailScreenState.Success(cachedInList)) }
            return
        }

        _state.update { it.copy(detailScreen = DetailScreenState.Loading) }
        viewModelScope.launch {
            val nextDetail =
                try {
                    val found = repository.getEquipmentById(id)
                    DetailScreenState.Success(found)
                } catch (e: RepositoryFailure.NoConnection) {
                    DetailScreenState.Error("Sem conexão para buscar detalhes do equipamento.")
                } catch (e: RepositoryFailure.InvalidResponse) {
                    DetailScreenState.Error("Não foi possível carregar o equipamento (${e.message}).")
                }
            _state.update { it.copy(detailScreen = nextDetail) }
        }
    }

    fun createEquipment(
        name: String,
        category: String,
        nfcTagId: String?,
    ) {
        val cleanName = name.trim()
        val cleanCategory = category.trim()
        val cleanTag = nfcTagId?.trim()?.takeIf { it.isNotEmpty() }

        viewModelScope.launch {
            try {
                val created = repository.createEquipment(cleanName, cleanCategory, cleanTag)
                _state.update { current ->
                    val currentList = current.items
                    val fromCache = (current.screen as? CatalogScreenState.Success)?.fromCache ?: false
                    current.copy(
                        screen = CatalogScreenState.Success(listOf(created) + currentList, fromCache = fromCache),
                        lastScannedUnregisteredTag = null,
                        feedbackMessage = "Equipamento '${created.name}' cadastrado com sucesso!",
                    )
                }
            } catch (_: RepositoryFailure) {
                val fallbackId = "EQ-LOCAL-${(_state.value.items.size + 1).toString().padStart(3, '0')}"
                val localCreated =
                    Equipment(
                        id = fallbackId,
                        name = cleanName,
                        category = cleanCategory,
                        nfcTagId = cleanTag ?: "Sem Tag NFC",
                        status = EquipmentStatus.AVAILABLE,
                    )
                _state.update { current ->
                    val currentList = current.items
                    current.copy(
                        screen = CatalogScreenState.Success(listOf(localCreated) + currentList, fromCache = true),
                        lastScannedUnregisteredTag = null,
                        feedbackMessage =
                            "Equipamento '${localCreated.name}' salvo localmente " +
                                "(servidor indisponível para escrita).",
                    )
                }
            }
        }
    }

    fun handleScannedNfcTag(
        tagId: String,
        onEquipmentResolved: (Equipment) -> Unit,
    ) {
        val cleanTag = tagId.trim()
        if (cleanTag.isEmpty()) return

        val inLoadedList =
            _state.value.items.find {
                it.nfcTagId.equals(cleanTag, ignoreCase = true)
            }
        if (inLoadedList != null) {
            _state.update {
                it.copy(
                    detailScreen = DetailScreenState.Success(inLoadedList),
                    feedbackMessage = "Tag NFC '$cleanTag' reconhecida: ${inLoadedList.name}",
                )
            }
            onEquipmentResolved(inLoadedList)
            return
        }

        viewModelScope.launch {
            try {
                val remoteMatch = repository.getEquipmentByTag(cleanTag)
                _state.update {
                    it.copy(
                        detailScreen = DetailScreenState.Success(remoteMatch),
                        feedbackMessage = "Tag NFC '$cleanTag' reconhecida: ${remoteMatch.name}",
                    )
                }
                onEquipmentResolved(remoteMatch)
            } catch (_: RepositoryFailure) {
                _state.update {
                    it.copy(
                        lastScannedUnregisteredTag = cleanTag,
                        feedbackMessage =
                            "Tag NFC '$cleanTag' detectada! Nenhum equipamento vinculado " +
                                "— toque em '+ Novo' para cadastrá-la.",
                    )
                }
            }
        }
    }

    fun borrowEquipment(
        id: String,
        borrowerName: String,
        userId: String = deterministicUserUuid(borrowerName),
        userRole: UserRole = UserRole.STUDENT,
    ) {
        val currentScreen = _state.value.screen as? CatalogScreenState.Success
        val currentDetail = (_state.value.detailScreen as? DetailScreenState.Success)?.equipment
        val target = currentScreen?.items?.find { it.id == id } ?: currentDetail ?: return

        val optimisticList =
            currentScreen?.items?.borrowingEquipment(
                id = id,
                borrowerName = borrowerName,
                returnDueDate = DEFAULT_RETURN_DUE_DATE,
            )
        val optimisticItem =
            target.copy(
                status = EquipmentStatus.BORROWED,
                borrowerName = borrowerName,
                returnDueDate = DEFAULT_RETURN_DUE_DATE,
            )

        _state.update { current ->
            current.copy(
                screen =
                    if (currentScreen != null && optimisticList != null) {
                        currentScreen.copy(items = optimisticList)
                    } else {
                        current.screen
                    },
                detailScreen = DetailScreenState.Success(optimisticItem),
                previousEquipmentSnapshot = target,
                feedbackMessage = "Retirada de '${target.name}' confirmada! Prazo: $DEFAULT_RETURN_DUE_DATE.",
            )
        }

        viewModelScope.launch {
            try {
                val confirmedFromServer =
                    repository.borrowEquipment(
                        equipmentId = id,
                        userId = userId,
                        borrowerName = borrowerName,
                        userRole = userRole,
                    )
                _state.update { current ->
                    val latestScreen = current.screen as? CatalogScreenState.Success
                    current.copy(
                        screen =
                            if (latestScreen != null) {
                                latestScreen.copy(items = latestScreen.items.replacingEquipment(confirmedFromServer))
                            } else {
                                current.screen
                            },
                        detailScreen = DetailScreenState.Success(confirmedFromServer),
                        feedbackMessage =
                            "Retirada de '${confirmedFromServer.name}' sincronizada! " +
                                "Prazo (${userRole.label}): ${confirmedFromServer.returnDueDate}.",
                    )
                }
            } catch (_: RepositoryFailure) {
                // Mantém o estado otimista local caso a rede esteja offline
            }
        }
    }

    fun returnEquipment(id: String) {
        val currentScreen = _state.value.screen as? CatalogScreenState.Success
        val currentDetail = (_state.value.detailScreen as? DetailScreenState.Success)?.equipment
        val target = currentScreen?.items?.find { it.id == id } ?: currentDetail ?: return

        val optimisticList = currentScreen?.items?.returningEquipment(id = id)
        val optimisticItem =
            target.copy(
                status = EquipmentStatus.AVAILABLE,
                borrowerName = null,
                returnDueDate = null,
            )

        _state.update { current ->
            current.copy(
                screen =
                    if (currentScreen != null && optimisticList != null) {
                        currentScreen.copy(items = optimisticList)
                    } else {
                        current.screen
                    },
                detailScreen = DetailScreenState.Success(optimisticItem),
                previousEquipmentSnapshot = target,
                feedbackMessage = "Devolução de '${target.name}' registrada com sucesso!",
            )
        }

        viewModelScope.launch {
            try {
                val confirmedFromServer = repository.returnEquipment(equipmentId = id)
                _state.update { current ->
                    val latestScreen = current.screen as? CatalogScreenState.Success
                    current.copy(
                        screen =
                            if (latestScreen != null) {
                                latestScreen.copy(items = latestScreen.items.replacingEquipment(confirmedFromServer))
                            } else {
                                current.screen
                            },
                        detailScreen = DetailScreenState.Success(confirmedFromServer),
                    )
                }
            } catch (_: RepositoryFailure) {
                // Mantém o estado otimista local caso a rede esteja offline
            }
        }
    }

    fun undoLastAction(
        userId: String = deterministicUserUuid("undo-user"),
        borrowerName: String = "Estudante",
        userRole: UserRole = UserRole.STUDENT,
    ) {
        val snapshot = _state.value.previousEquipmentSnapshot ?: return
        val currentScreen = _state.value.screen as? CatalogScreenState.Success

        _state.update { current ->
            current.copy(
                screen =
                    if (currentScreen != null) {
                        currentScreen.copy(items = currentScreen.items.replacingEquipment(snapshot))
                    } else {
                        current.screen
                    },
                detailScreen = DetailScreenState.Success(snapshot),
                previousEquipmentSnapshot = null,
                feedbackMessage = "Ação desfeita para '${snapshot.name}'.",
            )
        }

        viewModelScope.launch {
            try {
                if (snapshot.status == EquipmentStatus.AVAILABLE) {
                    repository.returnEquipment(snapshot.id)
                } else if (snapshot.status == EquipmentStatus.BORROWED) {
                    repository.borrowEquipment(
                        equipmentId = snapshot.id,
                        userId = userId,
                        borrowerName = snapshot.borrowerName ?: borrowerName,
                        userRole = userRole,
                    )
                }
            } catch (_: RepositoryFailure) {
                // Ignora erro de rede ao desfazer offline
            }
        }
    }

    fun dismissFeedback() {
        _state.update { it.copy(feedbackMessage = null) }
    }
}
