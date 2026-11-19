package com.example.smtpmonitoreo.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.smtpmonitoreo.data.model.RouteModel
import com.example.smtpmonitoreo.data.model.TransportUnit
import com.example.smtpmonitoreo.ui.theme.*

@Composable
fun SmtpMapView(
    units: List<TransportUnit>,
    routes: List<RouteModel> = emptyList(),
    selectedUnit: TransportUnit? = null,
    onUnitSelected: (TransportUnit) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(DarkSurfaceVariant)
            .border(1.dp, CyberBorder, RoundedCornerShape(20.dp))
    ) {
        // Renderizado del Mapa Cyberpunk con Cuadrícula de Radar y Rutas Polyline
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height

            // Dibujar cuadrícula de radar futurista
            val gridStep = 40.dp.toPx()
            var x = 0f
            while (x < width) {
                drawLine(
                    color = CyberBorder.copy(alpha = 0.3f),
                    start = Offset(x, 0f),
                    end = Offset(x, height),
                    strokeWidth = 1f
                )
                x += gridStep
            }
            var y = 0f
            while (y < height) {
                drawLine(
                    color = CyberBorder.copy(alpha = 0.3f),
                    start = Offset(0f, y),
                    end = Offset(width, y),
                    strokeWidth = 1f
                )
                y += gridStep
            }

            // Dibujar Trazados de Rutas (Polylines Neón)
            routes.forEachIndexed { index, route ->
                val routeColor = when (index % 3) {
                    0 -> CyberCyan
                    1 -> CyberEmerald
                    else -> CyberViolet
                }
                val path = Path()
                if (index == 0) {
                    path.moveTo(width * 0.15f, height * 0.85f)
                    path.lineTo(width * 0.45f, height * 0.45f)
                    path.lineTo(width * 0.75f, height * 0.25f)
                } else if (index == 1) {
                    path.moveTo(width * 0.2f, height * 0.2f)
                    path.lineTo(width * 0.45f, height * 0.45f)
                    path.lineTo(width * 0.8f, height * 0.7f)
                } else {
                    path.moveTo(width * 0.85f, height * 0.85f)
                    path.lineTo(width * 0.5f, height * 0.5f)
                    path.lineTo(width * 0.15f, height * 0.45f)
                }

                drawPath(
                    path = path,
                    color = routeColor.copy(alpha = 0.6f),
                    style = Stroke(width = 6f)
                )
            }
        }

        // Overlay con estado de radar y conteo de unidades activas
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = DarkBackground.copy(alpha = 0.85f),
                border = androidx.compose.foundation.BorderStroke(1.dp, CyberCyan.copy(alpha = 0.5f))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(CyberEmerald)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "LA PAZ EN VIVO · ${units.filter { it.isTransmitting }.size} UNIDADES",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                }
            }

            Surface(
                shape = RoundedCornerShape(10.dp),
                color = DarkBackground.copy(alpha = 0.85f),
                border = androidx.compose.foundation.BorderStroke(1.dp, CyberBorder)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.GpsFixed,
                        contentDescription = "GPS",
                        tint = CyberCyan,
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "GPS 5s",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = CyberCyan
                    )
                }
            }
        }

        // Marcadores de Minibuses y Vehículos Interactivos sobre el Mapa
        val activeUnits = units.filter { it.isTransmitting }
        activeUnits.forEachIndexed { index, unit ->
            val offsetX = when (index % 4) {
                0 -> 0.35f
                1 -> 0.55f
                2 -> 0.70f
                else -> 0.25f
            }
            val offsetY = when (index % 4) {
                0 -> 0.40f
                1 -> 0.50f
                2 -> 0.30f
                else -> 0.65f
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(
                        start = (offsetX * 260).dp,
                        top = (offsetY * 180).dp
                    )
            ) {
                VehicleMarkerPin(
                    unit = unit,
                    isSelected = selectedUnit?.id == unit.id,
                    pulseScale = pulseScale,
                    onClick = { onUnitSelected(unit) }
                )
            }
        }

        // Target / Scanner Reticle en el centro
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .size(36.dp)
                .border(1.dp, CyberCyan.copy(alpha = 0.3f), CircleShape)
        )
    }
}

@Composable
private fun VehicleMarkerPin(
    unit: TransportUnit,
    isSelected: Boolean,
    pulseScale: Float,
    onClick: () -> Unit
) {
    val accentColor = when (unit.vehicleType) {
        "Trufi" -> CyberViolet
        "Bus" -> RoleAdminColor
        else -> CyberCyan
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable { onClick() }
    ) {
        // Tag con la placa y línea
        Surface(
            shape = RoundedCornerShape(6.dp),
            color = if (isSelected) accentColor else DarkBackground.copy(alpha = 0.9f),
            border = androidx.compose.foundation.BorderStroke(1.dp, accentColor)
        ) {
            Text(
                text = "L-${unit.lineNumber} | ${unit.plate}",
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                color = if (isSelected) DarkBackground else TextPrimary,
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
            )
        }

        Spacer(modifier = Modifier.height(2.dp))

        // Icono resplandeciente del minibús con pulso
        Box(
            modifier = Modifier
                .size((28 * if (isSelected) pulseScale else 1f).dp)
                .clip(CircleShape)
                .background(accentColor.copy(alpha = 0.25f))
                .border(1.5.dp, accentColor, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.DirectionsBus,
                contentDescription = unit.plate,
                tint = accentColor,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}
