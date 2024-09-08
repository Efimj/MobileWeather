package com.example.openmeteoapi.model

import com.google.gson.annotations.SerializedName

data class Daily(
    @SerializedName("time") val date: List<String>,
    @SerializedName("weather_code") val weatherCode: List<Int>,
    @SerializedName("temperature_2m_max") val temperature2mMax: List<Double>,
    @SerializedName("temperature_2m_min") val temperature2mMin: List<Double>,
    @SerializedName("apparent_temperature_max") val apparentTemperatureMax: List<Double>,
    @SerializedName("apparent_temperature_min") val apparentTemperatureMin: List<Double>,
    @SerializedName("sunrise") val sunrise: List<String>,
    @SerializedName("sunset") val sunset: List<String>,
    @SerializedName("daylight_duration") val daylightDuration: List<Double>,
    @SerializedName("sunshine_duration") val sunshineDuration: List<Double>,
    @SerializedName("uv_index_max") val uvIndexMax: List<Double>,
    @SerializedName("uv_index_clear_sky_max") val uvIndexClearSkyMax: List<Double>,
    @SerializedName("precipitation_sum") val precipitationSum: List<Double>,
    @SerializedName("precipitation_probability_max") val precipitationProbabilityMax: List<Int>,
    @SerializedName("wind_speed_10m_max") val windSpeed10mMax: List<Double>,
    @SerializedName("wind_gusts_10m_max") val windGusts10mMax: List<Double>,
    @SerializedName("wind_direction_10m_dominant") val windDirection10mDominant: List<Int>
)