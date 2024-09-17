package com.yefim.mobileweatherapp.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetManager.ACTION_APPWIDGET_DELETED
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import androidx.compose.ui.graphics.toArgb
import com.yefim.mobileweatherapp.MainActivity
import com.yefim.mobileweatherapp.R
import com.yefim.mobileweatherapp.ui.theme.getColorScheme
import com.yefim.mobileweatherapp.util.ContextUtil
import com.yefim.mobileweatherapp.util.DateTimeUtil
import com.yefim.mobileweatherapp.util.settings.SettingsManager
import com.yefim.mobileweatherapp.util.settings.SettingsManager.settings
import com.yefim.mobileweatherapp.util.settings.WeatherForecast
import kotlinx.datetime.toJavaLocalDateTime
import java.time.format.DateTimeFormatter
import kotlin.math.roundToInt

class WeatherWidget : AppWidgetProvider() {
    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        updateWeatherWidgets(context, appWidgetManager)
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        super.onReceive(context, intent)
        if (intent != null && context != null) {
            when (intent.action) {
                ACTION_APPWIDGET_DELETED -> {
                    intent.extras?.let {
                        val id = it.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID)
                        WeatherWidgetStorage.removeById(context, id)
                    }
                }
            }
        }
    }

    companion object {
        fun updateWeatherWidgets(
            context: Context,
            appWidgetManager: AppWidgetManager,
        ) {
            SettingsManager.init(context)
            val forecast = settings.weatherForecasts
            val selectedForecast = settings.selectedWeatherForecast
            val savedWidgets = WeatherWidgetStorage.getAll(context)

            val savedWidgetsIds = savedWidgets.map { it.id }
            val widgetAddedToScreen = appWidgetManager.getAppWidgetIds(
                ComponentName(
                    context,
                    WeatherWidget::class.java
                )
            ).toList()

            val widgets = savedWidgets.filter { it.id in savedWidgetsIds }.map { it.id }
            val onlyExisted = widgetAddedToScreen.filterNot { it in savedWidgetsIds }
            val onlyDeleted = savedWidgetsIds.filterNot { it in widgetAddedToScreen }

            // for existed and provided widgets
            for (widgetId in widgets) {
                val widget = savedWidgets.find { it.id == widgetId } ?: continue
                val views = updateWeatherWidgetUi(context, forecast, widget)
                appWidgetManager.updateAppWidget(widget.id, views)
            }

            // for not saved in data store
            for (widgetId in onlyExisted) {
                if (selectedForecast == null) continue
                val widget = WeatherWidgetStorage.WeatherWidget(
                    id = widgetId,
                    location = selectedForecast.location.address
                )
                WeatherWidgetStorage.insert(context = context, listOf(widget))
                val views = updateWeatherWidgetUi(context, forecast, widget)

                appWidgetManager.updateAppWidget(widget.id, views)
            }

            WeatherWidgetStorage.removeById(context = context, onlyDeleted)
        }

        fun updateWeatherWidgetUi(
            context: Context,
            forecast: Set<WeatherForecast>,
            widget: WeatherWidgetStorage.WeatherWidget
        ): RemoteViews {
            val views = RemoteViews(context.packageName, R.layout.widget_weather)

            val currentTime = DateTimeUtil.getLocalDateTime()
            val weatherForecast = forecast.first { it.location.address == widget.location }
            val weather =
                weatherForecast.weatherForecast.find { it.date == currentTime.date } ?: weatherForecast.weatherForecast.first()

            val theme =
                getColorScheme(darkTheme = ContextUtil.isDarkTheme(context), context = context)

            val weatherIcon =
                weather.weatherHourly[currentTime.hour].getWeatherIcon(
                    weather.checkIsNight(
                        currentTime.time
                    )
                )
            views.setImageViewResource(R.id.weather_icon, weatherIcon)

            views.setTextViewText(R.id.location_text, widget.location)
            views.setTextColor(R.id.location_text, theme.onSecondaryContainer.toArgb())

            val formattedTime =
                currentTime.toJavaLocalDateTime()
                    .format(DateTimeFormatter.ofPattern("EEEE, MMM d, h a"))
            views.setTextViewText(R.id.time_text, formattedTime)
            views.setTextColor(R.id.time_text, theme.onSurface.copy(alpha = .8f).toArgb())

            val tempText =
                "${weather.temperature.getOrElse(currentTime.hour) { 0.0 }.roundToInt()}°"
            views.setTextViewText(R.id.temp_text, tempText)
            views.setTextColor(R.id.temp_text, theme.onSecondaryContainer.toArgb())

            views.setInt(
                R.id.widget_container,
                "setBackgroundColor",
                theme.surfaceContainer.toArgb()
            )

            val intent = Intent(context, MainActivity::class.java)
            intent.putExtra(Intent.EXTRA_UID, widget.id.toString())
            val pendingIntent: PendingIntent = PendingIntent.getActivity(
                context,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_container, pendingIntent)
            return views
        }
    }
}

