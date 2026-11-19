package com.darkmatter.nodus.ui.screens.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.AlignmentLine
import androidx.compose.ui.layout.FirstBaseline
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.darkmatter.nodus.ui.onboarding.onboardingTextStyle
import com.darkmatter.nodus.ui.theme.NebulaBackground
import com.darkmatter.nodus.ui.theme.NodusColors
import com.darkmatter.nodus.ui.theme.iosevkaCharonFontFamily
import com.darkmatter.nodus.ui.theme.khandFontFamily
import com.darkmatter.nodus.ui.theme.nebulaFill
import com.darkmatter.nodus.ui.theme.outerShadow
import nodus.shared.generated.resources.Res
import nodus.shared.generated.resources.auth_corner_compact
import nodus.shared.generated.resources.auth_corner_expanded
import nodus.shared.generated.resources.ic_eye
import nodus.shared.generated.resources.ic_eye_off
import nodus.shared.generated.resources.ic_google
import nodus.shared.generated.resources.nebula_stars
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.imageResource
import org.jetbrains.compose.resources.painterResource
import kotlin.math.max
import kotlin.math.min
import androidx.compose.ui.graphics.shadow.Shadow as BoxShadow

/**
 * Розміри з фреймів "IOS Sign In/Up Dark" (389×874) і "MacOS Welcome Dark" (1512×982).
 * Вертикальні відступи однакові, відрізняються ширина картки й деталі кнопки Google.
 */
internal class AuthSpec(
    val isExpanded: Boolean,
    val cardWidth: Dp,
    val fieldPadding: Dp,
    val corner: DrawableResource,
    val cornerSize: DpSize,
    val dividerLineWidth: Dp,
    val googleLogoSize: DpSize,
)

private val CompactSpec = AuthSpec(
    isExpanded = false,
    cardWidth = 289.dp,
    fieldPadding = 13.dp,
    corner = Res.drawable.auth_corner_compact,
    cornerSize = DpSize(14.9179.dp, 19.3464.dp),
    dividerLineWidth = 98.dp,
    googleLogoSize = DpSize(56.dp, 28.dp),
)

private val ExpandedSpec = AuthSpec(
    isExpanded = true,
    cardWidth = 765.dp,
    fieldPadding = 34.dp,
    corner = Res.drawable.auth_corner_expanded,
    cornerSize = DpSize(37.0179.dp, 19.3464.dp),
    dividerLineWidth = 260.dp,
    googleLogoSize = DpSize(46.dp, 23.dp),
)

private val ExpandedDesignWidth = 1512.dp
private val ExpandedDesignHeight = 982.dp

// Картка 765 dp і найвища форма (реєстрація) разом з полями навколо: у малому вікні масштаб
// рахується від них, щоб картка не дрібнішала разом з усім фреймом 1512×982
private val CardFitWidth = 900.dp
private val CardFitHeight = 720.dp

private val CardShape = RoundedCornerShape(8.dp)
private val ControlShape = RoundedCornerShape(10.dp)
private val ControlHeight = 46.dp
private val ControlCorner = 10.dp

/** Висота рядка помилки на екранах входу й реєстрації: на стільки картка вища за макет. */
internal val AuthMessageHeight = 22.dp

/**
 * Каркас екрана входу чи реєстрації: фон, перемикач теми і картка з кутовими дужками по центру.
 * Десктопний макет, як і онбординг, масштабується цілком під розмір вікна.
 */
@Composable
internal fun AuthCardScreen(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.(AuthSpec) -> Unit,
) {
    Box(modifier = modifier.fillMaxSize()) {
        NebulaBackground()
        BoxWithConstraints(Modifier.fillMaxSize().safeDrawingPadding()) {
            val isExpanded = maxWidth >= 600.dp && maxWidth > maxHeight
            val density = LocalDensity.current
            val screenDensity = if (isExpanded) {
                val frameScale = min(maxWidth / ExpandedDesignWidth, maxHeight / ExpandedDesignHeight)
                // Картка не менша, ніж дозволяє вікно, і не більша за макетну, поки вікно менше фрейму
                val cardScale = minOf(1f, maxWidth / CardFitWidth, maxHeight / CardFitHeight)
                Density(density.density * max(frameScale, cardScale), density.fontScale)
            } else {
                density
            }
            CompositionLocalProvider(LocalDensity provides screenDensity) {
                AuthCardScreenContent(
                    spec = if (isExpanded) ExpandedSpec else CompactSpec,
                    content = content,
                )
            }
        }
    }
}

@Composable
private fun AuthCardScreenContent(
    spec: AuthSpec,
    content: @Composable ColumnScope.(AuthSpec) -> Unit,
) {
    BoxWithConstraints(Modifier.fillMaxSize()) {
        // Картка по центру; якщо не вміщається (клавіатура, низьке вікно), екран прокручується
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .heightIn(min = maxHeight),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            AuthCard(spec = spec, content = content)
        }
    }
}

/** Картка: та сама "туманність", що й фон, заокруглення 8 dp і білі кутові дужки з макета. */
@Composable
private fun AuthCard(
    spec: AuthSpec,
    content: @Composable ColumnScope.(AuthSpec) -> Unit,
) {
    NebulaCard(
        width = spec.cardWidth,
        horizontalPadding = spec.fieldPadding,
        corner = spec.corner,
        cornerSize = spec.cornerSize,
    ) {
        content(spec)
    }
}

/** Спільна основа карток авторизації й відновлення пароля. */
@Composable
internal fun NebulaCard(
    width: Dp,
    horizontalPadding: Dp,
    corner: DrawableResource,
    cornerSize: DpSize,
    content: @Composable ColumnScope.() -> Unit,
) {
    val stars = imageResource(Res.drawable.nebula_stars)
    Box(Modifier.width(width).nebulaFill(stars, CardShape)) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(horizontal = horizontalPadding),
            horizontalAlignment = Alignment.CenterHorizontally,
            content = content,
        )
        CardCorners(corner, cornerSize, Modifier.matchParentSize())
    }
}

@Composable
private fun CardCorners(cornerResource: DrawableResource, cornerSize: DpSize, modifier: Modifier = Modifier) {
    val corner = painterResource(cornerResource)
    Box(modifier) {
        // Одна й та сама дужка, віддзеркалена для кожного кута
        listOf(
            Alignment.TopStart to (1f to 1f),
            Alignment.TopEnd to (-1f to 1f),
            Alignment.BottomStart to (1f to -1f),
            Alignment.BottomEnd to (-1f to -1f),
        ).forEach { (alignment, flip) ->
            Image(
                painter = corner,
                contentDescription = null,
                modifier = Modifier
                    .align(alignment)
                    .size(cornerSize)
                    .graphicsLayer {
                        scaleX = flip.first
                        scaleY = flip.second
                    },
            )
        }
    }
}

@Composable
internal fun AuthTitle(text: String) {
    BasicText(
        text = text,
        style = TextStyle(
            fontFamily = iosevkaCharonFontFamily(),
            fontWeight = FontWeight.Medium,
            fontSize = 48.sp,
            lineHeight = 48.sp,
            color = NodusColors.AuthTitle,
            textAlign = TextAlign.Center,
        ),
        maxLines = 1,
    )
}

/**
 * Поле з напівпрозорою заливкою soft-light і підписом-заглушкою зліва, як у макеті.
 * Розміри за замовчуванням з екранів входу й реєстрації; екрани відновлення пароля передають свої.
 * Рамка: у фокусі світла, з помилкою червона (як клітинки коду), а [alwaysBordered] малює
 * звичайну темну рамку з макетів відновлення пароля.
 */
@Composable
internal fun AuthField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    isError: Boolean,
    enabled: Boolean,
    keyboardType: KeyboardType = KeyboardType.Text,
    imeAction: ImeAction = ImeAction.Next,
    isPassword: Boolean = false,
    height: Dp = ControlHeight,
    fontSize: TextUnit = 14.sp,
    contentPaddingStart: Dp = 6.dp,
    alwaysBordered: Boolean = false,
) {
    var passwordVisible by remember { mutableStateOf(false) }
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()
    val khand = khandFontFamily()
    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        enabled = enabled,
        singleLine = true,
        textStyle = TextStyle(fontFamily = khand, fontSize = fontSize, color = NodusColors.FieldText),
        cursorBrush = SolidColor(NodusColors.GoogleLabel),
        visualTransformation = if (isPassword && !passwordVisible) PasswordVisualTransformation() else VisualTransformation.None,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType, imeAction = imeAction),
        interactionSource = interactionSource,
        modifier = Modifier.fillMaxWidth(),
        decorationBox = { innerTextField ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(height)
                    .outerShadow(ControlShape, BoxShadow(radius = 4.dp, color = NodusColors.Shadow, offset = DpOffset(0.dp, 4.dp)))
                    .softLightFill(NodusColors.FieldFill)
                    .fieldBorder(
                        isError = isError,
                        isFocused = isFocused,
                        alwaysBordered = alwaysBordered,
                    )
                    .padding(start = contentPaddingStart),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(Modifier.weight(1f)) {
                    if (value.isEmpty()) {
                        BasicText(
                            text = placeholder,
                            style = TextStyle(fontFamily = khand, fontSize = fontSize, color = NodusColors.FieldPlaceholder),
                        )
                    }
                    innerTextField()
                }
                if (isPassword) {
                    // Іконки ока немає в макетах: прості іконки кольору підказки
                    Image(
                        painter = painterResource(if (passwordVisible) Res.drawable.ic_eye_off else Res.drawable.ic_eye),
                        contentDescription = if (passwordVisible) "Hide password" else "Show password",
                        colorFilter = ColorFilter.tint(NodusColors.FieldPlaceholder),
                        modifier = Modifier
                            .clip(ControlShape)
                            .clickable { passwordVisible = !passwordVisible }
                            .padding(horizontal = 10.dp, vertical = 8.dp)
                            .size((fontSize.value * 1.4f).dp),
                    )
                }
            }
        },
    )
}

/**
 * Рамка поля або клітинки коду. [tintOnError] ще й підфарбовує заливку червоним, як клітинки
 * в макеті коду; на світлих полях входу така заливка стає яскраво-рожевою, тож там лише рамка.
 */
internal fun Modifier.fieldBorder(
    isError: Boolean,
    isFocused: Boolean,
    alwaysBordered: Boolean,
    cornerRadius: Dp = ControlCorner,
    tintOnError: Boolean = false,
): Modifier =
    drawBehind {
        val corner = CornerRadius(cornerRadius.toPx())
        when {
            isError -> {
                if (tintOnError) drawRoundRect(NodusColors.ErrorFill, cornerRadius = corner, blendMode = BlendMode.Softlight)
                val width = 2.dp.toPx()
                drawRoundRect(
                    color = NodusColors.ErrorBorder,
                    topLeft = Offset(width / 2, width / 2),
                    size = Size(size.width - width, size.height - width),
                    cornerRadius = corner,
                    style = Stroke(width),
                )
            }
            isFocused || alwaysBordered -> drawRoundRect(
                color = if (isFocused) NodusColors.FieldBorderFocused else NodusColors.FieldBorder,
                cornerRadius = corner,
                style = Stroke(1.dp.toPx()),
                blendMode = BlendMode.ColorDodge,
            )
        }
    }

/** Основна кнопка (Sign in / Register / Send a Code): два шари темної заливки soft-light. */
@Composable
internal fun AuthPrimaryButton(
    text: String,
    isLoading: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
    height: Dp = ControlHeight,
    fontSize: TextUnit = 14.sp,
) {
    AuthButtonBox(enabled = enabled && !isLoading, shadowOffsetY = 4.dp, onClick = onClick, height = height, fill = {
        softLightFill(NodusColors.PrimaryFill).softLightFill(NodusColors.PrimaryFill)
    }) {
        if (isLoading) {
            AuthProgress(NodusColors.PrimaryLabel)
        } else {
            AuthButtonLabel(text, NodusColors.PrimaryLabel, shadowOffsetY = 4.dp, fontSize = fontSize)
        }
    }
}

/**
 * Яскравіша кнопка (Save Password, Sign In після зміни пароля): два шари soft-light,
 * нижній на мобільному світлий, на десктопі фіолетовий.
 */
@Composable
internal fun AuthAccentButton(
    text: String,
    isLoading: Boolean,
    isExpanded: Boolean,
    onClick: () -> Unit,
    height: Dp,
    fontSize: TextUnit,
) {
    val bottom = if (isExpanded) NodusColors.AccentButtonFillBottomExpanded else NodusColors.AccentButtonFillBottomCompact
    AuthButtonBox(enabled = !isLoading, shadowOffsetY = 4.dp, onClick = onClick, height = height, fill = {
        softLightFill(bottom).softLightFill(NodusColors.AccentButtonFillTop)
    }) {
        if (isLoading) {
            AuthProgress(NodusColors.PrimaryLabel)
        } else {
            AuthButtonLabel(text, NodusColors.PrimaryLabel, shadowOffsetY = 4.dp, fontSize = fontSize)
        }
    }
}

/**
 * Кнопка Google: світла заливка hard-light. На телефоні логотип біля лівого краю,
 * на десктопі одразу перед підписом; підпис в обох випадках по центру.
 */
@Composable
internal fun AuthGoogleButton(
    spec: AuthSpec,
    text: String,
    isLoading: Boolean,
    enabled: Boolean,
    shadowOffsetY: Dp,
    onClick: () -> Unit,
) {
    AuthButtonBox(enabled = enabled && !isLoading, shadowOffsetY = 4.dp, onClick = onClick, fill = {
        drawBehind {
            drawRoundRect(NodusColors.GoogleFill, cornerRadius = CornerRadius(ControlCorner.toPx()), blendMode = BlendMode.Hardlight)
        }
    }) {
        val logo = painterResource(Res.drawable.ic_google)
        if (isLoading) {
            AuthProgress(NodusColors.GoogleLabel)
        } else if (spec.isExpanded) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Image(logo, contentDescription = null, modifier = Modifier.size(spec.googleLogoSize))
                Spacer(Modifier.width(1.dp))
                AuthButtonLabel(text, NodusColors.GoogleLabel, shadowOffsetY)
                // Порожнє місце праворуч, щоб підпис лишався точно по центру кнопки
                Spacer(Modifier.width(spec.googleLogoSize.width + 1.dp))
            }
        } else {
            Image(
                logo,
                contentDescription = null,
                modifier = Modifier.align(Alignment.CenterStart).size(spec.googleLogoSize),
            )
            AuthButtonLabel(text, NodusColors.GoogleLabel, shadowOffsetY)
        }
    }
}

/** Контурна кнопка переходу між входом і реєстрацією. */
@Composable
internal fun AuthSecondaryButton(
    text: String,
    borderColor: Color,
    shadowOffsetY: Dp,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    AuthButtonBox(enabled = enabled, shadowOffsetY = shadowOffsetY, onClick = onClick, fill = {
        border(1.dp, borderColor, ControlShape)
    }) {
        AuthButtonLabel(text, NodusColors.Outline, shadowOffsetY)
    }
}

@Composable
private fun AuthButtonBox(
    enabled: Boolean,
    shadowOffsetY: Dp,
    onClick: () -> Unit,
    fill: Modifier.() -> Modifier,
    height: Dp = ControlHeight,
    content: @Composable BoxScope.() -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(height)
            .outerShadow(ControlShape, BoxShadow(radius = 4.dp, color = NodusColors.Shadow, offset = DpOffset(0.dp, shadowOffsetY)))
            .fill()
            .clip(ControlShape)
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center,
        content = content,
    )
}

@Composable
private fun AuthButtonLabel(text: String, color: Color, shadowOffsetY: Dp, fontSize: TextUnit = 14.sp) {
    BasicText(
        text = text,
        style = onboardingTextStyle(
            fontFamily = khandFontFamily(),
            fontSize = fontSize,
            color = color,
            shadowOffsetY = shadowOffsetY,
        ),
    )
}

@Composable
private fun AuthProgress(color: Color) {
    CircularProgressIndicator(modifier = Modifier.size(18.dp), color = color, strokeWidth = 2.dp)
}

/** Розділювач "or" між основною кнопкою та входом через Google. */
@Composable
internal fun AuthOrDivider(spec: AuthSpec) {
    Row(
        modifier = Modifier.fillMaxWidth().height(14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        DividerLine(spec.dividerLineWidth)
        BasicText(
            text = "or",
            style = onboardingTextStyle(khandFontFamily(), 14.sp, NodusColors.FieldPlaceholder, shadowOffsetY = 0.dp, shadowBlur = 0.dp),
        )
        DividerLine(spec.dividerLineWidth)
    }
}

@Composable
private fun DividerLine(width: Dp) {
    Box(
        Modifier
            .width(width)
            .height(1.dp)
            .drawBehind { drawRect(NodusColors.FieldPlaceholder) },
    )
}

/** Дрібний підпис Khand Medium 12 ("Already have an Account?"). */
@Composable
internal fun AuthSmallText(text: String) {
    BasicText(
        text = text,
        style = onboardingTextStyle(
            fontFamily = khandFontFamily(),
            fontSize = 12.sp,
            color = NodusColors.Outline,
            shadowOffsetY = 4.dp,
            fontWeight = FontWeight.Medium,
        ),
    )
}

/** Підкреслене посилання Khand Medium ("Forgot your password?", "Back to Sign In", "Resend Code"). */
@Composable
internal fun AuthLink(
    text: String,
    fontSize: TextUnit,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    BasicText(
        text = text,
        style = onboardingTextStyle(
            fontFamily = khandFontFamily(),
            fontSize = fontSize,
            color = NodusColors.Link,
            shadowOffsetY = 4.dp,
            fontWeight = FontWeight.Medium,
        ).copy(textDecoration = TextDecoration.Underline),
        modifier = modifier.clip(ControlShape).clickable(enabled = enabled, onClick = onClick),
    )
}

/**
 * Рядок повідомлення фіксованої висоти: місце під помилку зарезервоване, тож форма не стрибає.
 * Стиль помилки з макета "Invalid Recovery code": Iosevka Charon, рожевий колір.
 */
@Composable
internal fun AuthMessage(
    message: String?,
    height: Dp,
    fontSize: TextUnit = 14.sp,
    color: Color = NodusColors.ErrorText,
) {
    Box(Modifier.fillMaxWidth().height(height), contentAlignment = Alignment.Center) {
        if (message != null) {
            BasicText(
                text = message,
                style = TextStyle(
                    fontFamily = iosevkaCharonFontFamily(),
                    fontWeight = FontWeight.Medium,
                    fontSize = fontSize,
                    color = color,
                    textAlign = TextAlign.Center,
                ),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.wrapContentHeight(unbounded = true),
            )
        }
    }
}

/**
 * Розміщує текст так, щоб його базова лінія була на [baseline] від верху блоку висотою [height],
 * як координати тексту в Figma. Текст може виходити за межі блоку; висота блоку від шрифту не залежить.
 */
internal fun Modifier.baselineSlot(height: Dp, baseline: Dp): Modifier = layout { measurable, constraints ->
    val placeable = measurable.measure(constraints.copy(minHeight = 0, maxHeight = Constraints.Infinity))
    val firstBaseline = placeable[FirstBaseline].takeIf { it != AlignmentLine.Unspecified } ?: 0
    val slotHeight = height.roundToPx()
    layout(placeable.width, slotHeight) {
        placeable.place(0, baseline.roundToPx() - firstBaseline)
    }
}

/** Напівпрозора заливка в режимі soft-light; без окремого шару, щоб змішуватися з карткою. */
internal fun Modifier.softLightFill(color: Color, corner: Dp = ControlCorner): Modifier = drawBehind {
    drawRoundRect(color, cornerRadius = CornerRadius(corner.toPx()), blendMode = BlendMode.Softlight)
}
