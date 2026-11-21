package com.example.smtpmonitoreo.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smtpmonitoreo.data.model.UserRole
import com.example.smtpmonitoreo.data.repository.AuthRepository
import com.example.smtpmonitoreo.data.repository.SessionState
import com.example.smtpmonitoreo.domain.AuthRules
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import com.example.smtpmonitoreo.domain.DemoAccounts
import com.example.smtpmonitoreo.domain.AppMonitor

data class AuthUiState(
    val email: String = "",
    val password: String = "",
    val fullName: String = "",
    val isRegisterMode: Boolean = false,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

class AuthViewModel(
    private val repository: AuthRepository,
    private val monitor: AppMonitor
) : ViewModel() {

    val session: StateFlow<SessionState> = repository.session

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    fun onEmailChanged(email: String) =
        _uiState.update { it.copy(email = email, errorMessage = null) }

    fun onPasswordChanged(password: String) =
        _uiState.update { it.copy(password = password, errorMessage = null) }

    fun onFullNameChanged(name: String) =
        _uiState.update { it.copy(fullName = name, errorMessage = null) }

    fun toggleMode() =
        _uiState.update { it.copy(isRegisterMode = !it.isRegisterMode, errorMessage = null) }

    fun submit() {
        val s = _uiState.value
        if (s.isLoading) return

        val validationError = if (s.isRegisterMode) {
            AuthRules.validateRegistration(s.fullName, s.email, s.password)
        } else {
            AuthRules.validateLogin(s.email, s.password)
        }
        if (validationError != null) {
            _uiState.update { it.copy(errorMessage = validationError) }
            return
        }

        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        viewModelScope.launch {
            val result = if (s.isRegisterMode) {
                repository.register(email = s.email, password = s.password, fullName = s.fullName)
            } else {
                repository.login(email = s.email, password = s.password)
            }
            finish(result, event = if (s.isRegisterMode) "sign_up" else "login")
        }
    }

    /** Solo debug: rellena el correo de la cuenta de prueba de ese rol. */
    fun prefillDemoAccount(role: UserRole) =
        _uiState.update {
            it.copy(email = DemoAccounts.emailFor(role), password = "", errorMessage = null)
        }

    fun logout() {
        viewModelScope.launch { repository.logout() }
    }

    private fun finish(result: Result<*>, event: String) {
        result
            .onSuccess {
                monitor.trackEvent(event, mapOf("method" to "email"))
                _uiState.value = AuthUiState() // no conservar la contraseña en memoria
            }
            .onFailure { e ->
                _uiState.update {
                    it.copy(isLoading = false, errorMessage = e.localizedMessage ?: "Error en la autenticación.")
                }
            }
    }
}