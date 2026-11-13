package com.darkmatter.nodus.ui.theme

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.ShaderBrush
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.graphicsLayer

/** Частка шейдера зірок поверх градієнта (opacity 15% у макеті). */
private const val NEBULA_ALPHA = 0.15f

private val NebulaGradientStops = arrayOf(
    0.043269f to NodusColors.NebulaCore,
    0.28365f to NodusColors.NebulaInner,
    0.57212f to NodusColors.NebulaMiddle,
    0.78606f to NodusColors.NebulaOuter,
    0.89303f to NodusColors.NebulaEdge,
    1f to NodusColors.NebulaDeep,
)

/**
 * Фон з макета: еліптичний радіальний градієнт, вписаний в екран, і шейдер Nebula поверх нього.
 * Шейдер важкий, але статичний, тому фон малюється в окремий шар (Offscreen):
 * на Android він кешується як текстура і не перераховується, поки не зміниться розмір.
 */
@Composable
fun NebulaBackground(modifier: Modifier = Modifier) {
    Spacer(
        modifier
            .fillMaxSize()
            .graphicsLayer { compositingStrategy = CompositingStrategy.Offscreen }
            .drawWithCache {
                val center = Offset(size.width / 2f, size.height / 2f)
                val radius = size.width / 2f
                val gradient = Brush.radialGradient(
                    colorStops = NebulaGradientStops,
                    center = center,
                    radius = radius,
                )
                val nebula = createRuntimeShader(NEBULA_SKSL, size.width, size.height)?.let(::ShaderBrush)

                onDrawBehind {
                    // Коло радіусом у пів ширини, розтягнуте по вертикалі до висоти екрана, дає еліпс як у Figma
                    scale(scaleX = 1f, scaleY = size.height / size.width, pivot = center) {
                        drawRect(
                            brush = gradient,
                            topLeft = Offset(0f, center.y - radius),
                            size = Size(size.width, size.width),
                        )
                    }
                    if (nebula != null) drawRect(brush = nebula, alpha = NEBULA_ALPHA)
                }
            },
    )
}
