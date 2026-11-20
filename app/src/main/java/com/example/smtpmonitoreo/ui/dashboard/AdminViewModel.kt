package com.example.smtpmonitoreo.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smtpmonitoreo.data.demo.DemoSeeder
import com.example.smtpmonitoreo.data.model.AccountStatus
import com.example.smtpmonitoreo.data.model.IncidentReport
import com.example.smtpmonitoreo.data.model.LogLevel
import com.example.smtpmonitoreo.data.model.SystemLog
import com.example.smtpmonitoreo.data.model.TransportUnit
import com.example.smtpmonitoreo.data.model.UserAccount
import com.example.smtpmonitoreo.data.model.UserRole
import com.example.smtpmonitoreo.data.repository.AdminRepository
import com.example.smtpmonitoreo.data.repository.AuthRepository
import com.example.smtpmonitoreo.data.repository.MonitoringRepository
import com.example.smtpmonitoreo.domain.HealthCalculator
import com.example.smtpmonitoreo.domain.SystemHealth
import com.example.smtpmonitoreo.domain.UserAdminRules
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

private const val MAX_VISIBLE_LOGS = 50

enum class AdminTab { CUENTAS, SISTEMA }

data class AdminUiState(
    val tab: AdminTab = AdminTab.CUENTAS,
    val notice: Notice? = null,

    // Datos en vivo
    val users: List<UserAccount> = emptyList(),
    val logs: List<SystemLog> = emptyList(),
    val health: SystemHealth = SystemHealth(),
    val currentEmail: String? = null,

    // Cuentas (RF-50, RF-51)
    val search: String = "",

    // Sistema y logs
    val logFilter: LogLevel? = null
) {
    val visibleUsers: List<UserAccount>
        get() = users
            .filter {
                search.isBlank() ||
                        it.fullName.contains(search, ignoreCase = true) ||
                        it.email.contains(search, ignoreCase = true)
            }
            .sortedBy { it.fullName.lowercase() }

    val visibleLogs: List<SystemLog>
        get() = logs.filter { logFilter == null || it.level == logFilter }.take(MAX_VISIBLE_LOGS)
}

private data class AdminSnapshot(
    val users: List<UserAccount>,
    val logs: List<SystemLog>,
    val units: List<TransportUnit>,
    val incidents: List<IncidentReport>,
    val currentEmail: String?
)

class AdminViewModel(
    private val admin: AdminRepository,
    private val monitoring: MonitoringRepository,
    private val auth: AuthRepository,
    private val seeder: DemoSeeder
) : ViewModel() {

    private val _state = MutableStateFlow(AdminUiState())
    val uiState: StateFlow<AdminUiState> = _state.asStateFlow()

    init {
        combine(
            admin.usersFlow,
            admin.logsFlow,
            monitoring.activeUnitsFlow,
            monitoring.incidentsFlow,
            auth.currentUserFlow
        ) { users, logs, units, incidents, me ->
            AdminSnapshot(users, logs, units, incidents, me?.email)
        }.onEach { snap ->
            _state.update {
                it.copy(
                    users = snap.users,
                    logs = snap.logs,
                    currentEmail = snap.currentEmail,
                    health = HealthCalculator.compute(snap.users, snap.units, snap.incidents, snap.logs)
                )
            }
        }.launchIn(viewModelScope)
    }

    // ---------- Navegación ----------

    fun onTabSelected(tab: AdminTab) =
        _state.update { it.copy(tab = tab, notice = null) }

    // ---------- Cuentas ----------

    fun onSearchChanged(query: String) =
        _state.update { it.copy(search = query) }

    fun changeRole(target: UserAccount, newRole: UserRole) {
        if (target.role == newRole) return
        val s = _state.value

        val error = UserAdminRules.roleChangeError(s.users, target, newRole, s.currentEmail)
        if (error != null) {
            showNotice(error, isError = true)
            return
        }

        viewModelScope.launch {
            admin.updateUserRole(target.uid, newRole)
                .onSuccess {
                    audit("cambió el rol de ${target.email}: ${target.role.displayName} → ${newRole.displayName}")
                    showNotice("Rol de ${target.fullName} actualizado a ${newRole.displayName}.")
                }
                .onFailure { e ->
                    showNotice(e.localizedMessage ?: "No se pudo cambiar el rol.", isError = true)
                }
        }
    }

    fun toggleStatus(target: UserAccount) {
        val s = _state.value
        val newStatus = if (target.status == AccountStatus.ACTIVO) AccountStatus.SUSPENDIDO else AccountStatus.ACTIVO

        val error = UserAdminRules.suspensionError(s.users, target, s.currentEmail)
        if (error != null) {
            showNotice(error, isError = true)
            return
        }

        viewModelScope.launch {
            admin.updateUserStatus(target.uid, newStatus)
                .onSuccess {
                    audit("${if (newStatus == AccountStatus.SUSPENDIDO) "suspendió" else "reactivó"} la cuenta ${target.email}")
                    showNotice("Cuenta de ${target.fullName}: ${newStatus.label.lowercase()}.")
                }
                .onFailure { e ->
                    showNotice(e.localizedMessage ?: "No se pudo actualizar la cuenta.", isError = true)
                }
        }
    }

    // ---------- Sistema y logs ----------

    fun onLogFilterSelected(level: LogLevel?) =
        _state.update { it.copy(logFilter = level) }

    /** Solo debug: carga rutas, unidades, reportes e infracciones de ejemplo en Firestore. */
    fun seedDemoData() {
        viewModelScope.launch {
            seeder.seed()
                .onSuccess {
                    audit("cargó los datos de ejemplo")
                    showNotice("Datos de ejemplo cargados en Firestore.")
                }
                .onFailure { e ->
                    showNotice(e.localizedMessage ?: "No se pudieron cargar los datos de ejemplo.", isError = true)
                }
        }
    }

    // ---------- Utilidades ----------

    private suspend fun audit(message: String) {
        val actor = _state.value.currentEmail ?: "admin"
        admin.appendLog(LogLevel.INFO, "ADMIN", "$actor $message")
    }

    private fun showNotice(text: String, isError: Boolean = false) =
        _state.update { it.copy(notice = Notice(text, isError)) }
}