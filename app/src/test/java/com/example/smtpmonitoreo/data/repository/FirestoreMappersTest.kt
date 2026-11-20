package com.example.smtpmonitoreo.data.repository

import com.example.smtpmonitoreo.data.model.IncidentCategory
import com.example.smtpmonitoreo.data.model.IncidentReport
import com.example.smtpmonitoreo.data.model.IncidentStatus
import com.example.smtpmonitoreo.data.model.RouteModel
import com.example.smtpmonitoreo.data.model.TransportUnit
import org.junit.Assert.assertEquals
import org.junit.Test

class FirestoreMappersTest {

    @Test
    fun la_ruta_guarda_los_waypoints_como_lista_de_mapas() {
        val route = RouteModel(
            lineNumber = "102",
            waypoints = listOf(-16.5 to -68.1),
            stops = listOf("A", "B")
        )

        val map = route.toMap()

        assertEquals(listOf(mapOf("lat" to -16.5, "lng" to -68.1)), map["waypoints"])
        assertEquals(listOf("A", "B"), map["stops"])
    }

    @Test
    fun la_unidad_guarda_rumbo_y_velocidad_como_double() {
        val map = TransportUnit(plate = "4521-FGH", heading = 90f, speedKmh = 28f).toMap()

        assertEquals("4521-FGH", map["plate"])
        assertEquals(90.0, map["heading"])
        assertEquals(28.0, map["speedKmh"])
    }

    @Test
    fun el_incidente_guarda_los_enums_por_nombre() {
        val map = IncidentReport(
            category = IncidentCategory.EMERGENCIA,
            status = IncidentStatus.EN_REVISION
        ).toMap()

        assertEquals("EMERGENCIA", map["category"])
        assertEquals("EN_REVISION", map["status"])
    }
}