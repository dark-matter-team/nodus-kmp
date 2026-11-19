package com.darkmatter.nodus.ui.screens.auth

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.darkmatter.nodus.ui.theme.NodusColors
import com.darkmatter.nodus.ui.theme.iosevkaCharonFontFamily
import com.darkmatter.nodus.ui.theme.khandFontFamily
import nodus.shared.generated.resources.Res
import nodus.shared.generated.resources.auth_constellation

private val ConstellationIllustration = ResetIllustration(
    image = Res.drawable.auth_constellation,
    compactSize = DpSize(326.dp, 182.dp),
    expandedSize = DpSize(671.dp, 374.5.dp),
    expandedOffsetX = 21.5.dp,
)

/** Фрейми "Recovery Code D Mobile" / "Code Recovery D" і їхні варіанти з помилкою. */
@Composable
fun VerifyCodeScreen(
    email: String,
    onCodeVerified: (code: String) -> Unit,
    onChangeEmail: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: VerifyCodeViewModel = viewModel { VerifyCodeViewModel(email) },
) {
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(state.verifiedCode) {
        state.verifiedCode?.let { code ->
            viewModel.onNavigated()
            onCodeVerified(code)
        }
    }

    ResetScreenScaffold(
        illustration = ConstellationIllustration,
        compactGap = 50.4.dp,
        contentOffsetX = (-9.5).dp,
        modifier = modifier,
    ) { spec ->
        val expanded = spec.isExpanded
        Column(Modifier.width(if (expanded) 621.dp else 316.dp)) {
            BaselineTextSlot(
                text = "We sent a ${AuthValidation.RESET_CODE_LENGTH}-digit code to\n${state.email}",
                style = resetTextStyle(khandFontFamily(), if (expanded) 40.sp else 24.sp, NodusColors.CodeMessage),
                height = if (expanded) 198.dp else 106.6.dp,
                baseline = if (expanded) 41.9.dp else 25.2.dp,
            )
            CodeCells(
                code = state.code,
                onCodeChange = viewModel::onCodeChange,
                isError = state.isCodeInvalid,
                enabled = !state.isLoading,
                cellSize = if (expanded) 91.dp else 46.dp,
                cellCorner = if (expanded) 14.dp else 10.dp,
                digitFontSize = if (expanded) 40.sp else 24.sp,
            )
            val message = state.errorMessage ?: state.infoMessage
            BaselineTextSlot(
                text = message.orEmpty(),
                style = TextStyle(
                    fontFamily = iosevkaCharonFontFamily(),
                    fontWeight = FontWeight.Medium,
                    fontSize = if (expanded) 28.sp else 17.5.sp,
                    color = if (state.errorMessage != null) NodusColors.ErrorText else NodusColors.Hint,
                ),
                height = if (expanded) 81.dp else 42.dp,
                baseline = if (expanded) 54.dp else 29.2.dp,
            )
            val linksHeight = if (expanded) 50.dp else 24.dp
            val linksBaseline = if (expanded) 37.dp else 17.dp
            val linkSize = if (expanded) 36.sp else 16.sp
            Box(Modifier.fillMaxWidth().height(linksHeight)) {
                AuthLink(
                    text = "Resend Code",
                    fontSize = linkSize,
                    enabled = !state.isLoading,
                    onClick = viewModel::resendCode,
                    modifier = Modifier.align(Alignment.TopStart).baselineSlot(linksHeight, linksBaseline),
                )
                AuthLink(
                    text = "Change Email",
                    fontSize = linkSize,
                    enabled = !state.isLoading,
                    onClick = onChangeEmail,
                    modifier = Modifier.align(Alignment.TopEnd).baselineSlot(linksHeight, linksBaseline),
                )
            }
        }
    }
}

/**
 * Шість клітинок коду. Під ними одне невидиме поле вводу: клітинки лише показують цифри,
 * тож вставка коду з буфера і стирання працюють як у звичайному полі.
 */
@Composable
private fun CodeCells(
    code: String,
    onCodeChange: (String) -> Unit,
    isError: Boolean,
    enabled: Boolean,
    cellSize: Dp,
    cellCorner: Dp,
    digitFontSize: TextUnit,
) {
    val focusRequester = remember { FocusRequester() }
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()
    LaunchedEffect(Unit) { focusRequester.requestFocus() }

    BasicTextField(
        value = code,
        onValueChange = onCodeChange,
        enabled = enabled,
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
        cursorBrush = SolidColor(Color.Transparent),
        interactionSource = interactionSource,
        modifier = Modifier.fillMaxWidth().focusRequester(focusRequester),
        decorationBox = { innerTextField ->
            val digitStyle = TextStyle(
                fontFamily = khandFontFamily(),
                fontSize = digitFontSize,
                color = NodusColors.CodeMessage,
            )
            Box {
                // Справжнє поле лишається в розмітці (фокус, клавіатура, вставка), але невидиме
                Box(Modifier.alpha(0f)) { innerTextField() }
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    repeat(AuthValidation.RESET_CODE_LENGTH) { index ->
                        Box(
                            modifier = Modifier
                                .size(cellSize)
                                .softLightFill(NodusColors.FieldFill, cellCorner)
                                .fieldBorder(
                                    isError = isError,
                                    // Підсвічуємо клітинку, у яку зараз піде наступна цифра
                                    isFocused = isFocused && index == code.length,
                                    alwaysBordered = true,
                                    cornerRadius = cellCorner,
                                    tintOnError = true,
                                ),
                            contentAlignment = Alignment.Center,
                        ) {
                            code.getOrNull(index)?.let { BasicText(it.toString(), style = digitStyle) }
                        }
                    }
                }
            }
        },
    )
}
