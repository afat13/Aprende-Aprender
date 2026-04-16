package com.example.aprendeaprender

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.aprendeaprender.ui.theme.AprendeAprenderTheme
import androidx.navigation.compose.rememberNavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.aprendeaprender.ui.screens.auth.login.*
import com.example.aprendeaprender.ui.screens.auth.splash.*
import com.example.aprendeaprender.ui.screens.home.*
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AprendeAprenderTheme {
                NavegacionInterna()
            }
        }
    }
}

@Composable
fun NavegacionInterna() {
    val PantallaActual = rememberNavController()

    NavHost(
        navController = PantallaActual,
        startDestination = "splash"
    ) {
        composable("splash") {
            SplashScreen(
                onNavegarAlLogin = {
                    PantallaActual.navigate("login") {
                        popUpTo("splash") { inclusive = true }
                    }
                },
                onNavegarAlHome = {
                    PantallaActual.navigate("home") {
                        popUpTo("splash") { inclusive = true }
                    }
                },
                estoyloggueado = false
            )
        }

        composable("login") {
            LoginScreen()
        }

        composable("home") {
            HomeScreen()
        }
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    AprendeAprenderTheme {
        Greeting("Android")
    }
}