package com.example.smtpmonitoreo.ui.dashboard

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsBus
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.ReportProblem
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.smtpmonitoreo.BuildConfig
import com.example.smtpmonitoreo.data.model.LogLevel
import com.example.smtpmonitoreo.data.model.SystemLog
import com.example.smtpmonitoreo.data.model.UserRole
import com.example.smtpmonitoreo.domain.DateFormats
import com.example.smtpmonitoreo.ui.components.CountBars
import com.example.smtpmonitoreo.ui.components.FilterPill
import com.example.smtpmonitoreo.ui.components.FormCard
import com.example.smtpmonitoreo.ui.components.KpiCard
import com.example.smtpmonitoreo.ui.components.ListCard
import com.example.smtpmonitoreo.ui.components.SectionLabel
import com.example.smtpmonitoreo.ui.components.StatusPill
import com.example.smtpmonitoreo.ui.theme.*
import com.example.smtpmonitoreo.ui.components.ActionButton

// ------------------------------------------------------------------
// TAB 2: MONITOREO TÉCNICO Y LOGS
// ------------------------------------------------------------------
@Composable
internal fun SistemaTab(
    state: AdminUiState,
    onLogFilterSelected: (LogLevel?) -> Unit,
    onSwitchRolePreview: (UserRole) -> Unit,
    onSeedDemoData: () -> Unit
) {
    val health = state.health

    SectionLabel("ESTADO DEL SISTEMA", RoleAdminColor)
    Spacer(modifier = Modifier.height(10.dp))

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        KpiCard(
            title = "Usuarios Activos",
            value = "${health.activeUsers}/${health.totalUsers}",
            icon = Icons.Default.People,
            accentColor = RoleAdminColor,
            modifier = Modifier.weight(1f)
        )
        KpiCard(
            title = "Unidades en Ruta",
            value = "${health.unitsTransmitting}/${health.totalUnits}",
            icon = Icons.Default.DirectionsBus,
            accentColor = CyberEmerald,
            modifier = Modifier.weight(1f)
        )
        KpiCard(
            title = "Reportes Abiertos",
            value = "${health.openIncidents}",
            icon = Icons.Default.ReportProblem,
            accentColor = CyberRose,
            modifier = Modifier.weight(1f)
        )
    }

    Spacer(modifier = Modifier.height(8.dp))
    Text(
        text = "Cuentas suspendidas: ${health.suspendedUsers} · Errores en el registro: ${health.errorLogs}",
        fontSize = 11.sp,
        color = TextSecondary
    )

    Spacer(modifier = Modifier.height(20.dp))

    SectionLabel("CUENTAS POR ROL", RoleAdminColor)
    Spacer(modifier = Modifier.height(8.dp))
    FormCard { CountBars(items = health.usersByRole, color = RoleAdminColor) }
    Spacer(modifier = Modifier.height(20.dp))

    SectionLabel("REGISTRO DE EVENTOS (LOGS)", CyberCyan)
    Spacer(modifier = Modifier.height(10.dp))

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        FilterPill(
            label = "Todos",
            selected = state.logFilter == null,
            color = RoleAdminColor,
            modifier = Modifier.weight(1f),
            onClick = { onLogFilterSelected(null) }
        )
        LogLevel.entries.forEach { level ->
            FilterPill(
                label = level.label,
                selected = state.logFilter == level,
                color = logColor(level),
                modifier = Modifier.weight(1f),
                onClick = { onLogFilterSelected(level) }
            )
        }
    }

    Spacer(modifier = Modifier.height(10.dp))

    if (state.visibleLogs.isEmpty()) {
        Text(
            text = "No hay eventos con este filtro.",
            fontSize = 12.sp,
            color = TextSecondary,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
    }

    state.visibleLogs.forEach { log -> LogRow(log) }
    Spacer(modifier = Modifier.height(6.dp))
    Text(
        text = "Cada cambio de rol o suspensión que hace un administrador se guarda como evento " +
                "de auditoría en Firestore (colección logs).",
        fontSize = 10.sp,
        color = TextMuted
    )

    // Solo en builds de depuración
    if (BuildConfig.DEMO_MODE) {
        Spacer(modifier = Modifier.height(24.dp))
        SectionLabel("HERRAMIENTAS DE DEPURACIÓN (SOLO DEBUG)")
        Spacer(modifier = Modifier.height(10.dp))

        ActionButton(
            text = "CARGAR DATOS DE EJEMPLO EN FIRESTORE",
            color = RoleAdminColor,
            contentColor = DarkBackground,
            onClick = onSeedDemoData
        )
        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf(UserRole.PASAJERO, UserRole.CHOFER, UserRole.ALCALDIA).forEach { role ->
                PreviewChip(
                    role = role,
                    modifier = Modifier.weight(1f),
                    onClick = { onSwitchRolePreview(role) }
                )
            }
        }
    }
}

@Composable
private fun LogRow(log: SystemLog) {
    ListCard {
        androidx.compose.foundation.layout.Column(modifier = Modifier.weight(1f)) {
            Text(
                text = log.message,
                fontSize = 12.sp,
                color = TextPrimary
            )
            Text(
                text = "${log.source} · ${DateFormats.dateTime(log.timestamp)}",
                fontSize = 10.sp,
                color = TextSecondary
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
        StatusPill(text = log.level.label, color = logColor(log.level))
    }
}

@Composable
private fun PreviewChip(
    role: UserRole,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .clickable { onClick() },
        color = DarkSurfaceVariant,
        border = BorderStroke(1.dp, role.accentColor.copy(alpha = 0.5f))
    ) {
        Text(
            text = role.displayName,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = role.accentColor,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp)
        )
    }
}

private fun logColor(level: LogLevel): Color = when (level) {
    LogLevel.INFO -> CyberCyan
    LogLevel.WARN -> CyberAmber
    LogLevel.ERROR -> CyberRose
}