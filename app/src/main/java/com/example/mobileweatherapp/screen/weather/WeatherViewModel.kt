package com.example.mobileweatherapp.screen.weather

import android.content.Context
import android.location.Location
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.openweatherapi.di.OpenWeatherInstance
import com.example.openweatherapi.model.DailyWeatherData
import com.example.openweatherapi.model.WeatherData
import com.example.openweatherapi.util.WeatherUtil
import com.example.mobileweatherapp.util.ContextUtil
import com.example.mobileweatherapp.util.LocationUtil.getAddressFromLocation
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate

/**
 * Enum representing the possible states.
 */
enum class WeatherResponseState {
    Done,
    PermissionsNotGranted,
    LocationNotFound,
    NetworkError,
    ForecastMissing
}

/**
 * Data class representing the state of the weather screen.
 *
 * @property weather The current weather data.
 * @property weatherByDay A map of daily weather data grouped by date.
 * @property weatherResponseState The current state of the weather response.
 * @property selectedDay The currently selected day for which weather data is displayed.
 * @property address The address associated with the current location.
 * @property location The current location of the user.
 */
data class WeatherScreenState(
    val weather: WeatherData? = null,
    val weatherByDay: Map<LocalDate, DailyWeatherData> = emptyMap(),
    val weatherResponseState: WeatherResponseState = WeatherResponseState.Done,
    val selectedDay: LocalDate = LocalDate.now(),
    val address: String = "",
    val location: Location? = null,
    val isLoading: Boolean = true,
)

/**
 * ViewModel for managing the state of the weather screen.
 */
class WeatherViewModel : ViewModel() {
    private val _weatherScreenState = MutableStateFlow(WeatherScreenState())
    val weatherScreenState: StateFlow<WeatherScreenState> = _weatherScreenState

    fun updateWeather(context: Context) {
        _weatherScreenState.value = _weatherScreenState.value.copy(isLoading = true)

        if (!ContextUtil.hasInternetConnection(context)) {
            updateWeatherResponseState(WeatherResponseState.NetworkError)
            return
        }

        if (!ContextUtil.checkLocationPermission(context)) {
            updateWeatherResponseState(WeatherResponseState.PermissionsNotGranted)
            return
        }

        val location = _weatherScreenState.value.location

        if (location == null) {
            updateWeatherResponseState(WeatherResponseState.LocationNotFound)
            return
        }

        val address = getAddressFromLocation(context = context, location = location)
        updateLocationName(address = address)

        viewModelScope.launch {
            try {
                val response = OpenWeatherInstance.getService().getForecast(
                    lat = location.latitude,
                    lon = location.longitude,
                )
                val weatherByDay = WeatherUtil.groupWeatherByDay(response)

                _weatherScreenState.value =
                    _weatherScreenState.value.copy(weather = response, weatherByDay = weatherByDay)

                updateWeatherResponseState(state = WeatherResponseState.Done)
            } catch (e: Exception) {
                Log.e("weather api", "OpenWeatherInstance fetch", e)
                updateWeatherResponseState(state = WeatherResponseState.ForecastMissing)
            }
            _weatherScreenState.value = _weatherScreenState.value.copy(isLoading = false)
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

    fun updateLocationName(address: String?) {
        if (address.isNullOrBlank()) {
            _weatherScreenState.value = _weatherScreenState.value.copy(address = "Not found")
            return
        }
        _weatherScreenState.value = _weatherScreenState.value.copy(address = address)
    }
}
