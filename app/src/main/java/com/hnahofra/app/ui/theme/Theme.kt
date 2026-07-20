package com.hnahofra.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val Red = Color(0xFFD32F2F)
private val RedDark = Color(0xFF9A0007)
private val Green = Color(0xFF2E7D32)

private val LightColors = lightColorScheme(
    primary = Red,
    onPrimary = Color.White,
    secondary = Green,
    onSecondary = Color.White,
)

private val DarkColors = darkColorScheme(
    primary = Red,
    onPrimary = Color.White,
    secondary = Green,
    onSecondary = Color.White,
)

@Composable
fun HnaHofraTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = if (isSystemInDarkTheme()) DarkColors else LightColors,
        content = content
    )
}
