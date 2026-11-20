package com.example.smtpmonitoreo.domain

import com.example.smtpmonitoreo.data.model.TransportUnit

sealed interface UnitLinkResult {
    data class Linked(val unit: TransportUnit) : UnitLinkResult
    data class NotRegistered(val plate: String) : UnitLinkResult
    data object InvalidPlate : UnitLinkResult
}

object UnitLinker {

    /** Valida el formato de la placa y la busca entre las unidades registradas. */
    fun resolve(plateInput: String, units: List<TransportUnit>): UnitLinkResult {
        val plate = PlateValidator.normalize(plateInput) ?: return UnitLinkResult.InvalidPlate
        val unit = units.find { it.plate == plate } ?: return UnitLinkResult.NotRegistered(plate)
        return UnitLinkResult.Linked(unit)
    }
}