package com.darkmatter.nodus.ui.screens.auth

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import nodus.shared.generated.resources.Res
import nodus.shared.generated.resources.auth_shield

private val ShieldIllustration = ResetIllustration(
    image = Res.drawable.auth_shield,
    compactSize = DpSize(189.dp, 172.dp),
    expandedSize = DpSize(544.dp, 496.dp),
    expandedOffsetX = (-14).dp,
)

/** Фрейми "Forgot Password L Mobile" (новий пароль) і "Forgot Password D-1". */
@Composable
fun NewPasswordScreen(
    email: String,
    code: String,
    onPasswordReset: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: NewPasswordViewModel = viewModel { NewPasswordViewModel(email, code) },
) {
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(state.isPasswordReset) {
        if (state.isPasswordReset) {
            viewModel.onNavigated()
            onPasswordReset()
        }
    }

    ResetScreenScaffold(
        illustration = ShieldIllustration,
        compactGap = 46.dp,
        contentOffsetX = (-36).dp,
        modifier = modifier,
    ) { spec ->
        val expanded = spec.isExpanded
        ResetCard(spec) {
            ResetCardHeader(
                spec = spec,
                title = "NEW PASSWORD",
                subtitle = "Create a secure password to protect your skill trees.",
                height = if (expanded) 139.dp else 104.dp,
                titleBaseline = if (expanded) 69.5.dp else 51.dp,
                subtitleBaseline = if (expanded) 96.6.dp else 69.4.dp,
            )
            AuthField(
                value = state.newPassword,
                onValueChange = viewModel::onNewPasswordChange,
                placeholder = "New Password",
                isError = state.newPasswordError != null,
                enabled = !state.isLoading,
                isPassword = true,
                height = spec.fieldHeight,
                fontSize = spec.fieldFontSize,
                contentPaddingStart = spec.fieldPaddingStart,
                alwaysBordered = true,
            )
            Spacer(Modifier.height(if (expanded) 28.dp else 12.dp))
            AuthField(
                value = state.passwordConfirmation,
                onValueChange = viewModel::onPasswordConfirmationChange,
                placeholder = "Confirm password",
                isError = state.passwordConfirmationError != null,
                enabled = !state.isLoading,
                imeAction = ImeAction.Done,
                isPassword = true,
                height = spec.fieldHeight,
                fontSize = spec.fieldFontSize,
                contentPaddingStart = spec.fieldPaddingStart,
                alwaysBordered = true,
            )
            PasswordHintRow(
                spec = spec,
                isRequirementMet = state.isLengthMet,
                error = state.shownError,
                height = if (expanded) 62.dp else 46.dp,
                baseline = if (expanded) 37.9.dp else 23.dp,
            )
            AuthAccentButton(
                text = "Save Password",
                isLoading = state.isLoading,
                isExpanded = expanded,
                onClick = viewModel::submit,
                height = spec.fieldHeight,
                fontSize = spec.buttonFontSize,
            )
            Spacer(Modifier.height(if (expanded) 28.dp else 18.dp))
        }
    }
}
