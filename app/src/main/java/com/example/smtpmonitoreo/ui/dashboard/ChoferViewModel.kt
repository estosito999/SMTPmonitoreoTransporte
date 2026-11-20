package com.example.smtpmonitoreo.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smtpmonitoreo.BuildConfig
import com.example.smtpmonitoreo.data.model.RouteModel
import com.example.smtpmonitoreo.data.model.TransportUnit
import com.example.smtpmonitoreo.data.repository.MonitoringRepository
import com.example.smtpmonitoreo.domain.UnitLinkResult
import com.example.smtpmonitoreo.domain.UnitLinker
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** Modos de envío de GPS (RNF-23, RNF-24). El intervalo se aplicará al servicio real en el Bloque 5. */
enum class GpsMode(val seconds: Int, val label: String, val sub: String) {
    ECO(15, "Eco", "Ahorro Máx."),
    NORMAL(5, "Normal", "Recomendado"),
    PRECISO(3, "Preciso", "Tráfico Alto")
}

data class ChoferUiState(
    // En debug se precarga la placa de la unidad demo; en release arranca vacía.
    val plateInput: String = if (BuildConfig.DEMO_MODE) "4521-FGH" else "",
    val gpsMode: GpsMode = GpsMode.NORMAL,
    val units: List<TransportUnit> = emptyList(),
    val routes: List<RouteModel> = emptyList(),
    val isBusy: Boolean = false,
    val error: String? = null
) {
    val link: UnitLinkResult get() = UnitLinker.resolve(plateInput, units)
    val currentUnit: TransportUnit? get() = (link as? UnitLinkResult.Linked)?.unit
    val isTransmitting: Boolean get() = currentUnit?.isTransmitting == true
}

class ChoferViewModel(
    private val monitoring: MonitoringRepository
) : ViewModel() {

    private val _state = MutableStateFlow(ChoferUiState())
    val uiState: StateFlow<ChoferUiState> = _state.asStateFlow()

    init {
        combine(
            monitoring.activeUnitsFlow,
            monitoring.routesFlow
        ) { units, routes -> units to routes }
            .onEach { (units, routes) ->
                _state.update { it.copy(units = units, routes = routes) }
            }
            .launchIn(viewModelScope)
    }

    fun onPlateChanged(value: String) {
        // La placa queda bloqueada mientras la unidad transmite.
        if (_state.value.isTransmitting) return
        _state.update { it.copy(plateInput = value.uppercase().take(10), error = null) }
    }

    fun onGpsModeSelected(mode: GpsMode) =
        _state.update { it.copy(gpsMode = mode) }

    /** Inicia o finaliza el recorrido (RF-31, RF-32). */
    fun toggleTransmission() {
        val s = _state.value
        if (s.isBusy) return

        val unit = when (val link = s.link) {
            is UnitLinkResult.Linked -> link.unit
            UnitLinkResult.InvalidPlate -> {
                showError("Placa inválida. Usa el formato 1234-ABC.")
                return
            }
            is UnitLinkResult.NotRegistered -> {
                showError("La placa ${link.plate} no está registrada. Pide a la Alcaldía que registre tu unidad.")
                return
            }
        }

        val stopping = unit.isTransmitting
        viewModelScope.launch {
            _state.update { it.copy(isBusy = true, error = null) }
            try {
                if (stopping) {
                    monitoring.stopGpsTransmission(unit.plate)
                } else {
                    monitoring.startGpsTransmission(unit.plate)
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                showError(e.localizedMessage ?: "No se pudo cambiar el estado de la transmisión.")
            } finally {
                _state.update { it.copy(isBusy = false) }
            }
        }
    }

    private fun showError(message: String) =
        _state.update { it.copy(error = message) }
}