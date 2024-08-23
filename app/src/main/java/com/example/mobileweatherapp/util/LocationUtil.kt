package com.example.mobileweatherapp.util

import android.content.Context
import android.location.Address
import android.location.Geocoder
import android.location.Location
import java.util.Locale

/**
 * Utility object for location-related operations.
 */
object LocationUtil {

    /**
     * Retrieves the address information (country, city, and district) from the given location.
     *
     * @param context the application context.
     * @param location the object containing latitude and longitude data.
     * @return a formatted string containing the country, city, and district of the location, or `null`.
     */
    fun getAddressFromLocation(context: Context, location: Location): String? {
        val geocoder = Geocoder(context, Locale.getDefault())
        val addresses: List<Address>?
        var result: String? = null

        try {
            addresses = geocoder.getFromLocation(location.latitude, location.longitude, 1)
            if (!addresses.isNullOrEmpty()) {
                val address = addresses[0]
                val country = address.countryName ?: ""
                val city = address.locality ?: ""
                val district = address.subLocality ?: address.subAdminArea ?: ""
                result = "$country, $city, $district"
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return result
    }
}