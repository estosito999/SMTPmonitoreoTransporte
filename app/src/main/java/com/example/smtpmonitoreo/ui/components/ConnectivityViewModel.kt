package com.example.smtpmonitoreo.ui.components

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smtpmonitoreo.domain.ConnectionState
import com.example.smtpmonitoreo.domain.ConnectionTracker
import com.example.smtpmonitoreo.domain.NetworkMonitor
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

class ConnectivityViewModel(
    monitor: NetworkMonitor
) : ViewModel() {

    private val _state = MutableStateFlow(ConnectionState.ONLINE)
    val state: StateFlow<ConnectionState> = _state.asStateFlow()

    private var restoredJob: Job? = null

    init {
        monitor.isOnline
            .onEach { online ->
                val next = ConnectionTracker.next(_state.value, online)
                _state.value = next

                restoredJob?.cancel()
                if (next == ConnectionState.RESTORED) {
                    restoredJob = viewModelScope.launch {
                        delay(RESTORED_VISIBLE_MS)
                        if (_state.value == ConnectionState.RESTORED) _state.value = ConnectionState.ONLINE
                    }
                }
            }
            .launchIn(viewModelScope)
    }

    private companion object {
        const val RESTORED_VISIBLE_MS = 3_000L
    }
}