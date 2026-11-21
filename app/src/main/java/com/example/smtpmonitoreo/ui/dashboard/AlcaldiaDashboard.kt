package com.example.smtpmonitoreo.ui.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.smtpmonitoreo.data.model.UserAccount
import com.example.smtpmonitoreo.data.model.UserRole
import com.example.smtpmonitoreo.ui.components.CountBars
import com.example.smtpmonitoreo.ui.components.DashboardTopBar
import com.example.smtpmonitoreo.ui.components.DropdownSelector
import com.example.smtpmonitoreo.ui.components.FormCard
import com.example.smtpmonitoreo.ui.components.ConnectionBanner
import com.example.smtpmonitoreo.ui.components.NoticeBanner
import com.example.smtpmonitoreo.ui.components.SectionLabel
import com.example.smtpmonitoreo.ui.components.SegmentedTabItem
import com.example.smtpmonitoreo.ui.components.SmtpMapView
import com.example.smtpmonitoreo.ui.theme.*
import org.koin.compose.viewmodel.koinViewModel

private data class TabSpec(val tab: AlcaldiaTab, val label: String, val icon: ImageVector)

private val alcaldiaTabs = listOf(
    TabSpec(AlcaldiaTab.SUPERVISION, "Supervisión", Icons.Default.Map),
    TabSpec(AlcaldiaTab.GESTION, "Rutas y Unidades", Icons.Default.EditRoad),
    TabSpec(AlcaldiaTab.INCIDENTES, "Incidentes", Icons.Default.ReportProblem),
    TabSpec(AlcaldiaTab.FISCALIZACION, "Fiscalización", Icons.Default.AssignmentLate)
)

@Composable
fun AlcaldiaDashboard(
    user: UserAccount,
    onLogout: () -> Unit,
    viewModel: AlcaldiaViewModel = koinViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

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

        // Banner de Conectividad (RNF-18) — se hace real en el bloque de conectividad
        ConnectionBanner()

        // Tabs de Control Municipal (desplazables: 4 no caben repartidas)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 10.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(DarkSurfaceVariant)
                .horizontalScroll(rememberScrollState())
                .padding(4.dp)
        ) {
            alcaldiaTabs.forEach { spec ->
                SegmentedTabItem(
                    text = spec.label,
                    icon = spec.icon,
                    selected = state.tab == spec.tab,
                    accentColor = RoleAlcaldiaColor,
                    horizontalPadding = 14.dp,
                    onClick = { viewModel.onTabSelected(spec.tab) }
                )
            }
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
                AlcaldiaTab.SUPERVISION -> SupervisionTab(
                    state = state,
                    onLineFilterSelected = viewModel::onLineFilterSelected
                )

                AlcaldiaTab.GESTION -> GestionTab(
                    state = state,
                    onRouteDraftChanged = viewModel::onRouteDraftChanged,
                    onSaveRoute = viewModel::saveRoute,
                    onUnitDraftChanged = viewModel::onUnitDraftChanged,
                    onSaveUnit = viewModel::saveUnit
                )

                AlcaldiaTab.INCIDENTES -> IncidentesTab(
                    state = state,
                    onFilterSelected = viewModel::onIncidentFilterSelected,
                    onStartReview = viewModel::startReview,
                    onAskToResolve = viewModel::askToResolve,
                    onNoteChanged = viewModel::onResolutionNoteChanged,
                    onConfirmResolve = viewModel::confirmResolve,
                    onDismissResolve = viewModel::dismissResolve
                )

                AlcaldiaTab.FISCALIZACION -> FiscalizacionTab(
                    state = state,
                    onDraftChanged = viewModel::onInfractionDraftChanged,
                    onSave = viewModel::saveInfraction,
                    onMarkPaid = viewModel::markInfractionPaid
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

// ------------------------------------------------------------------
// TAB 1: SUPERVISIÓN EN MAPA GENERAL Y ESTADÍSTICAS (RF-42, reportes)
// ------------------------------------------------------------------
@Composable
internal fun SupervisionTab(
    state: AlcaldiaUiState,
    onLineFilterSelected: (String) -> Unit
) {
    SectionLabel("MAPA GENERAL DE MUNICIPALIDAD LA PAZ", RoleAlcaldiaColor)
    Spacer(modifier = Modifier.height(10.dp))

    DropdownSelector(
        value = "LÍNEA: ${state.lineFilter ?: ALL_LINES}",
        options = state.lineOptions,
        onSelect = onLineFilterSelected
    )

    Spacer(modifier = Modifier.height(10.dp))

    SmtpMapView(
        units = state.filteredUnits,
        routes = state.routes,
        modifier = Modifier
            .fillMaxWidth()
            .height(280.dp)
    )

    Spacer(modifier = Modifier.height(16.dp))

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        KpiCard(
            title = "Unidades Activas",
            count = "${state.stats.activeUnits}",
            icon = Icons.Default.DirectionsBus,
            accentColor = RoleAlcaldiaColor,
            modifier = Modifier.weight(1f)
        )
        KpiCard(
            title = "Rutas Oficiales",
            count = "${state.routes.size}",
            icon = Icons.Default.AltRoute,
            accentColor = CyberCyan,
            modifier = Modifier.weight(1f)
        )
        KpiCard(
            title = "Reportes Hoy",
            count = "${state.stats.incidentsToday}",
            icon = Icons.Default.Warning,
            accentColor = CyberRose,
            modifier = Modifier.weight(1f)
        )
    }

    Spacer(modifier = Modifier.height(20.dp))

    SectionLabel("INCIDENCIAS POR CATEGORÍA", CyberRose)
    Spacer(modifier = Modifier.height(8.dp))
    FormCard { CountBars(items = state.stats.incidentsByCategory, color = CyberRose) }

    Spacer(modifier = Modifier.height(16.dp))

    SectionLabel("LÍNEAS CRÍTICAS (MÁS INCIDENCIAS)", CyberAmber)
    Spacer(modifier = Modifier.height(8.dp))
    FormCard { CountBars(items = state.stats.incidentsByLine, color = CyberAmber) }

    Spacer(modifier = Modifier.height(16.dp))

    SectionLabel("FRECUENCIA: UNIDADES ACTIVAS POR LÍNEA", CyberCyan)
    Spacer(modifier = Modifier.height(8.dp))
    FormCard {
        CountBars(
            items = state.stats.activeUnitsByLine,
            color = CyberCyan,
            emptyText = "No hay unidades transmitiendo."
        )
    }
}

@Composable
private fun KpiCard(
    title: String,
    count: String,
    icon: ImageVector,
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