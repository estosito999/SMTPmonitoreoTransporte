package com.example.smtpmonitoreo.domain

sealed interface Validation<out T> {
    data class Ok<T>(val value: T) : Validation<T>
    data class Error(val message: String) : Validation<Nothing>
}

/** Acepta coma o punto decimal ("2,50" o "2.50"). */
fun parseDecimal(input: String): Double? =
    input.trim().replace(',', '.').toDoubleOrNull()