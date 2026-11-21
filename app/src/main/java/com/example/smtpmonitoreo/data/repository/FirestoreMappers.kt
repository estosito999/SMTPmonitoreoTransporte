package com.example.smtpmonitoreo.data.repository

import com.example.smtpmonitoreo.data.model.AccountStatus
import com.example.smtpmonitoreo.data.model.IncidentCategory
import com.example.smtpmonitoreo.data.model.IncidentReport
import com.example.smtpmonitoreo.data.model.IncidentStatus
import com.example.smtpmonitoreo.data.model.InfractionModel
import com.example.smtpmonitoreo.data.model.InfractionStatus
import com.example.smtpmonitoreo.data.model.LogLevel
import com.example.smtpmonitoreo.data.model.RouteModel
import com.example.smtpmonitoreo.data.model.SystemLog
import com.example.smtpmonitoreo.data.model.TransportUnit
import com.example.smtpmonitoreo.data.model.UserAccount
import com.example.smtpmonitoreo.data.model.UserRole
import com.google.firebase.firestore.DocumentSnapshot

private const val DEFAULT_LAT = -16.5000
private const val DEFAULT_LNG = -68.1500

private inline fun <reified E : Enum<E>> enumOf(value: String?, default: E): E =
    enumValues<E>().firstOrNull { it.name == value } ?: default

// ---------- Unidades (el ID del documento es la placa) ----------

internal fun TransportUnit.toMap(): Map<String, Any> = mapOf(
    "plate" to plate,
    "lineNumber" to lineNumber,
    "syndicate" to syndicate,
    "vehicleType" to vehicleType,
    "latitude" to latitude,
    "longitude" to longitude,
    "heading" to heading.toDouble(),
    "speedKmh" to speedKmh.toDouble(),
    "driverName" to driverName,
    "isTransmitting" to isTransmitting,
    "lastUpdated" to lastUpdated
)

internal fun DocumentSnapshot.toTransportUnit(): TransportUnit? {
    val plate = getString("plate") ?: return null
    return TransportUnit(
        id = id,
        plate = plate,
        lineNumber = getString("lineNumber").orEmpty(),
        syndicate = getString("syndicate").orEmpty(),
        vehicleType = getString("vehicleType") ?: "Minibús",
        latitude = getDouble("latitude") ?: DEFAULT_LAT,
        longitude = getDouble("longitude") ?: DEFAULT_LNG,
        heading = (getDouble("heading") ?: 0.0).toFloat(),
        speedKmh = (getDouble("speedKmh") ?: 0.0).toFloat(),
        driverName = getString("driverName").orEmpty(),
        isTransmitting = getBoolean("isTransmitting") ?: false,
        lastUpdated = getLong("lastUpdated") ?: 0L
    )
}

// ---------- Rutas ----------

internal fun RouteModel.toMap(): Map<String, Any> = mapOf(
    "lineNumber" to lineNumber,
    "name" to name,
    "origin" to origin,
    "destination" to destination,
    "tariffBs" to tariffBs,
    "colorHex" to colorHex,
    "waypoints" to waypoints.map { mapOf("lat" to it.first, "lng" to it.second) },
    "stops" to stops
)

internal fun DocumentSnapshot.toRoute(): RouteModel? {
    val line = getString("lineNumber") ?: return null
    val waypoints = (get("waypoints") as? List<*>).orEmpty().mapNotNull { w ->
        val point = w as? Map<*, *> ?: return@mapNotNull null
        val lat = (point["lat"] as? Number)?.toDouble()
        val lng = (point["lng"] as? Number)?.toDouble()
        if (lat != null && lng != null) lat to lng else null
    }
    return RouteModel(
        id = id,
        lineNumber = line,
        name = getString("name").orEmpty(),
        origin = getString("origin").orEmpty(),
        destination = getString("destination").orEmpty(),
        tariffBs = getDouble("tariffBs") ?: 0.0,
        colorHex = getString("colorHex") ?: "#00F0FF",
        waypoints = waypoints,
        stops = (get("stops") as? List<*>).orEmpty().filterIsInstance<String>()
    )
}

// ---------- Incidentes ----------

internal fun IncidentReport.toMap(): Map<String, Any> = mapOf(
    "userEmail" to userEmail,
    "vehiclePlate" to vehiclePlate,
    "lineNumber" to lineNumber,
    "category" to category.name,
    "description" to description,
    "status" to status.name,
    "timestamp" to timestamp,
    "resolutionNote" to resolutionNote
)

internal fun DocumentSnapshot.toIncident(): IncidentReport = IncidentReport(
    id = id,
    userEmail = getString("userEmail").orEmpty(),
    vehiclePlate = getString("vehiclePlate").orEmpty(),
    lineNumber = getString("lineNumber").orEmpty(),
    category = enumOf(getString("category"), IncidentCategory.COBRO_IRREGULAR),
    description = getString("description").orEmpty(),
    status = enumOf(getString("status"), IncidentStatus.RECIBIDO),
    timestamp = getLong("timestamp") ?: 0L,
    resolutionNote = getString("resolutionNote").orEmpty()
)

// ---------- Infracciones ----------

internal fun InfractionModel.toMap(): Map<String, Any> = mapOf(
    "vehiclePlate" to vehiclePlate,
    "lineNumber" to lineNumber,
    "reason" to reason,
    "fineAmountBs" to fineAmountBs,
    "timestamp" to timestamp,
    "status" to status.name
)

internal fun DocumentSnapshot.toInfraction(): InfractionModel = InfractionModel(
    id = id,
    vehiclePlate = getString("vehiclePlate").orEmpty(),
    lineNumber = getString("lineNumber").orEmpty(),
    reason = getString("reason").orEmpty(),
    fineAmountBs = getDouble("fineAmountBs") ?: 0.0,
    timestamp = getLong("timestamp") ?: 0L,
    status = enumOf(getString("status"), InfractionStatus.EMITIDA)
)

// ---------- Usuarios y logs ----------

internal fun DocumentSnapshot.toUserAccount(): UserAccount = UserAccount(
    uid = id,
    email = getString("email").orEmpty(),
    fullName = getString("fullName").orEmpty(),
    role = UserRole.fromString(getString("role")),
    phone = getString("phone").orEmpty(),
    assignedVehicleId = getString("assignedVehicleId"),
    status = enumOf(getString("status"), AccountStatus.ACTIVO),
    createdAt = getLong("createdAt") ?: 0L
)

internal fun SystemLog.toMap(): Map<String, Any> = mapOf(
    "timestamp" to timestamp,
    "level" to level.name,
    "source" to source,
    "message" to message
)

internal fun DocumentSnapshot.toSystemLog(): SystemLog = SystemLog(
    id = id,
    timestamp = getLong("timestamp") ?: 0L,
    level = enumOf(getString("level"), LogLevel.INFO),
    source = getString("source").orEmpty(),
    message = getString("message").orEmpty()
)