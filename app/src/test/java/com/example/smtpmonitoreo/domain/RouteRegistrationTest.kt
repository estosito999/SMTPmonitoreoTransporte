package com.example.smtpmonitoreo.domain

import com.example.smtpmonitoreo.data.model.RouteModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class RouteRegistrationTest {

    private val existing = listOf(RouteModel(id = "r102", lineNumber = "102"))

    private val valid = RouteDraft(
        lineNumber = "300",
        name = "Línea 300: Ceja - Calacoto",
        origin = "Ceja El Alto",
        destination = "Calacoto",
        tariff = "2,50",
        stops = "Pérez Velasco, San Pedro, pérez velasco"
    )

    @Test
    fun acepta_una_ruta_valida_con_tarifa_con_coma_y_paradas_sin_repetir() {
        val route = (RouteRegistration.validate(valid, existing) as Validation.Ok).value

        assertEquals("300", route.lineNumber)
        assertEquals(2.5, route.tariffBs, 0.0)
        assertEquals(listOf("Pérez Velasco", "San Pedro"), route.stops)
    }

    @Test
    fun rechaza_una_linea_repetida() {
        val result = RouteRegistration.validate(valid.copy(lineNumber = "102"), existing)

        assertEquals(Validation.Error("La línea 102 ya tiene una ruta oficial."), result)
    }

    @Test
    fun rechaza_origen_igual_al_destino() {
        val result = RouteRegistration.validate(valid.copy(destination = "ceja el alto"), existing)

        assertEquals(Validation.Error("El origen y el destino deben ser distintos."), result)
    }

    @Test
    fun rechaza_tarifas_invalidas() {
        assertTrue(RouteRegistration.validate(valid.copy(tariff = "abc"), existing) is Validation.Error)
        assertTrue(RouteRegistration.validate(valid.copy(tariff = "0"), existing) is Validation.Error)
        assertTrue(RouteRegistration.validate(valid.copy(tariff = "75"), existing) is Validation.Error)
    }

    @Test
    fun rechaza_campos_vacios() {
        assertTrue(RouteRegistration.validate(valid.copy(lineNumber = " "), existing) is Validation.Error)
        assertTrue(RouteRegistration.validate(valid.copy(name = ""), existing) is Validation.Error)
        assertTrue(RouteRegistration.validate(valid.copy(origin = ""), existing) is Validation.Error)
    }
}