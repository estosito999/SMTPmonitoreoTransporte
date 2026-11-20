package com.example.smtpmonitoreo.data.model

enum class UserRole(
    val id: String,
    val displayName: String,
    val subtitle: String,
    val description: String
) {
    PASAJERO(
        id = "PASAJERO",
        displayName = "Pasajero",
        subtitle = "Usuario de Transporte",
        description = "Consulta rutas, unidades en tiempo real, ETA y reporta incidentes en La Paz."
    ),
    CHOFER(
        id = "CHOFER",
        displayName = "Chofer",
        subtitle = "Operador de Unidad",
        description = "Transmite ubicación GPS en vivo, vincula su minibus y gestiona recorridos."
    ),
    ALCALDIA(
        id = "ALCALDIA",
        displayName = "Alcaldía",
        subtitle = "Supervisión & Control",
        description = "Gestiona rutas, monitorea unidades, fiscaliza y resuelve reportes e infracciones."
    ),
    ADMINISTRADOR(
        id = "ADMINISTRADOR",
        displayName = "Administrador",
        subtitle = "Gestión de Sistema",
        description = "Administra usuarios, asigna roles, supervisa permisos y monitorea logs."
    );
    /** Roles con permisos de supervisión (leen todos los reportes e infracciones). */
    val isStaff: Boolean get() = this == ALCALDIA || this == ADMINISTRADOR

    companion object {
        fun fromString(value: String?): UserRole {
            return entries.find { it.name.equals(value, ignoreCase = true) || it.id.equals(value, ignoreCase = true) }
                ?: PASAJERO
        }
    }
}