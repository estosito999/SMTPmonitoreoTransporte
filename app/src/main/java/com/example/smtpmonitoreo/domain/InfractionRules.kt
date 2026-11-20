package com.example.smtpmonitoreo.domain

import com.example.smtpmonitoreo.data.model.InfractionModel
import com.example.smtpmonitoreo.data.model.InfractionStatus
import com.example.smtpmonitoreo.data.model.TransportUnit

object InfractionReasons {
    val all = listOf(
        "Desvío de ruta arbitrario",
        "Cobro irregular de pasaje",
        "Exceso de pasajeros",
        "Incumplimiento de frecuencia",
        "Maltrato al pasajero",
        "Unidad no autorizada"
    )
}

data class InfractionDraft(
    val plate: String = "",
    val reason: String = InfractionReasons.all.first(),
    val amount: String = "100"
)

object InfractionRules {
    private const val MIN_FINE_BS = 1.0
    private const val MAX_FINE_BS = 5000.0

    /** La línea se toma de la unidad registrada; la fecha es la del momento de emisión. */
    fun validate(draft: InfractionDraft, units: List<TransportUnit>, now: Long): Validation<InfractionModel> {
        val plate = PlateValidator.normalize(draft.plate)
            ?: return Validation.Error("Placa inválida. Usa el formato 1234-ABC.")
        val unit = units.find { it.plate == plate }
            ?: return Validation.Error("La placa $plate no está registrada.")

        val amount = parseDecimal(draft.amount)
        if (amount == null || amount < MIN_FINE_BS || amount > MAX_FINE_BS) {
            return Validation.Error("Monto inválido (entre 1 y 5000 Bs).")
        }

        return Validation.Ok(
            InfractionModel(
                vehiclePlate = plate,
                lineNumber = unit.lineNumber,
                reason = draft.reason,
                fineAmountBs = amount,
                timestamp = now,
                status = InfractionStatus.EMITIDA
            )
        )
    }
}