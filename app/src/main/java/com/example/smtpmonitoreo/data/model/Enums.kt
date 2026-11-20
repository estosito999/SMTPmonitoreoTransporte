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
enum class IncidentCategory(val label: String) {
    COBRO_IRREGULAR("Cobro irregular"),
    DESVIO_RUTA("Desvío de ruta"),
    MAL_SERVICIO("Mal servicio"),
    MALTRATO("Maltrato"),
    INSEGURIDAD("Inseguridad"),
    EMERGENCIA("Emergencia")
}