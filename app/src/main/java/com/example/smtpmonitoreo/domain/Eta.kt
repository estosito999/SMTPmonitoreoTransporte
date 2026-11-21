package com.example.smtpmonitoreo.domain

import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.math.sqrt

object EtaMath {
    private const val EARTH_RADIUS_KM = 6371.0

    /** Una ruta por calles suele ser más larga que la línea recta. */
    const val DETOUR_FACTOR = 1.3
    const val DEFAULT_URBAN_SPEED_KMH = 20f
    const val MIN_MOVING_SPEED_KMH = 5f

    fun haversineKm(lat1: Double, lng1: Double, lat2: Double, lng2: Double): Double {
        val dLat = Math.toRadians(lat2 - lat1)
        val dLng = Math.toRadians(lng2 - lng1)
        val a = sin(dLat / 2).pow(2.0) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) * sin(dLng / 2).pow(2.0)
        return 2 * EARTH_RADIUS_KM * atan2(sqrt(a), sqrt(1 - a))
    }

    /** Si la unidad está casi detenida se usa una velocidad urbana típica. */
    fun minutes(distanceKm: Double, speedKmh: Float): Int {
        val speed = if (speedKmh > MIN_MOVING_SPEED_KMH) speedKmh else DEFAULT_URBAN_SPEED_KMH
        return max(1, (distanceKm / speed * 60).roundToInt())
    }
}

class EtaCalculator(private val routing: RoutingService) {

    suspend fun minutes(from: Pair<Double, Double>, to: Pair<Double, Double>, speedKmh: Float): Int {
        val distanceKm = routing.roadDistanceKm(from, to)
            ?: (EtaMath.haversineKm(from.first, from.second, to.first, to.second) * EtaMath.DETOUR_FACTOR)
        return EtaMath.minutes(distanceKm, speedKmh)
    }
}