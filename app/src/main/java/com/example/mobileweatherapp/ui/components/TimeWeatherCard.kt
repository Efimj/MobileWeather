package com.example.mobileweatherapp.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.mobileweatherapp.R
import com.example.openweatherapi.model.DailyWeatherData
import java.time.LocalTime
import java.time.format.DateTimeFormatter

@Composable
fun TimeWeatherCard(
    hour: Int,
    currentWeather: DailyWeatherData
) {
    val isNow = LocalTime.now().hour == hour
    val timeText =
        if (isNow) stringResource(R.string.now) else LocalTime.of(hour, 0)
            .format(DateTimeFormatter.ofPattern("h:mm a"))
    val tempText = "${currentWeather.temperature[hour]}°"
    val humidityText = "${currentWeather.relativeHumidity[hour]}%"

    val borderColor by animateColorAsState(if (isNow) MaterialTheme.colorScheme.primary else Color.Transparent)

    Card(
        shape = MaterialTheme.shapes.small,
        border = BorderStroke(width = 2.dp, color = borderColor)
    ) {
        Row(modifier = Modifier.padding(10.dp)) {
            Text(
                modifier = Modifier.weight(1f),
                text = timeText
            )
            HorizontalSpacer(value = 20.dp)
            Text(
                text = tempText,
                color = MaterialTheme.colorScheme.primary
            )
            HorizontalSpacer(value = 20.dp)
            Text(
                text = humidityText,
            )
        }
    }
}
