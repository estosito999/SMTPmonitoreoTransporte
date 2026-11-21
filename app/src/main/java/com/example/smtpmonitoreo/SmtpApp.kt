package com.example.smtpmonitoreo

import android.app.Application
import com.example.smtpmonitoreo.di.appModules
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import org.osmdroid.config.Configuration
import java.io.File
import com.example.smtpmonitoreo.data.repository.AuthRepository
import com.example.smtpmonitoreo.data.repository.WriteErrorReporter
import com.example.smtpmonitoreo.domain.AppMonitor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.collect
import org.koin.android.ext.android.get

class SmtpApp : Application() {

    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    override fun onCreate() {
        super.onCreate()

        Configuration.getInstance().apply {
            load(this@SmtpApp, getSharedPreferences("osmdroid", MODE_PRIVATE))
            userAgentValue = "SMTPPaceno/${BuildConfig.VERSION_NAME} " +
                    "(Android; UMSA Sistemas Moviles II; coddycasas@gmail.com)"
            osmdroidBasePath = File(filesDir, "osmdroid")
            osmdroidTileCache = File(osmdroidBasePath, "tiles")
            tileFileSystemCacheMaxBytes = 300L * 1024 * 1024
            tileFileSystemCacheTrimBytes = 250L * 1024 * 1024
            expirationOverrideDuration = 7L * 24 * 60 * 60 * 1000
        }

        startKoin {
            androidContext(this@SmtpApp)
            modules(appModules)
        }

        // Monitoreo: errores no fatales de las escrituras, y usuario y rol en Crashlytics y Analytics
        val monitor: AppMonitor = get()
        WriteErrorReporter.onError = { monitor.recordError(it, "ESCRITURA") }
        val auth: AuthRepository = get()
        appScope.launch { auth.currentUserFlow.collect { monitor.onSessionChanged(it) } }
    }
}