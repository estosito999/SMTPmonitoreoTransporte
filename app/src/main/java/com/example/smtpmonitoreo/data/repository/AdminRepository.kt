package com.example.smtpmonitoreo.data.repository

import com.example.smtpmonitoreo.data.model.AccountStatus
import com.example.smtpmonitoreo.data.model.LogLevel
import com.example.smtpmonitoreo.data.model.SystemLog
import com.example.smtpmonitoreo.data.model.UserAccount
import com.example.smtpmonitoreo.data.model.UserRole
import kotlinx.coroutines.flow.StateFlow

interface AdminRepository {
    val usersFlow: StateFlow<List<UserAccount>>
    val logsFlow: StateFlow<List<SystemLog>>

    suspend fun updateUserRole(uid: String, role: UserRole): Result<Unit>
    suspend fun updateUserStatus(uid: String, status: AccountStatus): Result<Unit>
    suspend fun appendLog(level: LogLevel, source: String, message: String)
}