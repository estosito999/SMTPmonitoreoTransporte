package com.example.smtpmonitoreo.domain

import com.example.smtpmonitoreo.data.model.UserRole

/** Correos de las cuentas de prueba (sin contraseñas: nunca se guardan secretos en el código). */
object DemoAccounts {
    fun emailFor(role: UserRole): String = when (role) {
        UserRole.PASAJERO -> "pasajero.demo@smtp.lapaz.bo"
        UserRole.CHOFER -> "chofer.minibus102@smtp.lapaz.bo"
        UserRole.ALCALDIA -> "alcaldia.control@smtp.lapaz.bo"
        UserRole.ADMINISTRADOR -> "admin.master@smtp.lapaz.bo"
    }
}