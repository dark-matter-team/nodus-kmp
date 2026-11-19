package com.darkmatter.nodus.ui.screens.auth

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.darkmatter.nodus.ui.theme.NebulaBackground
import com.darkmatter.nodus.ui.theme.NodusColors
import com.darkmatter.nodus.ui.theme.iosevkaCharonFontFamily
import com.darkmatter.nodus.ui.theme.khandFontFamily
import nodus.shared.generated.resources.Res
import nodus.shared.generated.resources.auth_corner_reset_compact
import nodus.shared.generated.resources.auth_corner_reset_expanded
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.imageResource
import kotlin.math.min
import kotlin.math.roundToInt
import androidx.compose.ui.graphics.Shadow as TextShadow

/**
 * Розміри екранів відновлення пароля з фреймів Figma: "Forgot Password D Mobile" та інших
 * мобільних (389×874) і "Forgot Password D" та інших десктопних (1512×982).
 */
internal class ResetSpec(
    val isExpanded: Boolean,
    val cardWidth: Dp,
    val cardPadding: Dp,
    val corner: DrawableResource,
    val cornerSize: DpSize,
    val fieldHeight: Dp,
    val fieldFontSize: TextUnit,
    val fieldPaddingStart: Dp,
    val buttonFontSize: TextUnit,
    val titleFontSize: TextUnit,
    val subtitleFontSize: TextUnit,
    val linkFontSize: TextUnit,
    val messageFontSize: TextUnit,
)

private val CompactResetSpec = ResetSpec(
    isExpanded = false,
    cardWidth = 350.dp,
    cardPadding = 13.dp,
    corner = Res.drawable.auth_corner_reset_compact,
    cornerSize = DpSize(17.7791.dp, 11.132.dp),
    fieldHeight = 46.dp,
    fieldFontSize = 14.sp,
    fieldPaddingStart = 16.dp,
    buttonFontSize = 20.sp,
    titleFontSize = 40.sp,
    subtitleFontSize = 12.sp,
    linkFontSize = 14.sp,
    messageFontSize = 14.sp,
)

private val ExpandedResetSpec = ResetSpec(
    isExpanded = true,
    cardWidth = 538.dp,
    cardPadding = 20.dp,
    corner = Res.drawable.auth_corner_reset_expanded,
    cornerSize = DpSize(26.523.dp, 14.63.dp),
    fieldHeight = 77.dp,
    fieldFontSize = 24.sp,
    fieldPaddingStart = 25.dp,
    buttonFontSize = 32.sp,
    titleFontSize = 48.sp,
    subtitleFontSize = 16.sp,
    linkFontSize = 20.sp,
    messageFontSize = 20.sp,
)

private val ResetDesignWidth = 1512.dp
private val ResetDesignHeight = 982.dp

/** Ілюстрація з макета: розмір на телефоні й на десктопі та зсув від центру лівої половини десктопа. */
internal class ResetIllustration(
    val image: DrawableResource,
    val compactSize: DpSize,
    val expandedSize: DpSize,
    val expandedOffsetX: Dp,
)

/**
 * Каркас екранів відновлення: фон, ілюстрація і вміст.
 * На телефоні ілюстрація над вмістом, група по центру екрана з прокруткою.
 * На десктопі дві половини: ілюстрація ліворуч, вміст праворуч; макет масштабується під вікно,
 * як онбординг. [contentOffsetX] зсуває вміст від центру правої половини, як у фреймі.
 */
@Composable
internal fun ResetScreenScaffold(
    illustration: ResetIllustration,
    compactGap: Dp,
    contentOffsetX: Dp,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.(ResetSpec) -> Unit,
) {
    Box(modifier = modifier.fillMaxSize()) {
        NebulaBackground()
        BoxWithConstraints(Modifier.fillMaxSize().safeDrawingPadding()) {
            val isExpanded = maxWidth >= 600.dp && maxWidth > maxHeight
            if (isExpanded) {
                val scale = min(maxWidth / ResetDesignWidth, maxHeight / ResetDesignHeight)
                val density = LocalDensity.current
                CompositionLocalProvider(LocalDensity provides Density(density.density * scale, density.fontScale)) {
                    Row(Modifier.fillMaxSize(), verticalAlignment = Alignment.CenterVertically) {
                        Box(Modifier.weight(1f).fillMaxHeight(), contentAlignment = Alignment.Center) {
                            ScreenIllustration(
                                image = illustration.image,
                                size = illustration.expandedSize,
                                modifier = Modifier.offset(x = illustration.expandedOffsetX),
                            )
                        }
                        Box(Modifier.weight(1f).fillMaxHeight(), contentAlignment = Alignment.Center) {
                            Column(
                                modifier = Modifier.offset(x = contentOffsetX),
                                horizontalAlignment = Alignment.CenterHorizontally,
                            ) {
                                content(ExpandedResetSpec)
                            }
                        }
                    }
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                        .heightIn(min = maxHeight),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    ScreenIllustration(illustration.image, illustration.compactSize)
                    Spacer(Modifier.height(compactGap))
                    content(CompactResetSpec)
                }
            }
        }
    }
}

/**
 * Ілюстрації в макеті на чорному тлі з режимом накладання screen: чорне зникає, лишається світіння.
 * Малюються без окремого шару, щоб змішуватися з фоном екрана.
 */
@Composable
private fun ScreenIllustration(image: DrawableResource, size: DpSize, modifier: Modifier = Modifier) {
    val bitmap = imageResource(image)
    Canvas(modifier.size(size)) {
        drawImage(
            image = bitmap,
            dstOffset = IntOffset.Zero,
            dstSize = IntSize(this.size.width.roundToInt(), this.size.height.roundToInt()),
            blendMode = BlendMode.Screen,
            filterQuality = FilterQuality.High,
        )
    }
}

/** Картка відновлення: та сама "туманність", що й у входу, з меншими кутовими дужками. */
@Composable
internal fun ResetCard(spec: ResetSpec, content: @Composable ColumnScope.() -> Unit) {
    NebulaCard(
        width = spec.cardWidth,
        horizontalPadding = spec.cardPadding,
        corner = spec.corner,
        cornerSize = spec.cornerSize,
        content = content,
    )
}

/**
 * Заголовок і підзаголовок картки в блоці висотою [height]: базові лінії на [titleBaseline]
 * і [subtitleBaseline] від верху блоку, як у макеті.
 */
@Composable
internal fun ResetCardHeader(
    spec: ResetSpec,
    title: String,
    subtitle: String,
    height: Dp,
    titleBaseline: Dp,
    subtitleBaseline: Dp,
) {
    Box(Modifier.fillMaxWidth().height(height), contentAlignment = Alignment.TopCenter) {
        BasicText(
            text = title,
            style = resetTextStyle(iosevkaCharonFontFamily(), spec.titleFontSize, NodusColors.Title, FontWeight.Bold, shadowOffsetY = 1.dp),
            maxLines = 1,
            modifier = Modifier.baselineSlot(height, titleBaseline),
        )
        BasicText(
            text = subtitle,
            style = resetTextStyle(khandFontFamily(), spec.subtitleFontSize, NodusColors.Description, shadowOffsetY = 2.dp),
            modifier = Modifier.baselineSlot(height, subtitleBaseline),
        )
    }
}

/** Блок фіксованої висоти, де текст стоїть базовою лінією на [baseline] від верху. */
@Composable
internal fun BaselineTextSlot(
    text: String,
    style: TextStyle,
    height: Dp,
    baseline: Dp,
    modifier: Modifier = Modifier,
) {
    Box(modifier.fillMaxWidth().height(height), contentAlignment = Alignment.TopCenter) {
        BasicText(text = text, style = style, modifier = Modifier.baselineSlot(height, baseline))
    }
}

/** Посилання по центру блоку фіксованої висоти ("Back to Sign In"). */
@Composable
internal fun LinkSlot(
    text: String,
    spec: ResetSpec,
    height: Dp,
    baseline: Dp,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    Box(Modifier.fillMaxWidth().height(height), contentAlignment = Alignment.TopCenter) {
        AuthLink(
            text = text,
            fontSize = spec.linkFontSize,
            enabled = enabled,
            onClick = onClick,
            modifier = Modifier.baselineSlot(height, baseline),
        )
    }
}

/** Стиль текстів відновлення: природна висота рядка шрифту (як "Auto" у Figma) і тінь з макета. */
@Composable
internal fun resetTextStyle(
    fontFamily: FontFamily,
    fontSize: TextUnit,
    color: Color,
    fontWeight: FontWeight = FontWeight.Normal,
    shadowOffsetY: Dp = 4.dp,
): TextStyle {
    val density = LocalDensity.current
    return TextStyle(
        fontFamily = fontFamily,
        fontWeight = fontWeight,
        fontSize = fontSize,
        color = color,
        textAlign = TextAlign.Center,
        shadow = with(density) {
            TextShadow(color = NodusColors.Shadow, offset = Offset(0f, shadowOffsetY.toPx()), blurRadius = 4.dp.toPx())
        },
    )
}

/** Підказка під полями нового пароля: "✓ At least 8 characters" або текст помилки на її місці. */
@Composable
internal fun PasswordHintRow(
    spec: ResetSpec,
    isRequirementMet: Boolean,
    error: String?,
    height: Dp,
    baseline: Dp,
) {
    val khand = khandFontFamily()
    val checkX = if (spec.isExpanded) 6.dp else 3.dp
    val textX = if (spec.isExpanded) 25.dp else 16.dp
    Box(Modifier.fillMaxWidth().height(height)) {
        if (error != null) {
            BasicText(
                text = error,
                style = TextStyle(fontFamily = khand, fontSize = spec.fieldFontSize, color = NodusColors.ErrorText),
                maxLines = 1,
                modifier = Modifier.offset(x = textX).width(spec.cardWidth - spec.cardPadding * 2 - textX)
                    .baselineSlot(height, baseline),
            )
        } else {
            // Галочка світиться, коли пароль уже достатньо довгий
            val color = if (isRequirementMet) NodusColors.Hint else NodusColors.FieldPlaceholder
            val style = TextStyle(fontFamily = khand, fontSize = spec.fieldFontSize, color = color)
            BasicText(text = "✓", style = style, modifier = Modifier.offset(x = checkX).baselineSlot(height, baseline))
            BasicText(
                text = "At least ${AuthValidation.PASSWORD_MIN_LENGTH} characters",
                style = style,
                modifier = Modifier.offset(x = textX).baselineSlot(height, baseline),
            )
        }
    }
}
