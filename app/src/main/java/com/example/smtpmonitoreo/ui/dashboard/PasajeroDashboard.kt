package com.example.smtpmonitoreo.ui.dashboard

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.smtpmonitoreo.data.model.IncidentReport
import com.example.smtpmonitoreo.data.model.RouteModel
import com.example.smtpmonitoreo.data.model.TransportUnit
import com.example.smtpmonitoreo.data.model.UserAccount
import com.example.smtpmonitoreo.data.model.UserRole
import com.example.smtpmonitoreo.data.repository.MonitoringRepository
import com.example.smtpmonitoreo.data.repository.MonitoringRepositoryImpl
import com.example.smtpmonitoreo.ui.components.DashboardTopBar
import com.example.smtpmonitoreo.ui.components.NetworkStatusBanner
import com.example.smtpmonitoreo.ui.components.SmtpMapView
import com.example.smtpmonitoreo.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PasajeroDashboard(
    user: UserAccount,
    onLogout: () -> Unit,
    repository: MonitoringRepository = MonitoringRepositoryImpl.instance
) {
    val activeUnits by repository.activeUnitsFlow.collectAsState()
    val routes by repository.routesFlow.collectAsState()
    val incidents by repository.incidentsFlow.collectAsState()

    var selectedTab by remember { mutableStateOf(0) } // 0: Mapa, 1: Rutas, 2: Reportes
    var selectedUnit by remember { mutableStateOf<TransportUnit?>(null) }

    // Estados para búsqueda de rutas (RF-13)
    var selectedOrigin by remember { mutableStateOf("Pérez Velasco") }
    var selectedDestination by remember { mutableStateOf("Ceja El Alto") }
    var searchResultRoute by remember { mutableStateOf<RouteModel?>(routes.firstOrNull()) }

    // Estados para formulario de reporte (RF-18)
    var reportCategory by remember { mutableStateOf("Cobro irregular") }
    var reportPlate by remember { mutableStateOf("") }
    var reportLine by remember { mutableStateOf("102") }
    var reportDescription by remember { mutableStateOf("") }
    var reportSuccessMessage by remember { mutableStateOf<String?>(null) }

    val scope = rememberCoroutineScope()

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

        // Banner de Conectividad (RNF-18)
        NetworkStatusBanner(isConnected = true, isReconnecting = false)

        // Segmented Tabs: Mapa vs Buscar Ruta vs Reportar Incidente
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 10.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(DarkSurfaceVariant)
                .padding(4.dp)
        ) {
            TabItem(
                text = "Mapa en Vivo",
                icon = Icons.Default.Map,
                selected = selectedTab == 0,
                modifier = Modifier.weight(1f),
                onClick = { selectedTab = 0 }
            )
            TabItem(
                text = "Buscar Ruta",
                icon = Icons.Default.Search,
                selected = selectedTab == 1,
                modifier = Modifier.weight(1f),
                onClick = { selectedTab = 1 }
            )
            TabItem(
                text = "Reportar",
                icon = Icons.Default.ReportProblem,
                selected = selectedTab == 2,
                modifier = Modifier.weight(1f),
                onClick = { selectedTab = 2 }
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 10.dp)
        ) {
            when (selectedTab) {
                0 -> {
                    // TAB 1: MAPA EN VIVO Y ETA (RF-10, RF-14)
                    Text(
                        text = "MONITOREO EN TIEMPO REAL · LA PAZ",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = CyberCyan,
                        letterSpacing = 1.2.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    SmtpMapView(
                        units = activeUnits,
                        routes = routes,
                        selectedUnit = selectedUnit,
                        onUnitSelected = { selectedUnit = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(260.dp)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Ficha de Detalle de Unidad seleccionada o primera activa
                    val currentUnit = selectedUnit ?: activeUnits.firstOrNull()
                    if (currentUnit != null) {
                        val etaMinutes = repository.calculateEtaMinutes(
                            unitLat = currentUnit.latitude,
                            unitLng = currentUnit.longitude,
                            targetLat = -16.4965, // Pérez Velasco
                            targetLng = -68.1375,
                            speedKmh = currentUnit.speedKmh
                        )

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
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(40.dp)
                                                .clip(RoundedCornerShape(10.dp))
                                                .background(CyberCyan.copy(alpha = 0.15f)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.DirectionsBus,
                                                contentDescription = "Bus",
                                                tint = CyberCyan,
                                                modifier = Modifier.size(22.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column {
                                            Text(
                                                text = "LÍNEA ${currentUnit.lineNumber} | ${currentUnit.plate}",
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = TextPrimary
                                            )
                                            Text(
                                                text = "${currentUnit.vehicleType} · ${currentUnit.syndicate}",
                                                fontSize = 11.sp,
                                                color = TextSecondary
                                            )
                                        }
                                    }

                                    // ETA Chip (RF-14)
                                    Surface(
                                        shape = RoundedCornerShape(10.dp),
                                        color = CyberCyan.copy(alpha = 0.15f),
                                        border = androidx.compose.foundation.BorderStroke(1.dp, CyberCyan)
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            Text(
                                                text = "ETA PÉRZ VELASCO",
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = CyberCyan
                                            )
                                            Text(
                                                text = "$etaMinutes min",
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
                                        text = "Chofer: ${currentUnit.driverName}",
                                        fontSize = 12.sp,
                                        color = TextSecondary
                                    )
                                    Text(
                                        text = "Velocidad: ${currentUnit.speedKmh.toInt()} km/h",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = CyberEmerald
                                    )
                                }
                            }
                        }
                    }
                }

                1 -> {
                    // TAB 2: BÚSQUEDA DE RUTAS POR ORIGEN Y DESTINO (RF-13)
                    Text(
                        text = "BUSCADOR DE RUTAS PACEÑAS",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = CyberCyan,
                        letterSpacing = 1.2.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, CyberBorder, RoundedCornerShape(18.dp)),
                        shape = RoundedCornerShape(18.dp),
                        color = DarkSurface
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            // Selector de Origen
                            Text("Punto de Origen", fontSize = 12.sp, color = TextSecondary)
                            Spacer(modifier = Modifier.height(6.dp))
                            LocationSelector(
                                value = selectedOrigin,
                                options = listOf("Pérez Velasco", "Ceja El Alto", "Plaza Murillo", "San Pedro", "Calacoto"),
                                onSelect = { selectedOrigin = it }
                            )

                            Spacer(modifier = Modifier.height(14.dp))

                            // Selector de Destino
                            Text("Punto de Destino", fontSize = 12.sp, color = TextSecondary)
                            Spacer(modifier = Modifier.height(6.dp))
                            LocationSelector(
                                value = selectedDestination,
                                options = listOf("Ceja El Alto", "Pérez Velasco", "Sopocachi", "Miraflores", "Calacoto"),
                                onSelect = { selectedDestination = it }
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            Button(
                                onClick = {
                                    searchResultRoute = routes.find {
                                        it.origin.contains(selectedOrigin, ignoreCase = true) ||
                                        it.destination.contains(selectedDestination, ignoreCase = true)
                                    } ?: routes.firstOrNull()
                                },
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

                    // Resultado de la Búsqueda
                    searchResultRoute?.let { route ->
                        Text(
                            text = "RUTA RECOMENDADA",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = CyberEmerald,
                            letterSpacing = 1.sp
                        )
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
                                        text = route.name,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimary
                                    )
                                    Text(
                                        text = "Bs ${String.format("%.2f", route.tariffBs)}",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = CyberEmerald
                                    )
                                }

                                Spacer(modifier = Modifier.height(10.dp))
                                Text(
                                    text = "Paradas principales: ${route.stops.joinToString(" ➔ ")}",
                                    fontSize = 12.sp,
                                    color = TextSecondary
                                )
                            }
                        }
                    }
                }

                2 -> {
                    // TAB 3: REPORTAR INCIDENTE (RF-18, RF-19)
                    Text(
                        text = "FORMULARIO DE DENUNCIA E INCIDENTES",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = CyberRose,
                        letterSpacing = 1.2.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, CyberRose.copy(alpha = 0.4f), RoundedCornerShape(18.dp)),
                        shape = RoundedCornerShape(18.dp),
                        color = DarkSurface
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            // Categoria
                            Text("Tipo de Incidente", fontSize = 12.sp, color = TextSecondary)
                            Spacer(modifier = Modifier.height(6.dp))
                            LocationSelector(
                                value = reportCategory,
                                options = listOf("Cobro irregular", "Mal servicio", "Inseguridad", "Desvío de ruta"),
                                onSelect = { reportCategory = it }
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            OutlinedTextField(
                                value = reportPlate,
                                onValueChange = { reportPlate = it },
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
                                value = reportDescription,
                                onValueChange = { reportDescription = it },
                                label = { Text("Descripción del incidente") },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(100.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = CyberRose,
                                    unfocusedBorderColor = CyberBorder
                                )
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            Button(
                                onClick = {
                                    if (reportPlate.isBlank() || reportDescription.isBlank()) return@Button
                                    scope.launch {
                                        repository.submitIncident(
                                            IncidentReport(
                                                userEmail = user.email,
                                                vehiclePlate = reportPlate.trim(),
                                                lineNumber = reportLine,
                                                category = reportCategory,
                                                description = reportDescription.trim()
                                            )
                                        )
                                        reportPlate = ""
                                        reportDescription = ""
                                        reportSuccessMessage = "¡Reporte enviado exitosamente a la Alcaldía!"
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = CyberRose)
                            ) {
                                Text(
                                    text = "ENVIAR DENUNCIA A LA ALCALDÍA",
                                    color = TextPrimary,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                            }

                            reportSuccessMessage?.let { msg ->
                                Spacer(modifier = Modifier.height(10.dp))
                                Text(
                                    text = msg,
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
                    Text(
                        text = "HISTORIAL DE MIS REPORTES",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextSecondary,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    incidents.forEach { inc ->
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
                                        text = "${inc.category} | Placa: ${inc.vehiclePlate}",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimary
                                    )
                                    Text(
                                        text = inc.description,
                                        fontSize = 11.sp,
                                        color = TextSecondary,
                                        maxLines = 1
                                    )
                                }
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = when (inc.status) {
                                        "RESUELTO" -> CyberEmerald.copy(alpha = 0.2f)
                                        "EN_REVISION" -> CyberAmber.copy(alpha = 0.2f)
                                        else -> CyberRose.copy(alpha = 0.2f)
                                    }
                                ) {
                                    Text(
                                        text = inc.status,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = when (inc.status) {
                                            "RESUELTO" -> CyberEmerald
                                            "EN_REVISION" -> CyberAmber
                                            else -> CyberRose
                                        },
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TabItem(
    text: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    selected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(if (selected) DarkSurfaceElevated else Color.Transparent)
            .border(
                width = if (selected) 1.dp else 0.dp,
                color = if (selected) CyberCyan else Color.Transparent,
                shape = RoundedCornerShape(10.dp)
            )
            .clickable { onClick() }
            .padding(vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = text,
                tint = if (selected) CyberCyan else TextSecondary,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = text,
                fontSize = 12.sp,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                color = if (selected) CyberCyan else TextSecondary
            )
        }
    }
}

@Composable
private fun LocationSelector(
    value: String,
    options: List<String>,
    onSelect: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Box {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .border(1.dp, CyberBorder, RoundedCornerShape(10.dp))
                .clickable { expanded = true },
            color = DarkSurfaceVariant
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = value, fontSize = 13.sp, color = TextPrimary)
                Icon(
                    imageVector = Icons.Default.ArrowDropDown,
                    contentDescription = "Desplegar",
                    tint = CyberCyan
                )
            }
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.background(DarkSurfaceVariant)
        ) {
            options.forEach { opt ->
                DropdownMenuItem(
                    text = { Text(opt, color = TextPrimary, fontSize = 13.sp) },
                    onClick = {
                        onSelect(opt)
                        expanded = false
                    }
                )
            }
        }
    }
}
