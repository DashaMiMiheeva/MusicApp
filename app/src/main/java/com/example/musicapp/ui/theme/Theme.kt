package com.example.musicapp.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF44D7DD),
    onPrimary = Color.Black,
    background = Color.Black,
    onBackground = Color.White,
    surface = Color.Gray,
    onSurface = Color.White,
    onError = Color.Red,
    secondary = Color.LightGray,
    scrim = Color(0xFF1F1F1F)
)

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF44D7DD),
    onPrimary = Color.White,
    background = Color.White,
    onBackground = Color.Black,
    surface = Color.Gray,
    onSurface = Color.Black,
    onError = Color.Red,
    secondary = Color.LightGray,
    scrim = Color(0xFFCFC8C8)
)


@Composable
fun MusicAppTheme(darkTheme: Boolean, content: @Composable () -> Unit) {
    val colors = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colors,
        typography = Typography,
        content = content
    )
}