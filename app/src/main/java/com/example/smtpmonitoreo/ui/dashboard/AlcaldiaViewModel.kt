package com.example.smtpmonitoreo.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smtpmonitoreo.data.model.IncidentReport
import com.example.smtpmonitoreo.data.model.IncidentStatus
import com.example.smtpmonitoreo.data.model.InfractionModel
import com.example.smtpmonitoreo.data.model.InfractionStatus
import com.example.smtpmonitoreo.data.model.RouteModel
import com.example.smtpmonitoreo.data.model.TransportUnit
import com.example.smtpmonitoreo.data.repository.MonitoringRepository
import com.example.smtpmonitoreo.domain.AlcaldiaStats
import com.example.smtpmonitoreo.domain.InfractionDraft
import com.example.smtpmonitoreo.domain.InfractionRules
import com.example.smtpmonitoreo.domain.RouteDraft
import com.example.smtpmonitoreo.domain.RouteRegistration
import com.example.smtpmonitoreo.domain.StatsCalculator
import com.example.smtpmonitoreo.domain.UnitDraft
import com.example.smtpmonitoreo.domain.UnitRegistration
import com.example.smtpmonitoreo.domain.Validation
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

const val ALL_LINES = "TODAS"

enum class AlcaldiaTab { SUPERVISION, GESTION, INCIDENTES, FISCALIZACION }

data class Notice(val text: String, val isError: Boolean = false)

data class AlcaldiaUiState(
    val tab: AlcaldiaTab = AlcaldiaTab.SUPERVISION,
    val notice: Notice? = null,

    // Datos en vivo
    val units: List<TransportUnit> = emptyList(),
    val routes: List<RouteModel> = emptyList(),
    val incidents: List<IncidentReport> = emptyList(),
    val infractions: List<InfractionModel> = emptyList(),
    val stats: AlcaldiaStats = AlcaldiaStats(),

    // Supervisión
    val lineFilter: String? = null,

    // Gestión de rutas y unidades (RF-40, RF-41)
    val routeDraft: RouteDraft = RouteDraft(),
    val routeError: String? = null,
    val unitDraft: UnitDraft = UnitDraft(),
    val unitError: String? = null,

    // Incidentes (RF-43)
    val incidentFilter: IncidentStatus? = null,
    val resolvingIncident: IncidentReport? = null,
    val resolutionNote: String = "",

    // Fiscalización (RF-44)
    val infractionDraft: InfractionDraft = InfractionDraft(),
    val infractionError: String? = null
) {
    val filteredUnits: List<TransportUnit>
        get() = if (lineFilter == null) units else units.filter { it.lineNumber == lineFilter }

    val lineOptions: List<String>
        get() = listOf(ALL_LINES) + routes.map { it.lineNumber }

    val visibleIncidents: List<IncidentReport>
        get() = incidents
            .filter { incidentFilter == null || it.status == incidentFilter }
            .sortedByDescending { it.timestamp }

    val sortedInfractions: List<InfractionModel>
        get() = infractions.sortedByDescending { it.timestamp }
}

private data class Snapshot(
    val units: List<TransportUnit>,
    val routes: List<RouteModel>,
    val incidents: List<IncidentReport>,
    val infractions: List<InfractionModel>
)

class AlcaldiaViewModel(
    private val monitoring: MonitoringRepository
) : ViewModel() {

    private val _state = MutableStateFlow(AlcaldiaUiState())
    val uiState: StateFlow<AlcaldiaUiState> = _state.asStateFlow()

    init {
        combine(
            monitoring.activeUnitsFlow,
            monitoring.routesFlow,
            monitoring.incidentsFlow,
            monitoring.infractionsFlow
        ) { units, routes, incidents, infractions ->
            Snapshot(units, routes, incidents, infractions)
        }.onEach { snap ->
            _state.update {
                it.copy(
                    units = snap.units,
                    routes = snap.routes,
                    incidents = snap.incidents,
                    infractions = snap.infractions,
                    stats = StatsCalculator.compute(snap.units, snap.incidents, System.currentTimeMillis())
                )
            }
        }.launchIn(viewModelScope)
    }

    // ---------- Navegación y supervisión ----------

    fun onTabSelected(tab: AlcaldiaTab) =
        _state.update { it.copy(tab = tab, notice = null) }

    fun onLineFilterSelected(value: String) =
        _state.update { it.copy(lineFilter = value.takeUnless { v -> v == ALL_LINES }) }

    // ---------- Rutas ----------

    fun onRouteDraftChanged(draft: RouteDraft) =
        _state.update { it.copy(routeDraft = draft, routeError = null, notice = null) }

    fun saveRoute() {
        val s = _state.value
        when (val result = RouteRegistration.validate(s.routeDraft, s.routes)) {
            is Validation.Error -> _state.update { it.copy(routeError = result.message) }
            is Validation.Ok -> viewModelScope.launch {
                monitoring.addRoute(result.value)
                    .onSuccess {
                        _state.update {
                            it.copy(
                                routeDraft = RouteDraft(),
                                routeError = null,
                                notice = Notice("Ruta ${result.value.lineNumber} registrada.")
                            )
                        }
                    }
                    .onFailure { e ->
                        _state.update { it.copy(routeError = e.localizedMessage ?: "No se pudo guardar la ruta.") }
                    }
            }
        }
    }

    // ---------- Unidades y choferes ----------

    fun onUnitDraftChanged(draft: UnitDraft) =
        _state.update { it.copy(unitDraft = draft, unitError = null, notice = null) }

    fun saveUnit() {
        val s = _state.value
        when (val result = UnitRegistration.validate(s.unitDraft, s.units, s.routes)) {
            is Validation.Error -> _state.update { it.copy(unitError = result.message) }
            is Validation.Ok -> viewModelScope.launch {
                monitoring.addUnit(result.value)
                    .onSuccess {
                        _state.update {
                            it.copy(
                                unitDraft = UnitDraft(),
                                unitError = null,
                                notice = Notice("Unidad ${result.value.plate} registrada.")
                            )
                        }
                    }
                    .onFailure { e ->
                        _state.update { it.copy(unitError = e.localizedMessage ?: "No se pudo registrar la unidad.") }
                    }
            }
        }
    }

    // ---------- Incidentes ----------

    fun onIncidentFilterSelected(status: IncidentStatus?) =
        _state.update { it.copy(incidentFilter = status) }

    fun startReview(incident: IncidentReport) {
        viewModelScope.launch {
            monitoring.updateIncidentStatus(incident.id, IncidentStatus.EN_REVISION)
                .onFailure { e ->
                    showNotice(e.localizedMessage ?: "No se pudo actualizar el reporte.", isError = true)
                }
        }
    }

    fun askToResolve(incident: IncidentReport) =
        _state.update { it.copy(resolvingIncident = incident, resolutionNote = "") }

    fun onResolutionNoteChanged(note: String) =
        _state.update { it.copy(resolutionNote = note.take(300)) }

    fun dismissResolve() =
        _state.update { it.copy(resolvingIncident = null, resolutionNote = "") }

    fun confirmResolve() {
        val s = _state.value
        val incident = s.resolvingIncident ?: return
        val note = s.resolutionNote.trim()
        if (note.isEmpty()) return

        viewModelScope.launch {
            monitoring.updateIncidentStatus(incident.id, IncidentStatus.RESUELTO, note)
                .onSuccess {
                    _state.update {
                        it.copy(resolvingIncident = null, resolutionNote = "", notice = Notice("Reporte resuelto."))
                    }
                }
                .onFailure { e ->
                    showNotice(e.localizedMessage ?: "No se pudo resolver el reporte.", isError = true)
                }
        }
    }

    // ---------- Fiscalización ----------

    fun onInfractionDraftChanged(draft: InfractionDraft) =
        _state.update { it.copy(infractionDraft = draft, infractionError = null, notice = null) }

    fun saveInfraction() {
        val s = _state.value
        when (val result = InfractionRules.validate(s.infractionDraft, s.units, System.currentTimeMillis())) {
            is Validation.Error -> _state.update { it.copy(infractionError = result.message) }
            is Validation.Ok -> viewModelScope.launch {
                monitoring.addInfraction(result.value)
                    .onSuccess {
                        _state.update {
                            it.copy(
                                infractionDraft = InfractionDraft(),
                                infractionError = null,
                                notice = Notice("Sanción emitida a ${result.value.vehiclePlate}.")
                            )
                        }
                    }
                    .onFailure { e ->
                        _state.update {
                            it.copy(infractionError = e.localizedMessage ?: "No se pudo emitir la sanción.")
                        }
                    }
            }
        }
    }

    fun markInfractionPaid(infraction: InfractionModel) {
        viewModelScope.launch {
            monitoring.updateInfractionStatus(infraction.id, InfractionStatus.PAGADA)
                .onFailure { e ->
                    showNotice(e.localizedMessage ?: "No se pudo actualizar la sanción.", isError = true)
                }
        }
    }

    private fun showNotice(text: String, isError: Boolean = false) =
        _state.update { it.copy(notice = Notice(text, isError)) }
}