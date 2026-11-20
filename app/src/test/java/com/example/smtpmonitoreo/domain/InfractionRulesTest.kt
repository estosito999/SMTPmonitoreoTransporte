package com.example.smtpmonitoreo.domain

import com.example.smtpmonitoreo.data.model.InfractionStatus
import com.example.smtpmonitoreo.data.model.TransportUnit
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class InfractionRulesTest {

    private val units = listOf(TransportUnit(plate = "4521-FGH", lineNumber = "102"))

    @Test
    fun emite_la_infraccion_con_la_linea_de_la_unidad_y_la_fecha_indicada() {
        val draft = InfractionDraft(plate = "4521fgh", amount = "150,50")

        val infraction = (InfractionRules.validate(draft, units, now = 1_000L) as Validation.Ok).value

        assertEquals("4521-FGH", infraction.vehiclePlate)
        assertEquals("102", infraction.lineNumber)
        assertEquals(150.5, infraction.fineAmountBs, 0.0)
        assertEquals(1_000L, infraction.timestamp)
        assertEquals(InfractionStatus.EMITIDA, infraction.status)
    }

    @Test
    fun rechaza_una_placa_no_registrada() {
        val result = InfractionRules.validate(InfractionDraft(plate = "9999-ZZZ"), units, now = 0L)

        assertEquals(Validation.Error("La placa 9999-ZZZ no está registrada."), result)
    }

    @Test
    fun rechaza_montos_invalidos() {
        val base = InfractionDraft(plate = "4521-FGH")

        assertTrue(InfractionRules.validate(base.copy(amount = "x"), units, 0L) is Validation.Error)
        assertTrue(InfractionRules.validate(base.copy(amount = "0"), units, 0L) is Validation.Error)
        assertTrue(InfractionRules.validate(base.copy(amount = "6000"), units, 0L) is Validation.Error)
    }
}