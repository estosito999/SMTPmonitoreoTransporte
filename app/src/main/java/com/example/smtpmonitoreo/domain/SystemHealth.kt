package com.example.smtpmonitoreo.domain

import com.example.smtpmonitoreo.data.model.AccountStatus
import com.example.smtpmonitoreo.data.model.IncidentReport
import com.example.smtpmonitoreo.data.model.IncidentStatus
import com.example.smtpmonitoreo.data.model.LogLevel
import com.example.smtpmonitoreo.data.model.SystemLog
import com.example.smtpmonitoreo.data.model.TransportUnit
import com.example.smtpmonitoreo.data.model.UserAccount
import com.example.smtpmonitoreo.data.model.UserRole

data class SystemHealth(
    val totalUsers: Int = 0,
    val activeUsers: Int = 0,
    val suspendedUsers: Int = 0,
    val usersByRole: List<Pair<String, Int>> = emptyList(),
    val unitsTransmitting: Int = 0,
    val totalUnits: Int = 0,
    val openIncidents: Int = 0,
    val errorLogs: Int = 0
)

object HealthCalculator {

    fun compute(
        users: List<UserAccount>,
        units: List<TransportUnit>,
        incidents: List<IncidentReport>,
        logs: List<SystemLog>
    ): SystemHealth = SystemHealth(
        totalUsers = users.size,
        activeUsers = users.count { it.status == AccountStatus.ACTIVO },
        suspendedUsers = users.count { it.status == AccountStatus.SUSPENDIDO },
        usersByRole = UserRole.entries.map { role -> role.displayName to users.count { it.role == role } },
        unitsTransmitting = units.count { it.isTransmitting },
        totalUnits = units.size,
        openIncidents = incidents.count { it.status != IncidentStatus.RESUELTO },
        errorLogs = logs.count { it.level == LogLevel.ERROR }
    )
}