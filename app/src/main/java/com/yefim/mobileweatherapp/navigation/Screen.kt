package com.yefim.mobileweatherapp.navigation

import androidx.annotation.Keep
import kotlinx.serialization.Serializable

@Keep
@Serializable
sealed class Screen {
    @Serializable
    data object Weather : Screen()

    @Serializable
    data object Location : Screen()
}