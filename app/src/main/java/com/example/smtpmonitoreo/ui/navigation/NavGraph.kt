package com.example.smtpmonitoreo.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import org.koin.compose.viewmodel.koinViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.smtpmonitoreo.data.model.UserAccount
import com.example.smtpmonitoreo.data.model.UserRole
import com.example.smtpmonitoreo.ui.auth.AuthScreen
import com.example.smtpmonitoreo.ui.auth.AuthViewModel
import com.example.smtpmonitoreo.ui.dashboard.AdminDashboard
import com.example.smtpmonitoreo.ui.dashboard.AlcaldiaDashboard
import com.example.smtpmonitoreo.ui.dashboard.ChoferDashboard
import com.example.smtpmonitoreo.ui.dashboard.PasajeroDashboard

object NavRoutes {
    const val AUTH = "auth"
    const val DASHBOARD_PASAJERO = "dashboard_pasajero"
    const val DASHBOARD_CHOFER = "dashboard_chofer"
    const val DASHBOARD_ALCALDIA = "dashboard_alcaldia"
    const val DASHBOARD_ADMIN = "dashboard_admin"
}

@Composable
fun SmtpNavGraph(
    navController: NavHostController = rememberNavController(),
    authViewModel: AuthViewModel = koinViewModel()
) {
    val uiState by authViewModel.uiState.collectAsState()
    val currentUser = uiState.loggedInUser

    NavHost(
        navController = navController,
        startDestination = getDestinationForRole(currentUser?.role)
    ) {
        composable(NavRoutes.AUTH) {
            AuthScreen(
                viewModel = authViewModel,
                onAuthSuccess = { role ->
                    val destination = when (role) {
                        UserRole.PASAJERO -> NavRoutes.DASHBOARD_PASAJERO
                        UserRole.CHOFER -> NavRoutes.DASHBOARD_CHOFER
                        UserRole.ALCALDIA -> NavRoutes.DASHBOARD_ALCALDIA
                        UserRole.ADMINISTRADOR -> NavRoutes.DASHBOARD_ADMIN
                    }
                    navController.navigate(destination) {
                        popUpTo(NavRoutes.AUTH) { inclusive = true }
                    }
                }
            )
        }

        composable(NavRoutes.DASHBOARD_PASAJERO) {
            PasajeroDashboard(
                user = currentUser ?: UserAccount(fullName = "Pasajero Demo", role = UserRole.PASAJERO),
                onLogout = {
                    authViewModel.quickLoginAs(UserRole.PASAJERO) // Reset demo state
                    navController.navigate(NavRoutes.AUTH) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        composable(NavRoutes.DASHBOARD_CHOFER) {
            ChoferDashboard(
                user = currentUser ?: UserAccount(fullName = "Chofer Demo", role = UserRole.CHOFER),
                onLogout = {
                    navController.navigate(NavRoutes.AUTH) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        composable(NavRoutes.DASHBOARD_ALCALDIA) {
            AlcaldiaDashboard(
                user = currentUser ?: UserAccount(fullName = "Inspector Municipal", role = UserRole.ALCALDIA),
                onLogout = {
                    navController.navigate(NavRoutes.AUTH) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        composable(NavRoutes.DASHBOARD_ADMIN) {
            AdminDashboard(
                user = currentUser ?: UserAccount(fullName = "Admin Israel Pérez", role = UserRole.ADMINISTRADOR),
                onLogout = {
                    navController.navigate(NavRoutes.AUTH) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                onSwitchRolePreview = { targetRole ->
                    val destination = when (targetRole) {
                        UserRole.PASAJERO -> NavRoutes.DASHBOARD_PASAJERO
                        UserRole.CHOFER -> NavRoutes.DASHBOARD_CHOFER
                        UserRole.ALCALDIA -> NavRoutes.DASHBOARD_ALCALDIA
                        UserRole.ADMINISTRADOR -> NavRoutes.DASHBOARD_ADMIN
                    }
                    navController.navigate(destination)
                }
            )
        }
    }
}

private fun getDestinationForRole(role: UserRole?): String {
    return when (role) {
        UserRole.PASAJERO -> NavRoutes.DASHBOARD_PASAJERO
        UserRole.CHOFER -> NavRoutes.DASHBOARD_CHOFER
        UserRole.ALCALDIA -> NavRoutes.DASHBOARD_ALCALDIA
        UserRole.ADMINISTRADOR -> NavRoutes.DASHBOARD_ADMIN
        null -> NavRoutes.AUTH
    }
}
