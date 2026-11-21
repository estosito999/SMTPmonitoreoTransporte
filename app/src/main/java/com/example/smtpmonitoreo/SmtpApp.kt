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
    }
}