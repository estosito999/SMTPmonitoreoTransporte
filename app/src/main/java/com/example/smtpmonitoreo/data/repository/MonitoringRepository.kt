package com.example.smtpmonitoreo.data.repository

import com.example.smtpmonitoreo.data.model.IncidentReport
import com.example.smtpmonitoreo.data.model.IncidentStatus
import com.example.smtpmonitoreo.data.model.InfractionModel
import com.example.smtpmonitoreo.data.model.InfractionStatus
import com.example.smtpmonitoreo.data.model.RouteModel
import com.example.smtpmonitoreo.data.model.TransportUnit
import kotlinx.coroutines.flow.StateFlow

interface MonitoringRepository {
    val activeUnitsFlow: StateFlow<List<TransportUnit>>
    val routesFlow: StateFlow<List<RouteModel>>
    val incidentsFlow: StateFlow<List<IncidentReport>>
    val infractionsFlow: StateFlow<List<InfractionModel>>

    suspend fun startGpsTransmission(plate: String)
    suspend fun stopGpsTransmission(plate: String)
    suspend fun updateVehicleLocation(plate: String, lat: Double, lng: Double, speedKmh: Float, heading: Float)
    suspend fun submitIncident(report: IncidentReport): Result<WriteStatus>
    suspend fun updateIncidentStatus(reportId: String, newStatus: IncidentStatus, note: String = ""): Result<Unit>
    suspend fun addInfraction(infraction: InfractionModel): Result<Unit>
    suspend fun updateInfractionStatus(infractionId: String, newStatus: InfractionStatus): Result<Unit>
    suspend fun addRoute(route: RouteModel): Result<Unit>
    suspend fun addUnit(unit: TransportUnit): Result<Unit>
}