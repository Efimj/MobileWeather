package com.yefim.openmeteoapi.api

import com.yefim.openmeteoapi.model.WeatherData
import retrofit2.http.GET
import retrofit2.http.Query

private const val DefaultForecastDays = 7

/**
 *  Interface representing the API service for fetching weather forecast data.
 */
interface WeatherApiService {
    @GET("forecast?hourly=temperature_2m,relative_humidity_2m,weather_code&timezone=auto&daily=weather_code,temperature_2m_max,temperature_2m_min,apparent_temperature_max,apparent_temperature_min,sunrise,sunset,daylight_duration,sunshine_duration,uv_index_max,uv_index_clear_sky_max,precipitation_sum,precipitation_probability_max,wind_speed_10m_max,wind_gusts_10m_max,wind_direction_10m_dominant")
    suspend fun getForecast(
        @Query("latitude") lat: Double,
        @Query("longitude") lon: Double,
        @Query("forecast_days") days: Int = DefaultForecastDays
    ): WeatherData
}