package com.example.smtpmonitoreo.data.monitoring

import com.example.smtpmonitoreo.data.repository.AccountSuspendedException
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.firestore.FirebaseFirestoreException

object MonitoringRules {
    /** Debe coincidir con el límite de las reglas de Firestore para la colección logs. */
    const val MAX_LOG_MESSAGE = 300

    fun truncate(message: String): String = message.take(MAX_LOG_MESSAGE)

    /** Los errores esperados (sin red, cuenta suspendida) no se envían a Crashlytics. */
    fun shouldReport(error: Throwable): Boolean = when (error) {
        is AccountSuspendedException, is FirebaseNetworkException -> false
        is FirebaseFirestoreException -> error.code != FirebaseFirestoreException.Code.UNAVAILABLE
        else -> true
    }
}