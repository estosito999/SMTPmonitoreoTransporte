package com.example.smtpmonitoreo.domain

import com.example.smtpmonitoreo.data.model.IncidentReport
import com.example.smtpmonitoreo.data.model.IncidentStatus
import com.example.smtpmonitoreo.data.model.TransportUnit
import java.util.Calendar
import java.util.TimeZone

data class AlcaldiaStats(
    val activeUnits: Int = 0,
    val incidentsToday: Int = 0,
    val openIncidents: Int = 0,
    val incidentsByCategory: List<Pair<String, Int>> = emptyList(),
    /** Líneas con más incidencias (zonas críticas por línea). */
    val incidentsByLine: List<Pair<String, Int>> = emptyList(),
    /** Frecuencia: unidades transmitiendo por línea. */
    val activeUnitsByLine: List<Pair<String, Int>> = emptyList()
)

object StatsCalculator {

    fun compute(
        units: List<TransportUnit>,
        incidents: List<IncidentReport>,
        now: Long,
        zone: TimeZone = TimeZone.getDefault()
    ): AlcaldiaStats {
        val startOfDay = Calendar.getInstance(zone).apply {
            timeInMillis = now
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

        val active = units.filter { it.isTransmitting }

        return AlcaldiaStats(
            activeUnits = active.size,
            incidentsToday = incidents.count { it.timestamp >= startOfDay },
            openIncidents = incidents.count { it.status != IncidentStatus.RESUELTO },
            incidentsByCategory = incidents
                .groupingBy { it.category.label }.eachCount()
                .toList().sortedByDescending { it.second },
            incidentsByLine = incidents
                .filter { it.lineNumber.isNotBlank() }
                .groupingBy { it.lineNumber }.eachCount()
                .toList().sortedByDescending { it.second }.take(5),
            activeUnitsByLine = active
                .filter { it.lineNumber.isNotBlank() }
                .groupingBy { it.lineNumber }.eachCount()
                .toList().sortedByDescending { it.second }
        )
    }
}