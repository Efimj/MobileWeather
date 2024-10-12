package com.yefim.mobileweatherapp.util.workers

import android.content.Context
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

object WeatherForecastWorkManagerUtil {
    private const val WeatherForecastWorkerManager = "WeatherForecastWorkerManager"

    fun scheduleWork(context: Context) {
        val everyHourWidgetUpdateRequest =
            PeriodicWorkRequestBuilder<WeatherUpdateWorker>(6, TimeUnit.HOURS)
                .build()

        WorkManager.getInstance(context).apply {
            enqueueUniquePeriodicWork(
                WeatherForecastWorkerManager,
                ExistingPeriodicWorkPolicy.KEEP,
                everyHourWidgetUpdateRequest
            )
        }
    }
}