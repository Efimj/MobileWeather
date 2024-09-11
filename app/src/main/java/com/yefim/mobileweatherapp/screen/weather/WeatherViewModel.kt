package com.yefim.mobileweatherapp.screen.weather

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yefim.mobileweatherapp.util.settings.UserLocation
import com.yefim.openmeteoapi.di.OpenMeteoInstance
import com.yefim.openmeteoapi.model.DayWeatherData
import com.yefim.openmeteoapi.model.WeatherData
import com.yefim.openmeteoapi.util.WeatherUtil
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
    val weatherByDay: Map<LocalDate, DayWeatherData> = emptyMap(),
    val weatherResponseState: WeatherResponseState = WeatherResponseState.Done,
    val selectedDay: LocalDate = LocalDate.now(),
    val location: UserLocation? = null,
    val isLoading: Boolean = false,
)

/**
 * ViewModel for managing the state of the weather screen.
 */
class WeatherViewModel : ViewModel() {
    private val _weatherScreenState = MutableStateFlow(WeatherScreenState())
    val weatherScreenState: StateFlow<WeatherScreenState> = _weatherScreenState

    fun updateWeather() {
        _weatherScreenState.value = _weatherScreenState.value.copy(isLoading = true)

        viewModelScope.launch {
            _weatherScreenState.value.location?.let {
                try {
                    val response = OpenMeteoInstance.getWeatherService().getForecast(
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

    fun updateWeatherResponseState(state: WeatherResponseState) {
        _weatherScreenState.value = _weatherScreenState.value.copy(weatherResponseState = state)
    }

    fun selectDay(day: LocalDate) {
        _weatherScreenState.value = _weatherScreenState.value.copy(selectedDay = day)
    }

    fun changeLocation(location: UserLocation) {
        _weatherScreenState.value = _weatherScreenState.value.copy(location = location)
        updateWeather()
    }
}
