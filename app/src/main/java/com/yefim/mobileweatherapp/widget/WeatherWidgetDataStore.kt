package com.yefim.mobileweatherapp.widget

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

object WeatherWidgetDataStore {
    val WIDGET_ID = intPreferencesKey("widget_id")
    val WIDGET_LOCATION_KEY = stringPreferencesKey("location_key")

    data class WeatherWidget(
        val id: Int = -1,
        val location: String = "",
    )

    private val Context.widgetDataStore: DataStore<Preferences> by preferencesDataStore(name = "widget_prefs")

    suspend fun save(context: Context, widget: WeatherWidget) {
        context.widgetDataStore.edit { preferences ->
            preferences[WIDGET_ID] = widget.id
            preferences[WIDGET_LOCATION_KEY] = widget.location
        }
    }

    fun getWidgets(context: Context): Flow<WeatherWidget> {
        return context.widgetDataStore.data
            .map { preferences ->
                WeatherWidget(
                    id = preferences[WIDGET_ID] ?: -1,
                    location = preferences[WIDGET_LOCATION_KEY] ?: ""
                )
            }
    }

    suspend fun delete(context: Context, id: Int): Boolean {
        var isDeleted = false
        context.widgetDataStore.edit { preferences ->
            if (preferences[WIDGET_ID] == id) {
                preferences.clear()
                isDeleted = true
            }
        }
        return isDeleted
    }
}