package com.example.smtpmonitoreo.domain

import com.example.smtpmonitoreo.data.model.TransportUnit
import org.junit.Assert.assertEquals
import org.junit.Test

class UnitLinkerTest {

    private val unit102 = TransportUnit(id = "u1", plate = "4521-FGH", lineNumber = "102")
    private val unit231 = TransportUnit(id = "u2", plate = "2184-KLM", lineNumber = "231")
    private val units = listOf(unit102, unit231)

    @Test
    fun vincula_una_placa_registrada() {
        assertEquals(UnitLinkResult.Linked(unit231), UnitLinker.resolve("2184-KLM", units))
    }

    @Test
    fun normaliza_la_placa_antes_de_buscar() {
        assertEquals(UnitLinkResult.Linked(unit102), UnitLinker.resolve(" 4521fgh ", units))
    }

    @Test
    fun informa_cuando_la_placa_no_esta_registrada() {
        assertEquals(UnitLinkResult.NotRegistered("9999-ZZZ"), UnitLinker.resolve("9999-ZZZ", units))
    }

    @Test
    fun rechaza_formatos_invalidos() {
        assertEquals(UnitLinkResult.InvalidPlate, UnitLinker.resolve("ABC", units))
        assertEquals(UnitLinkResult.InvalidPlate, UnitLinker.resolve("", units))
    }
}