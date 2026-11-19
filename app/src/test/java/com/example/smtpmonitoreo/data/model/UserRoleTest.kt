package com.example.smtpmonitoreo.data.model

import org.junit.Assert.assertEquals
import org.junit.Test

class UserRoleTest {

    @Test
    fun fromString_reconoce_los_roles_sin_importar_mayusculas() {
        assertEquals(UserRole.CHOFER, UserRole.fromString("chofer"))
        assertEquals(UserRole.ADMINISTRADOR, UserRole.fromString("ADMINISTRADOR"))
        assertEquals(UserRole.ALCALDIA, UserRole.fromString("Alcaldia"))
    }

    @Test
    fun fromString_usa_pasajero_como_rol_de_menor_privilegio() {
        assertEquals(UserRole.PASAJERO, UserRole.fromString(null))
        assertEquals(UserRole.PASAJERO, UserRole.fromString("superusuario"))
    }
}