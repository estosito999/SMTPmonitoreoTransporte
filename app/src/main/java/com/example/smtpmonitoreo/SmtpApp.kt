package com.example.smtpmonitoreo

import android.app.Application
import org.osmdroid.config.Configuration

class SmtpApp : Application() {
    override fun onCreate() {
        super.onCreate()
        Configuration.getInstance().apply {
            load(
                this@SmtpApp,
                getSharedPreferences("osmdroid", MODE_PRIVATE)
            )
            userAgentValue = packageName
        }
    }
}