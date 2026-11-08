package com.darkmatter.nodus.ui.screens.auth

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun ResetPasswordScreen(
    email: String,
    onDone: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ResetPasswordViewModel = viewModel { ResetPasswordViewModel(email) },
) {
    val state by viewModel.uiState.collectAsState()

    if (state.isPasswordReset) {
        AuthScreenLayout(
            title = "Пароль змінено",
            subtitle = "Тепер увійдіть з новим паролем.",
            modifier = modifier,
        ) {
            SubmitButton(text = "До входу", isLoading = false, onClick = onDone)
        }
        return
    }

    AuthScreenLayout(
        title = "Новий пароль",
        subtitle = "Ми надіслали ${AuthValidation.RESET_CODE_LENGTH}-значний код на ${state.email}. " +
            "Код дійсний 15 хвилин.",
        modifier = modifier,
    ) {
        AuthTextField(
            value = state.code,
            onValueChange = viewModel::onCodeChange,
            label = "Код з листа",
            error = state.codeError,
            enabled = !state.isLoading,
            keyboardType = KeyboardType.NumberPassword,
        )
        PasswordField(
            value = state.newPassword,
            onValueChange = viewModel::onNewPasswordChange,
            label = "Новий пароль (щонайменше ${AuthValidation.PASSWORD_MIN_LENGTH} символів)",
            error = state.newPasswordError,
            enabled = !state.isLoading,
            imeAction = ImeAction.Next,
        )
        PasswordField(
            value = state.passwordConfirmation,
            onValueChange = viewModel::onPasswordConfirmationChange,
            label = "Повторіть новий пароль",
            error = state.passwordConfirmationError,
            enabled = !state.isLoading,
        )
        state.infoMessage?.let {
            Text(it, color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.bodyMedium)
        }
        FormErrorText(state.errorMessage)
        SubmitButton(text = "Змінити пароль", isLoading = state.isLoading, onClick = viewModel::submit)
        TextButton(onClick = viewModel::resendCode, enabled = !state.isLoading, modifier = Modifier.fillMaxWidth()) {
            Text("Надіслати код ще раз")
        }
        TextButton(onClick = onBack, enabled = !state.isLoading, modifier = Modifier.fillMaxWidth()) {
            Text("Назад")
        }
    }
}
