package com.example.smtpmonitoreo.domain

import kotlinx.coroutines.flow.Flow

interface NetworkMonitor {
    /** Emite true/false cada vez que cambia el acceso real a internet. */
    val isOnline: Flow<Boolean>

    fun isOnlineNow(): Boolean
}