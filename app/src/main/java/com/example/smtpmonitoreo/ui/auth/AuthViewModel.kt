package com.example.smtpmonitoreo.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smtpmonitoreo.data.model.UserAccount
import com.example.smtpmonitoreo.data.model.UserRole
import com.example.smtpmonitoreo.data.repository.AuthRepository
import com.example.smtpmonitoreo.data.repository.AuthRepositoryImpl
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AuthUiState(
    val email: String = "",
    val password: String = "",
    val fullName: String = "",
    val selectedRole: UserRole = UserRole.PASAJERO,
    val isRegisterMode: Boolean = false,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val loggedInUser: UserAccount? = null
)

class AuthViewModel(
    private val repository: AuthRepository = AuthRepositoryImpl.instance
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            repository.currentUserFlow.collect { user ->
                _uiState.update { it.copy(loggedInUser = user) }
            }
        }
    }

    fun onEmailChanged(email: String) {
        _uiState.update { it.copy(email = email, errorMessage = null) }
    }

    fun onPasswordChanged(password: String) {
        _uiState.update { it.copy(password = password, errorMessage = null) }
    }

    fun onFullNameChanged(name: String) {
        _uiState.update { it.copy(fullName = name, errorMessage = null) }
    }

    fun onRoleSelected(role: UserRole) {
        _uiState.update { it.copy(selectedRole = role) }
    }

    fun toggleMode() {
        _uiState.update {
            it.copy(
                isRegisterMode = !it.isRegisterMode,
                errorMessage = null
            )
        }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    fun submit() {
        val currentState = _uiState.value
        if (currentState.email.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Ingrese su correo electrónico.") }
            return
        }
        if (currentState.password.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Ingrese su contraseña.") }
            return
        }
        if (currentState.isRegisterMode && currentState.fullName.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Ingrese su nombre completo.") }
            return
        }

        _uiState.update { it.copy(isLoading = true, errorMessage = null) }

        viewModelScope.launch {
            val result = if (currentState.isRegisterMode) {
                repository.register(
                    email = currentState.email,
                    password = currentState.password,
                    fullName = currentState.fullName,
                    role = currentState.selectedRole
                )
            } else {
                repository.login(
                    email = currentState.email,
                    password = currentState.password
                )
            }

            result.onSuccess { user ->
                _uiState.update { it.copy(isLoading = false, loggedInUser = user) }
            }.onFailure { exception ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = exception.localizedMessage ?: "Error en la autenticación"
                    )
                }
            }
        }
    }

    fun quickLoginAs(role: UserRole) {
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        viewModelScope.launch {
            val user = repository.quickLoginAsRole(role)
            _uiState.update { it.copy(isLoading = false, loggedInUser = user) }
        }
    }
}
