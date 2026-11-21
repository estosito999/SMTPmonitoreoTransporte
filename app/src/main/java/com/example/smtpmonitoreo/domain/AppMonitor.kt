package com.example.smtpmonitoreo.domain

import com.example.smtpmonitoreo.data.model.LogLevel
import com.example.smtpmonitoreo.data.model.UserAccount

interface AppMonitor {
    /**
     * Evento operativo: va a Crashlytics (como rastro) y a la colección logs que ve el administrador.
     * Con [dedupeKey] se registra una sola vez por ejecución de la app (evita llenar el registro).
     */
    fun log(level: LogLevel, source: String, message: String, dedupeKey: String? = null)

    /** Error no fatal: va a Crashlytics. */
    fun recordError(error: Throwable, context: String)

    /** Métrica de uso (sin datos personales). */
    fun trackEvent(name: String, params: Map<String, String> = emptyMap())

    /** Asocia los reportes al usuario (su UID, nunca el correo) y a su rol. */
    fun onSessionChanged(user: UserAccount?)
}