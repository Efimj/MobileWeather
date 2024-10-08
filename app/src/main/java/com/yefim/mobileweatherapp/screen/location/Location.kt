package com.yefim.mobileweatherapp.screen.location

import android.app.Activity
import android.location.Location
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.rounded.AddLocation
import androidx.compose.material.icons.rounded.Flag
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.yefim.mobileweatherapp.R
import com.yefim.mobileweatherapp.navigation.NavigationHelper.navigateToMain
import com.yefim.mobileweatherapp.navigation.Screen
import com.yefim.mobileweatherapp.ui.components.HorizontalSpacer
import com.yefim.mobileweatherapp.ui.components.LocationCard
import com.yefim.mobileweatherapp.ui.components.VerticalSpacer
import com.yefim.mobileweatherapp.ui.helper.LocaleProvider
import com.yefim.mobileweatherapp.util.ContextUtil
import com.yefim.mobileweatherapp.util.LocationUtil
import com.yefim.mobileweatherapp.util.settings.SettingsManager
import com.yefim.mobileweatherapp.util.settings.SettingsManager.settings
import com.yefim.mobileweatherapp.util.settings.UserLocation
import com.yefim.openmeteoapi.model.LocationData
import com.google.android.gms.location.LocationServices
import com.yefim.mobileweatherapp.util.settings.WeatherForecast
import kotlinx.coroutines.launch

@Composable
fun LocationScreen(navController: NavHostController, viewModel: LocationViewModel = viewModel()) {
    val insets = LocaleProvider.LocalInsetsPaddings.current
    val locationState by viewModel.locationScreenState.collectAsState()
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(insets)
            .padding(vertical = 40.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            modifier = Modifier.size(60.dp),
            imageVector = Icons.Outlined.LocationOn,
            tint = MaterialTheme.colorScheme.primary,
            contentDescription = null
        )
        VerticalSpacer(value = 10.dp)
        Text(
            modifier = Modifier
                .padding(horizontal = 20.dp),
            text = stringResource(R.string.add_location),
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onSecondaryContainer,
            textAlign = TextAlign.Center
        )
        Text(
            modifier = Modifier
                .padding(horizontal = 20.dp),
            text = stringResource(R.string.need_provide_location),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(.8f),
            textAlign = TextAlign.Center
        )
        VerticalSpacer(value = 30.dp)
        Row(
            modifier = Modifier
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 20.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            SingleChoiceSegmentedButtonRow {
                LocationByType.entries.forEachIndexed { index, type ->
                    SegmentedButton(
                        shape = SegmentedButtonDefaults.itemShape(
                            index = index,
                            count = LocationByType.entries.size
                        ),
                        onClick = { viewModel.updateLocationByType(type) },
                        selected = type == locationState.type
                    ) {
                        Text(stringResource(type.title))
                    }
                }
            }
        }
        VerticalSpacer(value = 20.dp)
        AnimatedContent(targetState = locationState.type) {
            when (it) {
                LocationByType.Geolocation -> GetGeolocation(
                    state = locationState,
                    onUpdate = viewModel::updateLocation
                )

                LocationByType.Address -> AddressLocation(
                    state = locationState,
                    onUpdate = viewModel::updateAddressLocation,
                    onSelect = viewModel::updateLocation,
                )

                LocationByType.Coordinates -> GetCoordinates(
                    state = locationState,
                    onUpdate = viewModel::updateLocation,
                    onReset = viewModel::resetLocation,
                )
            }
        }
        VerticalSpacer(value = 20.dp)
        AnimatedVisibility(visible = locationState.location != null) {
            ExtendedFloatingActionButton(
                onClick = {
                    val location = locationState.location ?: return@ExtendedFloatingActionButton

                    val forecast = WeatherForecast(location = location)

                    SettingsManager.update(
                        context = context,
                        settings = settings.copy(
                            weatherForecasts = settings.weatherForecasts.plus(forecast),
                            selectedWeatherForecast = forecast
                        )
                    )

                    if (navController.previousBackStackEntry == null) {
                        navController.navigateToMain(Screen.Weather)
                    } else {
                        navController.popBackStack()
                    }
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
    }
}

@Composable
private fun AddressLocation(
    state: LocationScreenState,
    onUpdate: (String) -> Unit,
    onSelect: (UserLocation) -> Unit
) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    Column {
        Row(
            modifier = Modifier.padding(horizontal = 20.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            OutlinedTextField(
                modifier = Modifier.weight(1f),
                label = { Text(text = stringResource(R.string.address)) },
                value = state.addressString,
                shape = RoundedCornerShape(14.dp),
                onValueChange = onUpdate
            )
        }

        AnimatedContent(targetState = state.addressLocationResponse.result) {
            if (it != null) {
                Column(
                    modifier = Modifier
                        .animateContentSize()
                        .padding(top = 10.dp)
                        .padding(horizontal = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    it.take(3).forEach {
                        val isSelected =
                            it.latitude == state.location?.latitude && it.longitude == state.location?.longitude
                        val latitude = it.latitude
                        val longitude = it.longitude

                        if (latitude != null && longitude != null) {
                            LocationCard(
                                location = UserLocation(
                                    address = it.getAddress(),
                                    latitude = latitude,
                                    longitude = longitude
                                ),
                                isSelected = isSelected,
                            ) {
                                scope.launch {
                                    onUpdateLocation(
                                        data = it,
                                        onUpdate = onSelect
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun onUpdateLocation(
    data: LocationData,
    onUpdate: (UserLocation) -> Unit
) {
    val latitude = data.latitude
    val longitude = data.longitude

    if (longitude == null || latitude == null) return

    val userLocation = UserLocation(
        latitude = latitude,
        longitude = longitude,
        address = data.getAddress()
    )

    onUpdate(userLocation)
}

@Composable
private fun GetGeolocation(state: LocationScreenState, onUpdate: (UserLocation) -> Unit) {
    val context = LocalContext.current
    val lifecycleState by LocalLifecycleOwner.current.lifecycle.currentStateFlow.collectAsState()
    var neededPermissions by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(Unit, lifecycleState) {
        if (ContextUtil.checkLocationPermission(context)) {
            neededPermissions = false
            val fusedLocationClient =
                LocationServices.getFusedLocationProviderClient(context as Activity)

            fusedLocationClient.lastLocation
                .addOnSuccessListener { location: Location? ->
                    if (location != null) {
                        val address = LocationUtil.getAddressFromLocation(
                            context = context,
                            location = location
                        )

                        val userLocation = UserLocation(
                            latitude = location.latitude,
                            longitude = location.longitude,
                            address = address
                        )

                        onUpdate(userLocation)
                    }
                }
        } else {
            neededPermissions = true
        }
    }


    AnimatedContent(neededPermissions) { permissions ->
        if (permissions) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    modifier = Modifier
                        .padding(horizontal = 20.dp),
                    text = stringResource(R.string.permissions_are_required_for_the_program_to_work),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(.8f),
                    textAlign = TextAlign.Center
                )
                VerticalSpacer(value = 10.dp)
                ExtendedFloatingActionButton(
                    onClick = { ContextUtil.requestLocationPermission(activity = context as Activity) },
                    icon = {
                        Icon(
                            imageVector = Icons.Rounded.Flag,
                            contentDescription = null
                        )
                    },
                    text = { Text(text = stringResource(R.string.grant_permission)) },
                )
            }
        } else {
            AnimatedContent(state.location) {
                if (it == null) {
                    Box(modifier = Modifier.padding(10.dp)) {
                        CircularProgressIndicator()
                    }
                } else {
                    Text(
                        modifier = Modifier
                            .basicMarquee()
                            .padding(horizontal = 20.dp),
                        text = it.address,
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }
        }
    }
}

@Composable
private fun GetCoordinates(
    state: LocationScreenState,
    onUpdate: (UserLocation) -> Unit,
    onReset: () -> Unit
) {
    val context = LocalContext.current

    var latitude by remember { mutableStateOf(if (state.location == null) "" else state.location.latitude.toString()) }
    var longitude by remember { mutableStateOf(if (state.location == null) "" else state.location.longitude.toString()) }
    var latitudeWrong by remember { mutableStateOf(false) }
    var longitudeWrong by remember { mutableStateOf(false) }

    LaunchedEffect(latitude, longitude) {
        latitudeWrong = latitude.isBlank().not() && latitude.toDoubleOrNull() == null
        longitudeWrong = longitude.isBlank().not() && longitude.toDoubleOrNull() == null

        val latitude = latitude.toDoubleOrNull()
        val longitude = longitude.toDoubleOrNull()

        if (latitude == null || longitude == null) {
            onReset()
            return@LaunchedEffect
        }

        val address = LocationUtil.getAddressFromLocation(
            context = context,
            latitude = latitude,
            longitude = longitude
        )

        val userLocation = UserLocation(
            latitude = latitude,
            longitude = longitude,
            address = address
        )

        onUpdate(userLocation)
    }

    Column {
        AnimatedContent(targetState = state.location?.address) {
            if (!it.isNullOrBlank()) {
                Column {
                    Text(
                        modifier = Modifier
                            .basicMarquee()
                            .padding(horizontal = 20.dp),
                        text = it,
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                    VerticalSpacer(value = 10.dp)
                }
            }
        }
        Row(
            modifier = Modifier.padding(horizontal = 20.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            val cornerSize = 14.dp

            OutlinedTextField(
                modifier = Modifier.weight(1f),
                label = { Text(text = stringResource(R.string.latitude)) },
                value = latitude,
                isError = latitudeWrong,
                shape = RoundedCornerShape(topStart = cornerSize, bottomStart = cornerSize),
                onValueChange = { latitude = it })
            HorizontalSpacer(value = 5.dp)
            OutlinedTextField(modifier = Modifier.weight(1f),
                label = { Text(text = stringResource(R.string.longitude)) },
                value = longitude,
                isError = longitudeWrong,
                shape = RoundedCornerShape(topEnd = cornerSize, bottomEnd = cornerSize),
                onValueChange = { longitude = it })
        }
    }
}