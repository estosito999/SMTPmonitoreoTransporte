package com.example.smtpmonitoreo.ui.theme

import androidx.compose.ui.graphics.Color
import com.example.smtpmonitoreo.data.model.UserRole

val UserRole.accentColor: Color
    get() = when (this) {
        UserRole.PASAJERO -> RolePasajeroColor
        UserRole.CHOFER -> RoleChoferColor
        UserRole.ALCALDIA -> RoleAlcaldiaColor
        UserRole.ADMINISTRADOR -> RoleAdminColor
    }