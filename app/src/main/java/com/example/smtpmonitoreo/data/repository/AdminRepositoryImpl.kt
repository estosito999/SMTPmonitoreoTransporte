package com.example.smtpmonitoreo.data.repository

import com.example.smtpmonitoreo.data.model.AccountStatus
import com.example.smtpmonitoreo.data.model.LogLevel
import com.example.smtpmonitoreo.data.model.SystemLog
import com.example.smtpmonitoreo.data.model.UserAccount
import com.example.smtpmonitoreo.data.model.UserRole
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.tasks.await

private const val MAX_LOGS = 100L

class AdminRepositoryImpl(
    db: FirebaseFirestore,
    auth: AuthRepository
) : AdminRepository {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    private val usersRef = db.collection("users")
    private val logsRef = db.collection("logs")

    // Solo un administrador puede listar todas las cuentas y los logs
    override val usersFlow: StateFlow<List<UserAccount>> =
        auth.currentUserFlow.listenTo(
            scope,
            queryFor = { user -> if (user.role == UserRole.ADMINISTRADOR) usersRef else null }
        ) { it.toUserAccount() }

    override val logsFlow: StateFlow<List<SystemLog>> =
        auth.currentUserFlow.listenTo(
            scope,
            queryFor = { user ->
                if (user.role == UserRole.ADMINISTRADOR) {
                    logsRef.orderBy("timestamp", Query.Direction.DESCENDING).limit(MAX_LOGS)
                } else null
            }
        ) { it.toSystemLog() }

    override suspend fun updateUserRole(uid: String, role: UserRole): Result<Unit> = resultOf {
        usersRef.document(uid).update("role", role.id).await()
        Unit
    }

    override suspend fun updateUserStatus(uid: String, status: AccountStatus): Result<Unit> = resultOf {
        usersRef.document(uid).update("status", status.name).await()
        Unit
    }

    override suspend fun appendLog(level: LogLevel, source: String, message: String) {
        try {
            val log = SystemLog(
                timestamp = System.currentTimeMillis(),
                level = level,
                source = source,
                message = message
            )
            logsRef.add(log.toMap()).await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            // La auditoría nunca debe romper la acción que se está registrando
        }
    }
}