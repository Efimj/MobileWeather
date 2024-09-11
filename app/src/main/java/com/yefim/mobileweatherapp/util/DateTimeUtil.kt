package com.yefim.mobileweatherapp.util

import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

object DateTimeUtil {
    fun getLocalDateTime() = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
}