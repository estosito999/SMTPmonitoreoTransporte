package com.example.smtpmonitoreo.ui.dashboard

import androidx.compose.animation.animateColorAsState
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
import com.example.smtpmonitoreo.data.model.UserAccount
import com.example.smtpmonitoreo.data.model.UserRole
import com.example.smtpmonitoreo.data.repository.MonitoringRepository
import org.koin.compose.koinInject
import com.example.smtpmonitoreo.ui.components.DashboardTopBar
import com.example.smtpmonitoreo.ui.components.NetworkStatusBanner
import com.example.smtpmonitoreo.ui.components.SmtpMapView
import com.example.smtpmonitoreo.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChoferDashboard(
    user: UserAccount,
    onLogout: () -> Unit,
    repository: MonitoringRepository = koinInject()
) {
    val activeUnits by repository.activeUnitsFlow.collectAsState()
    val routes by repository.routesFlow.collectAsState()

    var vehiclePlate by remember { mutableStateOf("4521-FGH") }
    var lineNumber by remember { mutableStateOf("102") }

    // Selector de optimización de batería y datos (RNF-23, RNF-24)
    var transmissionIntervalSec by remember { mutableStateOf(5) } // 3s, 5s, 15s

    val currentUnit = activeUnits.find { it.plate == vehiclePlate }
    val isTransmitting = currentUnit?.isTransmitting ?: false

    val statusColor by animateColorAsState(
        targetValue = if (isTransmitting) CyberEmerald else CyberRose,
        label = "statusColor"
    )

    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        DashboardTopBar(
            user = user,
            role = UserRole.CHOFER,
            onLogout = onLogout
        )

        // Banner de Conectividad (RNF-18)
        NetworkStatusBanner(isConnected = true, isReconnecting = false)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 10.dp)
        ) {
            // Card de Vinculación de Unidad (RF-30)
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, CyberEmerald.copy(alpha = 0.5f), RoundedCornerShape(20.dp)),
                shape = RoundedCornerShape(20.dp),
                color = DarkSurface
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(CyberEmerald.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.DirectionsBus,
                                contentDescription = "Unidad",
                                tint = CyberEmerald,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "VINCULACIÓN DE UNIDAD PACEÑA",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = CyberEmerald,
                                letterSpacing = 1.sp
                            )
                            Text(
                                text = "Minibús $vehiclePlate · Línea $lineNumber",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedTextField(
                            value = vehiclePlate,
                            onValueChange = { vehiclePlate = it.uppercase() },
                            label = { Text("Placa") },
                            singleLine = true,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = CyberEmerald,
                                unfocusedBorderColor = CyberBorder
                            )
                        )
                        OutlinedTextField(
                            value = lineNumber,
                            onValueChange = { lineNumber = it },
                            label = { Text("Línea") },
                            singleLine = true,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = CyberEmerald,
                                unfocusedBorderColor = CyberBorder
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Selector de Optimización de Batería y Datos (RNF-23, RNF-24)
            Text(
                text = "AHORRO DE BATERÍA Y DATOS MÓVILES (RNF-23 / 24)",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = CyberEmerald,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                GpsModeChip(
                    label = "Eco (15s)",
                    sub = "Ahorro Máx.",
                    isSelected = transmissionIntervalSec == 15,
                    modifier = Modifier.weight(1f),
                    onClick = { transmissionIntervalSec = 15 }
                )
                GpsModeChip(
                    label = "Normal (5s)",
                    sub = "Recomendado",
                    isSelected = transmissionIntervalSec == 5,
                    modifier = Modifier.weight(1f),
                    onClick = { transmissionIntervalSec = 5 }
                )
                GpsModeChip(
                    label = "Preciso (3s)",
                    sub = "Tráfico Alto",
                    isSelected = transmissionIntervalSec == 3,
                    modifier = Modifier.weight(1f),
                    onClick = { transmissionIntervalSec = 3 }
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Control Principal Resplandeciente de GPS (RF-31, RF-32)
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.5.dp, statusColor, RoundedCornerShape(24.dp)),
                shape = RoundedCornerShape(24.dp),
                color = DarkSurface
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(76.dp)
                            .clip(CircleShape)
                            .background(statusColor.copy(alpha = 0.15f))
                            .border(2.dp, statusColor, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isTransmitting) Icons.Default.GpsFixed else Icons.Default.GpsOff,
                            contentDescription = "GPS Status",
                            tint = statusColor,
                            modifier = Modifier.size(38.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = if (isTransmitting) "TRANSMISIÓN EN VIVO ACTIVA" else "RECORRIDO EN PAUSA",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = TextPrimary
                    )

                    Text(
                        text = if (isTransmitting)
                            "Enviando ubicación GPS cada $transmissionIntervalSec segundos al mapa de pasajeros"
                        else
                            "Presiona el botón para iniciar la emisión de GPS en tiempo real",
                        fontSize = 11.sp,
                        color = TextSecondary,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    Button(
                        onClick = {
                            scope.launch {
                                if (isTransmitting) {
                                    repository.stopGpsTransmission(vehiclePlate)
                                } else {
                                    repository.startGpsTransmission(vehiclePlate, lineNumber)
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Unspecified),
                        contentPadding = PaddingValues()
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.horizontalGradient(
                                        if (isTransmitting)
                                            listOf(CyberRose, Color(0xFFFF5252))
                                        else
                                            listOf(CyberEmerald, Color(0xFF00C853))
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (isTransmitting) "FINALIZAR RECORRIDO GPS" else "INICIAR RECORRIDO GPS EN VIVO",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = DarkBackground,
                                letterSpacing = 1.sp
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Visor Telemétrico del Mapa en Vivo para el Chofer
            Text(
                text = "TELEMETRÍA EN VIVO Y POSICIÓN EN MAPA",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = CyberEmerald,
                letterSpacing = 1.2.sp
            )
            Spacer(modifier = Modifier.height(10.dp))

            SmtpMapView(
                units = activeUnits,
                routes = routes,
                selectedUnit = currentUnit,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                ChoferStatCard(
                    title = "Velocidad",
                    value = "${currentUnit?.speedKmh?.toInt() ?: 0} km/h",
                    icon = Icons.Default.Speed,
                    modifier = Modifier.weight(1f)
                )
                ChoferStatCard(
                    title = "Intervalo GPS",
                    value = "$transmissionIntervalSec seg",
                    icon = Icons.Default.Timer,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun GpsModeChip(
    label: String,
    sub: String,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .clickable { onClick() },
        color = if (isSelected) CyberEmerald.copy(alpha = 0.2f) else DarkSurfaceVariant,
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (isSelected) CyberEmerald else CyberBorder
        )
    ) {
        Column(
            modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = label,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = if (isSelected) CyberEmerald else TextPrimary
            )
            Text(
                text = sub,
                fontSize = 9.sp,
                color = TextSecondary
            )
        }
    }
}

@Composable
private fun ChoferStatCard(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
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
                tint = CyberEmerald,
                modifier = Modifier.size(22.dp)
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = value,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Text(
                text = title,
                fontSize = 11.sp,
                color = TextSecondary
            )
        }
    }
}
