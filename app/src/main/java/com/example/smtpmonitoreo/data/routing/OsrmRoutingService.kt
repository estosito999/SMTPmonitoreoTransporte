package com.example.smtpmonitoreo.data.routing

import android.content.Context
import com.example.smtpmonitoreo.domain.RoutingService
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import org.osmdroid.bonuspack.routing.OSRMRoadManager
import org.osmdroid.bonuspack.routing.Road
import org.osmdroid.config.Configuration
import org.osmdroid.util.GeoPoint
import java.util.Locale
import java.util.concurrent.ConcurrentHashMap

class OsrmRoutingService(context: Context) : RoutingService {

    private val manager = OSRMRoadManager(
        context.applicationContext,
        Configuration.getInstance().userAgentValue
    ).apply { setMean(OSRMRoadManager.MEAN_BY_CAR) }

    private val mutex = Mutex()
    private var lastRequestAt = 0L

    private val pathCache = ConcurrentHashMap<String, List<Pair<Double, Double>>>()
    private val distanceCache = ConcurrentHashMap<String, Double>()

    override suspend fun roadPath(points: List<Pair<Double, Double>>): List<Pair<Double, Double>>? {
        if (points.size < 2) return null
        val cacheKey = points.joinToString("|") { exactKey(it) }
        pathCache[cacheKey]?.let { return it }

        val road = fetchRoad(points) ?: return null
        val path = road.mRouteHigh.map { it.latitude to it.longitude }
        if (path.size < 2) return null
        pathCache[cacheKey] = path
        return path
    }

    override suspend fun roadDistanceKm(from: Pair<Double, Double>, to: Pair<Double, Double>): Double? {
        // Redondeo a ~100 m: evita pedir una ruta nueva por cada pequeño movimiento de la unidad
        val cacheKey = "${coarseKey(from)}>${coarseKey(to)}"
        distanceCache[cacheKey]?.let { return it }

        val road = fetchRoad(listOf(from, to)) ?: return null
        if (distanceCache.size > MAX_CACHE_ENTRIES) distanceCache.clear()
        distanceCache[cacheKey] = road.mLength
        return road.mLength
    }

    /** Las peticiones se hacen de una en una, separadas al menos 1 segundo. */
    private suspend fun fetchRoad(points: List<Pair<Double, Double>>): Road? = mutex.withLock {
        val wait = MIN_INTERVAL_MS - (System.currentTimeMillis() - lastRequestAt)
        if (wait > 0) delay(wait)
        try {
            val road = withContext(Dispatchers.IO) {
                manager.getRoad(ArrayList(points.map { GeoPoint(it.first, it.second) }))
            }
            if (road.mStatus == Road.STATUS_OK) road else null
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            null
        } finally {
            lastRequestAt = System.currentTimeMillis()
        }
    }

    private fun exactKey(p: Pair<Double, Double>) =
        String.format(Locale.US, "%.5f,%.5f", p.first, p.second)

    private fun coarseKey(p: Pair<Double, Double>) =
        String.format(Locale.US, "%.3f,%.3f", p.first, p.second)

    private companion object {
        const val MIN_INTERVAL_MS = 1_100L
        const val MAX_CACHE_ENTRIES = 300
    }
}