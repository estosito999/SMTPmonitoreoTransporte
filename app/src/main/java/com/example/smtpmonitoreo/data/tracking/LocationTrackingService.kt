package com.example.smtpmonitoreo.data.tracking

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.location.Location
import android.location.LocationManager
import android.os.Build
import android.os.IBinder
import android.os.Looper
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import androidx.core.location.LocationManagerCompat
import com.example.smtpmonitoreo.data.repository.MonitoringRepository
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import com.example.smtpmonitoreo.domain.NetworkMonitor

class LocationTrackingService : Service() {

    private val monitoring: MonitoringRepository by inject()
    private val network: NetworkMonitor by inject()
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val fused by lazy { LocationServices.getFusedLocationProviderClient(this) }

    private var callback: LocationCallback? = null
    private var plate: String? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_STOP) {
            stopSelf()
            return START_NOT_STICKY
        }

        val newPlate = intent?.getStringExtra(EXTRA_PLATE)
        if (newPlate == null) {
            stopSelf()
            return START_NOT_STICKY
        }
        val seconds = intent.getIntExtra(EXTRA_INTERVAL_SECONDS, DEFAULT_INTERVAL_SECONDS)

        // Debe llamarse enseguida tras startForegroundService
        startInForeground(newPlate)
        beginUpdates(newPlate, seconds)
        return START_NOT_STICKY
    }

    private fun beginUpdates(newPlate: String, seconds: Int) {
        TrackingState.setError(null)

        val locationManager = getSystemService(LocationManager::class.java)
        if (!LocationManagerCompat.isLocationEnabled(locationManager)) {
            fail("Activa el GPS del teléfono para transmitir.")
            return
        }

        val firstStart = plate == null
        plate = newPlate
        callback?.let { fused.removeLocationUpdates(it) }

        val intervalMs = seconds * 1_000L
        // Modo Eco: ubicación de bajo consumo; Normal y Preciso: alta precisión
        val priority = if (seconds >= ECO_SECONDS) {
            Priority.PRIORITY_BALANCED_POWER_ACCURACY
        } else {
            Priority.PRIORITY_HIGH_ACCURACY
        }
        val request = LocationRequest.Builder(priority, intervalMs)
            .setMinUpdateIntervalMillis(intervalMs / 2)
            .build()

        val newCallback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                result.lastLocation?.let { publish(newPlate, it) }
            }
        }
        callback = newCallback

        try {
            fused.requestLocationUpdates(request, newCallback, Looper.getMainLooper())
        } catch (e: SecurityException) {
            fail("Falta el permiso de ubicación precisa.")
            return
        }

        TrackingState.setRunning(true)
        if (firstStart) {
            serviceScope.launch {
                try {
                    monitoring.startGpsTransmission(newPlate)
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Exception) {
                    Log.w(TAG, "No se pudo marcar la unidad como en ruta", e)
                }
            }
        }
    }

    private fun publish(plate: String, location: Location) {
        // Sin conexión no se acumulan posiciones viejas: se retoma solo al volver la señal
        if (!network.isOnlineNow()) return
        val speedKmh = if (location.hasSpeed()) location.speed * 3.6f else 0f
        val heading = if (location.hasBearing()) location.bearing else 0f
        serviceScope.launch {
            try {
                monitoring.updateVehicleLocation(
                    plate, location.latitude, location.longitude, speedKmh, heading
                )
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.w(TAG, "No se pudo enviar la posición", e)
            }
        }
    }

    private fun fail(message: String) {
        TrackingState.setError(message)
        stopSelf()
    }

    private fun startInForeground(plate: String) {
        ensureChannel()
        val stopIntent = PendingIntent.getService(
            this,
            0,
            Intent(this, LocationTrackingService::class.java).setAction(ACTION_STOP),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_menu_mylocation)
            .setContentTitle("SMTP Paceño · Transmitiendo")
            .setContentText("Compartiendo la ubicación de la unidad $plate")
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .addAction(android.R.drawable.ic_menu_close_clear_cancel, "Detener", stopIntent)
            .build()

        ServiceCompat.startForeground(
            this,
            NOTIFICATION_ID,
            notification,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION
            } else {
                0
            }
        )
    }

    private fun ensureChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(
                NotificationChannel(CHANNEL_ID, "Transmisión de ubicación", NotificationManager.IMPORTANCE_LOW)
            )
        }
    }

    override fun onDestroy() {
        callback?.let { fused.removeLocationUpdates(it) }
        callback = null

        val lastPlate = plate
        plate = null
        TrackingState.setRunning(false)

        if (lastPlate != null) {
            // Escritura final: no debe cancelarse junto con el servicio
            CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
                try {
                    monitoring.stopGpsTransmission(lastPlate)
                } catch (e: Exception) {
                    Log.w(TAG, "No se pudo marcar la unidad como detenida", e)
                }
            }
        }
        serviceScope.cancel()
        super.onDestroy()
    }

    companion object {
        const val EXTRA_PLATE = "extra_plate"
        const val EXTRA_INTERVAL_SECONDS = "extra_interval_seconds"
        const val ACTION_STOP = "com.example.smtpmonitoreo.action.STOP_TRACKING"

        private const val TAG = "LocationTracking"
        private const val CHANNEL_ID = "tracking_channel"
        private const val NOTIFICATION_ID = 4001
        private const val DEFAULT_INTERVAL_SECONDS = 5
        private const val ECO_SECONDS = 15
    }
}