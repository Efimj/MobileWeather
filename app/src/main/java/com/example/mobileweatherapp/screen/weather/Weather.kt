package com.example.mobileweatherapp.screen.weather

import android.app.Activity
import android.content.Context
import android.location.Location
import android.location.LocationManager
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CalendarToday
import androidx.compose.material.icons.rounded.CloudUpload
import androidx.compose.material.icons.rounded.Flag
import androidx.compose.material.icons.rounded.LocationOff
import androidx.compose.material.icons.rounded.WifiOff
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.mobileweatherapp.R
import com.example.mobileweatherapp.ui.components.Stub
import com.example.mobileweatherapp.ui.components.TimeWeatherCard
import com.example.mobileweatherapp.ui.components.VerticalSpacer
import com.example.mobileweatherapp.ui.components.WeatherDayCard
import com.example.mobileweatherapp.util.ContextUtil
import com.google.android.gms.location.LocationServices
import java.time.LocalDate
import java.time.format.DateTimeFormatter

/**
 * Provide weather screen.
 */
@Composable
fun WeatherScreen(viewModel: WeatherViewModel = viewModel()) {
    val context = LocalContext.current
    val weatherState by viewModel.weatherScreenState.collectAsState()

    val updateAction: () -> Unit = { viewModel.updateWeather(context = context) }

    OnPermissionUpdated(viewModel)

    AnimatedContent(targetState = weatherState.weatherResponseState) { state ->
        when (state) {
            WeatherResponseState.Loading -> {
                Stub(
                    modifier = Modifier.fillMaxSize(),
                    icon = Icons.Rounded.CloudUpload,
                    title = stringResource(R.string.loading),
                    description = stringResource(R.string.it_has_to_happen_quickly),
                )
            }

            WeatherResponseState.PermissionsNotGranted -> {
                ErrorStub(
                    icon = Icons.Rounded.Flag,
                    title = stringResource(R.string.permissions_required),
                    description = stringResource(R.string.permissions_are_required_for_the_program_to_work),
                    buttonText = stringResource(R.string.grant_permission),
                    onButtonClick = { ContextUtil.requestLocationPermission(context as Activity); updateAction() }
                )
            }

            WeatherResponseState.LocationNotFound -> {
                ErrorStub(
                    icon = Icons.Rounded.LocationOff,
                    title = stringResource(R.string.location_not_found),
                    description = stringResource(R.string.you_need_to_enable_geolocation),
                    buttonText = stringResource(R.string.update),
                    onButtonClick = updateAction
                )
            }

            WeatherResponseState.NetworkError -> {
                ErrorStub(
                    icon = Icons.Rounded.WifiOff,
                    title = stringResource(R.string.turn_on_the_internet),
                    description = stringResource(R.string.internet_is_required_for_the_program_to_work),
                    buttonText = stringResource(R.string.update),
                    onButtonClick = updateAction
                )
            }

            WeatherResponseState.ForecastMissing -> {
                ErrorStub(
                    icon = Icons.Rounded.CalendarToday,
                    title = stringResource(R.string.forecast_missing),
                    description = stringResource(R.string.weather_forecast_is_missing_try_refreshing),
                    buttonText = stringResource(R.string.update),
                    onButtonClick = updateAction,
                )
            }

            else -> {
                weatherState.weather?.let {
                    WeatherScreenContent(
                        state = weatherState,
                        onUpdate = updateAction,
                        onSelectDay = viewModel::selectDay
                    )
                }
            }
        }
    }
}

/**
 * For permission observing.
 */
@Composable
private fun OnPermissionUpdated(
    viewModel: WeatherViewModel,
) {
    val context = LocalContext.current
    val lifecycleState by LocalLifecycleOwner.current.lifecycle.currentStateFlow.collectAsState()

    LaunchedEffect(lifecycleState) {
        if (ContextUtil.checkLocationPermission(context)) {
            val locationManager =
                context.getSystemService(Context.LOCATION_SERVICE) as LocationManager

            val fusedLocationClient =
                LocationServices.getFusedLocationProviderClient(context as Activity)

            fusedLocationClient.lastLocation
                .addOnSuccessListener { location: Location? ->
                    if (location != null) {
                        viewModel.updateLocation(location)
                        viewModel.updateWeather(context)
                    }
                }
        } else {
            viewModel.updateWeatherResponseState(WeatherResponseState.PermissionsNotGranted)
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

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun WeatherScreenContent(
    state: WeatherScreenState,
    onUpdate: () -> Unit,
    onSelectDay: (LocalDate) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(vertical = 20.dp)
    ) {
        Text(
            modifier = Modifier
                .basicMarquee()
                .padding(horizontal = 20.dp),
            text = state.address,
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onSecondaryContainer
        )
        VerticalSpacer(value = 10.dp)
        ForecastDaysList(state, onSelectDay)
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
        FooterAction(stringResource(R.string.update), onClick = onUpdate)
    }
}

@Composable
private fun ColumnScope.DayWeather(state: WeatherScreenState) {
    LazyColumn(
        modifier = Modifier.weight(1f),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        contentPadding = PaddingValues(horizontal = 20.dp)
    ) {
        val currentWeather = state.weatherByDay.getValue(state.selectedDay)
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
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        contentPadding = PaddingValues(horizontal = 20.dp)
    ) {
        items(items = state.weatherByDay.keys.toList(), key = { it.toString() }) { day ->
            val weatherByDay = state.weatherByDay.getValue(day)

            WeatherDayCard(
                weather = weatherByDay,
                isSelected = state.selectedDay == weatherByDay.date,
                onClick = onSelectDay
            )
        }
    }
}



