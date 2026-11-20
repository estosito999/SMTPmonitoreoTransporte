package com.example.smtpmonitoreo.domain

import com.example.smtpmonitoreo.data.model.RouteModel
import com.example.smtpmonitoreo.data.model.TransportUnit

object VehicleTypes {
    val all = listOf("Minibús", "Micro", "Trufi")
}

data class UnitDraft(
    val plate: String = "",
    val lineNumber: String = "",
    val syndicate: String = "",
    val vehicleType: String = VehicleTypes.all.first(),
    val driverName: String = ""
)

object UnitRegistration {

    fun validate(
        draft: UnitDraft,
        existing: List<TransportUnit>,
        routes: List<RouteModel>
    ): Validation<TransportUnit> {
        val plate = PlateValidator.normalize(draft.plate)
            ?: return Validation.Error("Placa inválida. Usa el formato 1234-ABC.")
        if (existing.any { it.plate == plate }) {
            return Validation.Error("La placa $plate ya está registrada.")
        }
        if (routes.none { it.lineNumber == draft.lineNumber }) {
            return Validation.Error("Selecciona una línea con ruta oficial.")
        }
        if (draft.syndicate.isBlank()) return Validation.Error("Ingresa el sindicato o asociación.")
        if (draft.driverName.isBlank()) return Validation.Error("Ingresa el nombre del chofer.")

        return Validation.Ok(
            TransportUnit(
                plate = plate,
                lineNumber = draft.lineNumber,
                syndicate = draft.syndicate.trim(),
                vehicleType = draft.vehicleType,
                driverName = draft.driverName.trim(),
                isTransmitting = false // recién registrada: aún no transmite
            )
        )
    }
}