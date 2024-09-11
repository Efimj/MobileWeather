package com.yefim.openmeteoapi.util

import com.yefim.openmeteoapi.model.DayWeatherData
import com.yefim.openmeteoapi.model.WeatherData
import java.time.LocalDate
import java.time.LocalDateTime

object WeatherUtil {
    /**
     * Groups the hourly weather data into a map where each key is a [LocalDate]
     * and the corresponding value is a [DayWeatherData].
     *
     * @param weatherData The hourly weather data to be grouped.
     * @return A map of [LocalDate] to [DayWeatherData] where each entry represents
     *         weather information for a specific day.
     */
    fun groupWeatherByDay(weatherData: WeatherData): Map<LocalDate, DayWeatherData> {
        val splitTemperature = weatherData.hourly.temperature.chunked(24)
        val splitRelativeHumidity = weatherData.hourly.relativeHumidity.chunked(24)
        val splitWeatherCode = weatherData.hourly.weatherList().chunked(24)

        val weatherMap = mutableMapOf<LocalDate, DayWeatherData>()

        weatherData.daily.date.forEachIndexed { index, date ->
            val date = LocalDate.parse(date)

            weatherMap[date] = DayWeatherData(
                date = date,
                temperature = splitTemperature[index],
                relativeHumidity = splitRelativeHumidity[index],
                weatherHourly = splitWeatherCode[index],
                temperatureMax = weatherData.daily.temperature2mMax[index],
                temperatureMin = weatherData.daily.temperature2mMin[index],
                apparentTemperatureMax = weatherData.daily.apparentTemperatureMax[index],
                apparentTemperatureMin = weatherData.daily.apparentTemperatureMin[index],
                sunrise = LocalDateTime.parse(weatherData.daily.sunrise[index]),
                sunset = LocalDateTime.parse(weatherData.daily.sunset[index]),
                daylightDuration = weatherData.daily.daylightDuration[index],
                sunshineDuration = weatherData.daily.sunshineDuration[index],
                uvIndexMax = weatherData.daily.uvIndexMax[index],
                uvIndexClearSkyMax = weatherData.daily.uvIndexClearSkyMax[index],
                precipitationSum = weatherData.daily.precipitationSum[index],
                precipitationProbabilityMax = weatherData.daily.precipitationProbabilityMax[index],
                windSpeedMax = weatherData.daily.windSpeed10mMax[index],
                windGustsMax = weatherData.daily.windGusts10mMax[index],
                windDirectionDominant = weatherData.daily.windDirection10mDominant[index],
                weather = weatherData.daily.weatherList()[index]
            )
        }

        return weatherMap
    }
}