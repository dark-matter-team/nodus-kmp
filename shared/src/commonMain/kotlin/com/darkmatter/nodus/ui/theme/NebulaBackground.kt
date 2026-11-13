package com.darkmatter.nodus.ui.theme

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import nodus.shared.generated.resources.Res
import nodus.shared.generated.resources.nebula_stars
import org.jetbrains.compose.resources.imageResource
import kotlin.math.roundToInt

/** Частка шару зірок поверх градієнта (opacity 15% у макеті). */
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
 * Фон з макета: еліптичний радіальний градієнт, вписаний в екран, і шар зірок поверх нього.
 * Зорі — це шейдер Nebula з Figma, заздалегідь відрендерений у текстуру (див. tools/nebula):
 * рахувати його щокадру занадто дорого. Текстура вписується по висоті й центрується по ширині,
 * як координати оригінального шейдера.
 */
@Composable
fun NebulaBackground(modifier: Modifier = Modifier) {
    val stars = imageResource(Res.drawable.nebula_stars)
    Spacer(
        modifier
            .fillMaxSize()
            .drawWithCache {
                val center = Offset(size.width / 2f, size.height / 2f)
                val radius = size.width / 2f
                val gradient = Brush.radialGradient(
                    colorStops = NebulaGradientStops,
                    center = center,
                    radius = radius,
                )
                val starsHeight = size.height
                val starsWidth = starsHeight * stars.width / stars.height
                val starsOffset = IntOffset(((size.width - starsWidth) / 2f).roundToInt(), 0)
                val starsSize = IntSize(starsWidth.roundToInt(), starsHeight.roundToInt())

                onDrawBehind {
                    // Коло радіусом у пів ширини, розтягнуте по вертикалі до висоти екрана, дає еліпс як у Figma
                    scale(scaleX = 1f, scaleY = size.height / size.width, pivot = center) {
                        drawRect(
                            brush = gradient,
                            topLeft = Offset(0f, center.y - radius),
                            size = Size(size.width, size.width),
                        )
                    }
                    drawImage(image = stars, dstOffset = starsOffset, dstSize = starsSize, alpha = NEBULA_ALPHA)
                }
            },
    )
}
