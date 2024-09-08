package com.example.mobileweatherapp.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.mobileweatherapp.R
import com.example.openmeteoapi.model.DailyWeatherData
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import kotlin.math.roundToInt

@Composable
fun TimeWeatherCard(
    hour: Int,
    currentWeather: DailyWeatherData
) {
    val isNow = LocalTime.now().hour == hour
    val currentTime = LocalTime.of(hour, 0)
    val timeText =
        if (isNow) stringResource(R.string.now) else currentTime.format(
            DateTimeFormatter.ofPattern(
                "h:mm a"
            )
        )
    val tempText = "${currentWeather.temperature.getOrElse(hour, { 0.0 }).roundToInt()}°"
    val humidityText = "${currentWeather.relativeHumidity.getOrNull(hour) ?: ""}%"

    val backgroundColor by animateColorAsState(if (isNow) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainerHigh)
    val color by animateColorAsState(if (isNow) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onPrimaryContainer)

    Card(
        shape = MaterialTheme.shapes.small,
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = tempText,
                        color = color,
                        style = MaterialTheme.typography.titleMedium,
                        textAlign = TextAlign.Center,
                        maxLines = 1
                    )
                    HorizontalSpacer(value = 7.dp)
                    AnimatedContent(
                        painterResource(
                            currentWeather.weatherHourly.getOrNull(hour)
                                ?.getWeatherIcon(
                                    currentWeather.checkIsNight(currentTime)
                                ) ?: R.drawable.not_available
                        )
                    ) {
                        Image(
                            modifier = Modifier.size(24.dp),
                            painter = it,
                            contentDescription = null
                        )
                    }
                }
                VerticalSpacer(value = 7.dp)
                Text(
                    text = timeText,
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}
