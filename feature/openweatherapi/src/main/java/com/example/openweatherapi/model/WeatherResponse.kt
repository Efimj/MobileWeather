package com.example.openweatherapi.model

import com.google.gson.annotations.SerializedName

data class WeatherData(
    @SerializedName("latitude")
    val latitude: Double,
    @SerializedName("longitude")
    val longitude: Double,
    @SerializedName("generationtime_ms")
    val generationTimeMS: Double,
    @SerializedName("utc_offset_seconds")
    val utcOffsetSeconds: Int,
    @SerializedName("timezone")
    val timezone: String,
    @SerializedName("timezone_abbreviation")
    val timezoneAbbreviation: String,
    @SerializedName("elevation")
    val elevation: Double,
    @SerializedName("hourly_units")
    val hourlyUnits: HourlyUnits,
    @SerializedName("hourly")
    val hourly: Hourly
)

data class HourlyUnits(
    @SerializedName("time")
    val time: String,
    @SerializedName("temperature_2m")
    val temperature: String,
    @SerializedName("relative_humidity_2m")
    val relativeHumidity: String,
    @SerializedName("weather_code")
    val weatherCode: String
)

data class Hourly(
    @SerializedName("time")
    val time: List<String>,
    @SerializedName("temperature_2m")
    val temperature: List<Double>,
    @SerializedName("relative_humidity_2m")
    val relativeHumidity: List<Int>,
    @SerializedName("weather_code")
    val weatherCode: List<Int>
)
