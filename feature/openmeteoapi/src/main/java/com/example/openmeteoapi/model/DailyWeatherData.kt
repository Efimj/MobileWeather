package com.example.openmeteoapi.model

import java.time.LocalDate

data class DailyWeatherData(
    val date: LocalDate,
    val temperature: List<Double>,
    val relativeHumidity: List<Int>,
    val weather: List<WeatherCode>,
)