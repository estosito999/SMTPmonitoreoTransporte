package com.example.smtpmonitoreo.data.model

data class IncidentReport(
    val id: String = "",
    val userEmail: String = "",
    val vehiclePlate: String = "",
    val lineNumber: String = "",
    val category: String = "Cobro irregular",
    val description: String = "",
    val status: IncidentStatus = IncidentStatus.RECIBIDO,
    val timestamp: Long = System.currentTimeMillis(),
    val resolutionNote: String = ""
)