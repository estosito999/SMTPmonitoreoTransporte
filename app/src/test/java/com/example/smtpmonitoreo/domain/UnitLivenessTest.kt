package com.example.smtpmonitoreo.domain

import com.example.smtpmonitoreo.data.model.TransportUnit
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class UnitLivenessTest {

    private val now = 1_000_000L

    private fun unit(transmitting: Boolean, lastUpdated: Long) =
        TransportUnit(plate = "4521-FGH", isTransmitting = transmitting, lastUpdated = lastUpdated)

    @Test
    fun una_unidad_que_transmite_y_envio_posicion_reciente_esta_viva() {
        assertTrue(UnitLiveness.isLive(unit(true, now - 10_000L), now))
    }

    @Test
    fun una_unidad_sin_senal_por_mas_de_la_ventana_se_considera_inactiva() {
        val stale = unit(true, now - UnitLiveness.LIVE_WINDOW_MS - 1)

        assertFalse(UnitLiveness.isLive(stale, now))
        assertFalse(UnitLiveness.withLiveness(stale, now).isTransmitting)
    }

    @Test
    fun una_unidad_que_no_transmite_nunca_esta_viva() {
        assertFalse(UnitLiveness.isLive(unit(false, now), now))
    }
}