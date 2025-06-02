package com.plcoding.bluetoothchat.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.MaterialTheme
import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorPalette = darkColors(
    primary = Blue200,
    primaryVariant = Blue700,
    secondary = LightBlue200,
    background = SurfaceDark,
    surface = SurfaceDark,
    onPrimary = BlueGrey900,
    onSecondary = BlueGrey900,
    onBackground = OnSurfaceDark,
    onSurface = OnSurfaceDark
)

private val LightColorPalette = lightColors(
    primary = Blue500,
    primaryVariant = Blue700,
    secondary = LightBlue200,
    background = SurfaceLight,
    surface = SurfaceLight,
    onPrimary = Color.White,
    onSecondary = BlueGrey900,
    onBackground = OnSurfaceLight,
    onSurface = OnSurfaceLight
)

@Composable
fun BluetoothChatTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colors = if (darkTheme) {
        DarkColorPalette
    } else {
        LightColorPalette
    }

    MaterialTheme(
        colors = colors,
        typography = Typography,
        shapes = Shapes,
        content = content
    )
}