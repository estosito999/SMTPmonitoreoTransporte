package com.example.smtpmonitoreo.data.model

data class RouteModel(
    val id: String = "",
    val lineNumber: String = "",
    val name: String = "",
    val origin: String = "",
    val destination: String = "",
    val tariffBs: Double = 2.50,
    val colorHex: String = "#00F0FF",
    val waypoints: List<Pair<Double, Double>> = emptyList(),
    val stops: List<String> = emptyList()
)
