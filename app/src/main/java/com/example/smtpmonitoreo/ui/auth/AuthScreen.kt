package com.example.smtpmonitoreo.ui.auth

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
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.smtpmonitoreo.data.model.UserRole
import com.example.smtpmonitoreo.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuthScreen(
    viewModel: AuthViewModel,
    onAuthSuccess: (UserRole) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var passwordVisible by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.loggedInUser) {
        uiState.loggedInUser?.let { user ->
            onAuthSuccess(user.role)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        // Futuristic background glowing gradient circles
        Box(
            modifier = Modifier
                .size(300.dp)
                .offset(x = (-80).dp, y = (-80).dp)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(CyberCyan.copy(alpha = 0.2f), Color.Transparent)
                    )
                )
        )
        Box(
            modifier = Modifier
                .size(320.dp)
                .align(Alignment.BottomEnd)
                .offset(x = 100.dp, y = 100.dp)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(CyberViolet.copy(alpha = 0.25f), Color.Transparent)
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 40.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(20.dp))

            // Header - Resplandor & Logo
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(
                            listOf(CyberCyan, CyberViolet)
                        )
                    )
                    .padding(3.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape)
                        .background(DarkSurface),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.DirectionsBus,
                        contentDescription = "SMTP Logo",
                        tint = CyberCyan,
                        modifier = Modifier.size(36.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "SMTP PACEÑO",
                fontSize = 28.sp,
                fontWeight = FontWeight.ExtraBold,
                color = TextPrimary,
                letterSpacing = 2.sp
            )
            Text(
                text = "Sistema de Monitoreo del Transporte Paceño",
                fontSize = 13.sp,
                color = TextSecondary,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(28.dp))

            // Container futurista con borde de neón
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, CyberBorder, RoundedCornerShape(24.dp))
                    .shadow(16.dp, RoundedCornerShape(24.dp), ambientColor = CyberCyan, spotColor = CyberViolet),
                shape = RoundedCornerShape(24.dp),
                color = DarkSurface
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Selector Tab: Iniciar Sesión vs Registro
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(DarkSurfaceVariant)
                            .padding(4.dp)
                    ) {
                        TabButton(
                            text = "Iniciar Sesión",
                            selected = !uiState.isRegisterMode,
                            modifier = Modifier.weight(1f),
                            onClick = { if (uiState.isRegisterMode) viewModel.toggleMode() }
                        )
                        TabButton(
                            text = "Registrarse",
                            selected = uiState.isRegisterMode,
                            modifier = Modifier.weight(1f),
                            onClick = { if (!uiState.isRegisterMode) viewModel.toggleMode() }
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Selector de Roles Futurista
                    Text(
                        text = "SELECCIONA TU ROL DE ACCESO",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = CyberCyan,
                        letterSpacing = 1.2.sp,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    RoleGrid(
                        selectedRole = uiState.selectedRole,
                        onRoleSelected = { viewModel.onRoleSelected(it) }
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // Campos Formulario
                    AnimatedVisibility(visible = uiState.isRegisterMode) {
                        Column {
                            FuturisticTextField(
                                value = uiState.fullName,
                                onValueChange = { viewModel.onFullNameChanged(it) },
                                label = "Nombre Completo",
                                leadingIcon = Icons.Default.Person
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                        }
                    }

                    FuturisticTextField(
                        value = uiState.email,
                        onValueChange = { viewModel.onEmailChanged(it) },
                        label = "Correo Electrónico",
                        leadingIcon = Icons.Default.Email
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    FuturisticTextField(
                        value = uiState.password,
                        onValueChange = { viewModel.onPasswordChanged(it) },
                        label = "Contraseña",
                        leadingIcon = Icons.Default.Lock,
                        isPassword = true,
                        passwordVisible = passwordVisible,
                        onPasswordToggle = { passwordVisible = !passwordVisible }
                    )

                    // Error Banner
                    uiState.errorMessage?.let { err ->
                        Spacer(modifier = Modifier.height(14.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(CyberRose.copy(alpha = 0.15f))
                                .border(1.dp, CyberRose, RoundedCornerShape(10.dp))
                                .padding(10.dp)
                        ) {
                            Text(
                                text = err,
                                color = CyberRose,
                                fontSize = 12.sp,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Botón Principal Resplandeciente
                    Button(
                        onClick = { viewModel.submit() },
                        enabled = !uiState.isLoading,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Unspecified
                        ),
                        contentPadding = PaddingValues()
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.horizontalGradient(
                                        listOf(uiState.selectedRole.accentColor, CyberViolet)
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            if (uiState.isLoading) {
                                CircularProgressIndicator(
                                    color = DarkBackground,
                                    modifier = Modifier.size(24.dp),
                                    strokeWidth = 2.5.dp
                                )
                            } else {
                                Text(
                                    text = if (uiState.isRegisterMode) "CREAR CUENTA EN EL SMTP" else "ACCEDER AL SISTEMA",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = DarkBackground,
                                    letterSpacing = 1.sp
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Acceso Rápido / Demo Toolbar para Pruebas Instantáneas del Sprint 1
            Text(
                text = "⚡ MODO PRUEBA RÁPIDA DE ROLES (DEMO)",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = TextSecondary,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                QuickRoleButton(
                    role = UserRole.PASAJERO,
                    icon = Icons.Default.DirectionsBus,
                    modifier = Modifier.weight(1f),
                    onClick = { viewModel.quickLoginAs(UserRole.PASAJERO) }
                )
                QuickRoleButton(
                    role = UserRole.CHOFER,
                    icon = Icons.Default.LocalShipping,
                    modifier = Modifier.weight(1f),
                    onClick = { viewModel.quickLoginAs(UserRole.CHOFER) }
                )
                QuickRoleButton(
                    role = UserRole.ALCALDIA,
                    icon = Icons.Default.Shield,
                    modifier = Modifier.weight(1f),
                    onClick = { viewModel.quickLoginAs(UserRole.ALCALDIA) }
                )
                QuickRoleButton(
                    role = UserRole.ADMINISTRADOR,
                    icon = Icons.Default.AdminPanelSettings,
                    modifier = Modifier.weight(1f),
                    onClick = { viewModel.quickLoginAs(UserRole.ADMINISTRADOR) }
                )
            }
        }
    }
}

@Composable
private fun TabButton(
    text: String,
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
            .padding(vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            fontSize = 13.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
            color = if (selected) CyberCyan else TextSecondary
        )
    }
}

@Composable
private fun RoleGrid(
    selectedRole: UserRole,
    onRoleSelected: (UserRole) -> Unit
) {
    val roles = UserRole.entries

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            RoleCard(
                role = roles[0], // PASAJERO
                icon = Icons.Default.PersonPin,
                isSelected = selectedRole == roles[0],
                modifier = Modifier.weight(1f),
                onClick = { onRoleSelected(roles[0]) }
            )
            RoleCard(
                role = roles[1], // CHOFER
                icon = Icons.Default.DirectionsBus,
                isSelected = selectedRole == roles[1],
                modifier = Modifier.weight(1f),
                onClick = { onRoleSelected(roles[1]) }
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            RoleCard(
                role = roles[2], // ALCALDIA
                icon = Icons.Default.Shield,
                isSelected = selectedRole == roles[2],
                modifier = Modifier.weight(1f),
                onClick = { onRoleSelected(roles[2]) }
            )
            RoleCard(
                role = roles[3], // ADMINISTRADOR
                icon = Icons.Default.AdminPanelSettings,
                isSelected = selectedRole == roles[3],
                modifier = Modifier.weight(1f),
                onClick = { onRoleSelected(roles[3]) }
            )
        }
    }
}

@Composable
private fun RoleCard(
    role: UserRole,
    icon: ImageVector,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(if (isSelected) DarkSurfaceElevated else DarkSurfaceVariant)
            .border(
                width = if (isSelected) 1.5.dp else 1.dp,
                color = if (isSelected) role.accentColor else CyberBorder,
                shape = RoundedCornerShape(14.dp)
            )
            .clickable { onClick() }
            .padding(12.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = role.displayName,
                tint = if (isSelected) role.accentColor else TextSecondary,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(
                    text = role.displayName,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isSelected) TextPrimary else TextSecondary
                )
                Text(
                    text = role.subtitle,
                    fontSize = 10.sp,
                    color = TextMuted,
                    maxLines = 1
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FuturisticTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    leadingIcon: ImageVector,
    isPassword: Boolean = false,
    passwordVisible: Boolean = false,
    onPasswordToggle: () -> Unit = {}
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label, fontSize = 12.sp) },
        leadingIcon = {
            Icon(
                imageVector = leadingIcon,
                contentDescription = label,
                tint = CyberCyan,
                modifier = Modifier.size(20.dp)
            )
        },
        trailingIcon = if (isPassword) {
            {
                IconButton(onClick = onPasswordToggle) {
                    Icon(
                        imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                        contentDescription = "Toggle password",
                        tint = TextSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        } else null,
        visualTransformation = if (isPassword && !passwordVisible) PasswordVisualTransformation() else VisualTransformation.None,
        singleLine = true,
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = DarkSurfaceVariant,
            unfocusedContainerColor = DarkSurfaceVariant,
            focusedBorderColor = CyberCyan,
            unfocusedBorderColor = CyberBorder,
            focusedLabelColor = CyberCyan,
            unfocusedLabelColor = TextSecondary,
            focusedTextColor = TextPrimary,
            unfocusedTextColor = TextPrimary
        ),
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
private fun QuickRoleButton(
    role: UserRole,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .clickable { onClick() },
        color = DarkSurfaceVariant,
        border = androidx.compose.foundation.BorderStroke(1.dp, role.accentColor.copy(alpha = 0.4f)),
        shape = RoundedCornerShape(10.dp)
    ) {
        Column(
            modifier = Modifier.padding(vertical = 10.dp, horizontal = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = role.displayName,
                tint = role.accentColor,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = role.displayName,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
        }
    }
}
