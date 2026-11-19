package com.darkmatter.nodus.ui.onboarding

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
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import com.darkmatter.nodus.ui.theme.NodusColors
import com.darkmatter.nodus.ui.theme.khandFontFamily
import com.darkmatter.nodus.ui.theme.outerShadow
import nodus.shared.generated.resources.Res
import nodus.shared.generated.resources.onboarding_dot_active
import nodus.shared.generated.resources.onboarding_dot_inactive
import org.jetbrains.compose.resources.painterResource
import androidx.compose.ui.graphics.Shadow as TextShadow
import androidx.compose.ui.graphics.shadow.Shadow as BoxShadow

/** Розміри, що відрізняються між мобільним і десктопним макетом. */
internal data class OnboardingSizes(
    val stepFontSize: TextUnit,
    val titleFontSize: TextUnit,
    val descriptionFontSize: TextUnit,
    val buttonFontSize: TextUnit,
    val buttonSize: DpSize,
    val buttonsHorizontalPadding: Dp,
    val dotSize: Dp,
    val dotSpacing: Dp,
)

private val ButtonShape = RoundedCornerShape(10.dp)

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
    fontWeight: FontWeight = FontWeight.Normal,
    textAlign: TextAlign = TextAlign.Center,
): TextStyle {
    val density = LocalDensity.current
    return TextStyle(
        fontFamily = fontFamily,
        fontWeight = fontWeight,
        fontSize = fontSize,
        lineHeight = fontSize,
        lineHeightStyle = LineHeightStyle(LineHeightStyle.Alignment.Center, LineHeightStyle.Trim.None),
        color = color,
        textAlign = textAlign,
        shadow = with(density) {
            TextShadow(color = shadowColor, offset = Offset(0f, shadowOffsetY.toPx()), blurRadius = shadowBlur.toPx())
        },
    )
}

@Composable
internal fun PageDots(count: Int, current: Int, sizes: OnboardingSizes, modifier: Modifier = Modifier) {
    Row(modifier = modifier, horizontalArrangement = Arrangement.spacedBy(sizes.dotSpacing)) {
        repeat(count) { index ->
            Image(
                painter = painterResource(
                    if (index == current) Res.drawable.onboarding_dot_active else Res.drawable.onboarding_dot_inactive,
                ),
                contentDescription = null,
                modifier = Modifier.size(sizes.dotSize),
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

/** NEXT, а на останньому слайді START: та сама заливка, інший колір підпису. */
@Composable
internal fun NextButton(
    isLastPage: Boolean,
    sizes: OnboardingSizes,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .size(sizes.buttonSize)
            .outerShadow(ButtonShape, BoxShadow(radius = 4.dp, color = NodusColors.Shadow, offset = DpOffset(0.dp, 4.dp)))
            // Два напівпрозорі шари в режимі soft-light, як у Figma; малюються без окремого шару, щоб змішуватись з фоном
            .drawBehind {
                val corner = CornerRadius(10.dp.toPx())
                drawRoundRect(color = NodusColors.NextFillBottom, cornerRadius = corner, blendMode = BlendMode.Softlight)
                drawRoundRect(color = NodusColors.NextFillTop, cornerRadius = corner, blendMode = BlendMode.Softlight)
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
