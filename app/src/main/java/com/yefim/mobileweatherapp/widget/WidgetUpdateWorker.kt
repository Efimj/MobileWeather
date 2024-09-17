package com.yefim.mobileweatherapp.widget

import android.appwidget.AppWidgetManager
import android.content.Context
import android.util.Log
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.yefim.mobileweatherapp.widget.WeatherWidget.Companion.updateWeatherWidgets

class WidgetUpdateWorker(appContext: Context, workerParams: WorkerParameters) : Worker(appContext, workerParams) {
    private val LogTag = "WidgetUpdateWorker"

    override fun doWork(): Result {
        Log.d(LogTag, "Widget updating started")

        val appWidgetManager = AppWidgetManager.getInstance(this.applicationContext)
        updateWeatherWidgets(context = this.applicationContext, appWidgetManager = appWidgetManager)

        return Result.success()
    }
}