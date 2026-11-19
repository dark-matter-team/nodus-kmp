package com.darkmatter.nodus.ui.screens.auth

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import nodus.shared.generated.resources.Res
import nodus.shared.generated.resources.auth_key

private val KeyIllustration = ResetIllustration(
    image = Res.drawable.auth_key,
    compactSize = DpSize(226.dp, 171.dp),
    expandedSize = DpSize(596.dp, 450.dp),
    expandedOffsetX = 8.dp,
)

/** Фрейми "Forgot Password D Mobile" і "Forgot Password D". */
@Composable
fun ForgotPasswordScreen(
    onCodeSent: (email: String) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ForgotPasswordViewModel = viewModel { ForgotPasswordViewModel() },
) {
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(state.codeSentTo) {
        state.codeSentTo?.let { email ->
            viewModel.onNavigated()
            onCodeSent(email)
        }
    }

    ResetScreenScaffold(
        illustration = KeyIllustration,
        compactGap = 64.dp,
        contentOffsetX = (-36).dp,
        modifier = modifier,
    ) { spec ->
        val expanded = spec.isExpanded
        ResetCard(spec) {
            ResetCardHeader(
                spec = spec,
                title = "FORGOT PASSWORD?",
                subtitle = "Enter your e-mail and we’ll send you a code",
                height = if (expanded) 125.dp else 103.dp,
                titleBaseline = if (expanded) 62.5.dp else 50.dp,
                subtitleBaseline = if (expanded) 83.6.dp else 68.4.dp,
            )
            AuthField(
                value = state.email,
                onValueChange = viewModel::onEmailChange,
                placeholder = "Email",
                isError = state.emailError != null,
                enabled = !state.isLoading,
                keyboardType = KeyboardType.Email,
                imeAction = ImeAction.Done,
                height = spec.fieldHeight,
                fontSize = spec.fieldFontSize,
                contentPaddingStart = spec.fieldPaddingStart,
                alwaysBordered = true,
            )
            AuthMessage(
                message = state.emailError ?: state.errorMessage,
                height = if (expanded) 28.dp else 32.dp,
                fontSize = spec.messageFontSize,
            )
            AuthPrimaryButton(
                text = "Send a Code",
                isLoading = state.isLoading,
                enabled = true,
                onClick = viewModel::submit,
                height = spec.fieldHeight,
                fontSize = spec.buttonFontSize,
            )
            LinkSlot(
                text = "Back to Sign In",
                spec = spec,
                height = if (expanded) 80.dp else 57.dp,
                baseline = if (expanded) 49.7.dp else 39.dp,
                enabled = !state.isLoading,
                onClick = onBack,
            )
        }
    }
}
