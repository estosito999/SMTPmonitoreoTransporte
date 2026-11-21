package com.example.smtpmonitoreo.ui.map

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smtpmonitoreo.data.model.RouteModel
import com.example.smtpmonitoreo.domain.RoutingService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class RoutePathsViewModel(
    private val routing: RoutingService
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
                    // Sin red: se dibuja una línea recta entre los puntos de la ruta
                    _paths.update { it + (route.id to (path ?: route.waypoints)) }
                }
            }
    }
}