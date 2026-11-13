package com.darkmatter.nodus.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

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

@Composable
fun NodusTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = NodusDarkColorScheme, content = content)
}
