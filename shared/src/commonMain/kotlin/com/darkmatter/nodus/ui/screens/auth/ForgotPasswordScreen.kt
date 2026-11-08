package com.darkmatter.nodus.ui.screens.auth

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.lifecycle.viewmodel.compose.viewModel

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

    AuthScreenLayout(
        title = "Забули пароль?",
        subtitle = "Введіть email акаунта, і ми надішлемо код для відновлення пароля.",
        modifier = modifier,
    ) {
        AuthTextField(
            value = state.email,
            onValueChange = viewModel::onEmailChange,
            label = "Email",
            error = state.emailError,
            enabled = !state.isLoading,
            keyboardType = KeyboardType.Email,
            imeAction = ImeAction.Done,
        )
        FormErrorText(state.errorMessage)
        SubmitButton(text = "Надіслати код", isLoading = state.isLoading, onClick = viewModel::submit)
        TextButton(onClick = onBack, enabled = !state.isLoading, modifier = Modifier.fillMaxWidth()) {
            Text("Назад до входу")
        }
    }
}
