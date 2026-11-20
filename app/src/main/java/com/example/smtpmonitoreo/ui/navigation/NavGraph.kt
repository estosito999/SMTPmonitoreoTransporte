package com.example.smtpmonitoreo.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.smtpmonitoreo.BuildConfig
import com.example.smtpmonitoreo.data.model.UserAccount
import com.example.smtpmonitoreo.data.model.UserRole
import com.example.smtpmonitoreo.data.repository.SessionState
import com.example.smtpmonitoreo.ui.auth.AuthScreen
import com.example.smtpmonitoreo.ui.auth.AuthViewModel
import com.example.smtpmonitoreo.ui.dashboard.AdminDashboard
import com.example.smtpmonitoreo.ui.dashboard.AlcaldiaDashboard
import com.example.smtpmonitoreo.ui.dashboard.ChoferDashboard
import com.example.smtpmonitoreo.ui.dashboard.PasajeroDashboard
import com.example.smtpmonitoreo.ui.theme.CyberCyan
import com.example.smtpmonitoreo.ui.theme.DarkBackground
import org.koin.compose.viewmodel.koinViewModel

object NavRoutes {
    const val AUTH = "auth"
    const val DASHBOARD_PASAJERO = "dashboard_pasajero"
    const val DASHBOARD_CHOFER = "dashboard_chofer"
    const val DASHBOARD_ALCALDIA = "dashboard_alcaldia"
    const val DASHBOARD_ADMIN = "dashboard_admin"

    fun dashboardFor(role: UserRole): String = when (role) {
        UserRole.PASAJERO -> DASHBOARD_PASAJERO
        UserRole.CHOFER -> DASHBOARD_CHOFER
        UserRole.ALCALDIA -> DASHBOARD_ALCALDIA
        UserRole.ADMINISTRADOR -> DASHBOARD_ADMIN
    }

    /** Un rol solo abre su dashboard; en debug el administrador puede previsualizar los demás. */
    fun canOpen(role: UserRole, route: String): Boolean =
        route == dashboardFor(role) ||
                (BuildConfig.DEMO_MODE && role == UserRole.ADMINISTRADOR && route != AUTH)
}

@Composable
fun SmtpNavGraph(
    navController: NavHostController = rememberNavController(),
    authViewModel: AuthViewModel = koinViewModel()
) {
    val sessionState = authViewModel.session.collectAsStateWithLifecycle()

    when (val initial = sessionState.value) {
        SessionState.Loading -> SplashScreen()

        else -> {
            // Se decide una sola vez, cuando ya se conoce la sesión
            val startDestination = remember {
                (initial as? SessionState.SignedIn)
                    ?.let { NavRoutes.dashboardFor(it.user.role) }
                    ?: NavRoutes.AUTH
            }

            SessionGuard(navController, sessionState)

            NavHost(navController = navController, startDestination = startDestination) {
                composable(NavRoutes.AUTH) {
                    AuthScreen(viewModel = authViewModel)
                }

                composable(NavRoutes.DASHBOARD_PASAJERO) {
                    val user = sessionUser(sessionState) ?: return@composable
                    PasajeroDashboard(user = user, onLogout = authViewModel::logout)
                }

                composable(NavRoutes.DASHBOARD_CHOFER) {
                    val user = sessionUser(sessionState) ?: return@composable
                    ChoferDashboard(user = user, onLogout = authViewModel::logout)
                }

                composable(NavRoutes.DASHBOARD_ALCALDIA) {
                    val user = sessionUser(sessionState) ?: return@composable
                    AlcaldiaDashboard(user = user, onLogout = authViewModel::logout)
                }

                composable(NavRoutes.DASHBOARD_ADMIN) {
                    val user = sessionUser(sessionState) ?: return@composable
                    AdminDashboard(
                        user = user,
                        onLogout = authViewModel::logout,
                        onSwitchRolePreview = { role ->
                            navController.navigate(NavRoutes.dashboardFor(role))
                        }
                    )
                }
            }
        }
    }
}

/** Mantiene la pantalla visible coherente con la sesión y con el rol del usuario. */
@Composable
private fun SessionGuard(
    navController: NavHostController,
    sessionState: State<SessionState>
) {
    val backStackEntry by navController.currentBackStackEntryAsState()
    val route = backStackEntry?.destination?.route
    val session = sessionState.value

    LaunchedEffect(session, route) {
        if (route == null) return@LaunchedEffect

        val target: String? = when (session) {
            SessionState.Loading -> null
            SessionState.SignedOut ->
                NavRoutes.AUTH.takeIf { route != NavRoutes.AUTH }
            is SessionState.SignedIn ->
                NavRoutes.dashboardFor(session.user.role)
                    .takeIf { !NavRoutes.canOpen(session.user.role, route) }
        }

        if (target != null) {
            navController.navigate(target) {
                popUpTo(navController.graph.id) { inclusive = true }
                launchSingleTop = true
            }
        }
    }
}

private fun sessionUser(sessionState: State<SessionState>): UserAccount? =
    (sessionState.value as? SessionState.SignedIn)?.user

@Composable
private fun SplashScreen() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(color = CyberCyan)
    }
}