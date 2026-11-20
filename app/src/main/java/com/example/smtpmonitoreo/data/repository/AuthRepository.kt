package com.example.smtpmonitoreo.data.repository

import com.example.smtpmonitoreo.data.model.UserAccount
import com.example.smtpmonitoreo.data.model.UserRole
import kotlinx.coroutines.flow.StateFlow

sealed interface SessionState {
    /** Verificando si hay una sesión guardada (arranque de la app). */
    data object Loading : SessionState
    data object SignedOut : SessionState
    data class SignedIn(val user: UserAccount) : SessionState
}

interface AuthRepository {
    val session: StateFlow<SessionState>
    val currentUserFlow: StateFlow<UserAccount?>

    suspend fun login(email: String, password: String): Result<UserAccount>

    /** Toda cuenta nueva nace como Pasajero; los demás roles los asigna un administrador. */
    suspend fun register(email: String, password: String, fullName: String): Result<UserAccount>

    suspend fun logout()
    fun getCurrentUser(): UserAccount?

}