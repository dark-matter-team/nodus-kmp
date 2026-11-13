package com.darkmatter.nodus.ui.theme

import android.graphics.RuntimeShader
import android.os.Build
import androidx.compose.ui.graphics.Shader

internal actual fun createRuntimeShader(sksl: String, width: Float, height: Float): Shader? {
    // RuntimeShader з'явився в Android 13; на старіших версіях фон лишається лише градієнтом
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return null
    return RuntimeShader(sksl).apply { setFloatUniform("iResolution", width, height) }
}
