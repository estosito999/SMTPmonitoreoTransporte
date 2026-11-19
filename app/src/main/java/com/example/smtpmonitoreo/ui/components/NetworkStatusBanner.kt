package com.example.smtpmonitoreo.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.smtpmonitoreo.ui.theme.*

@Composable
fun NetworkStatusBanner(
    isConnected: Boolean = true,
    isReconnecting: Boolean = false,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = !isConnected || isReconnecting,
        enter = expandVertically(),
        exit = shrinkVertically()
    ) {
        Surface(
            modifier = modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp)
                .border(
                    width = 1.dp,
                    color = if (isConnected) CyberEmerald else CyberRose,
                    shape = RoundedCornerShape(12.dp)
                ),
            shape = RoundedCornerShape(12.dp),
            color = DarkSurface
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = if (isConnected) Icons.Default.Wifi else Icons.Default.WifiOff,
                    contentDescription = "Conexión",
                    tint = if (isConnected) CyberEmerald else CyberRose,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = if (isConnected) "Sincronizado con Servidor SMTP" else "Sin Conexión a Internet",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = if (isReconnecting) "Reintentando conexión automáticamente..." else "Operando en modo sin conexión (datos locales)",
                        fontSize = 10.sp,
                        color = if (isConnected) CyberEmerald else CyberAmber
                    )
                }
            }
        }
    }
}
