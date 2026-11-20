package com.example.smtpmonitoreo.domain

import com.example.smtpmonitoreo.data.model.AccountStatus
import com.example.smtpmonitoreo.data.model.UserAccount
import com.example.smtpmonitoreo.data.model.UserRole
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class UserAdminRulesTest {

    private val admin = UserAccount(uid = "u4", email = "admin@smtp.bo", role = UserRole.ADMINISTRADOR)
    private val pasajero = UserAccount(uid = "u1", email = "p@smtp.bo", role = UserRole.PASAJERO)
    private val users = listOf(admin, pasajero)

    // ---------- Alta de cuentas ----------

    @Test
    fun crea_una_cuenta_valida_recortando_los_espacios() {
        val draft = AccountDraft(fullName = " Ana Mamani ", email = " ana@smtp.bo ", role = UserRole.CHOFER)

        val account = (UserAdminRules.validateNewAccount(draft, users) as Validation.Ok).value

        assertEquals("Ana Mamani", account.fullName)
        assertEquals("ana@smtp.bo", account.email)
        assertEquals(UserRole.CHOFER, account.role)
        assertEquals(AccountStatus.ACTIVO, account.status)
    }

    @Test
    fun rechaza_nombre_vacio_y_correo_invalido() {
        val ok = AccountDraft(fullName = "Ana", email = "ana@smtp.bo")

        assertTrue(UserAdminRules.validateNewAccount(ok.copy(fullName = " "), users) is Validation.Error)
        assertTrue(UserAdminRules.validateNewAccount(ok.copy(email = "ana@"), users) is Validation.Error)
    }

    @Test
    fun rechaza_un_correo_repetido_sin_importar_mayusculas() {
        val result = UserAdminRules.validateNewAccount(
            AccountDraft(fullName = "Otro", email = "P@SMTP.BO"),
            users
        )

        assertEquals(Validation.Error("Ya existe una cuenta con ese correo."), result)
    }

    // ---------- Cambio de rol ----------

    @Test
    fun no_permite_cambiar_el_propio_rol() {
        assertNotNull(UserAdminRules.roleChangeError(users, admin, UserRole.CHOFER, "admin@smtp.bo"))
    }

    @Test
    fun no_permite_quitar_al_ultimo_administrador_activo() {
        val error = UserAdminRules.roleChangeError(users, admin, UserRole.PASAJERO, "otro@smtp.bo")

        assertEquals("Debe existir al menos un administrador activo.", error)
    }

    @Test
    fun permite_cambiar_el_rol_si_hay_otro_administrador_activo() {
        val admin2 = UserAccount(uid = "u5", email = "admin2@smtp.bo", role = UserRole.ADMINISTRADOR)

        assertNull(UserAdminRules.roleChangeError(users + admin2, admin, UserRole.PASAJERO, "admin2@smtp.bo"))
        assertNull(UserAdminRules.roleChangeError(users, pasajero, UserRole.CHOFER, "admin@smtp.bo"))
    }

    // ---------- Suspensión ----------

    @Test
    fun no_permite_suspenderse_a_si_mismo_ni_al_ultimo_administrador() {
        assertNotNull(UserAdminRules.suspensionError(users, admin, "admin@smtp.bo"))
        assertNotNull(UserAdminRules.suspensionError(users, admin, "otro@smtp.bo"))
    }

    @Test
    fun permite_suspender_a_un_usuario_comun_y_reactivar_siempre() {
        val suspendido = pasajero.copy(status = AccountStatus.SUSPENDIDO)

        assertNull(UserAdminRules.suspensionError(users, pasajero, "admin@smtp.bo"))
        assertNull(UserAdminRules.suspensionError(users, suspendido, "admin@smtp.bo"))
    }
}