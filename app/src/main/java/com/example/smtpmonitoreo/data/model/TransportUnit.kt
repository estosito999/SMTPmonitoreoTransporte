package com.example.smtpmonitoreo.data.model

data class TransportUnit(
    val id: String = "",
    val plate: String = "",
    val lineNumber: String = "",
    val syndicate: String = "",
    val vehicleType: String = "Minibús",
    val latitude: Double = -16.5000,
    val longitude: Double = -68.1500,
    val heading: Float = 0f,
    val speedKmh: Float = 25f,
    val driverName: String = "",
    val isTransmitting: Boolean = true,
    val lastUpdated: Long = System.currentTimeMillis()
)
