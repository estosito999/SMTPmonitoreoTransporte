package com.example.smtpmonitoreo.ui.dashboard

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.ButtonDefaults.outlinedButtonColors
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.smtpmonitoreo.data.model.IncidentReport
import com.example.smtpmonitoreo.data.model.IncidentStatus
import com.example.smtpmonitoreo.domain.DateFormats
import com.example.smtpmonitoreo.ui.components.ActionButton
import com.example.smtpmonitoreo.ui.components.AppTextField
import com.example.smtpmonitoreo.ui.components.FormCard
import com.example.smtpmonitoreo.ui.components.SectionLabel
import com.example.smtpmonitoreo.ui.components.StatusPill
import com.example.smtpmonitoreo.ui.theme.*

// ------------------------------------------------------------------
// TAB 3: RESOLUCIÓN DE INCIDENTES (RF-43)
// ------------------------------------------------------------------
@Composable
internal fun IncidentesTab(
    state: AlcaldiaUiState,
    onFilterSelected: (IncidentStatus?) -> Unit,
    onStartReview: (IncidentReport) -> Unit,
    onAskToResolve: (IncidentReport) -> Unit,
    onNoteChanged: (String) -> Unit,
    onConfirmResolve: () -> Unit,
    onDismissResolve: () -> Unit
) {
    SectionLabel("REPORTES CIUDADANOS (${state.visibleIncidents.size})", CyberRose)
    Spacer(modifier = Modifier.height(10.dp))

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        StatusFilterChip(
            label = "Todos",
            selected = state.incidentFilter == null,
            color = RoleAlcaldiaColor,
            modifier = Modifier.weight(1f),
            onClick = { onFilterSelected(null) }
        )
        IncidentStatus.entries.forEach { status ->
            StatusFilterChip(
                label = status.label,
                selected = state.incidentFilter == status,
                color = incidentColor(status),
                modifier = Modifier.weight(1f),
                onClick = { onFilterSelected(status) }
            )
        }
    }

    Spacer(modifier = Modifier.height(14.dp))

    if (state.visibleIncidents.isEmpty()) {
        Text(
            text = "No hay reportes con este filtro.",
            fontSize = 12.sp,
            color = TextSecondary
        )
    }

    state.visibleIncidents.forEach { incident ->
        IncidentCard(
            incident = incident,
            onStartReview = { onStartReview(incident) },
            onAskToResolve = { onAskToResolve(incident) }
        )
        Spacer(modifier = Modifier.height(10.dp))
    }

    state.resolvingIncident?.let { incident ->
        ResolveDialog(
            incident = incident,
            note = state.resolutionNote,
            onNoteChanged = onNoteChanged,
            onConfirm = onConfirmResolve,
            onDismiss = onDismissResolve
        )
    }
}

@Composable
private fun IncidentCard(
    incident: IncidentReport,
    onStartReview: () -> Unit,
    onAskToResolve: () -> Unit
) {
    val color = incidentColor(incident.status)

    FormCard(borderColor = color.copy(alpha = 0.4f)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "${incident.category.label} | Placa ${incident.vehiclePlate}",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                val line = if (incident.lineNumber.isBlank()) "" else "Línea ${incident.lineNumber} · "
                Text(
                    text = line + DateFormats.dateTime(incident.timestamp),
                    fontSize = 11.sp,
                    color = TextSecondary
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            StatusPill(text = incident.status.label, color = color)
        }

        Spacer(modifier = Modifier.height(8.dp))
        Text(text = incident.description, fontSize = 12.sp, color = TextPrimary)

        if (incident.resolutionNote.isNotBlank()) {
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Resolución: ${incident.resolutionNote}",
                fontSize = 11.sp,
                color = CyberEmerald
            )
        }

        when (incident.status) {
            IncidentStatus.RECIBIDO -> {
                Spacer(modifier = Modifier.height(12.dp))
                ActionButton(
                    text = "TOMAR CASO",
                    color = CyberAmber,
                    contentColor = DarkBackground,
                    onClick = onStartReview
                )
            }

            IncidentStatus.EN_REVISION -> {
                Spacer(modifier = Modifier.height(12.dp))
                ActionButton(
                    text = "RESOLVER",
                    color = CyberEmerald,
                    contentColor = DarkBackground,
                    onClick = onAskToResolve
                )
            }

            IncidentStatus.RESUELTO -> Unit
        }
    }
}

@Composable
private fun ResolveDialog(
    incident: IncidentReport,
    note: String,
    onNoteChanged: (String) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DarkSurface,
        shape = RoundedCornerShape(20.dp),
        title = {
            Text(
                text = "RESOLVER REPORTE",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
        },
        text = {
            Column {
                Text(
                    text = "${incident.category.label} · Placa ${incident.vehiclePlate}",
                    fontSize = 12.sp,
                    color = TextSecondary
                )
                Spacer(modifier = Modifier.height(12.dp))
                AppTextField(
                    value = note,
                    onValueChange = onNoteChanged,
                    label = "Nota de resolución (obligatoria)",
                    accent = CyberEmerald,
                    singleLine = false
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                enabled = note.isNotBlank(),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = CyberEmerald)
            ) {
                Text("RESOLVER", color = DarkBackground, fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                shape = RoundedCornerShape(10.dp),
                colors = outlinedButtonColors(contentColor = TextSecondary)
            ) {
                Text("CANCELAR", fontSize = 12.sp)
            }
        }
    )
}

@Composable
private fun StatusFilterChip(
    label: String,
    selected: Boolean,
    color: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .clickable { onClick() },
        color = if (selected) color.copy(alpha = 0.2f) else DarkSurfaceVariant,
        border = BorderStroke(1.dp, if (selected) color else CyberBorder)
    ) {
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = if (selected) color else TextSecondary,
            textAlign = TextAlign.Center,
            maxLines = 1,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        )
    }
}

private fun incidentColor(status: IncidentStatus): Color = when (status) {
    IncidentStatus.RECIBIDO -> CyberRose
    IncidentStatus.EN_REVISION -> CyberAmber
    IncidentStatus.RESUELTO -> CyberEmerald
}