package com.example.smtpmonitoreo.data.model

data class UserAccount(
    val uid: String = "",
    val email: String = "",
    val fullName: String = "",
    val role: UserRole = UserRole.PASAJERO,
    val phone: String = "",
    val status: AccountStatus = AccountStatus.ACTIVO,
    val assignedVehicleId: String? = null,
    val assignedRouteId: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)