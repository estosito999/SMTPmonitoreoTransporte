package com.example.smtpmonitoreo.ui.dashboard

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.smtpmonitoreo.data.model.UserAccount
import com.example.smtpmonitoreo.data.model.UserRole
import com.example.smtpmonitoreo.ui.components.DashboardTopBar
import com.example.smtpmonitoreo.ui.components.ConnectionBanner
import com.example.smtpmonitoreo.ui.components.SmtpMapView
import com.example.smtpmonitoreo.ui.theme.*
import org.koin.compose.viewmodel.koinViewModel

private fun requiredPermissions(): Array<String> = buildList {
    add(Manifest.permission.ACCESS_FINE_LOCATION)
    add(Manifest.permission.ACCESS_COARSE_LOCATION)
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        add(Manifest.permission.POST_NOTIFICATIONS)
    }
}.toTypedArray()

@Composable
fun ChoferDashboard(
    user: UserAccount,
    onLogout: () -> Unit,
    viewModel: ChoferViewModel = koinViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    val statusColor by animateColorAsState(
        targetValue = if (state.isTransmitting) CyberEmerald else CyberRose,
        label = "statusColor"
    )

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { result ->
        if (result[Manifest.permission.ACCESS_FINE_LOCATION] == true) {
            viewModel.startTransmission()
        } else {
            viewModel.onPermissionDenied()
        }
    }

    val onToggle: () -> Unit = {
        val hasLocation = ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        when {
            state.isTransmitting -> viewModel.stopTransmission()
            hasLocation -> viewModel.startTransmission()
            else -> permissionLauncher.launch(requiredPermissions())
        }
    }

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

        // Banner de Conectividad (RNF-18) — se hace real en el bloque de conectividad
        ConnectionBanner()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .navigationBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 10.dp)
        ) {
            UnidadAsignadaCard(state = state)

            Spacer(modifier = Modifier.height(16.dp))

            // Selector de optimización de batería y datos (RNF-23, RNF-24)
            SectionLabel("AHORRO DE BATERÍA Y DATOS MÓVILES (RNF-23 / 24)")
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                GpsMode.entries.forEach { mode ->
                    GpsModeChip(
                        label = "${mode.label} (${mode.seconds}s)",
                        sub = mode.sub,
                        isSelected = state.gpsMode == mode,
                        modifier = Modifier.weight(1f),
                        onClick = { viewModel.onGpsModeSelected(mode) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            TransmisionCard(
                state = state,
                statusColor = statusColor,
                onToggle = onToggle
            )

            Spacer(modifier = Modifier.height(20.dp))

            SectionLabel("TELEMETRÍA EN VIVO Y POSICIÓN EN MAPA")
            Spacer(modifier = Modifier.height(10.dp))

            SmtpMapView(
                units = state.units,
                routes = state.routes,
                selectedUnit = state.currentUnit,
                followSelected = true,
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
                    value = "${state.currentUnit?.speedKmh?.toInt() ?: 0} km/h",
                    icon = Icons.Default.Speed,
                    modifier = Modifier.weight(1f)
                )
                ChoferStatCard(
                    title = "Intervalo GPS",
                    value = "${state.gpsMode.seconds} seg",
                    icon = Icons.Default.Timer,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

// Unidad asignada por el administrador (RF-30)
@Composable
private fun UnidadAsignadaCard(state: ChoferUiState) {
    val unit = state.currentUnit

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
                        text = "TU UNIDAD ASIGNADA",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = CyberEmerald,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = when {
                            unit != null -> "${unit.vehicleType} ${unit.plate}"
                            state.assignedPlate != null -> state.assignedPlate
                            else -> "Sin unidad asignada"
                        },
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = when {
                    unit != null -> "Línea ${unit.lineNumber} · ${unit.syndicate}"
                    state.assignedPlate != null -> "La unidad no está registrada en el sistema"
                    else -> "Pide al administrador que te asigne una unidad para poder transmitir."
                },
                fontSize = 11.sp,
                color = if (unit != null) CyberEmerald else TextSecondary
            )
        }
    }
}

// Control principal de GPS (RF-31, RF-32)
@Composable
private fun TransmisionCard(
    state: ChoferUiState,
    statusColor: Color,
    onToggle: () -> Unit
) {
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
                    imageVector = if (state.isTransmitting) Icons.Default.GpsFixed else Icons.Default.GpsOff,
                    contentDescription = "GPS Status",
                    tint = statusColor,
                    modifier = Modifier.size(38.dp)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = if (state.isTransmitting) "TRANSMISIÓN EN VIVO ACTIVA" else "RECORRIDO EN PAUSA",
                fontSize = 16.sp,
                fontWeight = FontWeight.ExtraBold,
                color = TextPrimary
            )

            Text(
                text = if (state.isTransmitting) {
                    "Enviando ubicación GPS cada ${state.gpsMode.seconds} segundos al mapa de pasajeros"
                } else {
                    "Presiona el botón para iniciar la emisión de GPS en tiempo real"
                },
                fontSize = 11.sp,
                color = TextSecondary,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(18.dp))

            Button(
                onClick = onToggle,
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
                                if (state.isTransmitting) {
                                    listOf(CyberRose, Color(0xFFFF5252))
                                } else {
                                    listOf(CyberEmerald, Color(0xFF00C853))
                                }
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (state.isTransmitting) "FINALIZAR RECORRIDO GPS" else "INICIAR RECORRIDO GPS EN VIVO",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = DarkBackground,
                        letterSpacing = 1.sp
                    )
                }
            }

            state.error?.let { error ->
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = error,
                    fontSize = 12.sp,
                    color = CyberRose,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
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
    icon: ImageVector,
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

@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text,
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        color = CyberEmerald,
        letterSpacing = 1.sp
    )
}