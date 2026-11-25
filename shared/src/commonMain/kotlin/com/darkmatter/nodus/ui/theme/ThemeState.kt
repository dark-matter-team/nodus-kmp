package com.darkmatter.nodus.ui.theme

import androidx.compose.runtime.compositionLocalOf

/** Стан теми, який App() роздає всім екранам через CompositionLocal. */
class ThemeState(
    val isDark: Boolean,
    val toggle: () -> Unit,
)

val LocalThemeState = compositionLocalOf<ThemeState> {
    error("ThemeState не надано: екран має відображатися всередині App()")
}
