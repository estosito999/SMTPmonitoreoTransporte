package com.example.smtpmonitoreo.domain

/** Cálculo de recorridos por calles (la implementación usa OSRM a través de OSMBonusPack). */
interface RoutingService {
    /** Trazado por calles que pasa por los puntos dados, o null si no se pudo calcular. */
    suspend fun roadPath(points: List<Pair<Double, Double>>): List<Pair<Double, Double>>?

    /** Distancia por calles en km entre dos puntos, o null si no se pudo calcular. */
    suspend fun roadDistanceKm(from: Pair<Double, Double>, to: Pair<Double, Double>): Double?
}