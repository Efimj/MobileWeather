package com.yefim.openmeteoapi.api

import android.annotation.SuppressLint
import com.yefim.openmeteoapi.model.LocationResponse
import retrofit2.http.GET
import retrofit2.http.Query
import java.util.Locale

private const val DefaultLocationsCount = 5
@SuppressLint("ConstantLocale")
private val DefaultLanguage = Locale.getDefault().language

interface GeocodingApiService {
    @GET("search?format=json")
    suspend fun getLocation(
        @Query("name") name: String,
        @Query("language") language: String = DefaultLanguage,
        @Query("count") count: Int = DefaultLocationsCount
    ): LocationResponse
}