package com.darkmatter.nodus

import androidx.compose.ui.window.ComposeUIViewController
import com.darkmatter.nodus.core.config.ApiConfig
import platform.Foundation.NSBundle
import platform.UIKit.UIViewController

fun MainViewController(): UIViewController {
    // Адреса бекенда з Info.plist (API_BASE_URL у Config.xcconfig або Secrets.xcconfig)
    val baseUrl = NSBundle.mainBundle.objectForInfoDictionaryKey("NodusApiBaseUrl") as? String
    ApiConfig.setBaseUrl(baseUrl.orEmpty())
    return ComposeUIViewController { App() }
}
