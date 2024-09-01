package com.example.mobileweatherapp

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import com.example.mobileweatherapp.screen.location.LocationScreen
import com.example.mobileweatherapp.screen.weather.WeatherScreen
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
        super.onCreate(savedInstanceState)

        actionBar?.hide()
        enableEdgeToEdge()

        setContent {
            MobileWeatherAppTheme(
                darkTheme = when (settings.nightMode) {
                    NightMode.Light -> false
                    NightMode.Dark -> true
                    else -> isSystemInDarkTheme()
                },
            ) {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Column(modifier = Modifier.padding(innerPadding)) {

                        LocationScreen()
//                        WeatherScreen()
                    }
                }
            }
        }
    }
}