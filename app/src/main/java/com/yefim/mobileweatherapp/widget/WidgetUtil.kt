package com.yefim.mobileweatherapp.widget

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.widget.RemoteViews
import com.yefim.mobileweatherapp.util.settings.SettingsManager
import com.yefim.mobileweatherapp.util.settings.SettingsManager.settings
import com.yefim.mobileweatherapp.util.settings.WeatherForecast
import com.yefim.mobileweatherapp.widget.providers.WeatherHorizontalPillWidget
import com.yefim.mobileweatherapp.widget.providers.WeatherHorizontalPillWidget.Companion.updateHorizontalPillWidgetUi
import com.yefim.mobileweatherapp.widget.providers.WeatherLargeCardWidget
import com.yefim.mobileweatherapp.widget.providers.WeatherLargeCardWidget.Companion.updateLargeCardWidgetUi
import com.yefim.mobileweatherapp.widget.storage.WeatherWidget
import com.yefim.mobileweatherapp.widget.storage.WeatherWidgetStorage
import com.yefim.mobileweatherapp.widget.storage.WeatherWidgetType

object WidgetUtil {
    val ProvidersList =
        listOf(WeatherHorizontalPillWidget::class.java, WeatherLargeCardWidget::class.java)

    fun updateWeatherWidgets(
        context: Context,
        appWidgetManager: AppWidgetManager,
        widgetType: WeatherWidgetType? = null
    ) {
        SettingsManager.init(context)
        val forecast = settings.weatherForecasts
        val selectedForecast = settings.selectedWeatherForecast
        val savedWidgets = WeatherWidgetStorage.getAll(context)

        val savedWidgetsIds = savedWidgets.map { it.id }
        val widgetAddedToScreen = getAllWidgetsId(appWidgetManager, context)

        val widgets = savedWidgets.filter { it.id in savedWidgetsIds }.map { it.id }
        val onlyExisted = widgetAddedToScreen.filterNot { it in savedWidgetsIds }
        val onlyDeleted = savedWidgetsIds.filterNot { it in widgetAddedToScreen }

        // for existed and provided widgets
        for (widgetId in widgets) {
            val widget = savedWidgets.find { it.id == widgetId } ?: continue
            val views = updateWeatherViews(context, forecast, widget)
            appWidgetManager.updateAppWidget(widget.id, views)
        }

        // for not saved in data store
        if (widgetType != null) {
            for (widgetId in onlyExisted) {
                if (selectedForecast == null) continue
                val widget = WeatherWidget(
                    id = widgetId,
                    location = selectedForecast.location.address,
                    type = widgetType
                )

                WeatherWidgetStorage.insert(context = context, listOf(widget))
                val views = updateWeatherViews(context, forecast, widget)

                appWidgetManager.updateAppWidget(widget.id, views)
            }
        }

        WeatherWidgetStorage.removeById(context = context, onlyDeleted)
    }

    private fun getAllWidgetsId(
        appWidgetManager: AppWidgetManager,
        context: Context
    ): List<Int> {
        val ids = mutableListOf<Int>()
        for (provider in ProvidersList) {
            ids.addAll(
                appWidgetManager.getAppWidgetIds(
                    ComponentName(
                        context,
                        provider
                    )
                ).toList()
            )
        }
        return ids
    }

    private fun updateWeatherViews(
        context: Context,
        forecast: Set<WeatherForecast>,
        widget: WeatherWidget
    ): RemoteViews {
        return when (widget.type) {
            WeatherWidgetType.HORIZONTAL_PILL -> updateHorizontalPillWidgetUi(
                context = context,
                forecast = forecast,
                widget = widget
            )

            WeatherWidgetType.LARGE_CARD -> updateLargeCardWidgetUi(
                context = context,
                forecast = forecast,
                widget = widget
            )
        }
    }
}