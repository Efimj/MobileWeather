package com.yefim.mobileweatherapp.screen.weather

import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yefim.mobileweatherapp.util.DateTimeUtil
import com.yefim.mobileweatherapp.util.settings.SettingsManager.settings
import com.yefim.mobileweatherapp.util.settings.WeatherForecast
import com.yefim.openmeteoapi.di.OpenMeteoInstance
import com.yefim.openmeteoapi.util.WeatherUtil
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
    val forecast: WeatherForecast? = settings.selectedWeatherForecast,
    val isLoading: Boolean = false,
)

/**
 * ViewModel for managing the state of the weather screen.
 */
class WeatherViewModel : ViewModel() {
    private val _weatherScreenState = mutableStateOf(WeatherScreenState())
    val weatherScreenState: State<WeatherScreenState> = _weatherScreenState

    fun updateWeather() {
        _weatherScreenState.value = _weatherScreenState.value.copy(isLoading = true)

        viewModelScope.launch {
            _weatherScreenState.value.forecast?.let {
                if (it.checkIsDataRelevant()) {
                    _weatherScreenState.value = _weatherScreenState.value.copy(forecast = it)
                } else {
                    try {
                        val response = OpenMeteoInstance.getWeatherService().getForecast(
                            lat = it.location.latitude,
                            lon = it.location.longitude,
                        )
                        val weatherByDay = WeatherUtil.groupWeatherByDay(response)

                        val forecast =
                            settings.selectedWeatherForecast?.copy(
                                weatherForecast = weatherByDay,
                                lastUpdateDate = DateTimeUtil.getLocalDateTime()
                            )
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
