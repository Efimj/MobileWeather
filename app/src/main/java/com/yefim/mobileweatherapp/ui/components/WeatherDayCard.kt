package com.yefim.mobileweatherapp.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.yefim.mobileweatherapp.R
import com.yefim.mobileweatherapp.util.DateTimeUtil
import com.yefim.openmeteoapi.model.DayWeatherData
import kotlinx.datetime.LocalDate
import kotlinx.datetime.toJavaLocalDate
import java.time.format.DateTimeFormatter
import kotlin.math.roundToInt

@Composable
fun WeatherDayCard(
    weather: DayWeatherData,
    shape: Shape = CardDefaults.shape,
    isSelected: Boolean = false,
    onClick: (LocalDate) -> Unit
) {
    val formatter = DateTimeFormatter.ofPattern("EEEE, MMM d")
    val date = weather.date
    val currentDateTime = DateTimeUtil.getLocalDateTime()

    val dateText =
        if (date == currentDateTime.date) stringResource(R.string.today) else date.toJavaLocalDate().format(formatter)

    val temperatureText = AnnotatedString.Builder().apply {
        withStyle(style = SpanStyle(color = MaterialTheme.colorScheme.onSurface)) {
            append("${weather.temperatureMax.roundToInt()}°")
        }
        withStyle(style = SpanStyle(color = MaterialTheme.colorScheme.primary)) {
            append("/${weather.temperatureMin.roundToInt()}°")
        }
    }.toAnnotatedString()

    val weatherImage = weather.weather.getWeatherIcon(
        weather.checkIsNight(currentDateTime.time)
    )

    val borderColor by animateColorAsState(if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent)

    Card(
        shape = shape,
        border = BorderStroke(width = 2.dp, color = borderColor),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onClick(date) }
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = dateText,
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.titleMedium,
            )
            Spacer(modifier = Modifier.weight(1f))
            Image(
                modifier = Modifier.size(24.dp),
                painter = painterResource(weatherImage),
                contentDescription = null
            )
            Spacer(modifier = Modifier.width(30.dp))
            Text(
                text = temperatureText,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Normal,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}