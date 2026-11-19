package com.example.smtpmonitoreo.ui.dashboard

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.smtpmonitoreo.data.model.InfractionModel
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
fun AlcaldiaDashboard(
    user: UserAccount,
    onLogout: () -> Unit,
    repository: MonitoringRepository = MonitoringRepositoryImpl.instance
) {
    val activeUnits by repository.activeUnitsFlow.collectAsState()
    val routes by repository.routesFlow.collectAsState()
    val incidents by repository.incidentsFlow.collectAsState()
    val infractions by repository.infractionsFlow.collectAsState()

    var selectedTab by remember { mutableStateOf(0) } // 0: Supervisión, 1: Rutas & Unidades, 2: Fiscalización
    var selectedLineFilter by remember { mutableStateOf("TODAS") }

    // Estados para nuevo registro de ruta (RF-41)
    var newRouteLine by remember { mutableStateOf("") }
    var newRouteName by remember { mutableStateOf("") }
    var newRouteOrigin by remember { mutableStateOf("") }
    var newRouteDestination by remember { mutableStateOf("") }
    var newRouteTariff by remember { mutableStateOf("2.50") }

    // Estados para emision de infraccion (RF-44)
    var infPlate by remember { mutableStateOf("") }
    var infLine by remember { mutableStateOf("102") }
    var infReason by remember { mutableStateOf("Desvío de ruta arbitrario") }
    var infAmount by remember { mutableStateOf("100.00") }

    val scope = rememberCoroutineScope()

    val filteredUnits = if (selectedLineFilter == "TODAS") activeUnits else activeUnits.filter { it.lineNumber == selectedLineFilter }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        DashboardTopBar(
            user = user,
            role = UserRole.ALCALDIA,
            onLogout = onLogout
        )

        // Banner de Conectividad (RNF-18)
        NetworkStatusBanner(isConnected = true, isReconnecting = false)

        // Tabs de Control Municipal
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 10.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(DarkSurfaceVariant)
                .padding(4.dp)
        ) {
            TabItem(
                text = "Supervisión",
                icon = Icons.Default.Map,
                selected = selectedTab == 0,
                modifier = Modifier.weight(1f),
                onClick = { selectedTab = 0 }
            )
            TabItem(
                text = "Rutas & Unidades",
                icon = Icons.Default.EditRoad,
                selected = selectedTab == 1,
                modifier = Modifier.weight(1f),
                onClick = { selectedTab = 1 }
            )
            TabItem(
                text = "Fiscalización",
                icon = Icons.Default.AssignmentLate,
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
                    // TAB 1: SUPERVISIÓN EN MAPA GENERAL (RF-42)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "MAPA GENERAL DE MUNICIPALIDAD LA PAZ",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = RoleAlcaldiaColor,
                            letterSpacing = 1.sp
                        )

                        // Filter Chip
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = DarkSurfaceVariant,
                            border = androidx.compose.foundation.BorderStroke(1.dp, CyberBorder),
                            modifier = Modifier.clickable {
                                selectedLineFilter = when (selectedLineFilter) {
                                    "TODAS" -> "102"
                                    "102" -> "231"
                                    "231" -> "Trufi 45"
                                    else -> "TODAS"
                                }
                            }
                        ) {
                            Text(
                                text = "LÍNEA: $selectedLineFilter ⚙️",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = CyberCyan,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    SmtpMapView(
                        units = filteredUnits,
                        routes = routes,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(280.dp)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // KPIs
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        KpiCard(
                            title = "Unidades Activas",
                            count = "${activeUnits.filter { it.isTransmitting }.size}",
                            icon = Icons.Default.DirectionsBus,
                            accentColor = RoleAlcaldiaColor,
                            modifier = Modifier.weight(1f)
                        )
                        KpiCard(
                            title = "Rutas Oficiales",
                            count = "${routes.size}",
                            icon = Icons.Default.AltRoute,
                            accentColor = CyberCyan,
                            modifier = Modifier.weight(1f)
                        )
                        KpiCard(
                            title = "Reportes Hoy",
                            count = "${incidents.size}",
                            icon = Icons.Default.Warning,
                            accentColor = CyberRose,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                1 -> {
                    // TAB 2: GESTIÓN DE RUTAS Y UNIDADES (RF-40, RF-41)
                    Text(
                        text = "REGISTRAR NUEVA RUTA OFICIAL",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = CyberCyan,
                        letterSpacing = 1.2.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, CyberBorder, RoundedCornerShape(18.dp)),
                        shape = RoundedCornerShape(18.dp),
                        color = DarkSurface
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                OutlinedTextField(
                                    value = newRouteLine,
                                    onValueChange = { newRouteLine = it },
                                    label = { Text("Número Línea") },
                                    singleLine = true,
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(12.dp)
                                )
                                OutlinedTextField(
                                    value = newRouteTariff,
                                    onValueChange = { newRouteTariff = it },
                                    label = { Text("Tarifa Bs") },
                                    singleLine = true,
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(12.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(10.dp))

                            OutlinedTextField(
                                value = newRouteName,
                                onValueChange = { newRouteName = it },
                                label = { Text("Nombre de la Ruta (Ej: Línea 102 - San Pedro)") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp)
                            )
                            Spacer(modifier = Modifier.height(10.dp))

                            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                OutlinedTextField(
                                    value = newRouteOrigin,
                                    onValueChange = { newRouteOrigin = it },
                                    label = { Text("Origen") },
                                    singleLine = true,
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(12.dp)
                                )
                                OutlinedTextField(
                                    value = newRouteDestination,
                                    onValueChange = { newRouteDestination = it },
                                    label = { Text("Destino") },
                                    singleLine = true,
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(12.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(14.dp))

                            Button(
                                onClick = {
                                    if (newRouteLine.isBlank() || newRouteName.isBlank()) return@Button
                                    scope.launch {
                                        repository.addRoute(
                                            RouteModel(
                                                lineNumber = newRouteLine.trim(),
                                                name = newRouteName.trim(),
                                                origin = newRouteOrigin.trim(),
                                                destination = newRouteDestination.trim(),
                                                tariffBs = newRouteTariff.toDoubleOrNull() ?: 2.50
                                            )
                                        )
                                        newRouteLine = ""
                                        newRouteName = ""
                                        newRouteOrigin = ""
                                        newRouteDestination = ""
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = CyberCyan)
                            ) {
                                Text("GUARDAR RUTA MUNICIPAL", color = DarkBackground, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Text(
                        text = "RUTAS MUNICIPALES VIGENTES",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextSecondary,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    routes.forEach { r ->
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
                                        text = r.name,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimary
                                    )
                                    Text(
                                        text = "${r.origin} ➔ ${r.destination}",
                                        fontSize = 11.sp,
                                        color = TextSecondary
                                    )
                                }
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = CyberEmerald.copy(alpha = 0.15f)
                                ) {
                                    Text(
                                        text = "Bs ${r.tariffBs}",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = CyberEmerald,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                2 -> {
                    // TAB 3: FISCALIZACIÓN Y RESOLUCIÓN DE INCIDENTES (RF-43, RF-44)
                    Text(
                        text = "EMITIR INFRACCIÓN VEHICULAR",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = CyberRose,
                        letterSpacing = 1.2.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, CyberRose.copy(alpha = 0.4f), RoundedCornerShape(18.dp)),
                        shape = RoundedCornerShape(18.dp),
                        color = DarkSurface
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                OutlinedTextField(
                                    value = infPlate,
                                    onValueChange = { infPlate = it.uppercase() },
                                    label = { Text("Placa Vehículo") },
                                    singleLine = true,
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(12.dp)
                                )
                                OutlinedTextField(
                                    value = infLine,
                                    onValueChange = { infLine = it },
                                    label = { Text("Línea") },
                                    singleLine = true,
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(12.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(10.dp))

                            OutlinedTextField(
                                value = infReason,
                                onValueChange = { infReason = it },
                                label = { Text("Motivo de la Infracción") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp)
                            )
                            Spacer(modifier = Modifier.height(14.dp))

                            Button(
                                onClick = {
                                    if (infPlate.isBlank()) return@Button
                                    scope.launch {
                                        repository.addInfraction(
                                            InfractionModel(
                                                vehiclePlate = infPlate.trim(),
                                                lineNumber = infLine,
                                                reason = infReason,
                                                fineAmountBs = infAmount.toDoubleOrNull() ?: 100.0,
                                                date = "24/05/2026",
                                                status = "EMITIDA"
                                            )
                                        )
                                        infPlate = ""
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = CyberRose)
                            ) {
                                Text("EMITIR SANCIÓN MUNICIPAL", color = TextPrimary, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Text(
                        text = "INFRACCIONES REGISTRADAS",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextSecondary,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    infractions.forEach { inf ->
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
                                        text = "Placa ${inf.vehiclePlate} | Línea ${inf.lineNumber}",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimary
                                    )
                                    Text(
                                        text = inf.reason,
                                        fontSize = 11.sp,
                                        color = TextSecondary
                                    )
                                }
                                Text(
                                    text = "Bs ${inf.fineAmountBs.toInt()}",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = CyberRose
                                )
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
                color = if (selected) RoleAlcaldiaColor else Color.Transparent,
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
                tint = if (selected) RoleAlcaldiaColor else TextSecondary,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = text,
                fontSize = 11.sp,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                color = if (selected) RoleAlcaldiaColor else TextSecondary
            )
        }
    }
}

@Composable
private fun KpiCard(
    title: String,
    count: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .border(1.dp, CyberBorder, RoundedCornerShape(16.dp)),
        color = DarkSurface
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = accentColor,
                modifier = Modifier.size(22.dp)
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = count,
                fontSize = 20.sp,
                fontWeight = FontWeight.ExtraBold,
                color = TextPrimary
            )
            Text(
                text = title,
                fontSize = 10.sp,
                color = TextSecondary,
                maxLines = 1
            )
        }
    }
}
