package com.example.smtpmonitoreo.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smtpmonitoreo.data.model.IncidentCategory
import com.example.smtpmonitoreo.data.model.IncidentReport
import com.example.smtpmonitoreo.data.model.RouteModel
import com.example.smtpmonitoreo.data.model.TransportUnit
import com.example.smtpmonitoreo.data.repository.AuthRepository
import com.example.smtpmonitoreo.data.repository.MonitoringRepository
import com.example.smtpmonitoreo.domain.EtaCalculator
import com.example.smtpmonitoreo.domain.LaPazPlaces
import com.example.smtpmonitoreo.domain.PlateValidator
import com.example.smtpmonitoreo.domain.RouteSearch
import com.example.smtpmonitoreo.domain.RouteSearchResult
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Locale
import com.example.smtpmonitoreo.data.repository.WriteStatus
import com.example.smtpmonitoreo.domain.AppMonitor

enum class PasajeroTab { MAPA, RUTAS, REPORTAR }

data class PasajeroUiState(
    val tab: PasajeroTab = PasajeroTab.MAPA,

    // Mapa en vivo
    val units: List<TransportUnit> = emptyList(),
    val routes: List<RouteModel> = emptyList(),
    val selectedUnitId: String? = null,
    val etaMinutes: Int? = null,

    // Buscador de rutas
    val origin: String = "Pérez Velasco",
    val destination: String = "Ceja El Alto",
    val searchResult: RouteSearchResult? = null,
    val searchMessage: String? = null,

    // Reporte de incidentes
    val reportCategory: IncidentCategory = IncidentCategory.COBRO_IRREGULAR,
    val reportPlate: String = "",
    val reportDescription: String = "",
    val reportError: String? = null,
    val reportSent: Boolean = false,
    val reportQueued: Boolean = false,
    val isSending: Boolean = false,
    val myIncidents: List<IncidentReport> = emptyList()
) {
    val selectedUnit: TransportUnit?
        get() = units.find { it.id == selectedUnitId } ?: units.firstOrNull { it.isTransmitting }
}

class PasajeroViewModel(
    private val monitoring: MonitoringRepository,
    private val auth: AuthRepository,
    private val eta: EtaCalculator,
    private val monitor: AppMonitor
) : ViewModel() {

    private val _state = MutableStateFlow(PasajeroUiState())
    val uiState: StateFlow<PasajeroUiState> = _state.asStateFlow()

    private var etaJob: Job? = null
    private var lastEtaKey: String? = null

    init {
        combine(
            monitoring.activeUnitsFlow,
            monitoring.routesFlow,
            monitoring.incidentsFlow,
            auth.currentUserFlow
        ) { units, routes, incidents, user ->
            val mine = incidents.filter { user != null && it.userEmail.equals(user.email, ignoreCase = true) }
            Triple(units, routes, mine)
        }.onEach { (units, routes, mine) ->
            _state.update { it.copy(units = units, routes = routes, myIncidents = mine) }
            refreshEta()
        }.launchIn(viewModelScope)
    }

    // ---------- Navegación y mapa ----------

    fun onTabSelected(tab: PasajeroTab) =
        _state.update { it.copy(tab = tab, reportSent = false) }

    fun onUnitSelected(unit: TransportUnit) {
        _state.update { it.copy(selectedUnitId = unit.id) }
        refreshEta()
    }

    // ---------- Buscador de rutas ----------

    fun onOriginSelected(name: String) {
        _state.update { it.copy(origin = name, searchResult = null, searchMessage = null) }
        refreshEta()
    }

    fun onDestinationSelected(name: String) =
        _state.update { it.copy(destination = name, searchResult = null, searchMessage = null) }

    fun searchRoute() {
        val s = _state.value
        if (s.origin.equals(s.destination, ignoreCase = true)) {
            _state.update {
                it.copy(searchResult = null, searchMessage = "El origen y el destino deben ser distintos.")
            }
            return
        }
        val result = RouteSearch.find(s.routes, s.origin, s.destination)
        _state.update {
            it.copy(
                searchResult = result,
                searchMessage = if (result == null) {
                    "No hay una ruta directa entre ${s.origin} y ${s.destination}."
                } else null
            )
        }
    }

    // ---------- Reporte de incidentes ----------

    fun onCategorySelected(category: IncidentCategory) =
        _state.update { it.copy(reportCategory = category, reportSent = false) }

    fun onPlateChanged(value: String) =
        _state.update { it.copy(reportPlate = value.uppercase().take(10), reportError = null, reportSent = false) }

    fun onDescriptionChanged(value: String) =
        _state.update { it.copy(reportDescription = value.take(300), reportError = null, reportSent = false) }

    /** Atajo desde la ficha de la unidad: abre el formulario con la placa ya cargada. */
    fun reportSelectedUnit() {
        val unit = _state.value.selectedUnit ?: return
        _state.update {
            it.copy(tab = PasajeroTab.REPORTAR, reportPlate = unit.plate, reportError = null, reportSent = false)
        }
    }

    fun submitReport() {
        val s = _state.value
        val plate = PlateValidator.normalize(s.reportPlate)
        val user = auth.getCurrentUser()

        val error = when {
            user == null -> "Inicia sesión para enviar un reporte."
            plate == null -> "Placa inválida. Usa el formato 1234-ABC."
            s.reportDescription.isBlank() -> "Describe brevemente lo ocurrido."
            else -> null
        }
        if (error != null || plate == null || user == null) {
            _state.update { it.copy(reportError = error) }
            return
        }

        val line = s.units.find { it.plate == plate }?.lineNumber.orEmpty()
        _state.update { it.copy(isSending = true, reportError = null) }

        viewModelScope.launch {
            monitoring.submitIncident(
                IncidentReport(
                    userEmail = user.email,
                    vehiclePlate = plate,
                    lineNumber = line,
                    category = s.reportCategory,
                    description = s.reportDescription.trim()
                )
            ).onSuccess { status ->
                _state.update {
                    it.copy(
                        isSending = false,
                        reportPlate = "",
                        reportDescription = "",
                        reportSent = true,
                        reportQueued = status == WriteStatus.QUEUED
                    )
                }
                monitor.trackEvent(
                    "incident_reported",
                    mapOf(
                        "category" to s.reportCategory.name,
                        "queued" to (status == WriteStatus.QUEUED).toString()
                    )
                )
            }.onFailure { e ->
                _state.update {
                    it.copy(isSending = false, reportError = e.localizedMessage ?: "No se pudo enviar el reporte.")
                }
            }
        }
    }

    // ---------- ETA (RF-14) ----------

    /**
     * ETA de la unidad seleccionada hacia tu parada (el origen elegido), por calles.
     * Solo se recalcula si cambió la unidad, la parada o la posición (~100 m).
     */
    private fun refreshEta() {
        val s = _state.value
        val unit = s.selectedUnit
        val target = LaPazPlaces.find(s.origin)

        if (unit == null || target == null) {
            etaJob?.cancel()
            lastEtaKey = null
            _state.update { it.copy(etaMinutes = null) }
            return
        }

        val key = String.format(
            Locale.US, "%s|%.3f|%.3f|%s|%d",
            unit.id, unit.latitude, unit.longitude, s.origin, (unit.speedKmh / 5).toInt()
        )
        if (key == lastEtaKey) return
        lastEtaKey = key

        etaJob?.cancel()
        etaJob = viewModelScope.launch {
            val minutes = eta.minutes(unit.latitude to unit.longitude, target, unit.speedKmh)
            _state.update { it.copy(etaMinutes = minutes) }
        }
    }
}