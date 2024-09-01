package com.example.mobileweatherapp

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.example.mobileweatherapp.navigation.AppNavHost
import com.example.mobileweatherapp.navigation.Screen
import com.example.mobileweatherapp.ui.helper.LocaleProvider
import com.example.mobileweatherapp.ui.theme.MobileWeatherAppTheme
import com.example.mobileweatherapp.util.settings.NightMode
import com.example.mobileweatherapp.util.settings.SettingsManager
import com.example.mobileweatherapp.util.settings.SettingsManager.settings

class MainActivity : ComponentActivity() {
    override fun attachBaseContext(newBase: Context) {
        SettingsManager.init(newBase)
        super.attachBaseContext(newBase)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        actionBar?.hide()
        super.onCreate(savedInstanceState)

        setContent {
            MobileWeatherAppTheme(
                darkTheme = when (settings.nightMode) {
                    NightMode.Light -> false
                    NightMode.Dark -> true
                    else -> isSystemInDarkTheme()
                },
            ) {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    CompositionLocalProvider(LocaleProvider.LocalInsetsPaddings provides innerPadding) {
                        AppNavHost(
                            navController = rememberNavController(),
                            startDestination = getStartDestination()
                        )
                    }
                }
            }
        }
    }

    fun getStartDestination(): Screen {
        return if (settings.location == null) {
            Screen.Location
        } else {
            Screen.Weather
        }
    }
}