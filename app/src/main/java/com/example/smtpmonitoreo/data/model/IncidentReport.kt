package com.example.smtpmonitoreo.data.model

data class IncidentReport(
    val id: String = "",
    val userEmail: String = "",
    val vehiclePlate: String = "",
    val lineNumber: String = "",
    val category: String = "Cobro irregular",
    val description: String = "",
    val status: String = "RECIBIDO", // RECIBIDO, EN_REVISION, RESUELTO
    val timestamp: Long = System.currentTimeMillis(),
    val resolutionNote: String = ""
)
