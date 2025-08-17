package com.example.shopease.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColorScheme = lightColorScheme(
    primary = Primary,
    primaryContainer = PrimaryLight,  // Use PrimaryLight for subtle accents
    onPrimary = OnPrimary,
    secondary = Secondary,
    secondaryContainer = SecondaryLight,
    background = Background,
    surface = Surface,
    onSurface = OnSurface,
    error = Error,
)

@Composable
fun ShopEaseTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = LightColorScheme,
        typography = Typography,  // Define fonts if needed
        content = content
    )
}