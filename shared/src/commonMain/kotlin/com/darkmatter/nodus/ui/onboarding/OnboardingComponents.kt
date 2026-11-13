package com.darkmatter.nodus.ui.onboarding

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.ClipOp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.addOutline
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.shadow.DropShadowPainter
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import com.darkmatter.nodus.ui.theme.NodusColors
import com.darkmatter.nodus.ui.theme.khandFontFamily
import nodus.shared.generated.resources.Res
import nodus.shared.generated.resources.ic_theme_toggle
import nodus.shared.generated.resources.onboarding_dot_active
import nodus.shared.generated.resources.onboarding_dot_inactive
import nodus.shared.generated.resources.onboarding_logo
import org.jetbrains.compose.resources.imageResource
import org.jetbrains.compose.resources.painterResource
import kotlin.math.roundToInt
import androidx.compose.ui.graphics.Shadow as TextShadow
import androidx.compose.ui.graphics.shadow.Shadow as BoxShadow

/** Розміри, що відрізняються між мобільним і десктопним макетом. */
internal data class OnboardingSizes(
    val stepFontSize: TextUnit,
    val titleFontSize: TextUnit,
    val descriptionFontSize: TextUnit,
    val buttonFontSize: TextUnit,
    val buttonSize: DpSize,
    val buttonsStartPadding: Dp,
    val buttonsEndPadding: Dp,
)

private val ButtonShape = RoundedCornerShape(10.dp)
private val DotSize = 20.dp
private val DotSpacing = 29.dp

/**
 * Текстовий стиль з макета. Висота рядка дорівнює розміру шрифту, як у текстових блоках Figma,
 * тому відступи між рядками можна брати прямо з координат макета.
 */
@Composable
internal fun onboardingTextStyle(
    fontFamily: FontFamily,
    fontSize: TextUnit,
    color: Color,
    shadowOffsetY: Dp,
    shadowColor: Color = NodusColors.Shadow,
    shadowBlur: Dp = 4.dp,
): TextStyle {
    val density = LocalDensity.current
    return TextStyle(
        fontFamily = fontFamily,
        fontSize = fontSize,
        lineHeight = fontSize,
        lineHeightStyle = LineHeightStyle(LineHeightStyle.Alignment.Center, LineHeightStyle.Trim.None),
        color = color,
        textAlign = TextAlign.Center,
        shadow = with(density) {
            TextShadow(color = shadowColor, offset = Offset(0f, shadowOffsetY.toPx()), blurRadius = shadowBlur.toPx())
        },
    )
}

/**
 * Логотип як шар "image 2" у макеті: рамка 68×67, у ній зображення 181.69% × 103.6%
 * зі зсувом -40.91% / -3.66%, обрізане по рамці й накладене на фон у режимі lighten.
 */
@Composable
internal fun OnboardingLogo(modifier: Modifier = Modifier) {
    val logo = imageResource(Res.drawable.onboarding_logo)
    Canvas(modifier.size(68.dp, 67.dp)) {
        // Обрізаємо через clipRect, а не clipToBounds: окремий шар зламав би змішування з фоном
        clipRect {
            drawImage(
                image = logo,
                dstOffset = IntOffset((-0.4091f * size.width).roundToInt(), (-0.0366f * size.height).roundToInt()),
                dstSize = IntSize((1.8169f * size.width).roundToInt(), (1.036f * size.height).roundToInt()),
                blendMode = BlendMode.Lighten,
            )
        }
    }
}

/**
 * Іконка перемикання теми. 40×40 враховує тінь навколо іконки 32×32.
 * Поки декоративна: світлої теми ще нема.
 */
@Composable
internal fun ThemeToggleIcon(modifier: Modifier = Modifier) {
    Image(
        painter = painterResource(Res.drawable.ic_theme_toggle),
        contentDescription = null,
        modifier = modifier.size(40.dp),
    )
}

@Composable
internal fun PageDots(count: Int, current: Int, modifier: Modifier = Modifier) {
    Row(modifier = modifier, horizontalArrangement = Arrangement.spacedBy(DotSpacing)) {
        repeat(count) { index ->
            Image(
                painter = painterResource(
                    if (index == current) Res.drawable.onboarding_dot_active else Res.drawable.onboarding_dot_inactive,
                ),
                contentDescription = null,
                modifier = Modifier.size(DotSize),
            )
        }
    }
}

@Composable
internal fun SkipButton(sizes: OnboardingSizes, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(sizes.buttonSize)
            .outerShadow(ButtonShape, BoxShadow(radius = 4.dp, color = NodusColors.Shadow, offset = DpOffset(0.dp, 2.dp)))
            .border(1.dp, NodusColors.Outline, ButtonShape)
            .clip(ButtonShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        BasicText(
            text = "SKIP",
            style = onboardingTextStyle(
                fontFamily = khandFontFamily(),
                fontSize = sizes.buttonFontSize,
                color = NodusColors.Outline,
                shadowOffsetY = 2.dp,
            ),
        )
    }
}

/**
 * NEXT, а на останньому слайді START. [lightFill] вмикає світлу заливку START з мобільного макета;
 * на десктопі START має ту саму темну заливку, що й NEXT.
 */
@Composable
internal fun NextButton(
    isLastPage: Boolean,
    lightFill: Boolean,
    sizes: OnboardingSizes,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val useLightFill = isLastPage && lightFill
    val fillTop = if (useLightFill) NodusColors.StartFillTop else NodusColors.NextFillTop
    val fillBottom = if (useLightFill) NodusColors.StartFillBottom else NodusColors.NextFillBottom

    Box(
        modifier = modifier
            .size(sizes.buttonSize)
            .outerShadow(ButtonShape, BoxShadow(radius = 4.dp, color = NodusColors.Shadow, offset = DpOffset(0.dp, 4.dp)))
            // Два напівпрозорі шари в режимі soft-light, як у Figma; малюються без окремого шару, щоб змішуватись з фоном
            .drawBehind {
                val corner = CornerRadius(10.dp.toPx())
                drawRoundRect(color = fillBottom, cornerRadius = corner, blendMode = BlendMode.Softlight)
                drawRoundRect(color = fillTop, cornerRadius = corner, blendMode = BlendMode.Softlight)
            }
            .clip(ButtonShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        BasicText(
            text = if (isLastPage) "START" else "NEXT",
            style = onboardingTextStyle(
                fontFamily = khandFontFamily(),
                fontSize = sizes.buttonFontSize,
                color = if (isLastPage) NodusColors.StartLabel else NodusColors.NextLabel,
                shadowOffsetY = 4.dp,
                shadowColor = if (isLastPage) NodusColors.StartLabelShadow else NodusColors.Shadow,
            ),
        )
    }
}

/**
 * Тінь лише ззовні фігури, як CSS box-shadow у макеті. Звичайний dropShadow затемнив би
 * й прозору середину кнопки SKIP.
 */
private fun Modifier.outerShadow(shape: Shape, shadow: BoxShadow): Modifier = drawWithCache {
    val painter = DropShadowPainter(shape, shadow)
    val outline = Path().apply { addOutline(shape.createOutline(size, layoutDirection, this@drawWithCache)) }
    onDrawBehind {
        clipPath(outline, clipOp = ClipOp.Difference) {
            with(painter) { draw(size) }
        }
    }
}
