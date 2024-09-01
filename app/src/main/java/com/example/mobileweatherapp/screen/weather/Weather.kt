package com.example.mobileweatherapp.screen.weather

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CloudUpload
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.mobileweatherapp.R
import com.example.mobileweatherapp.ui.components.Stub
import com.example.mobileweatherapp.ui.components.TimeWeatherCard
import com.example.mobileweatherapp.ui.components.VerticalSpacer
import com.example.mobileweatherapp.ui.components.WeatherDayCard
import com.example.mobileweatherapp.ui.helper.LocaleProvider
import com.example.mobileweatherapp.util.settings.SettingsManager.settings
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

/**
 * Provide weather screen.
 */
@Composable
fun WeatherScreen(navController: NavHostController, viewModel: WeatherViewModel = viewModel()) {
    val context = LocalContext.current
    val weatherState by viewModel.weatherScreenState.collectAsState()

    val updateAction: () -> Unit = { viewModel.updateWeather(context = context) }

    OnLaunch(viewModel)
    WeatherScreenContent(
        state = weatherState,
        onUpdate = updateAction,
        onSelectDay = viewModel::selectDay
    )
}

@Composable
private fun OnLaunch(
    viewModel: WeatherViewModel,
) {
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.updateWeather(context = context)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeatherScreenContent(
    state: WeatherScreenState,
    onUpdate: () -> Unit,
    onSelectDay: (LocalDate) -> Unit
) {
    val insets = LocaleProvider.LocalInsetsPaddings.current

    PullToRefreshBox(
        isRefreshing = state.isLoading,
        onRefresh = onUpdate
    ) {
        AnimatedContent(targetState = state.isLoading) { loading ->
            if (loading) {
                Stub(
                    modifier = Modifier.fillMaxSize().padding(insets),
                    icon = Icons.Rounded.CloudUpload,
                    title = stringResource(R.string.loading),
                    description = stringResource(R.string.it_has_to_happen_quickly),
                )
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(insets)
                        .padding(vertical = 20.dp)
                ) {
                    settings.location?.let {
                        Text(
                            modifier = Modifier
                                .basicMarquee()
                                .padding(horizontal = 20.dp),
                            text = it.address,
                            style = MaterialTheme.typography.headlineSmall,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                    VerticalSpacer(value = 10.dp)
                    AnimatedContent(state.selectedDay) {
                        Text(
                            modifier = Modifier
                                .fillMaxWidth()
                                .basicMarquee()
                                .padding(horizontal = 20.dp),
                            text = it.format(DateTimeFormatter.ofPattern("EEEE, MMM d")),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Normal,
                            color = MaterialTheme.colorScheme.onSurface.copy(.8f)
                        )
                    }
                    VerticalSpacer(value = 10.dp)
                    DayWeather(state)
                    VerticalSpacer(value = 20.dp)
                    Text(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp),
                        text = stringResource(R.string.weather_forecast),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Normal,
                        color = MaterialTheme.colorScheme.onSurface.copy(.8f)
                    )
                    VerticalSpacer(value = 10.dp)
                    ForecastDaysList(state, onSelectDay)
                }
            }
        }
    }
}

/**
 * When something wrong.
 */
@Composable
fun ErrorStub(
    icon: ImageVector,
    title: String,
    description: String,
    buttonText: String,
    onButtonClick: () -> Unit
) {
    Column {
        Stub(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            icon = icon,
            title = title,
            description = description
        )
        FooterAction(
            text = buttonText,
            onClick = onButtonClick
        )
    }
}

@Composable
fun FooterAction(text: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp, horizontal = 20.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Button(modifier = Modifier.fillMaxWidth(), onClick = onClick) {
            Text(text = text)
        }
    }
}

@Composable
private fun DayWeather(state: WeatherScreenState) {
    val scroll = rememberLazyListState()
    val currentWeather = state.weatherByDay.getOrDefault(state.selectedDay, null)

    LaunchedEffect(state.selectedDay) {
        val hour = LocalTime.now().hour
        scroll.animateScrollToItem(hour)
    }

    if (currentWeather == null) return

    LazyRow(
        state = scroll,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        contentPadding = PaddingValues(horizontal = 20.dp)
    ) {
        val timeRange = 0..23
        items(timeRange.toList()) {
            TimeWeatherCard(hour = it, currentWeather = currentWeather)
        }
    }
}

@Composable
private fun ForecastDaysList(
    state: WeatherScreenState,
    onSelectDay: (LocalDate) -> Unit
) {
    Column(
        modifier = Modifier.padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        state.weatherByDay.keys.forEach {
            val weatherByDay = state.weatherByDay.getValue(it)

            WeatherDayCard(
                weather = weatherByDay,
                isSelected = state.selectedDay == weatherByDay.date,
                onClick = onSelectDay
            )
        }
    }
}



