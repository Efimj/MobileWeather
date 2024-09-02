package com.example.openmeteoapi.util

import com.example.openmeteoapi.model.DailyWeatherData
import com.example.openmeteoapi.model.WeatherData
import java.time.LocalDate
import java.time.format.DateTimeFormatter

object WeatherUtil {
    /**
     * Groups the hourly weather data into a map where each key is a [LocalDate]
     * and the corresponding value is a [DailyWeatherData].
     *
     * @param weatherData The hourly weather data to be grouped.
     * @return A map of [LocalDate] to [DailyWeatherData] where each entry represents
     *         weather information for a specific day.
     */
    fun groupWeatherByDay(weatherData: WeatherData): Map<LocalDate, DailyWeatherData> {
        val dateFormatter = DateTimeFormatter.ISO_DATE_TIME
        return weatherData.hourly.time.indices.groupBy { index ->
            LocalDate.parse(
                weatherData.hourly.time[index],
                dateFormatter
            )
        }.mapValues { entry ->
            val indices = entry.value

            DailyWeatherData(
                date = entry.key,
                temperature = indices.map { weatherData.hourly.temperature[it] },
                relativeHumidity = indices.map { weatherData.hourly.relativeHumidity[it] },
                weatherCode = indices.map { weatherData.hourly.weatherCode[it] }
            )
        }
    }
}