package com.example.smtpmonitoreo.domain

import com.example.smtpmonitoreo.data.model.RouteModel

data class RouteSearchResult(
    val route: RouteModel,
    val stopsInOrder: List<String>
)

object RouteSearch {

    /** Origen + paradas + destino oficial, sin repetidos. */
    fun orderedStops(route: RouteModel): List<String> =
        (listOf(route.origin) + route.stops + route.destination)
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .distinctBy { it.lowercase() }

    fun find(routes: List<RouteModel>, origin: String, destination: String): RouteSearchResult? {
        if (origin.isBlank() || destination.isBlank()) return null

        return routes.mapNotNull { route ->
            val stops = orderedStops(route)
            val i = stops.indexOfFirst { it.contains(origin, ignoreCase = true) }
            val j = stops.indexOfFirst { it.contains(destination, ignoreCase = true) }
            when {
                i < 0 || j < 0 || i == j -> null
                i < j -> RouteSearchResult(route, stops.subList(i, j + 1))
                else -> RouteSearchResult(route, stops.subList(j, i + 1).reversed())
            }
        }.minByOrNull { it.stopsInOrder.size }
    }
}