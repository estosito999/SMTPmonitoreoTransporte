package com.example.smtpmonitoreo.domain

object LaPazPlaces {
    private val coordinates: Map<String, Pair<Double, Double>> = linkedMapOf(
        "Pérez Velasco" to (-16.4965 to -68.1375),
        "Ceja El Alto" to (-16.5100 to -68.1600),
        "Plaza Murillo" to (-16.4957 to -68.1336),
        "San Pedro" to (-16.5005 to -68.1390),
        "Sopocachi" to (-16.5115 to -68.1283),
        "Miraflores" to (-16.5040 to -68.1220),
        "Calacoto" to (-16.5400 to -68.0880)
    )

    val names: List<String> = coordinates.keys.toList()

    fun find(name: String): Pair<Double, Double>? = coordinates[name]
}