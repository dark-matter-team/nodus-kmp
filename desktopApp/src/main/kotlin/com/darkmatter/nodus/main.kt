package com.darkmatter.nodus

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.darkmatter.nodus.core.auth.GoogleDesktopClient
import com.darkmatter.nodus.core.auth.LocalGoogleDesktopClient
import com.darkmatter.nodus.core.config.ApiConfig

fun main() = application {
    // jpackage задає jpackage.app-path лише в запакованому застосунку: тоді прод, з IDE локальний бекенд
    if (System.getProperty("jpackage.app-path") != null) {
        ApiConfig.setBaseUrl(System.getProperty("nodus.api.prodBaseUrl").orEmpty())
    }
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
