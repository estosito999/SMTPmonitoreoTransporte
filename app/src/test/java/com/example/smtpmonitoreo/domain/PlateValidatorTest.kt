package com.example.smtpmonitoreo.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class PlateValidatorTest {

    @Test
    fun acepta_una_placa_con_guion() {
        assertEquals("4521-FGH", PlateValidator.normalize("4521-FGH"))
    }

    @Test
    fun normaliza_minusculas_espacios_y_agrega_el_guion() {
        assertEquals("4521-FGH", PlateValidator.normalize(" 4521fgh "))
    }

    @Test
    fun rechaza_placas_invalidas() {
        assertNull(PlateValidator.normalize(""))
        assertNull(PlateValidator.normalize("ABC"))
        assertNull(PlateValidator.normalize("45-FGH"))
        assertNull(PlateValidator.normalize("4521-FG"))
    }
}