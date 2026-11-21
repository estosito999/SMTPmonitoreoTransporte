package com.example.smtpmonitoreo.ui.map

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smtpmonitoreo.data.model.LogLevel
import com.example.smtpmonitoreo.data.model.RouteModel
import com.example.smtpmonitoreo.domain.AppMonitor
import com.example.smtpmonitoreo.domain.NetworkMonitor
import com.example.smtpmonitoreo.domain.RoutingService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class RoutePathsViewModel(
    private val routing: RoutingService,
    private val network: NetworkMonitor,
    private val monitor: AppMonitor
) : ViewModel() {

    private val _paths = MutableStateFlow<Map<String, List<Pair<Double, Double>>>>(emptyMap())

    /** ID de la ruta → puntos del trazado por calles. */
    val paths: StateFlow<Map<String, List<Pair<Double, Double>>>> = _paths.asStateFlow()

    private val requested = mutableSetOf<String>()

    fun ensurePaths(routes: List<RouteModel>) {
        routes
            .filter { it.id.isNotBlank() && it.waypoints.size >= 2 }
            .forEach { route ->
                if (!requested.add(route.id)) return@forEach
                viewModelScope.launch {
                    val path = routing.roadPath(route.waypoints)
                    if (path == null && network.isOnlineNow()) {
                        // Con red pero sin trazado: el servidor de rutas falló (se registra una vez por línea)
                        monitor.log(
                            LogLevel.WARN,
                            "RUTAS",
                            "Sin trazado por calles para la línea ${route.lineNumber}; se dibuja en línea recta",
                            dedupeKey = "road-fallback-${route.id}"
                        )
                    }
                    // Sin red: se dibuja una línea recta entre los puntos de la ruta
                    _paths.update { it + (route.id to (path ?: route.waypoints)) }
                }
            }
    }
}