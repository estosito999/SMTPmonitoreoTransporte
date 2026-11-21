package com.example.smtpmonitoreo.domain

import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test

class EtaMathTest {

    @Test
    fun la_distancia_entre_el_mismo_punto_es_cero() {
        assertEquals(0.0, EtaMath.haversineKm(-16.4965, -68.1375, -16.4965, -68.1375), 0.0)
    }

    @Test
    fun calcula_la_distancia_entre_perez_velasco_y_ceja_el_alto() {
        val km = EtaMath.haversineKm(-16.4965, -68.1375, -16.5100, -68.1600)

        assertEquals(2.83, km, 0.05)
    }

    @Test
    fun los_minutos_dependen_de_la_distancia_y_la_velocidad() {
        assertEquals(20, EtaMath.minutes(10.0, 30f))
    }

    @Test
    fun una_unidad_detenida_usa_la_velocidad_urbana_por_defecto() {
        assertEquals(15, EtaMath.minutes(5.0, 0f))
    }

    @Test
    fun el_minimo_es_un_minuto() {
        assertEquals(1, EtaMath.minutes(0.01, 30f))
    }
}

class EtaCalculatorTest {

    private class FakeRouting(private val km: Double?) : RoutingService {
        override suspend fun roadPath(points: List<Pair<Double, Double>>): List<Pair<Double, Double>>? = null
        override suspend fun roadDistanceKm(from: Pair<Double, Double>, to: Pair<Double, Double>): Double? = km
    }

    private val perezVelasco = -16.4965 to -68.1375
    private val ceja = -16.5100 to -68.1600

    @Test
    fun usa_la_distancia_por_calles_cuando_esta_disponible() = runBlocking {
        val minutes = EtaCalculator(FakeRouting(3.0)).minutes(perezVelasco, ceja, speedKmh = 30f)

        assertEquals(6, minutes)
    }

    @Test
    fun sin_red_usa_la_linea_recta_con_factor_de_curvas() = runBlocking {
        // 2.83 km en línea recta × 1.3 = 3.68 km → a 30 km/h ≈ 7 min
        val minutes = EtaCalculator(FakeRouting(null)).minutes(perezVelasco, ceja, speedKmh = 30f)

        assertEquals(7, minutes)
    }
}