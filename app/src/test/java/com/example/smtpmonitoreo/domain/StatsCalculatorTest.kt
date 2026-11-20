package com.example.smtpmonitoreo.domain

import com.example.smtpmonitoreo.data.model.IncidentCategory
import com.example.smtpmonitoreo.data.model.IncidentReport
import com.example.smtpmonitoreo.data.model.IncidentStatus
import com.example.smtpmonitoreo.data.model.TransportUnit
import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.TimeZone

class StatsCalculatorTest {

    private val utc: TimeZone = TimeZone.getTimeZone("UTC")
    private val now = 1_700_000_000_000L // 14/11/2023 22:13 UTC
    private val oneDay = 86_400_000L

    private val units = listOf(
        TransportUnit(id = "u1", plate = "1111-AAA", lineNumber = "102", isTransmitting = true),
        TransportUnit(id = "u2", plate = "2222-BBB", lineNumber = "102", isTransmitting = true),
        TransportUnit(id = "u3", plate = "3333-CCC", lineNumber = "231", isTransmitting = false)
    )

    private val incidents = listOf(
        IncidentReport(
            id = "i1", lineNumber = "102", category = IncidentCategory.COBRO_IRREGULAR,
            status = IncidentStatus.RECIBIDO, timestamp = now - 1_000L
        ),
        IncidentReport(
            id = "i2", lineNumber = "102", category = IncidentCategory.COBRO_IRREGULAR,
            status = IncidentStatus.EN_REVISION, timestamp = now - 2_000L
        ),
        IncidentReport(
            id = "i3", lineNumber = "231", category = IncidentCategory.DESVIO_RUTA,
            status = IncidentStatus.RESUELTO, timestamp = now - oneDay
        )
    )

    private val stats = StatsCalculator.compute(units, incidents, now, utc)

    @Test
    fun cuenta_solo_las_unidades_que_transmiten() {
        assertEquals(2, stats.activeUnits)
        assertEquals(listOf("102" to 2), stats.activeUnitsByLine)
    }

    @Test
    fun cuenta_los_reportes_de_hoy_y_los_abiertos() {
        assertEquals(2, stats.incidentsToday)
        assertEquals(2, stats.openIncidents)
    }

    @Test
    fun agrupa_incidencias_por_categoria_y_por_linea_de_mayor_a_menor() {
        assertEquals(listOf("Cobro irregular" to 2, "Desvío de ruta" to 1), stats.incidentsByCategory)
        assertEquals(listOf("102" to 2, "231" to 1), stats.incidentsByLine)
    }
}