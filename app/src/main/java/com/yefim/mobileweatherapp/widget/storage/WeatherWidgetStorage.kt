package com.yefim.mobileweatherapp.widget.storage

import android.content.Context
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

object WeatherWidgetStorage {
    private const val STORE_NAME = "WEATHER_WIDGET_STORAGE"
    private const val WEATHER_WIDGET_DATA = "WEATHER_WIDGET_DATA"

    fun insert(context: Context, data: List<WeatherWidget>) {
        val storedData = getAll(context)
        val newData = storedData.plus(data)
        updateStorage(context = context, data = newData)
    }

    fun removeById(context: Context, widgetId: Int) {
        val storedData = getAll(context)
        val newData = storedData.filterNot { it.id == widgetId }
        updateStorage(context = context, data = newData)
    }

    fun removeById(context: Context, widgetIds: List<Int>) {
        val storedData = getAll(context)
        val newData = storedData.filterNot { it.id in widgetIds }
        updateStorage(context = context, data = newData)
    }

    fun updateOne(context: Context, widget: WeatherWidget) {
        val storedData = getAll(context)
        val newData = storedData.map {
            if (it.id == widget.id) widget else it
        }
        updateStorage(context = context, data = newData)
    }

    fun save(context: Context, data: List<WeatherWidget>) {
        updateStorage(context = context, data = data)
    }

    fun getAll(context: Context): List<WeatherWidget> {
        return restore(context)
    }

    private fun updateStorage(context: Context, data: List<WeatherWidget>) {
        val store = context.getSharedPreferences(STORE_NAME, Context.MODE_PRIVATE)
        val savedSettings = Json.encodeToString(data)
        store.edit().putString(WEATHER_WIDGET_DATA, savedSettings).apply()
    }

    private fun restore(context: Context): List<WeatherWidget> {
        val store = context.getSharedPreferences(STORE_NAME, Context.MODE_PRIVATE)
        val savedSettings = store.getString(WEATHER_WIDGET_DATA, "")
        return if (savedSettings.isNullOrEmpty()) {
            emptyList()
        } else {
            try {
                Json.decodeFromString<List<WeatherWidget>>(savedSettings)
            } catch (e: Exception) {
                emptyList()
            }
        }
    }
}