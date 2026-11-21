package com.example.smtpmonitoreo.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.smtpmonitoreo.domain.ConnectionState
import com.example.smtpmonitoreo.ui.theme.*
import org.koin.compose.viewmodel.koinViewModel

/** Banner con el estado real de la conexión (RNF-18). Va debajo de la barra superior. */
@Composable
fun ConnectionBanner(viewModel: ConnectivityViewModel = koinViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    NetworkStatusBanner(state = state)
}

@Composable
fun NetworkStatusBanner(
    state: ConnectionState,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = state != ConnectionState.ONLINE,
        enter = expandVertically(),
        exit = shrinkVertically()
    ) {
        val offline = state == ConnectionState.OFFLINE
        val color = if (offline) CyberRose else CyberEmerald

        Surface(
            modifier = modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp)
                .border(1.dp, color, RoundedCornerShape(12.dp)),
            shape = RoundedCornerShape(12.dp),
            color = DarkSurface
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = if (offline) Icons.Default.WifiOff else Icons.Default.Wifi,
                    contentDescription = "Conexión",
                    tint = color,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = if (offline) "Sin conexión a internet" else "Conexión restablecida",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = if (offline) {
                            "Operando con datos locales; tus cambios se enviarán al reconectar"
                        } else {
                            "Sincronizando cambios con el servidor SMTP…"
                        },
                        fontSize = 10.sp,
                        color = if (offline) CyberAmber else CyberEmerald
                    )
                }
            }
        }
    }
}