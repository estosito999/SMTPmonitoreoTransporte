package com.example.smtpmonitoreo.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smtpmonitoreo.data.model.RouteModel
import com.example.smtpmonitoreo.data.model.TransportUnit
import com.example.smtpmonitoreo.data.repository.AuthRepository
import com.example.smtpmonitoreo.data.repository.MonitoringRepository
import com.example.smtpmonitoreo.data.tracking.LocationTracker
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** Modos de envío de GPS (RNF-23, RNF-24). */
enum class GpsMode(val seconds: Int, val label: String, val sub: String) {
    ECO(15, "Eco", "Ahorro Máx."),
    NORMAL(5, "Normal", "Recomendado"),
    PRECISO(3, "Preciso", "Tráfico Alto")
}

data class ChoferUiState(
    val gpsMode: GpsMode = GpsMode.NORMAL,
    val units: List<TransportUnit> = emptyList(),
    val routes: List<RouteModel> = emptyList(),
    val assignedPlate: String? = null,
    val isTransmitting: Boolean = false,
    val localError: String? = null,
    val trackerError: String? = null
) {
    val currentUnit: TransportUnit?
        get() = assignedPlate?.let { plate -> units.find { it.plate == plate } }

    val error: String? get() = localError ?: trackerError
}

class ChoferViewModel(
    private val monitoring: MonitoringRepository,
    private val auth: AuthRepository,
    private val tracker: LocationTracker
) : ViewModel() {

    private val _state = MutableStateFlow(ChoferUiState())
    val uiState: StateFlow<ChoferUiState> = _state.asStateFlow()

    init {
        // El administrador pudo asignarle (o cambiarle) la unidad desde la última vez
        viewModelScope.launch { auth.refreshProfile() }

        combine(
            monitoring.activeUnitsFlow,
            monitoring.routesFlow,
            auth.currentUserFlow,
            tracker.isRunning,
            tracker.error
        ) { units, routes, user, running, trackerError ->
            ChoferUiState(
                gpsMode = _state.value.gpsMode,
                units = units,
                routes = routes,
                assignedPlate = user?.assignedVehicleId,
                isTransmitting = running,
                localError = _state.value.localError,
                trackerError = trackerError
            )
        }.onEach { fresh -> _state.value = fresh }
            .launchIn(viewModelScope)
    }

    fun onGpsModeSelected(mode: GpsMode) {
        _state.update { it.copy(gpsMode = mode) }
        val s = _state.value
        val plate = s.assignedPlate
        // Si ya transmite, se reinicia el rastreo con el nuevo intervalo
        if (s.isTransmitting && plate != null) tracker.start(plate, mode.seconds)
    }

    /** Se llama cuando ya se concedió el permiso de ubicación (RF-31). */
    fun startTransmission() {
        val s = _state.value
        val plate = s.assignedPlate
        when {
            plate == null ->
                showError("Aún no tienes una unidad asignada. Pide al administrador que te asigne una.")
            s.currentUnit == null ->
                showError("La unidad $plate no está registrada. Contacta a la Alcaldía.")
            else -> {
                _state.update { it.copy(localError = null) }
                tracker.start(plate, s.gpsMode.seconds)
            }
        }
    }

    /** Finaliza el recorrido (RF-32). */
    fun stopTransmission() = tracker.stop()

    fun onPermissionDenied() =
        showError("Necesitas permitir la ubicación precisa para transmitir. Actívala en Ajustes de la app.")

    private fun showError(message: String) =
        _state.update { it.copy(localError = message) }
}