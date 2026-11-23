package com.darkmatter.nodus

import androidx.compose.runtime.Composable
import com.darkmatter.nodus.ui.navigation.AppNavHost
import com.darkmatter.nodus.ui.theme.NodusTheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

@Composable
fun App() {
    var isDarkTheme by remember { mutableStateOf(true) }

    NodusTheme(darkTheme = isDarkTheme) {
        AppNavHost(
            isDarkTheme = isDarkTheme,
            onToggleTheme = { isDarkTheme = !isDarkTheme },
        )
    }
}