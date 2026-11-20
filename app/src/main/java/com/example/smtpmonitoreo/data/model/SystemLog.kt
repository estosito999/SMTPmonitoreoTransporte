package com.example.smtpmonitoreo.data.model

data class SystemLog(
    val id: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val level: LogLevel = LogLevel.INFO,
    val source: String = "",
    val message: String = ""
)