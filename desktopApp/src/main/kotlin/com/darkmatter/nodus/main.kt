package com.darkmatter.nodus

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.darkmatter.nodus.core.auth.GoogleDesktopClient
import com.darkmatter.nodus.core.auth.LocalGoogleDesktopClient
import com.darkmatter.nodus.core.config.ApiConfig

fun main() = application {
    // Адреса бекенда з local.properties (api.baseUrl) через jvmArgs у build.gradle.kts
    ApiConfig.setBaseUrl(System.getProperty("nodus.api.baseUrl").orEmpty())
    // Ключі OAuth-клієнта Google приходять з local.properties через jvmArgs у build.gradle.kts
    val googleClient = GoogleDesktopClient(
        clientId = System.getProperty("nodus.google.desktopClientId").orEmpty(),
        clientSecret = System.getProperty("nodus.google.desktopClientSecret").orEmpty(),
    )
    Window(
        onCloseRequest = ::exitApplication,
        title = "Nodus",
    ) {
        CompositionLocalProvider(LocalGoogleDesktopClient provides googleClient) {
            App()
        }
    }
}
