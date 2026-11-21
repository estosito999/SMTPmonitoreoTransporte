package com.example.smtpmonitoreo

import android.app.Application
import com.example.smtpmonitoreo.di.appModules
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import org.osmdroid.config.Configuration
import java.io.File

class SmtpApp : Application() {
    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidContext(this@SmtpApp)
            modules(appModules)
        }

        Configuration.getInstance().apply {
            load(
                this@SmtpApp,
                getSharedPreferences("osmdroid", MODE_PRIVATE)
            )
            userAgentValue = packageName
            // Caché de teselas en el almacenamiento privado de la app
            osmdroidBasePath = File(cacheDir, "osmdroid")
            osmdroidTileCache = File(osmdroidBasePath, "tiles")
        }
    }
}