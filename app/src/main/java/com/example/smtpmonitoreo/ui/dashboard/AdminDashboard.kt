package com.example.smtpmonitoreo.ui.dashboard

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.smtpmonitoreo.data.model.AccountStatus
import com.example.smtpmonitoreo.data.model.UserAccount
import com.example.smtpmonitoreo.data.model.UserRole
import com.example.smtpmonitoreo.domain.AccountDraft
import com.example.smtpmonitoreo.ui.components.ActionButton
import com.example.smtpmonitoreo.ui.components.AppTextField
import com.example.smtpmonitoreo.ui.components.DashboardTopBar
import com.example.smtpmonitoreo.ui.components.FormCard
import com.example.smtpmonitoreo.ui.components.FormError
import com.example.smtpmonitoreo.ui.components.ConnectionBanner
import com.example.smtpmonitoreo.ui.components.NoticeBanner
import com.example.smtpmonitoreo.ui.components.SectionLabel
import com.example.smtpmonitoreo.ui.components.SegmentedTabItem
import com.example.smtpmonitoreo.ui.components.StatusPill
import com.example.smtpmonitoreo.ui.theme.*
import org.koin.compose.viewmodel.koinViewModel
import com.example.smtpmonitoreo.ui.components.DropdownSelector


private const val NO_UNIT = "Sin unidad"
@Composable
fun AdminDashboard(
    user: UserAccount,
    onLogout: () -> Unit,
    onSwitchRolePreview: (UserRole) -> Unit = {},
    viewModel: AdminViewModel = koinViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

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

        // Banner de Conectividad (RNF-18) — se hace real en el bloque de conectividad
        ConnectionBanner()

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 10.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(DarkSurfaceVariant)
                .padding(4.dp)
        ) {
            SegmentedTabItem(
                text = "Cuentas y Roles",
                icon = Icons.Default.People,
                selected = state.tab == AdminTab.CUENTAS,
                accentColor = RoleAdminColor,
                modifier = Modifier.weight(1f),
                onClick = { viewModel.onTabSelected(AdminTab.CUENTAS) }
            )
            SegmentedTabItem(
                text = "Sistema y Logs",
                icon = Icons.Default.Storage,
                selected = state.tab == AdminTab.SISTEMA,
                accentColor = RoleAdminColor,
                modifier = Modifier.weight(1f),
                onClick = { viewModel.onTabSelected(AdminTab.SISTEMA) }
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .navigationBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 10.dp)
        ) {
            state.notice?.let { notice ->
                NoticeBanner(
                    text = notice.text,
                    color = if (notice.isError) CyberRose else CyberEmerald
                )
                Spacer(modifier = Modifier.height(12.dp))
            }

            when (state.tab) {
                AdminTab.CUENTAS -> CuentasTab(
                    state = state,
                    onSearchChanged = viewModel::onSearchChanged,
                    onChangeRole = viewModel::changeRole,
                    onToggleStatus = viewModel::toggleStatus,
                    onAssignVehicle = viewModel::assignVehicle
                )

                AdminTab.SISTEMA -> SistemaTab(
                    state = state,
                    onLogFilterSelected = viewModel::onLogFilterSelected,
                    onSwitchRolePreview = onSwitchRolePreview,
                    onSeedDemoData = viewModel::seedDemoData
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

// ------------------------------------------------------------------
// TAB 1: CUENTAS, ROLES Y PERMISOS (RF-50, RF-51)
// ------------------------------------------------------------------
@Composable
private fun CuentasTab(
    state: AdminUiState,
    onSearchChanged: (String) -> Unit,
    onChangeRole: (UserAccount, UserRole) -> Unit,
    onToggleStatus: (UserAccount) -> Unit,
    onAssignVehicle: (UserAccount, String?) -> Unit
) {
    SectionLabel("CUENTAS REGISTRADAS Y PERMISOS (${state.users.size})", RoleAdminColor)
    Spacer(modifier = Modifier.height(8.dp))
    Text(
        text = "Las cuentas se crean cuando cada persona se registra en la app (siempre como Pasajero). " +
                "Desde aquí le asignas su rol, la unidad (si es chofer) o la suspendes.",
        fontSize = 11.sp,
        color = TextSecondary
    )
    Spacer(modifier = Modifier.height(12.dp))

    AppTextField(
        value = state.search,
        onValueChange = onSearchChanged,
        label = "Buscar por nombre o correo",
        accent = RoleAdminColor
    )
    Spacer(modifier = Modifier.height(10.dp))

    if (state.visibleUsers.isEmpty()) {
        Text(
            text = "No hay cuentas que coincidan.",
            fontSize = 12.sp,
            color = TextSecondary,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
    }

    state.visibleUsers.forEach { account ->
        UserCard(
            account = account,
            isMe = state.currentEmail?.equals(account.email, ignoreCase = true) == true,
            unitPlates = state.unitPlates,
            onChangeRole = { role -> onChangeRole(account, role) },
            onToggleStatus = { onToggleStatus(account) },
            onAssignVehicle = { plate -> onAssignVehicle(account, plate) }
        )
    }
}

@Composable
private fun UserCard(
    account: UserAccount,
    isMe: Boolean,
    unitPlates: List<String>,
    onChangeRole: (UserRole) -> Unit,
    onToggleStatus: () -> Unit,
    onAssignVehicle: (String?) -> Unit
) {
    val suspended = account.status == AccountStatus.SUSPENDIDO

    FormCard(
        modifier = Modifier.padding(vertical = 4.dp),
        borderColor = account.role.accentColor.copy(alpha = 0.3f)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = account.fullName + if (isMe) " (tú)" else "",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (suspended) TextMuted else TextPrimary
                )
                Text(text = account.email, fontSize = 11.sp, color = TextSecondary)
            }
            Spacer(modifier = Modifier.width(8.dp))
            StatusPill(
                text = account.status.label,
                color = if (suspended) CyberRose else CyberEmerald
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            UserRole.entries.forEach { role ->
                RoleChip(
                    role = role,
                    selected = account.role == role,
                    modifier = Modifier.weight(1f),
                    onClick = { onChangeRole(role) }
                )
            }
        }

        // Solo los choferes tienen una unidad asignada
        if (account.role == UserRole.CHOFER) {
            Spacer(modifier = Modifier.height(10.dp))
            Text("Unidad asignada", fontSize = 12.sp, color = TextSecondary)
            Spacer(modifier = Modifier.height(6.dp))
            DropdownSelector(
                value = account.assignedVehicleId ?: NO_UNIT,
                options = listOf(NO_UNIT) + unitPlates,
                onSelect = { onAssignVehicle(it.takeUnless { v -> v == NO_UNIT }) }
            )
        }

        TextButton(onClick = onToggleStatus) {
            Text(
                text = if (suspended) "Reactivar cuenta" else "Suspender cuenta",
                fontSize = 12.sp,
                color = if (suspended) CyberEmerald else CyberRose
            )
        }
    }
}

@Composable
private fun RoleChip(
    role: UserRole,
    selected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .clickable { onClick() },
        color = if (selected) role.accentColor.copy(alpha = 0.2f) else DarkSurfaceVariant,
        border = BorderStroke(1.dp, if (selected) role.accentColor else CyberBorder)
    ) {
        Text(
            text = role.displayName,
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            color = if (selected) role.accentColor else TextSecondary,
            textAlign = TextAlign.Center,
            maxLines = 1,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        )
    }
}