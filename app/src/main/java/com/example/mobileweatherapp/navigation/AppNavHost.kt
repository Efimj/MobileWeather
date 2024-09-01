package com.example.mobileweatherapp.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.mobileweatherapp.screen.location.LocationScreen
import com.example.mobileweatherapp.screen.weather.WeatherScreen

@Composable
fun AppNavHost(
    navController: NavHostController,
    startDestination: Screen = Screen.Weather,
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable<Screen.Weather>() {
            WeatherScreen(navController)
        }
        composable<Screen.Location>() {
            LocationScreen(navController)
        }
    }
}