package com.example.smtpmonitoreo.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class AuthRulesTest {

    @Test
    fun login_valido_no_tiene_error() {
        assertNull(AuthRules.validateLogin(" ana@smtp.bo ", "secreto"))
    }

    @Test
    fun login_exige_correo_valido_y_contrasena() {
        assertEquals("Ingresa tu correo electrónico.", AuthRules.validateLogin(" ", "x"))
        assertEquals("Correo electrónico inválido.", AuthRules.validateLogin("ana@", "x"))
        assertEquals("Ingresa tu contraseña.", AuthRules.validateLogin("ana@smtp.bo", ""))
    }

    @Test
    fun registro_valido_no_tiene_error() {
        assertNull(AuthRules.validateRegistration("Ana Mamani", "ana@smtp.bo", "123456"))
    }

    @Test
    fun registro_exige_nombre_correo_y_contrasena_de_6_caracteres() {
        assertEquals("Ingresa tu nombre completo.", AuthRules.validateRegistration(" ", "ana@smtp.bo", "123456"))
        assertEquals("Correo electrónico inválido.", AuthRules.validateRegistration("Ana", "ana", "123456"))
        assertEquals(
            "La contraseña debe tener al menos 6 caracteres.",
            AuthRules.validateRegistration("Ana", "ana@smtp.bo", "12345")
        )
    }
}