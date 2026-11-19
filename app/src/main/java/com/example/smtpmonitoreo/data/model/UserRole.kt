package com.example.smtpmonitoreo.data.model

import androidx.compose.ui.graphics.Color
import com.example.smtpmonitoreo.ui.theme.RoleAdminColor
import com.example.smtpmonitoreo.ui.theme.RoleAlcaldiaColor
import com.example.smtpmonitoreo.ui.theme.RoleChoferColor
import com.example.smtpmonitoreo.ui.theme.RolePasajeroColor

enum class UserRole(
    val id: String,
    val displayName: String,
    val subtitle: String,
    val description: String,
    val accentColor: Color
) {
    PASAJERO(
        id = "PASAJERO",
        displayName = "Pasajero",
        subtitle = "Usuario de Transporte",
        description = "Consulta rutas, unidades en tiempo real, ETA y reporta incidentes en La Paz.",
        accentColor = RolePasajeroColor
    ),
    CHOFER(
        id = "CHOFER",
        displayName = "Chofer",
        subtitle = "Operador de Unidad",
        description = "Transmite ubicación GPS en vivo, vincula su minibus y gestiona recorridos.",
        accentColor = RoleChoferColor
    ),
    ALCALDIA(
        id = "ALCALDIA",
        displayName = "Alcaldía",
        subtitle = "Supervisión & Control",
        description = "Gestiona rutas, monitorea unidades, fiscaliza y resuelve reportes e infracciones.",
        accentColor = RoleAlcaldiaColor
    ),
    ADMINISTRADOR(
        id = "ADMINISTRADOR",
        displayName = "Administrador",
        subtitle = "Gestión de Sistema",
        description = "Administra usuarios, asigna roles, supervisa permisos y monitorea logs.",
        accentColor = RoleAdminColor
    );

    companion object {
        fun fromString(value: String?): UserRole {
            return entries.find { it.name.equals(value, ignoreCase = true) || it.id.equals(value, ignoreCase = true) }
                ?: PASAJERO
        }
    }
}
