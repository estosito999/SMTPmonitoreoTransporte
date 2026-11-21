
package com.example.smtpmonitoreo.ui.dashboard

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.smtpmonitoreo.domain.RouteDraft
import com.example.smtpmonitoreo.domain.UnitDraft
import com.example.smtpmonitoreo.domain.VehicleTypes
import com.example.smtpmonitoreo.ui.components.ActionButton
import com.example.smtpmonitoreo.ui.components.AppTextField
import com.example.smtpmonitoreo.ui.components.DropdownSelector
import com.example.smtpmonitoreo.ui.components.FormCard
import com.example.smtpmonitoreo.ui.components.FormError
import com.example.smtpmonitoreo.ui.components.ListCard
import com.example.smtpmonitoreo.ui.components.SectionLabel
import com.example.smtpmonitoreo.ui.components.StatusPill
import com.example.smtpmonitoreo.ui.theme.*
import com.example.smtpmonitoreo.domain.LaPazPlaces

// ------------------------------------------------------------------
// TAB 2: GESTIÓN DE RUTAS, UNIDADES Y CHOFERES (RF-40, RF-41)
// ------------------------------------------------------------------
@Composable
internal fun GestionTab(
    state: AlcaldiaUiState,
    onRouteDraftChanged: (RouteDraft) -> Unit,
    onSaveRoute: () -> Unit,
    onUnitDraftChanged: (UnitDraft) -> Unit,
    onSaveUnit: () -> Unit
) {
    RouteSection(state, onRouteDraftChanged, onSaveRoute)
    Spacer(modifier = Modifier.height(28.dp))
    UnitSection(state, onUnitDraftChanged, onSaveUnit)
}

@Composable
private fun RouteSection(
    state: AlcaldiaUiState,
    onChange: (RouteDraft) -> Unit,
    onSave: () -> Unit
) {
    val d = state.routeDraft

    SectionLabel("REGISTRAR NUEVA RUTA OFICIAL", CyberCyan)
    Spacer(modifier = Modifier.height(10.dp))

    FormCard {
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            AppTextField(
                value = d.lineNumber,
                onValueChange = { onChange(d.copy(lineNumber = it)) },
                label = "Número Línea",
                modifier = Modifier.weight(1f)
            )
            AppTextField(
                value = d.tariff,
                onValueChange = { onChange(d.copy(tariff = it)) },
                label = "Tarifa Bs",
                keyboardType = KeyboardType.Decimal,
                modifier = Modifier.weight(1f)
            )
        }
        Spacer(modifier = Modifier.height(10.dp))
        AppTextField(
            value = d.name,
            onValueChange = { onChange(d.copy(name = it)) },
            label = "Nombre (Ej: Línea 102 - San Pedro)"
        )
        Spacer(modifier = Modifier.height(10.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            AppTextField(
                value = d.origin,
                onValueChange = { onChange(d.copy(origin = it)) },
                label = "Origen",
                modifier = Modifier.weight(1f)
            )
            AppTextField(
                value = d.destination,
                onValueChange = { onChange(d.copy(destination = it)) },
                label = "Destino",
                modifier = Modifier.weight(1f)
            )
        }
        Spacer(modifier = Modifier.height(10.dp))
        AppTextField(
            value = d.stops,
            onValueChange = { onChange(d.copy(stops = it)) },
            label = "Paradas intermedias (separadas por coma)"
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "El trazado en el mapa usa los lugares conocidos: ${LaPazPlaces.names.joinToString(", ")}.",
            fontSize = 11.sp,
            color = TextSecondary
        )
        FormError(state.routeError)
        Spacer(modifier = Modifier.height(14.dp))
        ActionButton(
            text = "GUARDAR RUTA MUNICIPAL",
            color = CyberCyan,
            contentColor = DarkBackground,
            onClick = onSave
        )
    }

    Spacer(modifier = Modifier.height(20.dp))

    SectionLabel("RUTAS MUNICIPALES VIGENTES (${state.routes.size})")
    Spacer(modifier = Modifier.height(10.dp))

    state.routes.forEach { route ->
        ListCard {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = route.name,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Text(
                    text = "${route.origin} ➔ ${route.destination} · ${route.stops.size} paradas" +
                            if (route.waypoints.size < 2) " · sin trazado" else "",
                    fontSize = 11.sp,
                    color = TextSecondary
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            StatusPill(text = "Bs ${"%.2f".format(route.tariffBs)}", color = CyberEmerald)
        }
    }
}

@Composable
private fun UnitSection(
    state: AlcaldiaUiState,
    onChange: (UnitDraft) -> Unit,
    onSave: () -> Unit
) {
    val d = state.unitDraft

    SectionLabel("REGISTRAR UNIDAD Y CHOFER", CyberEmerald)
    Spacer(modifier = Modifier.height(10.dp))

    FormCard {
        AppTextField(
            value = d.plate,
            onValueChange = { onChange(d.copy(plate = it.uppercase().take(10))) },
            label = "Placa (Ej: 4521-FGH)",
            accent = CyberEmerald
        )
        Spacer(modifier = Modifier.height(10.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Column(modifier = Modifier.weight(1f)) {
                Text("Línea", fontSize = 12.sp, color = TextSecondary)
                Spacer(modifier = Modifier.height(6.dp))
                DropdownSelector(
                    value = d.lineNumber.ifBlank { "Seleccionar" },
                    options = state.routes.map { it.lineNumber },
                    onSelect = { onChange(d.copy(lineNumber = it)) }
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text("Tipo de vehículo", fontSize = 12.sp, color = TextSecondary)
                Spacer(modifier = Modifier.height(6.dp))
                DropdownSelector(
                    value = d.vehicleType,
                    options = VehicleTypes.all,
                    onSelect = { onChange(d.copy(vehicleType = it)) }
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))
        AppTextField(
            value = d.syndicate,
            onValueChange = { onChange(d.copy(syndicate = it)) },
            label = "Sindicato o asociación",
            accent = CyberEmerald
        )
        Spacer(modifier = Modifier.height(10.dp))
        AppTextField(
            value = d.driverName,
            onValueChange = { onChange(d.copy(driverName = it)) },
            label = "Nombre del chofer",
            accent = CyberEmerald
        )
        FormError(state.unitError)
        Spacer(modifier = Modifier.height(14.dp))
        ActionButton(
            text = "REGISTRAR UNIDAD",
            color = CyberEmerald,
            contentColor = DarkBackground,
            onClick = onSave
        )
    }

    Spacer(modifier = Modifier.height(20.dp))

    SectionLabel("UNIDADES REGISTRADAS (${state.units.size})")
    Spacer(modifier = Modifier.height(10.dp))

    state.units.sortedBy { it.plate }.forEach { unit ->
        ListCard {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "${unit.plate} | Línea ${unit.lineNumber}",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Text(
                    text = "${unit.vehicleType} · ${unit.driverName} · ${unit.syndicate}",
                    fontSize = 11.sp,
                    color = TextSecondary,
                    maxLines = 1
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            if (unit.isTransmitting) {
                StatusPill(text = "EN RUTA", color = CyberEmerald)
            } else {
                StatusPill(text = "INACTIVA", color = TextSecondary)
            }
        }
    }
}
