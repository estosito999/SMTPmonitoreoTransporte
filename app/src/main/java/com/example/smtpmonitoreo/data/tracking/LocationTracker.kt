package com.example.smtpmonitoreo.data.tracking

import kotlinx.coroutines.flow.StateFlow

interface LocationTracker {
    /** true mientras el servicio de rastreo está activo. */
    val isRunning: StateFlow<Boolean>

    /** Último error del servicio (GPS apagado, sin permiso…), o null. */
    val error: StateFlow<String?>

    /** Inicia el rastreo, o lo reinicia con el nuevo intervalo si ya estaba activo. */
    fun start(plate: String, intervalSeconds: Int)

    fun stop()
}