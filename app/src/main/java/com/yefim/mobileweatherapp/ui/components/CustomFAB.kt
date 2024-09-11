package com.yefim.mobileweatherapp.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector

@Composable
fun CustomFAB(modifier: Modifier, text: String?, icon: ImageVector?, onClick: () -> Unit) {
    ExtendedFloatingActionButton(
        modifier = modifier,
        onClick = onClick,
        icon = {
            AnimatedContent(icon) {
                if (it != null) {
                    Icon(
                        imageVector = it,
                        contentDescription = null
                    )
                }
            }
        },
        text = {
            AnimatedContent(text) {
                if (!it.isNullOrBlank()) {
                    Text(text = it)
                }
            }
        },
    )
}