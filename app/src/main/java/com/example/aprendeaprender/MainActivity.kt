package com.example.aprendeaprender

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.aprendeaprender.navigation.AppNavHost
import com.example.aprendeaprender.ui.theme.AprendeAprenderTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            AprendeAprenderTheme {
                AppNavHost()
            }
        }
    }
}