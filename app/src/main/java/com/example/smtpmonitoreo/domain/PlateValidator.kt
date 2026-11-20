package com.example.smtpmonitoreo.domain

object PlateValidator {
    private val pattern = Regex("^(\\d{3,4})-?([A-Z]{3})$")

    /** Devuelve la placa en formato 1234-ABC, o null si no es válida. */
    fun normalize(raw: String): String? {
        val match = pattern.matchEntire(raw.trim().uppercase().replace(" ", "")) ?: return null
        return "${match.groupValues[1]}-${match.groupValues[2]}"
    }
}