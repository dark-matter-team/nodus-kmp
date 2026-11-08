package com.darkmatter.nodus.ui.screens.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun RegisterScreen(
    onRegistered: () -> Unit,
    onOpenLogin: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: RegisterViewModel = viewModel { RegisterViewModel() },
) {
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(state.isRegistered) {
        if (state.isRegistered) onRegistered()
    }

    AuthScreenLayout(title = "Реєстрація", modifier = modifier) {
        AuthTextField(
            value = state.email,
            onValueChange = viewModel::onEmailChange,
            label = "Email",
            error = state.emailError,
            enabled = !state.isLoading,
            keyboardType = KeyboardType.Email,
        )
        AuthTextField(
            value = state.nickname,
            onValueChange = viewModel::onNicknameChange,
            label = "Нікнейм",
            error = state.nicknameError,
            enabled = !state.isLoading,
        )
        PasswordField(
            value = state.password,
            onValueChange = viewModel::onPasswordChange,
            label = "Пароль (щонайменше ${AuthValidation.PASSWORD_MIN_LENGTH} символів)",
            error = state.passwordError,
            enabled = !state.isLoading,
            imeAction = ImeAction.Next,
        )
        PasswordField(
            value = state.passwordConfirmation,
            onValueChange = viewModel::onPasswordConfirmationChange,
            label = "Повторіть пароль",
            error = state.passwordConfirmationError,
            enabled = !state.isLoading,
        )
        FormErrorText(state.errorMessage)
        SubmitButton(text = "Зареєструватися", isLoading = state.isLoading, onClick = viewModel::submit)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("Вже маєте акаунт?")
            TextButton(onClick = onOpenLogin, enabled = !state.isLoading) {
                Text("Увійти")
            }
        }
    }
}
