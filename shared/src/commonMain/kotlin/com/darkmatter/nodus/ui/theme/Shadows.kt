package com.darkmatter.nodus.ui.theme

import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.ClipOp
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.addOutline
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.shadow.DropShadowPainter
import androidx.compose.ui.graphics.shadow.Shadow

/**
 * Тінь лише ззовні фігури, як CSS box-shadow у макетах Figma. Звичайний dropShadow затемнив би
 * й напівпрозору середину кнопок і полів.
 */
fun Modifier.outerShadow(shape: Shape, shadow: Shadow): Modifier = drawWithCache {
    val painter = DropShadowPainter(shape, shadow)
    val outline = Path().apply { addOutline(shape.createOutline(size, layoutDirection, this@drawWithCache)) }
    onDrawBehind {
        clipPath(outline, clipOp = ClipOp.Difference) {
            with(painter) { draw(size) }
        }
    }
}
