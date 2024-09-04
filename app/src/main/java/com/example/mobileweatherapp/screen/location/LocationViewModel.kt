package com.example.mobileweatherapp.screen.location

import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mobileweatherapp.R
import com.example.mobileweatherapp.util.settings.UserLocation
import com.example.openmeteoapi.di.OpenMeteoInstance
import com.example.openmeteoapi.model.LocationData
import com.example.openmeteoapi.model.LocationResponse
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

enum class LocationByType(@StringRes val title: Int) {
    Geolocation(R.string.geolocation),
    Address(R.string.address),
    Coordinates(R.string.coordinates),
}

data class LocationScreenState(
    val type: LocationByType = LocationByType.Geolocation,
    val location: UserLocation? = null,
    val addressString: String = "",
    val addressLocationResponse: LocationResponse = LocationResponse(result = null)
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

    fun updateAddressLocation(address: String) {
        _locationScreenState.value = locationScreenState.value.copy(addressString = address)

        viewModelScope.launch {
            val response = OpenMeteoInstance.getGeocodingService().getLocation(
                name = address
            )

            _locationScreenState.value =
                locationScreenState.value.copy(addressLocationResponse = response)
        }
    }
}