package com.example.mobileweatherapp.screen.location

import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import com.example.mobileweatherapp.R
import com.example.mobileweatherapp.util.settings.UserLocation
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

enum class LocationByType(@StringRes val title: Int) {
    Geolocation(R.string.geolocation),
    Coordinates(R.string.coordinates),
}

data class LocationScreenState(
    val type: LocationByType = LocationByType.Geolocation,
    val location: UserLocation? = null,
)

class LocationViewModel : ViewModel() {
    private val _locationScreenState = MutableStateFlow(LocationScreenState())
    val locationScreenState: StateFlow<LocationScreenState> = _locationScreenState

    fun updateLocationByType(type: LocationByType) {
        _locationScreenState.value = locationScreenState.value.copy(type = type)
    }

    fun updateLocation(userLocation: UserLocation) {
        _locationScreenState.value = locationScreenState.value.copy(location = userLocation)
    }

    fun resetLocation() {
        _locationScreenState.value = locationScreenState.value.copy(location = null)
    }
}