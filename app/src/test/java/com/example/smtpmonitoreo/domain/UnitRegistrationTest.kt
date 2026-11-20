package com.example.smtpmonitoreo.domain

import com.example.smtpmonitoreo.data.model.RouteModel
import com.example.smtpmonitoreo.data.model.TransportUnit
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class UnitRegistrationTest {

    private val routes = listOf(RouteModel(lineNumber = "102"))
    private val existing = listOf(TransportUnit(plate = "4521-FGH", lineNumber = "102"))

    private val valid = UnitDraft(
        plate = " 5555abc ",
        lineNumber = "102",
        syndicate = "Sindicato San Cristóbal",
        vehicleType = "Minibús",
        driverName = "Juan Pérez"
    )

    @Test
    fun registra_una_unidad_valida_normalizada_y_sin_transmitir() {
        val unit = (UnitRegistration.validate(valid, existing, routes) as Validation.Ok).value

        assertEquals("5555-ABC", unit.plate)
        assertEquals("102", unit.lineNumber)
        assertFalse(unit.isTransmitting)
    }

    @Test
    fun rechaza_una_placa_ya_registrada() {
        val result = UnitRegistration.validate(valid.copy(plate = "4521-FGH"), existing, routes)

        assertEquals(Validation.Error("La placa 4521-FGH ya está registrada."), result)
    }

    @Test
    fun rechaza_una_placa_con_formato_invalido() {
        val result = UnitRegistration.validate(valid.copy(plate = "ABC"), existing, routes)

        assertEquals(Validation.Error("Placa inválida. Usa el formato 1234-ABC."), result)
    }

    @Test
    fun rechaza_una_linea_sin_ruta_oficial() {
        val result = UnitRegistration.validate(valid.copy(lineNumber = "999"), existing, routes)

        assertEquals(Validation.Error("Selecciona una línea con ruta oficial."), result)
    }

    @Test
    fun exige_sindicato_y_chofer() {
        assertTrue(UnitRegistration.validate(valid.copy(syndicate = " "), existing, routes) is Validation.Error)
        assertTrue(UnitRegistration.validate(valid.copy(driverName = ""), existing, routes) is Validation.Error)
    }
}