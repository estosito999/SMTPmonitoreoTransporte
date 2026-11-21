package com.example.smtpmonitoreo.data.monitoring

import android.os.Bundle
import com.example.smtpmonitoreo.BuildConfig
import com.example.smtpmonitoreo.data.model.LogLevel
import com.example.smtpmonitoreo.data.model.SystemLog
import com.example.smtpmonitoreo.data.model.UserAccount
import com.example.smtpmonitoreo.data.repository.awaitOrQueue
import com.example.smtpmonitoreo.data.repository.toMap
import com.example.smtpmonitoreo.domain.AppMonitor
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.util.concurrent.ConcurrentHashMap

class FirebaseAppMonitor(
    private val db: FirebaseFirestore,
    private val firebaseAuth: FirebaseAuth,
    private val crashlytics: FirebaseCrashlytics,
    private val analytics: FirebaseAnalytics
) : AppMonitor {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val logged = ConcurrentHashMap.newKeySet<String>()

    init {
        crashlytics.setCustomKey("build", if (BuildConfig.DEMO_MODE) "debug" else "release")
    }

    override fun log(level: LogLevel, source: String, message: String, dedupeKey: String?) {
        if (dedupeKey != null && !logged.add(dedupeKey)) return

        val text = MonitoringRules.truncate(message)
        crashlytics.log("[${level.label}] $source: $text")

        // Las reglas de Firestore exigen sesión para escribir en logs
        if (firebaseAuth.currentUser == null) return

        scope.launch {
            try {
                val entry = SystemLog(
                    timestamp = System.currentTimeMillis(),
                    level = level,
                    source = source,
                    message = text
                )
                db.collection("logs").add(entry.toMap()).awaitOrQueue()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                // Un log nunca debe romper la app
            }
        }
    }

    override fun recordError(error: Throwable, context: String) {
        if (!MonitoringRules.shouldReport(error)) return
        crashlytics.log("Contexto: $context")
        crashlytics.recordException(error)
    }

    override fun trackEvent(name: String, params: Map<String, String>) {
        val bundle = Bundle().apply { params.forEach { (key, value) -> putString(key, value) } }
        analytics.logEvent(name, bundle)
    }

    override fun onSessionChanged(user: UserAccount?) {
        crashlytics.setUserId(user?.uid.orEmpty())
        crashlytics.setCustomKey("role", user?.role?.id ?: "none")
        analytics.setUserId(user?.uid)
        analytics.setUserProperty("role", user?.role?.id)
    }
}