package com.example.smtpmonitoreo.data.demo

import com.example.smtpmonitoreo.BuildConfig
import com.example.smtpmonitoreo.data.model.IncidentCategory
import com.example.smtpmonitoreo.data.model.IncidentReport
import com.example.smtpmonitoreo.data.model.IncidentStatus
import com.example.smtpmonitoreo.data.model.InfractionModel
import com.example.smtpmonitoreo.data.model.InfractionStatus
import com.example.smtpmonitoreo.data.model.RouteModel
import com.example.smtpmonitoreo.data.model.TransportUnit
import com.example.smtpmonitoreo.data.repository.resultOf
import com.example.smtpmonitoreo.data.repository.toMap
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class DemoSeeder(private val db: FirebaseFirestore) {

    // Coordenadas emblemáticas de La Paz
    private val perezVelasco = -16.4965 to -68.1375
    private val pradoLaPaz = -16.5020 to -68.1320
    private val sanPedro = -16.5005 to -68.1390
    private val miraflores = -16.5040 to -68.1220
    private val cejaElAlto = -16.5100 to -68.1600
    private val calacoto = -16.5400 to -68.0880

    suspend fun seed(): Result<Unit> {
        if (!BuildConfig.DEMO_MODE) {
            return Result.failure(IllegalStateException("Los datos de ejemplo solo están disponibles en debug."))
        }
        return resultOf {
            val now = System.currentTimeMillis()
            val batch = db.batch()

            routes().forEach { (id, route) ->
                batch.set(db.collection("routes").document(id), route.toMap())
            }
            units(now).forEach { unit ->
                batch.set(db.collection("units").document(unit.plate), unit.toMap())
            }
            incidents(now).forEach { incident ->
                batch.set(db.collection("incidents").document(incident.id), incident.toMap())
            }
            infractions(now).forEach { infraction ->
                batch.set(db.collection("infractions").document(infraction.id), infraction.toMap())
            }

            batch.commit().await()
            Unit
        }
    }

    private fun routes(): List<Pair<String, RouteModel>> = listOf(
        "route_102" to RouteModel(
            lineNumber = "102",
            name = "Línea 102: Ceja El Alto - Pérez Velasco - Sopocachi",
            origin = "Ceja El Alto",
            destination = "Sopocachi",
            tariffBs = 2.50,
            colorHex = "#00F0FF",
            waypoints = listOf(cejaElAlto, perezVelasco, pradoLaPaz, sanPedro),
            stops = listOf("Ceja El Alto", "Pérez Velasco", "El Obelisco", "Plaza Estudiante", "San Pedro")
        ),
        "route_231" to RouteModel(
            lineNumber = "231",
            name = "Línea 231: Periférica - Pérez Velasco - Miraflores",
            origin = "Periférica",
            destination = "Miraflores",
            tariffBs = 2.00,
            colorHex = "#00FF9D",
            waypoints = listOf(perezVelasco, pradoLaPaz, miraflores),
            stops = listOf("Periférica", "Pérez Velasco", "Plaza Murillo", "Hospital del Niño", "Miraflores")
        ),
        "route_trufi45" to RouteModel(
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

    private fun units(now: Long): List<TransportUnit> = listOf(
        TransportUnit(
            plate = "4521-FGH", lineNumber = "102", syndicate = "Sindicato San Cristóbal",
            vehicleType = "Minibús", latitude = perezVelasco.first, longitude = perezVelasco.second,
            heading = 180f, speedKmh = 28f, driverName = "Don Rolando Mamani",
            isTransmitting = false, lastUpdated = now
        ),
        TransportUnit(
            plate = "2184-KLM", lineNumber = "231", syndicate = "Sindicato Pedro Domingo Murillo",
            vehicleType = "Minibús", latitude = pradoLaPaz.first, longitude = pradoLaPaz.second,
            heading = 90f, speedKmh = 32f, driverName = "Juan Carlos Condori",
            isTransmitting = false, lastUpdated = now
        ),
        TransportUnit(
            plate = "3091-XYZ", lineNumber = "Trufi 45", syndicate = "Asociación de Trufis Zona Sur",
            vehicleType = "Trufi", latitude = miraflores.first, longitude = miraflores.second,
            heading = 45f, speedKmh = 40f, driverName = "Gonzalo Flores",
            isTransmitting = false, lastUpdated = now
        )
    )

    private fun incidents(now: Long): List<IncidentReport> = listOf(
        IncidentReport(
            id = "inc_seed_1",
            userEmail = "pasajero.demo@smtp.lapaz.bo",
            vehiclePlate = "4521-FGH",
            lineNumber = "102",
            category = IncidentCategory.COBRO_IRREGULAR,
            description = "Cobro de tarifa nocturna a las 17:30 en Pérez Velasco.",
            status = IncidentStatus.RECIBIDO,
            timestamp = now - 3_600_000L
        ),
        IncidentReport(
            id = "inc_seed_2",
            userEmail = "usuario.lapaz@gmail.com",
            vehiclePlate = "2184-KLM",
            lineNumber = "231",
            category = IncidentCategory.DESVIO_RUTA,
            description = "El minibús no llegó a Miraflores y cortó ruta en la Plaza Uyuni.",
            status = IncidentStatus.EN_REVISION,
            timestamp = now - 7_200_000L
        )
    )

    private fun infractions(now: Long): List<InfractionModel> = listOf(
        InfractionModel(
            id = "inf_seed_1",
            vehiclePlate = "2184-KLM",
            lineNumber = "231",
            reason = "Desvío de ruta arbitrario en zona Miraflores",
            fineAmountBs = 100.0,
            timestamp = now - 86_400_000L,
            status = InfractionStatus.EMITIDA
        )
    )
}