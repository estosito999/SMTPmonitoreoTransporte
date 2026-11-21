package com.example.smtpmonitoreo.domain

import com.example.smtpmonitoreo.data.model.RouteModel

data class RouteDraft(
    val lineNumber: String = "",
    val name: String = "",
    val origin: String = "",
    val destination: String = "",
    val tariff: String = "2.50",
    /** Paradas intermedias separadas por coma. */
    val stops: String = ""
)

object RouteRegistration {
    private const val MAX_TARIFF_BS = 50.0

    fun validate(draft: RouteDraft, existing: List<RouteModel>): Validation<RouteModel> {
        val line = draft.lineNumber.trim()
        val name = draft.name.trim()
        val origin = draft.origin.trim()
        val destination = draft.destination.trim()

        if (line.isEmpty()) return Validation.Error("Ingresa el número de línea.")
        if (existing.any { it.lineNumber.equals(line, ignoreCase = true) }) {
            return Validation.Error("La línea $line ya tiene una ruta oficial.")
        }
        if (name.isEmpty()) return Validation.Error("Ingresa el nombre de la ruta.")
        if (origin.isEmpty() || destination.isEmpty()) {
            return Validation.Error("Indica el origen y el destino.")
        }
        if (origin.equals(destination, ignoreCase = true)) {
            return Validation.Error("El origen y el destino deben ser distintos.")
        }

        val tariff = parseDecimal(draft.tariff)
        if (tariff == null || tariff <= 0.0 || tariff > MAX_TARIFF_BS) {
            return Validation.Error("Tarifa inválida (entre 0.01 y 50 Bs).")
        }

        val stops = parseStops(draft.stops)

        return Validation.Ok(
            RouteModel(
                lineNumber = line,
                name = name,
                origin = origin,
                destination = destination,
                tariffBs = tariff,
                stops = stops,
                // Trazado: solo los puntos que se reconocen como lugares conocidos de La Paz
                waypoints = LaPazPlaces.waypointsFor(origin, stops, destination)
            )
        )
    }

    fun parseStops(raw: String): List<String> =
        raw.split(',')
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .distinctBy { it.lowercase() }
}