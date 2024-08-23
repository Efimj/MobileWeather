package com.example.mobileweatherapp.ui.components

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp

@Composable
fun VerticalSpacer(modifier: Modifier = Modifier, value: Dp) {
    Spacer(modifier = modifier.height(value))
}

@Composable
fun HorizontalSpacer(modifier: Modifier = Modifier, value: Dp) {
    Spacer(modifier = modifier.width(value))
}