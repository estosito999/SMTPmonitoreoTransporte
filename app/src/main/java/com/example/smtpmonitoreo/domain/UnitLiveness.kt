package com.example.smtpmonitoreo.domain

import com.example.smtpmonitoreo.data.model.TransportUnit

object UnitLiveness {
    /** Ventana máxima sin recibir posición antes de considerar inactiva a una unidad. */
    const val LIVE_WINDOW_MS = 120_000L

    fun isLive(unit: TransportUnit, now: Long): Boolean =
        unit.isTransmitting && now - unit.lastUpdated <= LIVE_WINDOW_MS

    fun withLiveness(unit: TransportUnit, now: Long): TransportUnit =
        unit.copy(isTransmitting = isLive(unit, now))
}