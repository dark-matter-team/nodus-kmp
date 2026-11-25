package com.darkmatter.nodus.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Темна тема основна; світла з'явиться пізніше
private val NodusDarkColorScheme = darkColorScheme(
    primary = NodusColors.Accent,
    onPrimary = NodusColors.StartLabel,
    secondary = NodusColors.Title,
    background = NodusColors.NebulaDeep,
    onBackground = NodusColors.StartLabel,
    surface = NodusColors.NebulaDeep,
    onSurface = NodusColors.StartLabel,
)
// Світла тема (Neon Nebula Light). Кольори ≈ зі скриншотів: звірити з макетом
private val NodusLightColorScheme = lightColorScheme(
    primary = Color(0xFF6B3FA0),
    onPrimary = Color.White,
    secondary = Color(0xFF7B3FA8),
    background = Color(0xFFD39DE0),
    onBackground = Color(0xFF3A1D55),
    surface = Color(0xFFD39DE0),
    onSurface = Color(0xFF3A1D55),
)
@Composable
fun NodusTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) NodusDarkColorScheme else NodusLightColorScheme,
        content = content,
    )
}