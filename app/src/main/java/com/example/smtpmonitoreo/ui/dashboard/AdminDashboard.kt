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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.smtpmonitoreo.data.model.UserAccount
import com.example.smtpmonitoreo.data.model.UserRole
import com.example.smtpmonitoreo.ui.components.DashboardTopBar
import com.example.smtpmonitoreo.ui.components.NetworkStatusBanner
import com.example.smtpmonitoreo.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboard(
    user: UserAccount,
    onLogout: () -> Unit,
    onSwitchRolePreview: (UserRole) -> Unit = {}
) {
    var registeredUsers by remember {
        mutableStateOf(
            listOf(
                UserAccount(uid = "u1", email = "pasajero.demo@smtp.lapaz.bo", fullName = "Carlos Mendoza", role = UserRole.PASAJERO, status = "ACTIVO"),
                UserAccount(uid = "u2", email = "chofer.minibus102@smtp.lapaz.bo", fullName = "Don Rolando Mamani", role = UserRole.CHOFER, status = "ACTIVO"),
                UserAccount(uid = "u3", email = "alcaldia.control@smtp.lapaz.bo", fullName = "Ing. Laura Quispe", role = UserRole.ALCALDIA, status = "ACTIVO"),
                UserAccount(uid = "u4", email = "admin.master@smtp.lapaz.bo", fullName = "Admin Israel Pérez", role = UserRole.ADMINISTRADOR, status = "ACTIVO")
            )
        )
    }

    // Formulario para crear usuario (RF-50)
    var newUserEmail by remember { mutableStateOf("") }
    var newUserName by remember { mutableStateOf("") }
    var newUserRole by remember { mutableStateOf(UserRole.PASAJERO) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        DashboardTopBar(
            user = user,
            role = UserRole.ADMINISTRADOR,
            onLogout = onLogout
        )

        // Banner de Conectividad (RNF-18)
        NetworkStatusBanner(isConnected = true, isReconnecting = false)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(20.dp)
        ) {
            // Header del Administrador
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, RoleAdminColor.copy(alpha = 0.5f), RoundedCornerShape(20.dp)),
                shape = RoundedCornerShape(20.dp),
                color = DarkSurface
            ) {
                Row(
                    modifier = Modifier.padding(18.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(50.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(RoleAdminColor.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AdminPanelSettings,
                            contentDescription = "Admin",
                            tint = RoleAdminColor,
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column {
                        Text(
                            text = "CONSOLA DE ADMINISTRACIÓN",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = RoleAdminColor,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = "Gestión Total de Usuarios & Roles",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Formulario para Crear / Asignar Cuenta (RF-50, RF-51)
            Text(
                text = "CREAR NUEVA CUENTA DE USUARIO",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = RoleAdminColor,
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
                    OutlinedTextField(
                        value = newUserName,
                        onValueChange = { newUserName = it },
                        label = { Text("Nombre Completo") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = newUserEmail,
                        onValueChange = { newUserEmail = it },
                        label = { Text("Correo Electrónico") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Text("Rol Asignado", fontSize = 12.sp, color = TextSecondary)
                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        UserRole.entries.forEach { r ->
                            val isSel = newUserRole == r
                            Surface(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable { newUserRole = r },
                                color = if (isSel) r.accentColor.copy(alpha = 0.2f) else DarkSurfaceVariant,
                                border = androidx.compose.foundation.BorderStroke(1.dp, if (isSel) r.accentColor else CyberBorder)
                            ) {
                                Text(
                                    text = r.displayName,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSel) r.accentColor else TextSecondary,
                                    modifier = Modifier.padding(vertical = 8.dp),
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Button(
                        onClick = {
                            if (newUserEmail.isBlank() || newUserName.isBlank()) return@Button
                            registeredUsers = registeredUsers + UserAccount(
                                uid = "u_${System.currentTimeMillis()}",
                                email = newUserEmail.trim(),
                                fullName = newUserName.trim(),
                                role = newUserRole,
                                status = "ACTIVO"
                            )
                            newUserName = ""
                            newUserEmail = ""
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = RoleAdminColor)
                    ) {
                        Text("REGISTRAR USUARIO Y ROLES", color = DarkBackground, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Lista de Cuentas y Control de Roles (RF-50, RF-51)
            Text(
                text = "CUENTAS REGISTRADAS Y PERMISOS (${registeredUsers.size})",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = TextSecondary,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(10.dp))

            registeredUsers.forEach { account ->
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
                                text = account.fullName,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Text(
                                text = account.email,
                                fontSize = 11.sp,
                                color = TextSecondary
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = account.role.accentColor.copy(alpha = 0.15f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, account.role.accentColor)
                        ) {
                            Text(
                                text = account.role.displayName,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = account.role.accentColor,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Visualizador interactivo de Dashboard de otro rol
            Text(
                text = "PROBAR DASHBOARD DE OTRO ROL",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = TextSecondary,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                RolePreviewChip(
                    role = UserRole.PASAJERO,
                    modifier = Modifier.weight(1f),
                    onClick = { onSwitchRolePreview(UserRole.PASAJERO) }
                )
                RolePreviewChip(
                    role = UserRole.CHOFER,
                    modifier = Modifier.weight(1f),
                    onClick = { onSwitchRolePreview(UserRole.CHOFER) }
                )
                RolePreviewChip(
                    role = UserRole.ALCALDIA,
                    modifier = Modifier.weight(1f),
                    onClick = { onSwitchRolePreview(UserRole.ALCALDIA) }
                )
            }
        }
    }
}

@Composable
private fun RolePreviewChip(
    role: UserRole,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .clickable { onClick() },
        color = DarkSurfaceVariant,
        border = androidx.compose.foundation.BorderStroke(1.dp, role.accentColor.copy(alpha = 0.5f))
    ) {
        Box(
            modifier = Modifier.padding(vertical = 10.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = role.displayName,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = role.accentColor
            )
        }
    }
}
