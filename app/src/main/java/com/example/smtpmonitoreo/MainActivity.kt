package com.example.smtpmonitoreo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.smtpmonitoreo.ui.navigation.SmtpNavGraph
import com.example.smtpmonitoreo.ui.theme.SMTPmonitoreoTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SMTPmonitoreoTheme {
                SmtpNavGraph()
            }
        }
    }
}
