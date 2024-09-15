package com.yefim.mobileweatherapp.screen.weather

import android.content.Context
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.rounded.AddLocation
import androidx.compose.material.icons.rounded.Cancel
import androidx.compose.material.icons.rounded.CloudUpload
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Explore
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.yefim.mobileweatherapp.R
import com.yefim.mobileweatherapp.navigation.NavigationHelper.navigateToSecondary
import com.yefim.mobileweatherapp.navigation.Screen
import com.yefim.mobileweatherapp.ui.components.CustomFAB
import com.yefim.mobileweatherapp.ui.components.HorizontalSpacer
import com.yefim.mobileweatherapp.ui.components.LocationCard
import com.yefim.mobileweatherapp.ui.components.Stub
import com.yefim.mobileweatherapp.ui.components.TimeWeatherCard
import com.yefim.mobileweatherapp.ui.components.VerticalSpacer
import com.yefim.mobileweatherapp.ui.components.WeatherDayCard
import com.yefim.mobileweatherapp.ui.helper.LocaleProvider
import com.yefim.mobileweatherapp.ui.modifier.fadingEdges
import com.yefim.mobileweatherapp.util.DateTimeUtil
import com.yefim.mobileweatherapp.util.settings.SettingsManager
import com.yefim.mobileweatherapp.util.settings.SettingsManager.settings
import com.yefim.mobileweatherapp.util.settings.WeatherForecast
import com.yefim.openmeteoapi.model.DayWeatherData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlinx.datetime.toJavaLocalDate
import kotlinx.datetime.toJavaLocalTime
import java.time.format.DateTimeFormatter
import kotlin.math.max
import kotlin.math.roundToInt

/**
 * Provide weather screen.
 */
@Composable
fun WeatherScreen(navController: NavHostController, viewModel: WeatherViewModel = viewModel()) {
    val context = LocalContext.current
    val weatherState by viewModel.weatherScreenState

    val updateAction: () -> Unit = { viewModel.updateWeather() }

    LaunchedEffect(Unit) {
        settings.selectedWeatherForecast?.let { viewModel.changeSelectedForecast(it) }
    }

    LaunchedEffect(weatherState.forecast) {
        val currentForecast = weatherState.forecast ?: return@LaunchedEffect

        SettingsManager.update(
            context = context,
            settings = settings.copy(
                selectedWeatherForecast = currentForecast,
                weatherForecasts = settings.weatherForecasts.map { forecast ->
                    if (currentForecast.location.latitude == forecast.location.latitude && currentForecast.location.longitude == forecast.location.longitude) {
                        currentForecast
                    } else {
                        forecast
                    }
                }.toSet()
            )
        )
    }

    WeatherScreenContent(
        state = weatherState,
        onUpdate = updateAction,
        onSelectDay = viewModel::selectDay,
        changeLocation = viewModel::changeSelectedForecast,
        addLocation = { navController.navigateToSecondary(Screen.Location) }
    )
}

private enum class ScreenState {
    Loading,
    Empty,
    Forecast
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeatherScreenContent(
    state: WeatherScreenState,
    onUpdate: () -> Unit,
    onSelectDay: (LocalDate) -> Unit,
    changeLocation: (WeatherForecast) -> Unit,
    addLocation: () -> Unit
) {
    val insets = LocaleProvider.LocalInsetsPaddings.current

    val screenState = when {
        state.isLoading -> ScreenState.Loading
        state.forecast?.weatherForecast?.firstOrNull() == null -> ScreenState.Empty
        else -> ScreenState.Forecast
    }

    PullToRefreshBox(
        isRefreshing = state.isLoading,
        onRefresh = onUpdate
    ) {
        AnimatedContent(targetState = screenState) {
            when (it) {
                ScreenState.Loading -> Stub(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(insets),
                    icon = Icons.Rounded.CloudUpload,
                    title = stringResource(R.string.loading),
                    description = stringResource(R.string.it_has_to_happen_quickly),
                )

                ScreenState.Empty -> Stub(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(insets),
                    icon = Icons.Rounded.Explore,
                    title = stringResource(R.string.forecast_missing),
                    description = stringResource(R.string.weather_forecast_is_missing_try_refreshing),
                )

                ScreenState.Forecast -> {
                    val forecast by remember(state.forecast?.weatherForecast, state.selectedDay) {
                        mutableStateOf(
                            checkDateValid(
                                forecast = state.forecast?.weatherForecast!!,
                                state = state,
                                onSelectDay = onSelectDay
                            )
                        )
                    }

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(insets)
                            .padding(vertical = 20.dp)
                    ) {
                        Location(
                            state = state,
                            changeForecast = changeLocation,
                            addLocation = addLocation,
                            onUpdate = {
                                settings.selectedWeatherForecast?.let {
                                    changeLocation(
                                        it
                                    )
                                }
                            }
                        )
                        VerticalSpacer(value = 20.dp)
                        WeatherHeader(weather = forecast)
                        VerticalSpacer(value = 20.dp)
                        AnimatedContent(state.selectedDay) {
                            Text(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .basicMarquee()
                                    .padding(horizontal = 20.dp),
                                text = it.toJavaLocalDate()
                                    .format(DateTimeFormatter.ofPattern("EEEE, MMM d")),
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
                        VerticalSpacer(value = 20.dp)
                        Humidity(forecast)
                        VerticalSpacer(value = 40.dp)
                    }
                }
            }
        }
    }
}

@Composable
private fun Humidity(it: DayWeatherData) {
    val time = DateTimeUtil.getLocalDateTime()
    Column(
        modifier = Modifier
            .padding(horizontal = 20.dp)
            .clip(MaterialTheme.shapes.large)
            .background(MaterialTheme.colorScheme.surfaceContainer)
            .padding(vertical = 20.dp)
    ) {

        val averageHumidity = it.relativeHumidity.average().toInt()
        Column(modifier = Modifier.padding(horizontal = 20.dp)) {
            Text(
                text = stringResource(R.string.average_humidity),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary.copy(alpha = .8f)
            )
            Text(
                text = buildAnnotatedString {
                    withStyle(style = MaterialTheme.typography.headlineMedium.toSpanStyle()) {
                        append("$averageHumidity")
                    }
                    withStyle(style = MaterialTheme.typography.titleMedium.toSpanStyle()) {
                        append("%")
                    }
                },
                style = MaterialTheme.typography.headlineMedium
            )
        }
        VerticalSpacer(value = 4.dp)

        val scroll = rememberLazyListState()

        LaunchedEffect(it.relativeHumidity) {
            scroll.animateScrollToItem(time.hour)
        }

        LazyRow(
            state = scroll,
            modifier = Modifier
                .height(100.dp)
                .fadingEdges(scroll),
            contentPadding = PaddingValues(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.Bottom
        ) {
            itemsIndexed(it.relativeHumidity) { index, humidity ->
                val isNow = time.hour == index
                val currentTime = LocalTime(index, 0)
                val timeText =
                    if (isNow) stringResource(R.string.now) else currentTime.toJavaLocalTime()
                        .format(
                            DateTimeFormatter.ofPattern("h a")
                        )

                Column(
                    modifier = Modifier.width(30.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    val barHeight by animateFloatAsState(humidity / 100f)

                    Spacer(modifier = Modifier.weight(max(1 - barHeight, 0.01f)))
                    Text(
                        text = "${humidity}%",
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 1
                    )

                    val clampedPercentage = humidity / 100f

                    val backgroundColor = lerp(
                        MaterialTheme.colorScheme.onPrimary,
                        MaterialTheme.colorScheme.primary,
                        clampedPercentage
                    )

                    Box(
                        modifier = Modifier
                            .padding(vertical = 8.dp)
                            .fillMaxWidth()
                            .weight(max(1 + barHeight, 0.01f))
                            .clip(MaterialTheme.shapes.medium)
                            .background(backgroundColor)
                    )
                    Text(
                        text = timeText,
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 1,
                        softWrap = false
                    )
                }
            }
        }
    }
}

@Composable
private fun WeatherHeader(weather: DayWeatherData) {
    val localDateTime = DateTimeUtil.getLocalDateTime()
    val hour = localDateTime.hour

    val tempText = "${weather.temperature[hour].roundToInt()}°"
    val currentTime = LocalTime(hour, 0)
    val timeText = currentTime.toJavaLocalTime().format(DateTimeFormatter.ofPattern("h:mm a"))
    val temperatureMinMaxText =
        "${weather.temperatureMax.roundToInt()}°/${weather.temperatureMin.roundToInt()}°"

    val weatherImage = weather.weather.getWeatherIcon(
        weather.checkIsNight(localDateTime.time)
    )

    Row(
        modifier = Modifier
            .padding(horizontal = 20.dp)
            .clip(MaterialTheme.shapes.large)
            .background(MaterialTheme.colorScheme.primaryContainer)
            .padding(20.dp)
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = timeText,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = .8f)
            )
            Text(
                text = tempText,
                style = MaterialTheme.typography.displayMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Text(
                text = temperatureMinMaxText,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = .8f)
            )
        }
        Column(horizontalAlignment = Alignment.End) {
            AnimatedContent(painterResource(weatherImage)) {
                Image(
                    modifier = Modifier.size(54.dp),
                    painter = it,
                    contentDescription = null
                )
            }
            Text(
                text = stringResource(weather.weather.description),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = .8f)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun Location(
    state: WeatherScreenState,
    changeForecast: (WeatherForecast) -> Unit,
    addLocation: () -> Unit,
    onUpdate: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState()
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    Row(
        modifier = Modifier
            .padding(horizontal = 20.dp)
            .clip(CircleShape)
            .clickable { scope.launch { sheetState.show() } }
            .background(MaterialTheme.colorScheme.surfaceContainerHighest)
            .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Outlined.LocationOn,
            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = .8f),
            contentDescription = null
        )
        HorizontalSpacer(value = 20.dp)
        Text(
            modifier = Modifier.weight(1f),
            text = state.forecast?.location?.address ?: stringResource(R.string.address_unknown),
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = .8f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }

    if (sheetState.isVisible) {
        ModalBottomSheet(
            sheetState = sheetState,
            onDismissRequest = {
                scope.launch {
                    sheetState.hide()
                }
            },
        ) {
            var editing by remember { mutableStateOf(false) }
            var selectedForecast by remember { mutableStateOf(emptyList<WeatherForecast>()) }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    text = stringResource(R.string.saved_locations),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Normal,
                    color = MaterialTheme.colorScheme.onSurface.copy(.8f)
                )
                VerticalSpacer(value = 10.dp)

                val scroll = rememberLazyListState()

                LazyColumn(
                    state = scroll,
                    modifier = Modifier
                        .weight(1f, false)
                        .fadingEdges(scrollableState = scroll, isVertical = true),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(horizontal = 20.dp)
                ) {
                    items(settings.weatherForecasts.toList()) { forecast ->
                        val isSelected =
                            if (editing) selectedForecast.contains(forecast) else forecast == settings.selectedWeatherForecast

                        LocationCard(
                            modifier = Modifier.animateItem(),
                            location = forecast.location,
                            isSelected = isSelected,
                            onClick = {
                                if (editing) {
                                    selectedForecast = if (isSelected) {
                                        selectedForecast.minus(forecast)
                                    } else if (selectedForecast.size < settings.weatherForecasts.size - 1) {
                                        selectedForecast.plus(forecast)
                                    } else {
                                        selectedForecast
                                    }
                                } else {
                                    scope.launch {
                                        sheetState.hide()
                                    }.invokeOnCompletion {
                                        SettingsManager.update(
                                            context = context,
                                            settings = settings.copy(selectedWeatherForecast = forecast)
                                        )
                                        changeForecast(forecast)
                                    }
                                }
                            }
                        )
                    }
                }
                VerticalSpacer(value = 20.dp)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.Center
                ) {
                    val icon = if (editing) Icons.Rounded.Delete else Icons.Rounded.AddLocation
                    val text = if (editing) stringResource(R.string.delete) else stringResource(
                        R.string.add_location
                    )

                    CustomFAB(
                        modifier = Modifier.padding(start = 10.dp),
                        onClick = {
                            if (editing) {
                                editing = false
                                onDelete(
                                    selectedForecast = selectedForecast,
                                    scope = scope,
                                    context = context,
                                    onUpdate = onUpdate
                                )
                            } else {
                                scope.launch {
                                    sheetState.hide()
                                }.invokeOnCompletion { addLocation() }
                            }
                        },
                        icon = icon,
                        text = text,
                    )
                    AnimatedContent(
                        targetState = settings.weatherForecasts.size > 1
                    ) { isMultipleLocations ->
                        if (isMultipleLocations) {
                            val icon = if (editing) Icons.Rounded.Cancel else Icons.Rounded.Edit
                            val text =
                                if (editing) stringResource(R.string.cancel) else stringResource(R.string.edit)

                            CustomFAB(
                                modifier = Modifier.padding(start = 10.dp),
                                onClick = { editing = !editing },
                                icon = icon,
                                text = text
                            )
                        }
                    }
                }
                VerticalSpacer(value = 40.dp)
            }
        }
    }
}

private fun onDelete(
    selectedForecast: List<WeatherForecast>,
    scope: CoroutineScope,
    context: Context,
    onUpdate: () -> Unit,
) {
    if (settings.weatherForecasts.size < 2) return

    val isLocationChanged = selectedForecast.contains(settings.selectedWeatherForecast)
    val newLocations = settings.weatherForecasts - selectedForecast
    val selectedLocation =
        newLocations.firstOrNull { it != settings.selectedWeatherForecast }
            ?: settings.selectedWeatherForecast

    scope.launch {
        SettingsManager.update(
            context = context,
            settings = settings.copy(
                weatherForecasts = newLocations,
                selectedWeatherForecast = selectedLocation
            )
        )
        if (isLocationChanged) onUpdate()
    }
}

@Composable
private fun DayWeather(state: WeatherScreenState) {
    val scroll = rememberLazyListState()
    val currentWeather =
        state.forecast?.weatherForecast?.firstOrNull { it.date == state.selectedDay }

    LaunchedEffect(state.selectedDay) {
        val hour = DateTimeUtil.getLocalDateTime().hour
        scroll.animateScrollToItem(hour)
    }

    if (currentWeather == null) return

    LazyRow(
        modifier = Modifier.fadingEdges(scroll),
        state = scroll,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
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
    val forecast = state.forecast?.weatherForecast ?: return

    LaunchedEffect(state.forecast) {
        checkDateValid(forecast = forecast, state = state, onSelectDay = onSelectDay)
    }

    Column(
        modifier = Modifier.padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        val countDays = forecast.size ?: 0

        forecast.forEachIndexed { index, forecast ->
            val maxShapeVal = 16.dp
            val minShapeVal = 8.dp

            val shape = when {
                index == 0 && countDays == 1 -> RoundedCornerShape(maxShapeVal)
                index == 0 -> RoundedCornerShape(
                    topStart = maxShapeVal,
                    topEnd = maxShapeVal,
                    bottomEnd = minShapeVal,
                    bottomStart = minShapeVal
                )

                index > 0 && index < countDays - 1 -> RoundedCornerShape(minShapeVal)
                index == countDays - 1 -> RoundedCornerShape(
                    bottomStart = maxShapeVal,
                    bottomEnd = maxShapeVal,
                    topStart = minShapeVal,
                    topEnd = minShapeVal
                )

                else -> {
                    RoundedCornerShape(maxShapeVal)
                }
            }

            WeatherDayCard(
                weather = forecast,
                shape = shape,
                isSelected = state.selectedDay == forecast.date,
                onClick = onSelectDay
            )
        }
    }
}

private fun checkDateValid(
    forecast: Set<DayWeatherData>,
    state: WeatherScreenState,
    onSelectDay: (LocalDate) -> Unit
): DayWeatherData {
    val validDay = forecast.find { it.date == state.selectedDay }
    return validDay ?: forecast.first().also { onSelectDay(it.date) }
}



