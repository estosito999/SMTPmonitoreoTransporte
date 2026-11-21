package com.example.smtpmonitoreo.domain

import org.junit.Assert.assertEquals
import org.junit.Test

class ConnectionTrackerTest {

    @Test
    fun sin_internet_el_estado_es_offline() {
        assertEquals(ConnectionState.OFFLINE, ConnectionTracker.next(ConnectionState.ONLINE, online = false))
        assertEquals(ConnectionState.OFFLINE, ConnectionTracker.next(ConnectionState.RESTORED, online = false))
    }

    @Test
    fun al_volver_el_internet_pasa_por_restored() {
        assertEquals(ConnectionState.RESTORED, ConnectionTracker.next(ConnectionState.OFFLINE, online = true))
    }

    @Test
    fun con_internet_estable_no_cambia() {
        assertEquals(ConnectionState.ONLINE, ConnectionTracker.next(ConnectionState.ONLINE, online = true))
        assertEquals(ConnectionState.RESTORED, ConnectionTracker.next(ConnectionState.RESTORED, online = true))
    }
}