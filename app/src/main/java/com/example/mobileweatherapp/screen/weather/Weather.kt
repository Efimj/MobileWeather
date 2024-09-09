package com.example.mobileweatherapp.screen.weather

import android.content.Context
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.mobileweatherapp.R
import com.example.mobileweatherapp.navigation.NavigationHelper.navigateToSecondary
import com.example.mobileweatherapp.navigation.Screen
import com.example.mobileweatherapp.ui.components.CustomFAB
import com.example.mobileweatherapp.ui.components.HorizontalSpacer
import com.example.mobileweatherapp.ui.components.LocationCard
import com.example.mobileweatherapp.ui.components.Stub
import com.example.mobileweatherapp.ui.components.TimeWeatherCard
import com.example.mobileweatherapp.ui.components.VerticalSpacer
import com.example.mobileweatherapp.ui.components.WeatherDayCard
import com.example.mobileweatherapp.ui.helper.LocaleProvider
import com.example.mobileweatherapp.ui.modifier.fadingEdges
import com.example.mobileweatherapp.util.settings.SettingsManager
import com.example.mobileweatherapp.util.settings.SettingsManager.settings
import com.example.mobileweatherapp.util.settings.UserLocation
import com.example.openmeteoapi.model.DayWeatherData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import kotlin.math.roundToInt

/**
 * Provide weather screen.
 */
@Composable
fun WeatherScreen(navController: NavHostController, viewModel: WeatherViewModel = viewModel()) {
    val weatherState by viewModel.weatherScreenState.collectAsState()

    val updateAction: () -> Unit = { viewModel.updateWeather() }

    LaunchedEffect(Unit) {
        settings.selectedLocation?.let { viewModel.changeLocation(it) }
    }

    WeatherScreenContent(
        state = weatherState,
        onUpdate = updateAction,
        onSelectDay = viewModel::selectDay,
        changeLocation = viewModel::changeLocation,
        addLocation = { navController.navigateToSecondary(Screen.Location) }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeatherScreenContent(
    state: WeatherScreenState,
    onUpdate: () -> Unit,
    onSelectDay: (LocalDate) -> Unit,
    changeLocation: (UserLocation) -> Unit,
    addLocation: () -> Unit
) {
    val insets = LocaleProvider.LocalInsetsPaddings.current

    PullToRefreshBox(
        isRefreshing = state.isLoading,
        onRefresh = onUpdate
    ) {
        AnimatedContent(targetState = state.isLoading) { loading ->
            if (loading) {
                Stub(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(insets),
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
                    Location(
                        state = state,
                        changeLocation = changeLocation,
                        addLocation = addLocation,
                        onUpdate = { settings.selectedLocation?.let { changeLocation(it) } }
                    )
                    VerticalSpacer(value = 20.dp)
                    state.weatherByDay.getOrDefault(state.selectedDay, null)
                        ?.let { WeatherHeader(weather = it) }
                    VerticalSpacer(value = 20.dp)
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

@Composable
private fun WeatherHeader(weather: DayWeatherData) {
    val hour = LocalTime.now().hour

    val tempText = "${weather.temperature[hour].roundToInt()}°"
    val currentTime = LocalTime.of(hour, 0)
    val timeText = currentTime.format(DateTimeFormatter.ofPattern("h:mm a"))
    val temperatureMinMaxText =
        "${weather.temperatureMax.roundToInt()}°/${weather.temperatureMin.roundToInt()}°"

    val weatherImage = weather.weather.getWeatherIcon(
        weather.checkIsNight(LocalTime.now())
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
    changeLocation: (UserLocation) -> Unit,
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
            text = state.location?.address ?: stringResource(R.string.address_unknown),
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
            var selectedLocations by remember { mutableStateOf(emptyList<UserLocation>()) }

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
                    items(settings.locations.toList()) { location ->
                        val isSelected =
                            if (editing) selectedLocations.contains(location) else location == settings.selectedLocation

                        LocationCard(
                            modifier = Modifier.animateItem(),
                            location = location,
                            isSelected = isSelected,
                            onClick = {
                                if (editing) {
                                    selectedLocations = if (isSelected) {
                                        selectedLocations.minus(location)
                                    } else if (selectedLocations.size < settings.locations.size - 1) {
                                        selectedLocations.plus(location)
                                    } else {
                                        selectedLocations
                                    }
                                } else {
                                    scope.launch {
                                        sheetState.hide()
                                    }.invokeOnCompletion {
                                        SettingsManager.update(
                                            context = context,
                                            settings = settings.copy(selectedLocation = location)
                                        )
                                        changeLocation(location)
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
                                    selectedLocations = selectedLocations,
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
                        targetState = settings.locations.size > 1
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
    selectedLocations: List<UserLocation>,
    scope: CoroutineScope,
    context: Context,
    onUpdate: () -> Unit,
) {
    if (settings.locations.size < 2) return

    val isLocationChanged = selectedLocations.contains(settings.selectedLocation)
    val newLocations = settings.locations - selectedLocations
    val selectedLocation =
        newLocations.firstOrNull { it != settings.selectedLocation } ?: settings.selectedLocation

    scope.launch {
        SettingsManager.update(
            context = context,
            settings = settings.copy(
                locations = newLocations,
                selectedLocation = selectedLocation
            )
        )
        if (isLocationChanged) onUpdate()
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
    Column(
        modifier = Modifier.padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        val keys = state.weatherByDay.keys
        keys.forEachIndexed { index, localDate ->
            val weatherByDay = state.weatherByDay.getValue(localDate)

            val maxShapeVal = 12.dp
            val minShapeVal = 6.dp

            val shape = when {
                index == 0 && keys.size == 1 -> RoundedCornerShape(maxShapeVal)
                index == 0 -> RoundedCornerShape(
                    topStart = maxShapeVal,
                    topEnd = maxShapeVal,
                    bottomEnd = minShapeVal,
                    bottomStart = minShapeVal
                )

                index > 0 && index < keys.size - 1 -> RoundedCornerShape(minShapeVal)
                index == keys.size - 1 -> RoundedCornerShape(
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
                weather = weatherByDay,
                shape = shape,
                isSelected = state.selectedDay == weatherByDay.date,
                onClick = onSelectDay
            )
        }
    }
}



