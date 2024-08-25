package com.example.openweatherapi.api

import com.example.openweatherapi.model.WeatherData
import retrofit2.http.GET
import retrofit2.http.Query

private const val DefaultForecastDays = 7

/**
 *  Interface representing the API service for fetching weather forecast data.
 */
interface OpenWeatherApiService {
    @GET("forecast?hourly=temperature_2m,relative_humidity_2m,weather_code&timezone=auto")
    suspend fun getForecast(
        @Query("latitude") lat: Double,
        @Query("longitude") lon: Double,
        @Query("forecast_days") days: Int = DefaultForecastDays
    ): WeatherData
}