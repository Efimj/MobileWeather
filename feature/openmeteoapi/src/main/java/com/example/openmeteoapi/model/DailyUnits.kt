package com.example.openmeteoapi.model

import com.google.gson.annotations.SerializedName

data class DailyUnits(
    @SerializedName("time") val time: String,
    @SerializedName("weather_code") val weatherCode: String,
    @SerializedName("temperature_2m_max") val temperature2mMax: String,
    @SerializedName("temperature_2m_min") val temperature2mMin: String,
    @SerializedName("apparent_temperature_max") val apparentTemperatureMax: String,
    @SerializedName("apparent_temperature_min") val apparentTemperatureMin: String,
    @SerializedName("sunrise") val sunrise: String,
    @SerializedName("sunset") val sunset: String,
    @SerializedName("daylight_duration") val daylightDuration: String,
    @SerializedName("sunshine_duration") val sunshineDuration: String,
    @SerializedName("uv_index_max") val uvIndexMax: String,
    @SerializedName("uv_index_clear_sky_max") val uvIndexClearSkyMax: String,
    @SerializedName("precipitation_sum") val precipitationSum: String,
    @SerializedName("precipitation_probability_max") val precipitationProbabilityMax: String,
    @SerializedName("wind_speed_10m_max") val windSpeed10mMax: String,
    @SerializedName("wind_gusts_10m_max") val windGusts10mMax: String,
    @SerializedName("wind_direction_10m_dominant") val windDirection10mDominant: String
)