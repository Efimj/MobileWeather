package com.example.openmeteoapi.di

import com.example.openmeteoapi.api.GeocodingApiService
import com.example.openmeteoapi.api.WeatherApiService
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/**
 * Provides an instance of [WeatherApiService] using Retrofit.
 */
object OpenMeteoInstance {
    private const val WeatherURI = "https://api.open-meteo.com/v1/"
    private const val GeocodingURI = "https://geocoding-api.open-meteo.com/v1/"

    /**
     * Returns the [WeatherApiService] for making API requests.
     */
    fun getWeatherService(): WeatherApiService {
        val retrofit = Retrofit.Builder()
            .baseUrl(WeatherURI)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        return retrofit.create(WeatherApiService::class.java)
    }

    fun getGeocodingService(): GeocodingApiService {
        val retrofit = Retrofit.Builder()
            .baseUrl(GeocodingURI)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        return retrofit.create(GeocodingApiService::class.java)
    }
}