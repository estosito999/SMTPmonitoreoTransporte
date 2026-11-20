package com.example.smtpmonitoreo.data.repository

import com.example.smtpmonitoreo.data.model.AccountStatus
import com.example.smtpmonitoreo.data.model.LogLevel
import com.example.smtpmonitoreo.data.model.SystemLog
import com.example.smtpmonitoreo.data.model.UserAccount
import com.example.smtpmonitoreo.data.model.UserRole
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.util.UUID

private const val MAX_LOGS = 200

class AdminRepositoryImpl : AdminRepository {

    private val startedAt = System.currentTimeMillis()

    private val _usersFlow = MutableStateFlow(
        listOf(
            UserAccount(uid = "u1", email = "pasajero.demo@smtp.lapaz.bo", fullName = "Carlos Mendoza", role = UserRole.PASAJERO),
            UserAccount(uid = "u2", email = "chofer.minibus102@smtp.lapaz.bo", fullName = "Don Rolando Mamani", role = UserRole.CHOFER),
            UserAccount(uid = "u3", email = "alcaldia.control@smtp.lapaz.bo", fullName = "Ing. Laura Quispe", role = UserRole.ALCALDIA),
            UserAccount(uid = "u4", email = "admin.master@smtp.lapaz.bo", fullName = "Admin Israel Pérez", role = UserRole.ADMINISTRADOR)
        )
    )
    override val usersFlow: StateFlow<List<UserAccount>> = _usersFlow.asStateFlow()

    // Eventos de ejemplo (modo demostración) hasta conectar Firebase / Crashlytics
    private val _logsFlow = MutableStateFlow(
        listOf(
            SystemLog("log_3", startedAt - 60_000L, LogLevel.ERROR, "SYNC", "Timeout al sincronizar incidentes (simulado)"),
            SystemLog("log_2", startedAt - 180_000L, LogLevel.WARN, "GPS", "Unidad 3091-XYZ sin señal por 120 s (simulado)"),
            SystemLog("log_1", startedAt - 300_000L, LogLevel.INFO, "SISTEMA", "Aplicación iniciada en modo demostración")
        )
    )
    override val logsFlow: StateFlow<List<SystemLog>> = _logsFlow.asStateFlow()

    override suspend fun createUser(user: UserAccount): Result<Unit> {
        val now = System.currentTimeMillis()
        _usersFlow.update { it + user.copy(uid = "u_$now", createdAt = now) }
        return Result.success(Unit)
    }

    override suspend fun updateUserRole(uid: String, role: UserRole): Result<Unit> {
        _usersFlow.update { list -> list.map { if (it.uid == uid) it.copy(role = role) else it } }
        return Result.success(Unit)
    }

    override suspend fun updateUserStatus(uid: String, status: AccountStatus): Result<Unit> {
        _usersFlow.update { list -> list.map { if (it.uid == uid) it.copy(status = status) else it } }
        return Result.success(Unit)
    }

    override suspend fun appendLog(level: LogLevel, source: String, message: String) {
        val log = SystemLog(
            id = UUID.randomUUID().toString(),
            timestamp = System.currentTimeMillis(),
            level = level,
            source = source,
            message = message
        )
        _logsFlow.update { (listOf(log) + it).take(MAX_LOGS) }
    }
}