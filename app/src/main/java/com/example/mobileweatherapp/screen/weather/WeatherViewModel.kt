package com.example.mobileweatherapp.screen.weather

import android.app.Activity
import android.content.Context
import android.location.Location
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mobileweatherapp.util.ContextUtil
import com.example.mobileweatherapp.util.LocationUtil.getAddressFromLocation
import com.example.mobileweatherapp.util.settings.SettingsManager
import com.example.mobileweatherapp.util.settings.SettingsManager.settings
import com.example.mobileweatherapp.util.settings.UserLocation
import com.example.openweatherapi.di.OpenWeatherInstance
import com.example.openweatherapi.model.DailyWeatherData
import com.example.openweatherapi.model.WeatherData
import com.example.openweatherapi.util.WeatherUtil
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate

/**
 * Enum representing the possible states.
 */
enum class WeatherResponseState {
    Done,
    ForecastMissing
}

/**
 * Data class representing the state of the weather screen.
 *
 * @property weather The current weather data.
 * @property weatherByDay A map of daily weather data grouped by date.
 * @property weatherResponseState The current state of the weather response.
 * @property selectedDay The currently selected day for which weather data is displayed.
 * @property location The current location of the user.
 */
data class WeatherScreenState(
    val weather: WeatherData? = null,
    val weatherByDay: Map<LocalDate, DailyWeatherData> = emptyMap(),
    val weatherResponseState: WeatherResponseState = WeatherResponseState.Done,
    val selectedDay: LocalDate = LocalDate.now(),
    val location: Location? = null,
    val isLoading: Boolean = false,
)

/**
 * ViewModel for managing the state of the weather screen.
 */
class WeatherViewModel : ViewModel() {
    private val _weatherScreenState = MutableStateFlow(WeatherScreenState())
    val weatherScreenState: StateFlow<WeatherScreenState> = _weatherScreenState

    fun updateWeather(context: Context) {
        _weatherScreenState.value = _weatherScreenState.value.copy(isLoading = true)

        updateLocation(context)

        viewModelScope.launch {
            settings.location?.let {
                try {
                    val response = OpenWeatherInstance.getService().getForecast(
                        lat = it.latitude,
                        lon = it.longitude,
                    )
                    val weatherByDay = WeatherUtil.groupWeatherByDay(response)

                    _weatherScreenState.value =
                        _weatherScreenState.value.copy(
                            weather = response,
                            weatherByDay = weatherByDay
                        )

                    updateWeatherResponseState(state = WeatherResponseState.Done)
                } catch (e: Exception) {
                    Log.e("weather api", "OpenWeatherInstance fetch", e)
                    updateWeatherResponseState(state = WeatherResponseState.ForecastMissing)
                }
            }
            _weatherScreenState.value = _weatherScreenState.value.copy(isLoading = false)
        }
    }

    private fun updateLocation(context: Context) {
        var location = _weatherScreenState.value.location

        if (ContextUtil.checkLocationPermission(context)) {
            val fusedLocationClient =
                LocationServices.getFusedLocationProviderClient(context as Activity)

            fusedLocationClient.lastLocation.addOnSuccessListener { it: Location? ->
                if (it != null) {
                    location = it
                }
            }
        }

        location?.let {
            val address = getAddressFromLocation(context = context, location = it)

            SettingsManager.update(
                context = context,
                settings = settings.copy(
                    location = UserLocation(
                        address = address,
                        latitude = it.latitude,
                        longitude = it.longitude
                    )
                )
            )
        }
    }

    fun updateWeatherResponseState(state: WeatherResponseState) {
        _weatherScreenState.value = _weatherScreenState.value.copy(weatherResponseState = state)
    }

    fun selectDay(day: LocalDate) {
        _weatherScreenState.value = _weatherScreenState.value.copy(selectedDay = day)
    }

    fun updateLocation(location: Location) {
        _weatherScreenState.value = _weatherScreenState.value.copy(location = location)
    }
}
