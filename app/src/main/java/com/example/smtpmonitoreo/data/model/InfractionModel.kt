package com.example.smtpmonitoreo.data.model

data class InfractionModel(
    val id: String = "",
    val vehiclePlate: String = "",
    val lineNumber: String = "",
    val reason: String = "",
    val fineAmountBs: Double = 100.0,
    val timestamp: Long = System.currentTimeMillis(),
    val status: InfractionStatus = InfractionStatus.EMITIDA
)