package com.darkmatter.nodus

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import com.darkmatter.nodus.ui.navigation.AppNavHost

@Composable
fun App() {
    MaterialTheme {
        AppNavHost()
    }
}