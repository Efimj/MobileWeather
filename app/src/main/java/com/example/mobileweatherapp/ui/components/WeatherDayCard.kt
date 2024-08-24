package com.example.mobileweatherapp.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.mobileweatherapp.R
import com.example.openweatherapi.model.DailyWeatherData
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
fun WeatherDayCard(
    weather: DailyWeatherData,
    isSelected: Boolean = false,
    onClick: (LocalDate) -> Unit
) {
    val formatter = DateTimeFormatter.ofPattern("EEEE, MMM d")
    val date = weather.date

    val dateText =
        if (date == LocalDate.now()) stringResource(R.string.today) else date.format(formatter)
    val tempText = "${weather.temperature.max()}°/${weather.temperature.min()}°"

    val borderColor by animateColorAsState(if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent)

    Card(
        border = BorderStroke(width = 2.dp, color = borderColor),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)
    ) {
        Column(
            modifier = Modifier
                .clickable { onClick(date) }
                .padding(12.dp)
        ) {
            Text(
                text = dateText,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = .8f)
            )
            Text(
                modifier = Modifier.fillMaxWidth(),
                text = tempText,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Normal,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}