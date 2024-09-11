package com.yefim.mobileweatherapp.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.yefim.mobileweatherapp.screen.location.LocationScreen
import com.yefim.mobileweatherapp.screen.weather.WeatherScreen

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