package com.example.smtpmonitoreo.domain

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object DateFormats {
    private val locale: Locale = Locale.forLanguageTag("es-BO")

    fun date(millis: Long): String = SimpleDateFormat("dd/MM/yyyy", locale).format(Date(millis))

    fun dateTime(millis: Long): String = SimpleDateFormat("dd/MM/yyyy HH:mm", locale).format(Date(millis))
}