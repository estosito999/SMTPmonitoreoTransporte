package com.example.smtpmonitoreo.data.model

enum class IncidentStatus(val label: String) {
    RECIBIDO("Recibido"),
    EN_REVISION("En revisión"),
    RESUELTO("Resuelto")
}

enum class InfractionStatus(val label: String) {
    EMITIDA("Emitida"),
    PAGADA("Pagada"),
    APELADA("Apelada")
}