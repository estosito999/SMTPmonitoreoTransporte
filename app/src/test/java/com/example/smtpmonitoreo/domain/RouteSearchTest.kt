package com.example.smtpmonitoreo.domain

import com.example.smtpmonitoreo.data.model.RouteModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class RouteSearchTest {

    private val ruta102 = RouteModel(
        id = "r102", lineNumber = "102",
        origin = "Ceja El Alto", destination = "Sopocachi",
        stops = listOf("Ceja El Alto", "Pérez Velasco", "El Obelisco", "Plaza Estudiante", "San Pedro")
    )
    private val ruta231 = RouteModel(
        id = "r231", lineNumber = "231",
        origin = "Periférica", destination = "Miraflores",
        stops = listOf("Periférica", "Pérez Velasco", "Plaza Murillo", "Hospital del Niño", "Miraflores")
    )
    private val expreso = RouteModel(
        id = "rexp", lineNumber = "Exp",
        origin = "Pérez Velasco", destination = "Miraflores",
        stops = listOf("Pérez Velasco", "Miraflores")
    )

    @Test
    fun encuentra_la_ruta_en_el_sentido_del_recorrido() {
        val result = RouteSearch.find(listOf(ruta102, ruta231), "Ceja El Alto", "San Pedro")

        assertEquals("102", result?.route?.lineNumber)
        assertEquals(
            listOf("Ceja El Alto", "Pérez Velasco", "El Obelisco", "Plaza Estudiante", "San Pedro"),
            result?.stopsInOrder
        )
    }

    @Test
    fun encuentra_la_ruta_en_sentido_inverso() {
        val result = RouteSearch.find(listOf(ruta102), "Pérez Velasco", "Ceja El Alto")

        assertEquals(listOf("Pérez Velasco", "Ceja El Alto"), result?.stopsInOrder)
    }

    @Test
    fun usa_el_destino_oficial_aunque_no_este_en_las_paradas() {
        val result = RouteSearch.find(listOf(ruta102, ruta231), "San Pedro", "Sopocachi")

        assertEquals("102", result?.route?.lineNumber)
    }

    @Test
    fun devuelve_null_si_no_hay_ruta_directa() {
        assertNull(RouteSearch.find(listOf(ruta102, ruta231), "Plaza Murillo", "San Pedro"))
    }

    @Test
    fun elige_la_ruta_con_menos_paradas() {
        val result = RouteSearch.find(listOf(ruta231, expreso), "Pérez Velasco", "Miraflores")

        assertEquals("Exp", result?.route?.lineNumber)
    }

    @Test
    fun devuelve_null_con_origen_o_destino_vacios() {
        assertNull(RouteSearch.find(listOf(ruta102), "", "San Pedro"))
        assertNull(RouteSearch.find(listOf(ruta102), "Ceja El Alto", "  "))
    }
}