package com.darkmatter.nodus.ui.theme

import androidx.compose.ui.graphics.Shader
import androidx.compose.ui.graphics.asComposeShader
import org.jetbrains.skia.RuntimeEffect
import org.jetbrains.skia.RuntimeShaderBuilder

internal actual fun createRuntimeShader(sksl: String, width: Float, height: Float): Shader? {
    val effect = RuntimeEffect.makeForShader(sksl)
    return RuntimeShaderBuilder(effect)
        .apply { uniform("iResolution", width, height) }
        .makeShader()
        .asComposeShader()
}
