package com.yefim.mobileweatherapp.widget.storage

import kotlinx.serialization.Serializable

@Serializable
enum class WeatherWidgetType{
    HORIZONTAL_PILL,
    LARGE_CARD
}

@Serializable
data class WeatherWidget(
    val id: Int = -1,
    val location: String = "",
    val type: WeatherWidgetType = WeatherWidgetType.HORIZONTAL_PILL
)
