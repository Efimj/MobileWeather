package com.yefim.mobileweatherapp.screen.weather

import android.text.format.DateUtils
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yefim.mobileweatherapp.util.DateTimeUtil
import com.yefim.mobileweatherapp.util.settings.SettingsManager.settings
import com.yefim.mobileweatherapp.util.settings.WeatherForecast
import com.yefim.openmeteoapi.di.OpenMeteoInstance
import com.yefim.openmeteoapi.model.DayWeatherData
import com.yefim.openmeteoapi.model.WeatherData
import com.yefim.openmeteoapi.util.WeatherUtil
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate

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
 * @property forecast The current location of the user.
 */
data class WeatherScreenState(
    val weatherResponseState: WeatherResponseState = WeatherResponseState.Done,
    val selectedDay: LocalDate = DateTimeUtil.getLocalDateTime().date,
    val forecast: WeatherForecast? = null,
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
            _weatherScreenState.value.forecast?.let {
                try {
                    val response = OpenMeteoInstance.getWeatherService().getForecast(
                        lat = it.location.latitude,
                        lon = it.location.longitude,
                    )
                    val weatherByDay = WeatherUtil.groupWeatherByDay(response)

                    val forecast =
                        settings.selectedWeatherForecast?.copy(weatherForecast = weatherByDay)
                            ?: settings.weatherForecasts.first()

                    _weatherScreenState.value =
                        _weatherScreenState.value.copy(
                            forecast = forecast
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

    fun changeSelectedForecast(forecast: WeatherForecast) {
        _weatherScreenState.value = _weatherScreenState.value.copy(forecast = forecast)
        updateWeather()
    }
}
