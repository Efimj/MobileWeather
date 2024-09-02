package com.example.openmeteoapi.di

import com.example.openmeteoapi.api.OpenWeatherApiService
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/**
 * Provides an instance of [OpenWeatherApiService] using Retrofit.
 */
object OpenWeatherInstance {
    private const val OpenWeatherURI = "https://api.open-meteo.com/v1/"

    /**
     * Returns the [OpenWeatherApiService] for making API requests.
     */
    fun getService(): OpenWeatherApiService {
        val retrofit = Retrofit.Builder()
            .baseUrl(OpenWeatherURI)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        return retrofit.create(OpenWeatherApiService::class.java)
    }
}