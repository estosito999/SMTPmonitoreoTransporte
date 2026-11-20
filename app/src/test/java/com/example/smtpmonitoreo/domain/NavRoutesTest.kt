package com.example.smtpmonitoreo.ui.navigation

import com.example.smtpmonitoreo.data.model.UserRole
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class NavRoutesTest {

    @Test
    fun cada_rol_tiene_su_dashboard() {
        assertEquals(NavRoutes.DASHBOARD_PASAJERO, NavRoutes.dashboardFor(UserRole.PASAJERO))
        assertEquals(NavRoutes.DASHBOARD_CHOFER, NavRoutes.dashboardFor(UserRole.CHOFER))
        assertEquals(NavRoutes.DASHBOARD_ALCALDIA, NavRoutes.dashboardFor(UserRole.ALCALDIA))
        assertEquals(NavRoutes.DASHBOARD_ADMIN, NavRoutes.dashboardFor(UserRole.ADMINISTRADOR))
    }

    @Test
    fun un_rol_puede_abrir_su_propio_dashboard() {
        assertTrue(NavRoutes.canOpen(UserRole.PASAJERO, NavRoutes.DASHBOARD_PASAJERO))
        assertTrue(NavRoutes.canOpen(UserRole.ADMINISTRADOR, NavRoutes.DASHBOARD_ADMIN))
    }

    @Test
    fun un_rol_no_privilegiado_no_abre_dashboards_ajenos_ni_el_login() {
        assertFalse(NavRoutes.canOpen(UserRole.PASAJERO, NavRoutes.DASHBOARD_ADMIN))
        assertFalse(NavRoutes.canOpen(UserRole.CHOFER, NavRoutes.DASHBOARD_ALCALDIA))
        assertFalse(NavRoutes.canOpen(UserRole.PASAJERO, NavRoutes.AUTH))
    }
}