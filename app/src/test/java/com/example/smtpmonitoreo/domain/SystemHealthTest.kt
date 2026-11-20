package com.example.smtpmonitoreo.domain

import com.example.smtpmonitoreo.data.model.AccountStatus
import com.example.smtpmonitoreo.data.model.IncidentReport
import com.example.smtpmonitoreo.data.model.IncidentStatus
import com.example.smtpmonitoreo.data.model.LogLevel
import com.example.smtpmonitoreo.data.model.SystemLog
import com.example.smtpmonitoreo.data.model.TransportUnit
import com.example.smtpmonitoreo.data.model.UserAccount
import com.example.smtpmonitoreo.data.model.UserRole
import org.junit.Assert.assertEquals
import org.junit.Test

class SystemHealthTest {

    private val users = listOf(
        UserAccount(uid = "1", role = UserRole.PASAJERO),
        UserAccount(uid = "2", role = UserRole.PASAJERO, status = AccountStatus.SUSPENDIDO),
        UserAccount(uid = "3", role = UserRole.ADMINISTRADOR)
    )
    private val units = listOf(
        TransportUnit(id = "a", isTransmitting = true),
        TransportUnit(id = "b", isTransmitting = true),
        TransportUnit(id = "c", isTransmitting = false)
    )
    private val incidents = listOf(
        IncidentReport(id = "i1", status = IncidentStatus.RECIBIDO),
        IncidentReport(id = "i2", status = IncidentStatus.EN_REVISION),
        IncidentReport(id = "i3", status = IncidentStatus.RESUELTO)
    )
    private val logs = listOf(
        SystemLog(id = "l1", level = LogLevel.ERROR),
        SystemLog(id = "l2", level = LogLevel.INFO),
        SystemLog(id = "l3", level = LogLevel.INFO)
    )

    private val health = HealthCalculator.compute(users, units, incidents, logs)

    @Test
    fun cuenta_usuarios_activos_suspendidos_y_por_rol() {
        assertEquals(3, health.totalUsers)
        assertEquals(2, health.activeUsers)
        assertEquals(1, health.suspendedUsers)
        assertEquals(
            listOf("Pasajero" to 2, "Chofer" to 0, "Alcaldía" to 0, "Administrador" to 1),
            health.usersByRole
        )
    }

    @Test
    fun cuenta_unidades_reportes_abiertos_y_errores() {
        assertEquals(2, health.unitsTransmitting)
        assertEquals(3, health.totalUnits)
        assertEquals(2, health.openIncidents)
        assertEquals(1, health.errorLogs)
    }
}