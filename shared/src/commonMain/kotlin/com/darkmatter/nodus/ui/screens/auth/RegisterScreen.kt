package com.darkmatter.nodus.ui.screens.auth

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.darkmatter.nodus.core.auth.rememberGoogleSignInProvider
import com.darkmatter.nodus.ui.theme.NodusColors

/** Відступи з фреймів "IOS Sign Up Dark" і "MacOS Welcome Dark" (Sign Up). */
@Composable
fun RegisterScreen(
    onRegistered: () -> Unit,
    onOpenLogin: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: RegisterViewModel = viewModel { RegisterViewModel() },
) {
    val state by viewModel.uiState.collectAsState()
    val googleSignInProvider = rememberGoogleSignInProvider()

    LaunchedEffect(state.isRegistered) {
        if (state.isRegistered) onRegistered()
    }

    AuthCardScreen(modifier = modifier) { spec ->
        Spacer(Modifier.height(30.dp))
        AuthTitle("SIGN UP")
        Spacer(Modifier.height(30.dp))
        AuthField(
            value = state.email,
            onValueChange = viewModel::onEmailChange,
            placeholder = "Email",
            isError = state.emailError != null,
            enabled = !state.isBusy,
            keyboardType = KeyboardType.Email,
        )
        Spacer(Modifier.height(13.dp))
        AuthField(
            value = state.nickname,
            onValueChange = viewModel::onNicknameChange,
            placeholder = "Username",
            isError = state.nicknameError != null,
            enabled = !state.isBusy,
        )
        Spacer(Modifier.height(13.dp))
        AuthField(
            value = state.password,
            onValueChange = viewModel::onPasswordChange,
            placeholder = "Password",
            isError = state.passwordError != null,
            enabled = !state.isBusy,
            isPassword = true,
        )
        Spacer(Modifier.height(13.dp))
        AuthField(
            value = state.passwordConfirmation,
            onValueChange = viewModel::onPasswordConfirmationChange,
            placeholder = "Repeat your password",
            isError = state.passwordConfirmationError != null,
            enabled = !state.isBusy,
            imeAction = ImeAction.Done,
            isPassword = true,
        )
        // Відступ 13 dp з макета плюс зарезервований рядок під помилку, щоб форма не стрибала
        AuthMessage(
            message = state.errorMessage ?: state.emailError ?: state.nicknameError ?: state.passwordError
                ?: state.passwordConfirmationError,
            height = 13.dp + AuthMessageHeight,
        )
        AuthPrimaryButton(
            text = "Register",
            isLoading = state.isLoading,
            enabled = !state.isGoogleLoading,
            onClick = viewModel::submit,
        )
        if (googleSignInProvider.isSupported) {
            Spacer(Modifier.height(15.dp))
            AuthOrDivider(spec)
            Spacer(Modifier.height(15.dp))
            AuthGoogleButton(
                spec = spec,
                text = "Sign up with Google",
                isLoading = state.isGoogleLoading,
                enabled = !state.isLoading,
                shadowOffsetY = 4.dp,
                onClick = { viewModel.signInWithGoogle(googleSignInProvider) },
            )
        }
        Spacer(Modifier.height(13.5.dp))
        AuthSmallText("Already have an Account?")
        Spacer(Modifier.height(2.5.dp))
        AuthSecondaryButton(
            text = "Sign In",
            borderColor = NodusColors.SecondaryBorderDark,
            shadowOffsetY = 4.dp,
            enabled = !state.isBusy,
            onClick = onOpenLogin,
        )
        Spacer(Modifier.height(if (spec.isExpanded) 20.dp else 14.dp))
    }
}
