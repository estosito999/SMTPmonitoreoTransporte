package com.example.smtpmonitoreo.ui.components

import org.osmdroid.util.BoundingBox
import org.osmdroid.util.GeoPoint

/** Área de La Paz (con El Alto y Zona Sur) a la que se limita el mapa. */
object LaPazMap {
    const val NORTH = -16.43
    const val SOUTH = -16.60
    const val WEST = -68.24
    const val EAST = -68.04

    const val MIN_ZOOM = 12.0
    const val MAX_ZOOM = 19.0
    const val DEFAULT_ZOOM = 14.0

    /** Plaza Murillo */
    val center = GeoPoint(-16.4955, -68.1336)
    val bounds = BoundingBox(NORTH, EAST, SOUTH, WEST)
}