package com.example.smtpmonitoreo.data.repository

import com.example.smtpmonitoreo.data.model.UserAccount
import com.example.smtpmonitoreo.data.model.UserRole
import kotlinx.coroutines.flow.StateFlow

interface AuthRepository {
    val currentUserFlow: StateFlow<UserAccount?>

    suspend fun login(email: String, password: String): Result<UserAccount>
    suspend fun register(email: String, password: String, fullName: String, role: UserRole): Result<UserAccount>
    suspend fun logout()
    fun getCurrentUser(): UserAccount?
    suspend fun quickLoginAsRole(role: UserRole): UserAccount
}
