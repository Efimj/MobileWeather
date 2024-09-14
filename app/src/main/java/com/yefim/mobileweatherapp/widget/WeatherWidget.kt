package com.yefim.mobileweatherapp.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.widget.ImageView
import android.widget.RemoteViews
import androidx.compose.ui.graphics.toArgb
import androidx.core.graphics.drawable.DrawableCompat
import com.yefim.mobileweatherapp.MainActivity
import com.yefim.mobileweatherapp.R
import com.yefim.mobileweatherapp.ui.theme.getColorScheme
import com.yefim.mobileweatherapp.util.ContextUtil
import com.yefim.mobileweatherapp.util.DateTimeUtil
import com.yefim.mobileweatherapp.util.settings.SettingsManager
import com.yefim.mobileweatherapp.util.settings.SettingsManager.settings
import kotlinx.datetime.toJavaLocalDateTime
import java.time.format.DateTimeFormatter
import kotlin.math.roundToInt

class WeatherWidget : AppWidgetProvider() {
    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        SettingsManager.init(context)

        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    private fun updateAppWidget(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int
    ) {
        val theme = getColorScheme(darkTheme = ContextUtil.isDarkTheme(context), context = context)
        val views = RemoteViews(context.packageName, R.layout.widget_weather)

        val forecast = settings.selectedWeatherForecast ?: return
        val weather = forecast.weatherForecast.first()
        val currentTime = DateTimeUtil.getLocalDateTime()

        val weatherIcon =
            weather.weatherHourly[currentTime.hour].getWeatherIcon(weather.checkIsNight(currentTime.time))
        views.setImageViewResource(R.id.weather_icon, weatherIcon)

        views.setTextViewText(R.id.location_text, forecast.location.address)
        views.setTextColor(R.id.location_text, theme.onSurface.toArgb())

        val formattedTime =
            currentTime.toJavaLocalDateTime()
                .format(DateTimeFormatter.ofPattern("EEEE, MMM d, h a"))
        views.setTextViewText(R.id.time_text, formattedTime)
        views.setTextColor(R.id.time_text, theme.onSurface.copy(alpha = .8f).toArgb())

        val tempText = "${weather.temperature.getOrElse(currentTime.hour) { 0.0 }.roundToInt()}°"
        views.setTextViewText(R.id.temp_text, tempText)
        views.setTextColor(R.id.temp_text, theme.onPrimaryContainer.toArgb())

        views.setInt(R.id.widget_container, "setBackgroundColor", theme.surfaceContainer.toArgb())

        val intent = Intent(context, MainActivity::class.java)
        intent.putExtra(Intent.EXTRA_UID, appWidgetId.toString())
        val pendingIntent: PendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        views.setOnClickPendingIntent(R.id.widget_container, pendingIntent)

        appWidgetManager.updateAppWidget(appWidgetId, views)
    }
}

