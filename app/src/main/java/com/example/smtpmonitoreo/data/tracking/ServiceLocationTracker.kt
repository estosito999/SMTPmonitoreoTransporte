package com.example.smtpmonitoreo.data.tracking

import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat
import kotlinx.coroutines.flow.StateFlow

class ServiceLocationTracker(private val context: Context) : LocationTracker {

    override val isRunning: StateFlow<Boolean> = TrackingState.running
    override val error: StateFlow<String?> = TrackingState.error

    override fun start(plate: String, intervalSeconds: Int) {
        TrackingState.setError(null)
        val intent = Intent(context, LocationTrackingService::class.java)
            .putExtra(LocationTrackingService.EXTRA_PLATE, plate)
            .putExtra(LocationTrackingService.EXTRA_INTERVAL_SECONDS, intervalSeconds)
        ContextCompat.startForegroundService(context, intent)
    }

    override fun stop() {
        context.stopService(Intent(context, LocationTrackingService::class.java))
    }
}