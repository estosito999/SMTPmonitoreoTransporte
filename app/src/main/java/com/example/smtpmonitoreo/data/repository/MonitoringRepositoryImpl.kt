package com.example.smtpmonitoreo.data.repository

import com.example.smtpmonitoreo.data.model.IncidentReport
import com.example.smtpmonitoreo.data.model.IncidentStatus
import com.example.smtpmonitoreo.data.model.InfractionModel
import com.example.smtpmonitoreo.data.model.InfractionStatus
import com.example.smtpmonitoreo.data.model.RouteModel
import com.example.smtpmonitoreo.data.model.TransportUnit
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.tasks.await
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.math.sqrt
import com.example.smtpmonitoreo.domain.UnitLiveness
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn

class MonitoringRepositoryImpl(
    db: FirebaseFirestore,
    auth: AuthRepository
) : MonitoringRepository {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    private val unitsRef = db.collection("units")
    private val routesRef = db.collection("routes")
    private val incidentsRef = db.collection("incidents")
    private val infractionsRef = db.collection("infractions")

    // ---------- Lecturas en tiempo real (solo con sesión activa) ----------

    private val rawUnits: StateFlow<List<TransportUnit>> =
        auth.currentUserFlow.listenTo(scope, queryFor = { unitsRef }) { it.toTransportUnit() }

    // Reloj que refresca el estado "en ruta" aunque no lleguen cambios de Firestore
    private val clock = flow {
        while (true) {
            emit(System.currentTimeMillis())
            delay(15_000L)
        }
    }

    override val activeUnitsFlow: StateFlow<List<TransportUnit>> =
        combine(rawUnits, clock) { units, now -> units.map { UnitLiveness.withLiveness(it, now) } }
            .stateIn(scope, SharingStarted.WhileSubscribed(5_000), emptyList())

    override val routesFlow: StateFlow<List<RouteModel>> =
        auth.currentUserFlow.listenTo(scope, queryFor = { routesRef }) { it.toRoute() }

    override val incidentsFlow: StateFlow<List<IncidentReport>> =
        auth.currentUserFlow.listenTo(
            scope,
            queryFor = { user ->
                if (user.role.isStaff) incidentsRef
                else incidentsRef.whereEqualTo("userEmail", user.email)
            }
        ) { it.toIncident() }

    override val infractionsFlow: StateFlow<List<InfractionModel>> =
        auth.currentUserFlow.listenTo(
            scope,
            queryFor = { user -> if (user.role.isStaff) infractionsRef else null }
        ) { it.toInfraction() }

    // ---------- Rastreo GPS (RF-31, RF-32) ----------

    override suspend fun startGpsTransmission(plate: String) = setTransmitting(plate, true)

    override suspend fun stopGpsTransmission(plate: String) = setTransmitting(plate, false)

    private suspend fun setTransmitting(plate: String, transmitting: Boolean) {
        unitsRef.document(plate)
            .update(
                mapOf(
                    "isTransmitting" to transmitting,
                    "lastUpdated" to System.currentTimeMillis()
                )
            )
            .await()
    }

    override suspend fun updateVehicleLocation(
        plate: String,
        lat: Double,
        lng: Double,
        speedKmh: Float,
        heading: Float
    ) {
        unitsRef.document(plate)
            .update(
                mapOf(
                    "latitude" to lat,
                    "longitude" to lng,
                    "speedKmh" to speedKmh.toDouble(),
                    "heading" to heading.toDouble(),
                    "lastUpdated" to System.currentTimeMillis()
                )
            )
            .await()
    }

    // ---------- Incidentes (RF-18, RF-43) ----------

    override suspend fun submitIncident(report: IncidentReport): Result<Unit> = resultOf {
        val newReport = report.copy(
            status = IncidentStatus.RECIBIDO,
            timestamp = System.currentTimeMillis()
        )
        incidentsRef.add(newReport.toMap()).await()
        Unit
    }

    override suspend fun updateIncidentStatus(
        reportId: String,
        newStatus: IncidentStatus,
        note: String
    ): Result<Unit> = resultOf {
        incidentsRef.document(reportId)
            .update(mapOf("status" to newStatus.name, "resolutionNote" to note))
            .await()
        Unit
    }

    // ---------- Infracciones (RF-44) ----------

    override suspend fun addInfraction(infraction: InfractionModel): Result<Unit> = resultOf {
        infractionsRef.add(infraction.toMap()).await()
        Unit
    }

    override suspend fun updateInfractionStatus(
        infractionId: String,
        newStatus: InfractionStatus
    ): Result<Unit> = resultOf {
        infractionsRef.document(infractionId).update("status", newStatus.name).await()
        Unit
    }

    // ---------- Rutas y unidades (RF-40, RF-41) ----------

    override suspend fun addRoute(route: RouteModel): Result<Unit> = resultOf {
        routesRef.add(route.toMap()).await()
        Unit
    }

    override suspend fun addUnit(unit: TransportUnit): Result<Unit> = resultOf {
        val ref = unitsRef.document(unit.plate) // la placa es el ID: es única por diseño
        if (ref.get().await().exists()) {
            throw IllegalStateException("La placa ${unit.plate} ya está registrada.")
        }
        ref.set(unit.toMap()).await()
        Unit
    }


}