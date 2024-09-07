package com.example.openmeteoapi.model

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.example.openmeteoapi.R

enum class WeatherCode(
    val code: Int,
    @StringRes description: Int,
    @DrawableRes dayIcon: Int,
    @DrawableRes nightIcon: Int? = null,
) {
    CLEAR_SKY(0, R.string.clear_sky, R.drawable.clear_day, R.drawable.clear_night),
    MAINLY_CLEAR(
        1,
        R.string.mainly_clear,
        R.drawable.mostly_clear_day,
        R.drawable.mostly_clear_night
    ),
    PARTLY_CLOUDY(
        2,
        R.string.partly_cloudy,
        R.drawable.mostly_cloudy_day,
        R.drawable.mostly_cloudy_night
    ),
    OVERCAST(3, R.string.overcast, R.drawable.cloudy),
    FOG(45, R.string.fog, R.drawable.haze_fog_dust_smoke),
    RIME_FOG(48, R.string.depositing_rime_fog, R.drawable.haze_fog_dust_smoke),
    DRIZZLE_LIGHT(
        51,
        R.string.light_drizzle,
        R.drawable.scattered_showers_day,
        R.drawable.scattered_showers_night
    ),
    DRIZZLE_MODERATE(
        53,
        R.string.moderate_drizzle,
        R.drawable.scattered_showers_day,
        R.drawable.scattered_showers_night
    ),
    DRIZZLE_DENSE(
        55,
        R.string.dense_drizzle,
        R.drawable.scattered_showers_day,
        R.drawable.scattered_showers_night
    ),
    FREEZING_DRIZZLE_LIGHT(56, R.string.light_freezing_drizzle, R.drawable.mixed_rain_snow),
    FREEZING_DRIZZLE_DENSE(57, R.string.dense_freezing_drizzle, R.drawable.mixed_rain_snow),
    RAIN_SLIGHT(61, R.string.slight_rain, R.drawable.showers_rain),
    RAIN_MODERATE(63, R.string.moderate_rain, R.drawable.showers_rain),
    RAIN_HEAVY(65, R.string.heavy_rain, R.drawable.heavy_rain),
    FREEZING_RAIN_LIGHT(66, R.string.light_freezing_rain, R.drawable.mixed_rain_snow),
    FREEZING_RAIN_HEAVY(67, R.string.heavy_freezing_rain, R.drawable.mixed_rain_snow),
    SNOW_SLIGHT(
        71,
        R.string.slight_snow,
        R.drawable.scattered_snow_showers_day,
        R.drawable.scattered_snow_showers_night
    ),
    SNOW_MODERATE(73, R.string.moderate_snow, R.drawable.showers_snow),
    SNOW_HEAVY(75, R.string.heavy_snow, R.drawable.heavy_snow),
    SNOW_GRAINS(77, R.string.snow_grains, R.drawable.icy),
    RAIN_SHOWERS_SLIGHT(80, R.string.slight_rain_showers, R.drawable.cloudy_with_rain),
    RAIN_SHOWERS_MODERATE(81, R.string.moderate_rain_showers, R.drawable.showers_rain),
    RAIN_SHOWERS_VIOLENT(82, R.string.violent_rain_showers, R.drawable.heavy_rain),
    SNOW_SHOWERS_SLIGHT(85, R.string.slight_snow_showers, R.drawable.showers_snow),
    SNOW_SHOWERS_HEAVY(86, R.string.heavy_snow_showers, R.drawable.heavy_snow),
    THUNDERSTORM_SLIGHT(
        95,
        R.string.slight_thunderstorm,
        R.drawable.isolated_scattered_thunderstorms_day,
        R.drawable.isolated_scattered_thunderstorms_night
    ),
    THUNDERSTORM_WITH_HAIL_SLIGHT(
        96,
        R.string.thunderstorm_with_slight_hail,
        R.drawable.isolated_thunderstorms
    ),
    THUNDERSTORM_WITH_HAIL_HEAVY(
        99,
        R.string.thunderstorm_with_heavy_hail,
        R.drawable.strong_thunderstorms
    )
}
