package com.yefim.mobileweatherapp.widget.providers

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetManager.ACTION_APPWIDGET_DELETED
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import androidx.compose.ui.graphics.toArgb
import com.yefim.mobileweatherapp.MainActivity
import com.yefim.mobileweatherapp.R
import com.yefim.mobileweatherapp.ui.theme.getColorScheme
import com.yefim.mobileweatherapp.util.ContextUtil
import com.yefim.mobileweatherapp.util.DateTimeUtil
import com.yefim.mobileweatherapp.util.settings.WeatherForecast
import com.yefim.mobileweatherapp.widget.WidgetUtil.updateWeatherWidgets
import com.yefim.mobileweatherapp.widget.storage.WeatherWidget
import com.yefim.mobileweatherapp.widget.storage.WeatherWidgetStorage
import com.yefim.mobileweatherapp.widget.storage.WeatherWidgetType
import kotlinx.datetime.toJavaLocalDateTime
import java.time.format.DateTimeFormatter
import kotlin.math.roundToInt

class WeatherLargeCardWidget : AppWidgetProvider() {
    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        updateWeatherWidgets(
            context = context,
            appWidgetManager = appWidgetManager,
            widgetType = WeatherWidgetType.LARGE_CARD
        )
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
        fun updateLargeCardWidgetUi(
            context: Context,
            forecast: Set<WeatherForecast>,
            widget: WeatherWidget
        ): RemoteViews {
            val views = RemoteViews(context.packageName, R.layout.weather_widget_large_card)

            val currentTime = DateTimeUtil.getLocalDateTime()
            val weatherForecast = forecast.first { it.location.address == widget.location }
            val weather =
                weatherForecast.weatherForecast.find { it.date == currentTime.date }
                    ?: weatherForecast.weatherForecast.first()

            val theme =
                getColorScheme(darkTheme = ContextUtil.isDarkTheme(context), context = context)

            val weatherIcon =
                weather.weatherHourly[currentTime.hour].getWeatherIcon(
                    weather.checkIsNight(
                        currentTime.time
                    )
                )

            val formattedTime =
                currentTime.toJavaLocalDateTime()
                    .format(DateTimeFormatter.ofPattern("h a"))

            views.setImageViewResource(R.id.weather_icon, weatherIcon)

            views.setTextViewText(R.id.time_text, formattedTime)
            views.setTextColor(R.id.time_text, theme.onSurface.toArgb())

            val bigTempText =
                "${weather.temperature.getOrElse(currentTime.hour) { 0.0 }.roundToInt()}°"
            views.setTextViewText(R.id.big_temp_text, bigTempText)
            views.setTextColor(R.id.time_text, theme.primary.toArgb())

            val maxMinTempText =
                "${weather.temperatureMax.roundToInt()}° / ${weather.temperatureMin.roundToInt()}°"
            views.setTextViewText(R.id.min_max_temp_text, maxMinTempText)
            views.setTextColor(R.id.temp_text, theme.onSurface.toArgb())

            views.setTextViewText(R.id.location_text, widget.location)
            views.setTextColor(R.id.location_text, theme.onSurface.toArgb())

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