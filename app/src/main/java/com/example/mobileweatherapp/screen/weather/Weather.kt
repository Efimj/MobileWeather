package com.example.mobileweatherapp.screen.weather

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.rounded.AddLocation
import androidx.compose.material.icons.rounded.CloudUpload
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.mobileweatherapp.R
import com.example.mobileweatherapp.navigation.NavigationHelper.navigateToSecondary
import com.example.mobileweatherapp.navigation.Screen
import com.example.mobileweatherapp.ui.components.HorizontalSpacer
import com.example.mobileweatherapp.ui.components.LocationCard
import com.example.mobileweatherapp.ui.components.Stub
import com.example.mobileweatherapp.ui.components.TimeWeatherCard
import com.example.mobileweatherapp.ui.components.VerticalSpacer
import com.example.mobileweatherapp.ui.components.WeatherDayCard
import com.example.mobileweatherapp.ui.helper.LocaleProvider
import com.example.mobileweatherapp.util.settings.SettingsManager
import com.example.mobileweatherapp.util.settings.SettingsManager.settings
import com.example.mobileweatherapp.util.settings.UserLocation
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

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
                        addLocation = addLocation
                    )
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun Location(
    state: WeatherScreenState,
    changeLocation: (UserLocation) -> Unit,
    addLocation: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState()
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    Row(
        modifier = Modifier.padding(horizontal = 20.dp),
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
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onSecondaryContainer,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        HorizontalSpacer(value = 20.dp)
        Card(modifier = Modifier.size(45.dp), shape = MaterialTheme.shapes.small) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clickable { scope.launch { sheetState.show() } },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.Settings,
                    contentDescription = null
                )
            }
        }
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
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(horizontal = 20.dp)
                ) {
                    items(settings.locations.toList()) { location ->
                        LocationCard(
                            location = location,
                            isSelected = location == settings.selectedLocation,
                            onClick = {
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
                    ExtendedFloatingActionButton(
                        onClick = {
                            scope.launch {
                                sheetState.hide()
                            }.invokeOnCompletion { addLocation() }
                        },
                        icon = {
                            Icon(
                                imageVector = Icons.Rounded.AddLocation,
                                contentDescription = null
                            )
                        },
                        text = { Text(text = stringResource(R.string.add_location)) },
                    )
                }
                VerticalSpacer(value = 40.dp)
            }
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



