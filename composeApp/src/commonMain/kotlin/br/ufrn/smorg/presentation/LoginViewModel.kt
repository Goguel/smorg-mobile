package br.ufrn.smorg.presentation

import androidx.lifecycle.ViewModel
import br.ufrn.smorg.domain.UserRole
import br.ufrn.smorg.domain.deterministicUserUuid
import br.ufrn.smorg.domain.formatDisplayName
import br.ufrn.smorg.domain.isIdentifierValid
import br.ufrn.smorg.domain.isPasswordValid
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class LoginUiState(
    val identifier: String = "",
    val password: String = "",
    val selectedRole: UserRole = UserRole.STUDENT,
    val isPasswordVisible: Boolean = false,
    val identifierError: String? = null,
    val passwordError: String? = null,
) {
    val displayName: String
        get() = formatDisplayName(identifier)

    val userUuid: String
        get() = deterministicUserUuid(identifier)
}

class LoginViewModel : ViewModel() {
    private val _state = MutableStateFlow(LoginUiState())
    val state: StateFlow<LoginUiState> = _state.asStateFlow()

    fun onIdentifierChange(newIdentifier: String) {
        _state.update {
            it.copy(
                identifier = newIdentifier,
                identifierError = null,
            )
        }
    }

    fun onPasswordChange(newPassword: String) {
        _state.update {
            it.copy(
                password = newPassword,
                passwordError = null,
            )
        }
    }

    fun onRoleSelect(role: UserRole) {
        _state.update { it.copy(selectedRole = role) }
    }

    fun togglePasswordVisibility() {
        _state.update { it.copy(isPasswordVisible = !it.isPasswordVisible) }
    }

    fun clearFields() {
        _state.update { LoginUiState(selectedRole = it.selectedRole) }
    }

    fun submitLogin(): Boolean {
        val current = _state.value
        val validIdentifier = isIdentifierValid(current.identifier)
        val validPassword = isPasswordValid(current.password)

        _state.update {
            it.copy(
                identifierError = if (!validIdentifier) "A matrícula ou e-mail é obrigatória." else null,
                passwordError = if (!validPassword) "A senha deve ter no mínimo 6 caracteres." else null,
            )
        }

        return validIdentifier && validPassword
    }
}
