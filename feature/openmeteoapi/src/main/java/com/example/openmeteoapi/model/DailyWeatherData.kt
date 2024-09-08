package com.example.openmeteoapi.model

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

data class DailyWeatherData(
    val date: LocalDate,
    val temperature: List<Double>,
    val relativeHumidity: List<Int>,
    val weatherHourly: List<WeatherCode>,
    val weather: WeatherCode,
    val temperatureMax: Double,
    val temperatureMin: Double,
    val apparentTemperatureMax: Double,
    val apparentTemperatureMin: Double,
    val sunrise: LocalDateTime,
    val sunset: LocalDateTime,
    val daylightDuration: Double,
    val sunshineDuration: Double,
    val uvIndexMax: Double,
    val uvIndexClearSkyMax: Double,
    val precipitationSum: Double,
    val precipitationProbabilityMax: Int,
    val windSpeedMax: Double,
    val windGustsMax: Double,
    val windDirectionDominant: Int,
) {
    fun checkIsNight(time: LocalTime): Boolean {
        return (time.isAfter(this.sunrise.toLocalTime()) && time.isBefore(this.sunset.toLocalTime())).not()
    }
}