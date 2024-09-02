package com.example.mobileweatherapp.util.settings

import androidx.annotation.Keep
import kotlinx.serialization.Serializable

@Keep
enum class NightMode {
    Light,
    Dark,
    System,
}

@Serializable
data class UserLocation(val address: String, val longitude: Double, val latitude: Double)

@Keep
@Serializable
data class SettingsState(
    val checkUpdates: Boolean = true,
    val secureMode: Boolean = false,
    val nightMode: NightMode = NightMode.System,
    val selectedLocation: UserLocation? = null,
    val locations: Set<UserLocation> = emptySet(),
)