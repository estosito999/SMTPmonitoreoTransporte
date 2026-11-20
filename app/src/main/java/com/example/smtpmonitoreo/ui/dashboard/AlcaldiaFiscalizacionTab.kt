package com.example.smtpmonitoreo.ui.dashboard

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.smtpmonitoreo.data.model.InfractionModel
import com.example.smtpmonitoreo.data.model.InfractionStatus
import com.example.smtpmonitoreo.domain.DateFormats
import com.example.smtpmonitoreo.domain.InfractionDraft
import com.example.smtpmonitoreo.domain.InfractionReasons
import com.example.smtpmonitoreo.ui.components.ActionButton
import com.example.smtpmonitoreo.ui.components.AppTextField
import com.example.smtpmonitoreo.ui.components.DropdownSelector
import com.example.smtpmonitoreo.ui.components.FormCard
import com.example.smtpmonitoreo.ui.components.FormError
import com.example.smtpmonitoreo.ui.components.ListCard
import com.example.smtpmonitoreo.ui.components.SectionLabel
import com.example.smtpmonitoreo.ui.components.StatusPill
import com.example.smtpmonitoreo.ui.theme.*

// ------------------------------------------------------------------
// TAB 4: FISCALIZACIÓN Y CONTROL DE INFRACCIONES (RF-44)
// ------------------------------------------------------------------
@Composable
internal fun FiscalizacionTab(
    state: AlcaldiaUiState,
    onDraftChanged: (InfractionDraft) -> Unit,
    onSave: () -> Unit,
    onMarkPaid: (InfractionModel) -> Unit
) {
    val d = state.infractionDraft

    SectionLabel("EMITIR INFRACCIÓN VEHICULAR", CyberRose)
    Spacer(modifier = Modifier.height(10.dp))

    FormCard(borderColor = CyberRose.copy(alpha = 0.4f)) {
        AppTextField(
            value = d.plate,
            onValueChange = { onDraftChanged(d.copy(plate = it.uppercase().take(10))) },
            label = "Placa del vehículo (Ej: 4521-FGH)",
            accent = CyberRose
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "La línea se toma de la unidad registrada.",
            fontSize = 11.sp,
            color = TextSecondary
        )

        Spacer(modifier = Modifier.height(10.dp))
        Text("Motivo de la infracción", fontSize = 12.sp, color = TextSecondary)
        Spacer(modifier = Modifier.height(6.dp))
        DropdownSelector(
            value = d.reason,
            options = InfractionReasons.all,
            onSelect = { onDraftChanged(d.copy(reason = it)) }
        )

        Spacer(modifier = Modifier.height(10.dp))
        AppTextField(
            value = d.amount,
            onValueChange = { onDraftChanged(d.copy(amount = it)) },
            label = "Monto de la multa (Bs)",
            accent = CyberRose,
            keyboardType = KeyboardType.Decimal
        )

        FormError(state.infractionError)
        Spacer(modifier = Modifier.height(14.dp))
        ActionButton(
            text = "EMITIR SANCIÓN MUNICIPAL",
            color = CyberRose,
            contentColor = TextPrimary,
            onClick = onSave
        )
    }

    Spacer(modifier = Modifier.height(20.dp))

    SectionLabel("INFRACCIONES REGISTRADAS (${state.infractions.size})")
    Spacer(modifier = Modifier.height(10.dp))

    if (state.infractions.isEmpty()) {
        Text(text = "Aún no hay infracciones registradas.", fontSize = 12.sp, color = TextSecondary)
    }

    state.sortedInfractions.forEach { infraction ->
        ListCard {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Placa ${infraction.vehiclePlate} | Línea ${infraction.lineNumber}",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Text(text = infraction.reason, fontSize = 11.sp, color = TextSecondary)
                Text(
                    text = DateFormats.date(infraction.timestamp),
                    fontSize = 11.sp,
                    color = TextSecondary
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "Bs ${"%.2f".format(infraction.fineAmountBs)}",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = CyberRose
                )
                StatusPill(text = infraction.status.label, color = infractionColor(infraction.status))
                if (infraction.status == InfractionStatus.EMITIDA) {
                    TextButton(onClick = { onMarkPaid(infraction) }) {
                        Text("Marcar pagada", fontSize = 11.sp, color = CyberEmerald)
                    }
                }
            }
        }
    }
}

private fun infractionColor(status: InfractionStatus): Color = when (status) {
    InfractionStatus.EMITIDA -> CyberAmber
    InfractionStatus.PAGADA -> CyberEmerald
    InfractionStatus.APELADA -> CyberViolet
}