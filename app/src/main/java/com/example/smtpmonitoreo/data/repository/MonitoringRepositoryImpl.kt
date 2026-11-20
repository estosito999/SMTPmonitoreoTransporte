package com.example.smtpmonitoreo.data.repository

import com.example.smtpmonitoreo.data.model.IncidentReport
import com.example.smtpmonitoreo.data.model.InfractionModel
import com.example.smtpmonitoreo.data.model.RouteModel
import com.example.smtpmonitoreo.data.model.TransportUnit
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.math.*
import kotlinx.coroutines.SupervisorJob
import com.example.smtpmonitoreo.data.model.IncidentStatus
import com.example.smtpmonitoreo.data.model.InfractionStatus
import com.example.smtpmonitoreo.data.model.IncidentCategory

class MonitoringRepositoryImpl : MonitoringRepository {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    // Coordenadas emblemáticas de La Paz
    private val perezVelasco = Pair(-16.4965, -68.1375)
    private val pradoLaPaz = Pair(-16.5020, -68.1320)
    private val sanPedro = Pair(-16.5005, -68.1390)
    private val miraflores = Pair(-16.5040, -68.1220)
    private val cejaElAlto = Pair(-16.5100, -68.1600)
    private val calacoto = Pair(-16.5400, -68.0880)

    // Rutas precargadas de La Paz
    private val initialRoutes = listOf(
        RouteModel(
            id = "route_102",
            lineNumber = "102",
            name = "Línea 102: Ceja El Alto - Pérez Velasco - Sopocachi",
            origin = "Ceja El Alto",
            destination = "Sopocachi",
            tariffBs = 2.50,
            colorHex = "#00F0FF",
            waypoints = listOf(cejaElAlto, perezVelasco, pradoLaPaz, sanPedro),
            stops = listOf("Ceja El Alto", "Pérez Velasco", "El Obelisco", "Plaza Estudiante", "San Pedro")
        ),
        RouteModel(
            id = "route_231",
            lineNumber = "231",
            name = "Línea 231: Periférica - Pérez Velasco - Miraflores",
            origin = "Periférica",
            destination = "Miraflores",
            tariffBs = 2.00,
            colorHex = "#00FF9D",
            waypoints = listOf(perezVelasco, pradoLaPaz, miraflores),
            stops = listOf("Periférica", "Pérez Velasco", "Plaza Murillo", "Hospital del Niño", "Miraflores")
        ),
        RouteModel(
            id = "route_trufi45",
            lineNumber = "Trufi 45",
            name = "Trufi 45: Calacoto - San Pedro - Pérez Velasco",
            origin = "Calacoto",
            destination = "Pérez Velasco",
            tariffBs = 3.50,
            colorHex = "#B056FF",
            waypoints = listOf(calacoto, miraflores, pradoLaPaz, perezVelasco),
            stops = listOf("Calacoto Calle 8", "San Miguel", "Plaza Isabel La Católica", "Pérez Velasco")
        )
    )

    // Unidades iniciales en movimiento por La Paz
    private val initialUnits = listOf(
        TransportUnit(
            id = "unit_1",
            plate = "4521-FGH",
            lineNumber = "102",
            syndicate = "Sindicato San Cristóbal",
            vehicleType = "Minibús",
            latitude = -16.4965,
            longitude = -68.1375,
            heading = 180f,
            speedKmh = 28f,
            driverName = "Don Rolando Mamani",
            isTransmitting = true
        ),
        TransportUnit(
            id = "unit_2",
            plate = "2184-KLM",
            lineNumber = "231",
            syndicate = "Sindicato Pedro Domingo Murillo",
            vehicleType = "Minibús",
            latitude = -16.5020,
            longitude = -68.1320,
            heading = 90f,
            speedKmh = 32f,
            driverName = "Juan Carlos Condori",
            isTransmitting = true
        ),
        TransportUnit(
            id = "unit_3",
            plate = "3091-XYZ",
            lineNumber = "Trufi 45",
            syndicate = "Asociación de Trufis Zona Sur",
            vehicleType = "Trufi",
            latitude = -16.5040,
            longitude = -68.1220,
            heading = 45f,
            speedKmh = 40f,
            driverName = "Gonzalo Flores",
            isTransmitting = true
        )
    )

    // Reportes de prueba iniciales (RF-18, RF-43)
    private val initialIncidents = listOf(
        IncidentReport(
            id = "inc_1",
            userEmail = "pasajero.demo@smtp.lapaz.bo",
            vehiclePlate = "4521-FGH",
            lineNumber = "102",
            category = IncidentCategory.COBRO_IRREGULAR,
            description = "Cobro de tarifa nocturna a las 17:30 en Pérez Velasco.",
            status = IncidentStatus.RECIBIDO,
            timestamp = System.currentTimeMillis() - 3600000
        ),
        IncidentReport(
            id = "inc_2",
            userEmail = "usuario.lapaz@gmail.com",
            vehiclePlate = "2184-KLM",
            lineNumber = "231",
            category = IncidentCategory.DESVIO_RUTA,
            description = "El minibús no llegó a Miraflores y cortó ruta en la Plaza Uyuni.",
            status = IncidentStatus.EN_REVISION,
            timestamp = System.currentTimeMillis() - 7200000
        )
    )

    // Infracciones registradas (RF-44)
    private val initialInfractions = listOf(
        InfractionModel(
            id = "inf_1",
            vehiclePlate = "2184-KLM",
            lineNumber = "231",
            reason = "Desvío de ruta arbitrario en zona Miraflores",
            fineAmountBs = 100.0,
            timestamp = System.currentTimeMillis() - 86_400_000L,
            status = InfractionStatus.EMITIDA
        )
    )

    private val _activeUnitsFlow = MutableStateFlow(initialUnits)
    override val activeUnitsFlow: StateFlow<List<TransportUnit>> = _activeUnitsFlow.asStateFlow()

    private val _routesFlow = MutableStateFlow(initialRoutes)
    override val routesFlow: StateFlow<List<RouteModel>> = _routesFlow.asStateFlow()

    private val _incidentsFlow = MutableStateFlow(initialIncidents)
    override val incidentsFlow: StateFlow<List<IncidentReport>> = _incidentsFlow.asStateFlow()

    private val _infractionsFlow = MutableStateFlow(initialInfractions)
    override val infractionsFlow: StateFlow<List<InfractionModel>> = _infractionsFlow.asStateFlow()

    init {
        // Motor de simulación GPS en vivo (RNF-01: actualización cada 5-10 segundos)
        scope.launch {
            while (true) {
                delay(5000)
                simulateGpsMovement()
            }
        }
    }

    private fun simulateGpsMovement() {
        _activeUnitsFlow.update { currentUnits ->
            currentUnits.map { unit ->
                if (unit.isTransmitting) {
                    // Desplazamiento orgánico realista
                    val deltaLat = (Math.random() - 0.5) * 0.0008
                    val deltaLng = (Math.random() - 0.5) * 0.0008
                    unit.copy(
                        latitude = unit.latitude + deltaLat,
                        longitude = unit.longitude + deltaLng,
                        heading = (unit.heading + 15f) % 360f,
                        speedKmh = 20f + (Math.random() * 15f).toFloat(),
                        lastUpdated = System.currentTimeMillis()
                    )
                } else {
                    unit
                }
            }
        }
    }

    override suspend fun startGpsTransmission(plate: String, lineNumber: String) {
        _activeUnitsFlow.update { currentUnits ->
            val existing = currentUnits.find { it.plate == plate }
            if (existing != null) {
                currentUnits.map {
                    if (it.plate == plate) it.copy(isTransmitting = true, lineNumber = lineNumber) else it
                }
            } else {
                currentUnits + TransportUnit(
                    id = "unit_${System.currentTimeMillis()}",
                    plate = plate,
                    lineNumber = lineNumber,
                    syndicate = "Sindicato Paceño",
                    vehicleType = "Minibús",
                    latitude = -16.4965,
                    longitude = -68.1375,
                    isTransmitting = true
                )
            }
        }
    }

    override suspend fun stopGpsTransmission(plate: String) {
        _activeUnitsFlow.update { currentUnits ->
            currentUnits.map {
                if (it.plate == plate) it.copy(isTransmitting = false) else it
            }
        }
    }

    override suspend fun updateVehicleLocation(plate: String, lat: Double, lng: Double, speed: Float) {
        _activeUnitsFlow.update { currentUnits ->
            currentUnits.map {
                if (it.plate == plate) {
                    it.copy(latitude = lat, longitude = lng, speedKmh = speed, lastUpdated = System.currentTimeMillis())
                } else it
            }
        }
    }

    override suspend fun submitIncident(report: IncidentReport): Result<Unit> {
        val newReport = report.copy(
            id = "inc_${System.currentTimeMillis()}",
            timestamp = System.currentTimeMillis(),
            status = IncidentStatus.RECIBIDO
        )
        _incidentsFlow.update { listOf(newReport) + it }
        return Result.success(Unit)
    }

    override suspend fun updateIncidentStatus(reportId: String, newStatus: IncidentStatus, note: String): Result<Unit> {
        _incidentsFlow.update { reports ->
            reports.map {
                if (it.id == reportId) it.copy(status = newStatus, resolutionNote = note) else it
            }
        }
        return Result.success(Unit)
    }

    override suspend fun addInfraction(infraction: InfractionModel): Result<Unit> {
        val newInfraction = infraction.copy(id = "inf_${System.currentTimeMillis()}")
        _infractionsFlow.update { listOf(newInfraction) + it }
        return Result.success(Unit)
    }
    override suspend fun updateInfractionStatus(infractionId: String, newStatus: InfractionStatus): Result<Unit> {
        _infractionsFlow.update { list ->
            list.map { if (it.id == infractionId) it.copy(status = newStatus) else it }
        }
        return Result.success(Unit)
    }

    override suspend fun addRoute(route: RouteModel): Result<Unit> {
        val newRoute = route.copy(id = "route_${System.currentTimeMillis()}")
        _routesFlow.update { it + newRoute }
        return Result.success(Unit)
    }

    override suspend fun addUnit(unit: TransportUnit): Result<Unit> {
        val newUnit = unit.copy(id = "unit_${System.currentTimeMillis()}")
        _activeUnitsFlow.update { it + newUnit }
        return Result.success(Unit)
    }

    override fun calculateEtaMinutes(
        unitLat: Double,
        unitLng: Double,
        targetLat: Double,
        targetLng: Double,
        speedKmh: Float
    ): Int {
        val dLat = Math.toRadians(targetLat - unitLat)
        val dLng = Math.toRadians(targetLng - unitLng)
        val a = sin(dLat / 2).pow(2.0) + cos(Math.toRadians(unitLat)) * cos(Math.toRadians(targetLat)) * sin(dLng / 2).pow(2.0)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        val distanceKm = 6371.0 * c // Earth radius in km

        val effectiveSpeed = if (speedKmh > 5f) speedKmh else 25f
        val hours = distanceKm / effectiveSpeed
        val minutes = (hours * 60).roundToInt()
        return max(1, minutes)
    }


}
