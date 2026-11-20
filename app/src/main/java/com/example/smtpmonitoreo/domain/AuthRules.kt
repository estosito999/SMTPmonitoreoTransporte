package com.example.smtpmonitoreo.domain

object AuthRules {
    const val MIN_PASSWORD_LENGTH = 6
    private val emailPattern = Regex("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")

    /** Devuelve el mensaje de error, o null si los datos son válidos. */
    fun validateLogin(email: String, password: String): String? = when {
        email.isBlank() -> "Ingresa tu correo electrónico."
        !emailPattern.matches(email.trim()) -> "Correo electrónico inválido."
        password.isEmpty() -> "Ingresa tu contraseña."
        else -> null
    }

    fun validateRegistration(fullName: String, email: String, password: String): String? = when {
        fullName.isBlank() -> "Ingresa tu nombre completo."
        email.isBlank() -> "Ingresa tu correo electrónico."
        !emailPattern.matches(email.trim()) -> "Correo electrónico inválido."
        password.length < MIN_PASSWORD_LENGTH ->
            "La contraseña debe tener al menos $MIN_PASSWORD_LENGTH caracteres."
        else -> null
    }
}