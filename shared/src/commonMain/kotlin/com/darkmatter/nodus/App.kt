package com.darkmatter.nodus

import androidx.compose.runtime.Composable
import com.darkmatter.nodus.ui.navigation.AppNavHost
import com.darkmatter.nodus.ui.theme.NodusTheme

@Composable
fun App() {
    NodusTheme {
        AppNavHost()
    }
}
