package com.yefim.mobileweatherapp.widget

import android.content.Context
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import java.util.concurrent.TimeUnit

object WidgetWorkManagerUtil {
    private const val WidgetWorkerManager = "WidgetWorkerManager"

    fun scheduleWork(context: Context) {
        val everyHourWidgetUpdateRequest =
            PeriodicWorkRequestBuilder<WidgetUpdateWorker>(1, TimeUnit.HOURS)
                .setInitialDelay(secondsUntilNextHour(), TimeUnit.SECONDS)
                .build()

        WorkManager.getInstance(context).apply {
            enqueueUniquePeriodicWork(
                WidgetWorkerManager,
                ExistingPeriodicWorkPolicy.KEEP,
                everyHourWidgetUpdateRequest
            )
        }
    }

    fun secondsUntilNextHour(): Long {
        val now = LocalDateTime.now()
        val nextHour = now.plusHours(1).withMinute(0).withSecond(0).withNano(0)
        return ChronoUnit.SECONDS.between(now, nextHour)
    }
}