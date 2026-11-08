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
import androidx.compose.ui.text.input.KeyboardType
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun LoginScreen(
    onLoggedIn: () -> Unit,
    onOpenRegister: () -> Unit,
    onForgotPassword: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: LoginViewModel = viewModel { LoginViewModel() },
) {
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(state.isLoggedIn) {
        if (state.isLoggedIn) onLoggedIn()
    }

    AuthScreenLayout(title = "Вхід", modifier = modifier) {
        AuthTextField(
            value = state.login,
            onValueChange = viewModel::onLoginChange,
            label = "Email або нікнейм",
            error = state.loginError,
            enabled = !state.isLoading,
            keyboardType = KeyboardType.Email,
        )
        PasswordField(
            value = state.password,
            onValueChange = viewModel::onPasswordChange,
            label = "Пароль",
            error = state.passwordError,
            enabled = !state.isLoading,
        )
        TextButton(
            onClick = onForgotPassword,
            enabled = !state.isLoading,
            modifier = Modifier.align(Alignment.End),
        ) {
            Text("Забули пароль?")
        }
        FormErrorText(state.errorMessage)
        SubmitButton(text = "Увійти", isLoading = state.isLoading, onClick = viewModel::submit)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("Немає акаунта?")
            TextButton(onClick = onOpenRegister, enabled = !state.isLoading) {
                Text("Зареєструватися")
            }
        }
    }
}
