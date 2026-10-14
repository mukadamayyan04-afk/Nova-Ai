package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val NovaDarkColorScheme = darkColorScheme(
    primary = NovaCyan,
    onPrimary = Color.Black,
    primaryContainer = NovaSurfaceElevated,
    onPrimaryContainer = NovaCyanBright,
    secondary = NovaElectricBlue,
    onSecondary = Color.White,
    secondaryContainer = NovaSurfaceCard,
    onSecondaryContainer = NovaCyan,
    tertiary = NovaPurple,
    onTertiary = Color.White,
    background = NovaBackground,
    onBackground = NovaTextPrimary,
    surface = NovaSurfaceDark,
    onSurface = NovaTextPrimary,
    surfaceVariant = NovaSurfaceCard,
    onSurfaceVariant = NovaTextSecondary,
    outline = NovaBorder,
    error = NovaRed,
    onError = Color.White
)

@Composable
fun MyApplicationTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = NovaDarkColorScheme,
        typography = Typography,
        content = content
    )
}
