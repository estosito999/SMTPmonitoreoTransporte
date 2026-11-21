package com.example.smtpmonitoreo.domain

enum class ConnectionState {
    ONLINE,

    /** Sin internet: la app trabaja con datos locales. */
    OFFLINE,

    /** Internet recién recuperado: se muestra unos segundos mientras se sincroniza. */
    RESTORED
}

object ConnectionTracker {

    fun next(previous: ConnectionState, online: Boolean): ConnectionState = when {
        !online -> ConnectionState.OFFLINE
        previous == ConnectionState.OFFLINE -> ConnectionState.RESTORED
        else -> previous // ONLINE sigue ONLINE; RESTORED lo apaga el temporizador
    }
}