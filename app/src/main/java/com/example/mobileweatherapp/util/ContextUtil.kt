package com.example.mobileweatherapp.util

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import androidx.core.app.ActivityCompat

/**
 * Utility object for handling context-related operations.
 */
object ContextUtil {

    /**
     * Checks if the location permissions (both coarse and fine) are granted.
     *
     * @param context The context to check permissions for.
     * @return `true` if both ACCESS_COARSE_LOCATION and ACCESS_FINE_LOCATION permissions
     *         are granted, `false` otherwise.
     */
    fun checkLocationPermission(context: Context) = ActivityCompat.checkSelfPermission(
        context,
        Manifest.permission.ACCESS_COARSE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
        context,
        Manifest.permission.ACCESS_FINE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED

    /**
     * Requests location permissions (both coarse and fine) from the user.
     *
     * This method will prompt the user to grant ACCESS_COARSE_LOCATION and
     * ACCESS_FINE_LOCATION permissions.
     *
     * @param activity The activity from which the permission request is made.
     * @param requestCode The request code for identifying the permission request result.
     */
    fun requestLocationPermission(activity: Activity, requestCode: Int = 1) =
        ActivityCompat.requestPermissions(
            activity,
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ),
            requestCode
        )

    /**
     * Checks if there is an active internet connection available.
     *
     * @param context The context to check connectivity for.
     * @return `true` if there is an active internet connection, `false` otherwise.
     */
    fun hasInternetConnection(context: Context): Boolean {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetwork = connectivityManager.activeNetwork ?: return false
        val networkCapabilities =
            connectivityManager.getNetworkCapabilities(activeNetwork) ?: return false
        return networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }
}