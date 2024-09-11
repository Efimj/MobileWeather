package com.yefim.mobileweatherapp

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.WindowCompat
import androidx.navigation.compose.rememberNavController
import com.yefim.mobileweatherapp.navigation.AppNavHost
import com.yefim.mobileweatherapp.navigation.Screen
import com.yefim.mobileweatherapp.ui.helper.LocaleProvider
import com.yefim.mobileweatherapp.ui.theme.MobileWeatherAppTheme
import com.yefim.mobileweatherapp.util.settings.NightMode
import com.yefim.mobileweatherapp.util.settings.SettingsManager
import com.yefim.mobileweatherapp.util.settings.SettingsManager.settings

class MainActivity : ComponentActivity() {
    override fun attachBaseContext(newBase: Context) {
        SettingsManager.init(newBase)
        super.attachBaseContext(newBase)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        installSplashScreen()
        super.onCreate(savedInstanceState)
        actionBar?.hide()

        setContent {
            MobileWeatherAppTheme(
                darkTheme = when (settings.nightMode) {
                    NightMode.Light -> false
                    NightMode.Dark -> true
                    else -> isSystemInDarkTheme()
                },
            ) {
                Scaffold(modifier = Modifier.fillMaxSize().imePadding()) { innerPadding ->
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
        return if (settings.selectedLocation == null) {
            Screen.Location
        } else {
            Screen.Weather
        }
    }
}