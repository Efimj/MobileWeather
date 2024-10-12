package com.yefim.mobileweatherapp.util.workers

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.yefim.mobileweatherapp.util.DateTimeUtil
import com.yefim.mobileweatherapp.util.settings.SettingsManager
import com.yefim.mobileweatherapp.util.settings.SettingsManager.settings
import com.yefim.openmeteoapi.di.OpenMeteoInstance
import com.yefim.openmeteoapi.util.WeatherUtil

class WeatherUpdateWorker(appContext: Context, workerParams: WorkerParameters) :
    CoroutineWorker(appContext, workerParams) {
    private val LogTag = "WeatherUpdateWorker"

    override suspend fun doWork(): Result {
        Log.d(LogTag, "Weather updating started")

        updateWeatherForecast()

        return Result.success()
    }

    private suspend fun updateWeatherForecast(count: Int = 10) {
        SettingsManager.init(this.applicationContext)
        val forecast = settings.weatherForecasts.sortedBy { it.lastUpdateDate }.take(count)
        val selectedForecast = settings.selectedWeatherForecast

        forecast.forEach { currentForecast ->
            val latitude = currentForecast.location.latitude
            val longitude = currentForecast.location.longitude

            val response = OpenMeteoInstance.getWeatherService().getForecast(
                lat = latitude,
                lon = longitude
            )

            val updatedWeatherForecast = currentForecast.copy(
                weatherForecast = WeatherUtil.groupWeatherByDay(response),
                lastUpdateDate = DateTimeUtil.getLocalDateTime()
            )

            val updatedForecasts = settings.weatherForecasts.map { forecast ->
                if (forecast.location.latitude == latitude && forecast.location.longitude == longitude) {
                    updatedWeatherForecast
                } else {
                    forecast
                }
            }.toSet()

            val updatedSettings = settings.copy(
                weatherForecasts = updatedForecasts,
                selectedWeatherForecast = if (selectedForecast != null && selectedForecast.location == currentForecast.location) {
                    updatedWeatherForecast
                } else {
                    selectedForecast
                }
            )

            SettingsManager.update(context = this.applicationContext, settings = updatedSettings)
        }
    }
}