package com.darkmatter.nodus

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import com.darkmatter.nodus.ui.navigation.AppNavHost
import com.darkmatter.nodus.ui.theme.NodusTheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import com.darkmatter.nodus.ui.theme.LocalThemeState
import com.darkmatter.nodus.ui.theme.ThemeState

@Composable
fun App() {
    var isDarkTheme by rememberSaveable { mutableStateOf(true) }
    val themeState = ThemeState(
        isDark = isDarkTheme,
        toggle = { isDarkTheme = !isDarkTheme },
    )
    NodusTheme(darkTheme = isDarkTheme) {
        CompositionLocalProvider(LocalThemeState provides themeState) {
            AppNavHost()
        }
    }
}