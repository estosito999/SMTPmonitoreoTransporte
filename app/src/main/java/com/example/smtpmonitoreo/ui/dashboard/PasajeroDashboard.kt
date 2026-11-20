package com.example.smtpmonitoreo.ui.dashboard

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.smtpmonitoreo.data.model.IncidentCategory
import com.example.smtpmonitoreo.data.model.IncidentReport
import com.example.smtpmonitoreo.data.model.IncidentStatus
import com.example.smtpmonitoreo.data.model.TransportUnit
import com.example.smtpmonitoreo.data.model.UserAccount
import com.example.smtpmonitoreo.data.model.UserRole
import com.example.smtpmonitoreo.domain.LaPazPlaces
import com.example.smtpmonitoreo.ui.components.DashboardTopBar
import com.example.smtpmonitoreo.ui.components.DropdownSelector
import com.example.smtpmonitoreo.ui.components.NetworkStatusBanner
import com.example.smtpmonitoreo.ui.components.SegmentedTabItem
import com.example.smtpmonitoreo.ui.components.SmtpMapView
import com.example.smtpmonitoreo.ui.theme.*
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun PasajeroDashboard(
    user: UserAccount,
    onLogout: () -> Unit,
    viewModel: PasajeroViewModel = koinViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        DashboardTopBar(
            user = user,
            role = UserRole.PASAJERO,
            onLogout = onLogout
        )

        // Banner de Conectividad (RNF-18) — se hace real en el bloque de conectividad
        NetworkStatusBanner(isConnected = true, isReconnecting = false)

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 10.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(DarkSurfaceVariant)
                .padding(4.dp)
        ) {
            SegmentedTabItem(
                text = "Mapa en Vivo",
                icon = Icons.Default.Map,
                selected = state.tab == PasajeroTab.MAPA,
                accentColor = CyberCyan,
                modifier = Modifier.weight(1f),
                onClick = { viewModel.onTabSelected(PasajeroTab.MAPA) }
            )
            SegmentedTabItem(
                text = "Buscar Ruta",
                icon = Icons.Default.Search,
                selected = state.tab == PasajeroTab.RUTAS,
                accentColor = CyberCyan,
                modifier = Modifier.weight(1f),
                onClick = { viewModel.onTabSelected(PasajeroTab.RUTAS) }
            )
            SegmentedTabItem(
                text = "Reportar",
                icon = Icons.Default.ReportProblem,
                selected = state.tab == PasajeroTab.REPORTAR,
                accentColor = CyberCyan,
                modifier = Modifier.weight(1f),
                onClick = { viewModel.onTabSelected(PasajeroTab.REPORTAR) }
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .navigationBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 10.dp)
        ) {
            when (state.tab) {
                PasajeroTab.MAPA -> MapaTab(
                    state = state,
                    onUnitSelected = viewModel::onUnitSelected,
                    onReportUnit = viewModel::reportSelectedUnit
                )

                PasajeroTab.RUTAS -> RutasTab(
                    state = state,
                    onOriginSelected = viewModel::onOriginSelected,
                    onDestinationSelected = viewModel::onDestinationSelected,
                    onSearch = viewModel::searchRoute
                )

                PasajeroTab.REPORTAR -> ReportarTab(
                    state = state,
                    onCategorySelected = viewModel::onCategorySelected,
                    onPlateChanged = viewModel::onPlateChanged,
                    onDescriptionChanged = viewModel::onDescriptionChanged,
                    onSubmit = viewModel::submitReport
                )
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

// ------------------------------------------------------------------
// TAB 1: MAPA EN VIVO Y ETA (RF-10, RF-14)
// ------------------------------------------------------------------
@Composable
private fun MapaTab(
    state: PasajeroUiState,
    onUnitSelected: (TransportUnit) -> Unit,
    onReportUnit: () -> Unit
) {
    SectionLabel("MONITOREO EN TIEMPO REAL · LA PAZ", CyberCyan)
    Spacer(modifier = Modifier.height(10.dp))

    SmtpMapView(
        units = state.units,
        routes = state.routes,
        selectedUnit = state.selectedUnit,
        onUnitSelected = onUnitSelected,
        modifier = Modifier
            .fillMaxWidth()
            .height(260.dp)
    )

    Spacer(modifier = Modifier.height(16.dp))

    val unit = state.selectedUnit
    if (unit == null) {
        Text(
            text = "No hay unidades transmitiendo en este momento.",
            fontSize = 12.sp,
            color = TextSecondary
        )
    } else {
        UnitCard(
            unit = unit,
            etaMinutes = state.etaMinutes,
            stopName = state.origin,
            onReport = onReportUnit
        )
    }
}

@Composable
private fun UnitCard(
    unit: TransportUnit,
    etaMinutes: Int?,
    stopName: String,
    onReport: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, CyberCyan.copy(alpha = 0.5f), RoundedCornerShape(18.dp)),
        shape = RoundedCornerShape(18.dp),
        color = DarkSurface
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(CyberCyan.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.DirectionsBus,
                            contentDescription = "Unidad",
                            tint = CyberCyan,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "LÍNEA ${unit.lineNumber} | ${unit.plate}",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = "${unit.vehicleType} · ${unit.syndicate}",
                            fontSize = 11.sp,
                            color = TextSecondary
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                // ETA hacia tu parada (el origen elegido en "Buscar Ruta") — RF-14
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = CyberCyan.copy(alpha = 0.15f),
                    border = BorderStroke(1.dp, CyberCyan)
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "ETA · ${stopName.uppercase()}",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = CyberCyan,
                            maxLines = 1
                        )
                        Text(
                            text = etaMinutes?.let { "$it min" } ?: "—",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = TextPrimary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = CyberBorder)
            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Chofer: ${unit.driverName}",
                    fontSize = 12.sp,
                    color = TextSecondary
                )
                Text(
                    text = "Velocidad: ${unit.speedKmh.toInt()} km/h",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = CyberEmerald
                )
            }

            TextButton(onClick = onReport) {
                Icon(
                    imageVector = Icons.Default.ReportProblem,
                    contentDescription = null,
                    tint = CyberRose,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text("Reportar esta unidad", color = CyberRose, fontSize = 12.sp)
            }
        }
    }
}

// ------------------------------------------------------------------
// TAB 2: BÚSQUEDA DE RUTAS POR ORIGEN Y DESTINO (RF-13)
// ------------------------------------------------------------------
@Composable
private fun RutasTab(
    state: PasajeroUiState,
    onOriginSelected: (String) -> Unit,
    onDestinationSelected: (String) -> Unit,
    onSearch: () -> Unit
) {
    SectionLabel("BUSCADOR DE RUTAS PACEÑAS", CyberCyan)
    Spacer(modifier = Modifier.height(12.dp))

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, CyberBorder, RoundedCornerShape(18.dp)),
        shape = RoundedCornerShape(18.dp),
        color = DarkSurface
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Punto de Origen (tu parada)", fontSize = 12.sp, color = TextSecondary)
            Spacer(modifier = Modifier.height(6.dp))
            DropdownSelector(
                value = state.origin,
                options = LaPazPlaces.names,
                onSelect = onOriginSelected
            )

            Spacer(modifier = Modifier.height(14.dp))

            Text("Punto de Destino", fontSize = 12.sp, color = TextSecondary)
            Spacer(modifier = Modifier.height(6.dp))
            DropdownSelector(
                value = state.destination,
                options = LaPazPlaces.names,
                onSelect = onDestinationSelected
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = onSearch,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = CyberCyan)
            ) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Buscar",
                    tint = DarkBackground,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "ENCONTRAR MINIBÚS / RUTA",
                    color = DarkBackground,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
            }
        }
    }

    Spacer(modifier = Modifier.height(20.dp))

    state.searchMessage?.let { message ->
        Text(
            text = message,
            fontSize = 12.sp,
            color = CyberAmber,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )
    }

    state.searchResult?.let { result ->
        SectionLabel("RUTA RECOMENDADA", CyberEmerald)
        Spacer(modifier = Modifier.height(8.dp))

        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, CyberEmerald.copy(alpha = 0.5f), RoundedCornerShape(16.dp)),
            shape = RoundedCornerShape(16.dp),
            color = DarkSurface
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = result.route.name,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary,
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Bs ${"%.2f".format(result.route.tariffBs)}",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = CyberEmerald
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "Tu recorrido (${result.stopsInOrder.size} paradas): " +
                            result.stopsInOrder.joinToString(" ➔ "),
                    fontSize = 12.sp,
                    color = TextSecondary
                )
            }
        }
    }
}

// ------------------------------------------------------------------
// TAB 3: REPORTAR INCIDENTE (RF-18, RF-19)
// ------------------------------------------------------------------
@Composable
private fun ReportarTab(
    state: PasajeroUiState,
    onCategorySelected: (IncidentCategory) -> Unit,
    onPlateChanged: (String) -> Unit,
    onDescriptionChanged: (String) -> Unit,
    onSubmit: () -> Unit
) {
    SectionLabel("FORMULARIO DE DENUNCIA E INCIDENTES", CyberRose)
    Spacer(modifier = Modifier.height(12.dp))

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, CyberRose.copy(alpha = 0.4f), RoundedCornerShape(18.dp)),
        shape = RoundedCornerShape(18.dp),
        color = DarkSurface
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Tipo de Incidente", fontSize = 12.sp, color = TextSecondary)
            Spacer(modifier = Modifier.height(6.dp))
            DropdownSelector(
                value = state.reportCategory.label,
                options = IncidentCategory.entries.map { it.label },
                onSelect = { label ->
                    IncidentCategory.entries.firstOrNull { it.label == label }
                        ?.let(onCategorySelected)
                }
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = state.reportPlate,
                onValueChange = onPlateChanged,
                label = { Text("Placa del Minibús (Ej: 4521-FGH)") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = CyberRose,
                    unfocusedBorderColor = CyberBorder
                )
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = state.reportDescription,
                onValueChange = onDescriptionChanged,
                label = { Text("Descripción del incidente") },
                maxLines = 4,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(110.dp),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = CyberRose,
                    unfocusedBorderColor = CyberBorder
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = onSubmit,
                enabled = !state.isSending,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = CyberRose)
            ) {
                if (state.isSending) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                        color = TextPrimary
                    )
                } else {
                    Text(
                        text = "ENVIAR DENUNCIA A LA ALCALDÍA",
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }
            }

            state.reportError?.let { error ->
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = error,
                    fontSize = 12.sp,
                    color = CyberRose,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            if (state.reportSent) {
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "¡Reporte enviado exitosamente a la Alcaldía!",
                    fontSize = 12.sp,
                    color = CyberEmerald,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }

    Spacer(modifier = Modifier.height(20.dp))

    // Historial de reportes del usuario (RF-19)
    SectionLabel("MIS REPORTES (${state.myIncidents.size})", TextSecondary)
    Spacer(modifier = Modifier.height(10.dp))

    if (state.myIncidents.isEmpty()) {
        Text(
            text = "Aún no has enviado reportes.",
            fontSize = 12.sp,
            color = TextSecondary
        )
    } else {
        state.myIncidents.forEach { incident -> IncidentRow(incident) }
    }
}

@Composable
private fun IncidentRow(incident: IncidentReport) {
    val statusColor = incidentStatusColor(incident.status)

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .border(1.dp, CyberBorder, RoundedCornerShape(14.dp)),
        shape = RoundedCornerShape(14.dp),
        color = DarkSurface
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "${incident.category.label} | Placa: ${incident.vehiclePlate}",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Text(
                    text = incident.description,
                    fontSize = 11.sp,
                    color = TextSecondary,
                    maxLines = 1
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = statusColor.copy(alpha = 0.2f)
            ) {
                Text(
                    text = incident.status.label,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = statusColor,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }
    }
}

private fun incidentStatusColor(status: IncidentStatus): Color = when (status) {
    IncidentStatus.RESUELTO -> CyberEmerald
    IncidentStatus.EN_REVISION -> CyberAmber
    IncidentStatus.RECIBIDO -> CyberRose
}

@Composable
private fun SectionLabel(text: String, color: Color) {
    Text(
        text = text,
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        color = color,
        letterSpacing = 1.2.sp
    )
}