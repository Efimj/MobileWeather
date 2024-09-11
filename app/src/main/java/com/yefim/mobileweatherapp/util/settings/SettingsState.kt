package com.yefim.mobileweatherapp.util.settings

import androidx.annotation.Keep
import com.yefim.mobileweatherapp.util.DateTimeUtil
import com.yefim.openmeteoapi.model.DayWeatherData
import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.Serializable

@Keep
enum class NightMode {
    Light,
    Dark,
    System,
}

@Serializable
data class UserLocation(val address: String, val longitude: Double, val latitude: Double)

@Serializable
data class WeatherForecast(
    val location: UserLocation,
    val weatherForecast: Set<DayWeatherData> = emptySet(),
    val lastInundationDate: LocalDateTime = DateTimeUtil.getLocalDateTime(),
)

@Keep
@Serializable
data class SettingsState(
    val checkUpdates: Boolean = true,
    val secureMode: Boolean = false,
    val nightMode: NightMode = NightMode.System,
    val selectedWeatherForecast: WeatherForecast? = null,
    val weatherForecasts: Set<WeatherForecast> = emptySet(),
)