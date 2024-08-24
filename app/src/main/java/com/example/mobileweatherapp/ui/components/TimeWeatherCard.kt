package com.example.mobileweatherapp.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.mobileweatherapp.R
import com.example.openweatherapi.model.DailyWeatherData
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import kotlin.math.roundToInt

@Composable
fun TimeWeatherCard(
    hour: Int,
    currentWeather: DailyWeatherData
) {
    val isNow = LocalTime.now().hour == hour
    val timeText =
        if (isNow) stringResource(R.string.now) else LocalTime.of(hour, 0)
            .format(DateTimeFormatter.ofPattern("h:mm a"))
    val tempText = "${currentWeather.temperature[hour].roundToInt()}°"
    val humidityText = "${currentWeather.relativeHumidity[hour]}%"

    val borderColor by animateColorAsState(if (isNow) MaterialTheme.colorScheme.primary else Color.Transparent)

    Card(
        shape = MaterialTheme.shapes.small,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
        border = BorderStroke(width = 2.dp, color = borderColor)
    ) {
        Column(
            modifier = Modifier.size(70.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = tempText,
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.titleMedium,
                textAlign = TextAlign.Center,
                maxLines = 1
            )
            VerticalSpacer(value = 5.dp)
            Text(
                text = timeText,
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center,
                maxLines = 1
            )
        }
    }
}
