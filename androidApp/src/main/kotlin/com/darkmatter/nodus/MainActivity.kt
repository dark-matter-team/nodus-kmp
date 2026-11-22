package com.darkmatter.nodus

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.tooling.preview.Preview
import com.darkmatter.nodus.core.auth.LocalGoogleWebClientId
import com.darkmatter.nodus.core.config.ApiConfig

class MainActivity : ComponentActivity() {

    // Результат не обробляємо: після дозволу запити до локального бекенда просто почнуть проходити
    private val localNetworkPermissionRequest =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) {}

    override fun onCreate(savedInstanceState: Bundle?) {
        // Тема застосунку темна незалежно від системної, тому іконки системних панелей завжди світлі
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(Color.TRANSPARENT),
        )
        super.onCreate(savedInstanceState)

        // Адреса бекенда з типу збірки (build.gradle.kts): debug локальна, release прод
        ApiConfig.setBaseUrl(BuildConfig.API_BASE_URL)
        requestLocalNetworkAccessForDebug()

        setContent {
            CompositionLocalProvider(LocalGoogleWebClientId provides BuildConfig.GOOGLE_WEB_CLIENT_ID) {
                App()
            }
        }
    }

    /**
     * Debug-збірка ходить на локальний бекенд (10.0.2.2), а з Android 17 це приватна мережа,
     * доступ до якої потребує ACCESS_LOCAL_NETWORK. Дозвіл оголошено лише в src/debug.
     */
    private fun requestLocalNetworkAccessForDebug() {
        if (!BuildConfig.DEBUG || Build.VERSION.SDK_INT < ANDROID_17_API_LEVEL) return
        val permission = Manifest.permission.ACCESS_LOCAL_NETWORK
        if (checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED) {
            localNetworkPermissionRequest.launch(permission)
        }
    }

    private companion object {
        const val ANDROID_17_API_LEVEL = 37
    }
}

@Preview
@Composable
fun AppAndroidPreview() {
    App()
}