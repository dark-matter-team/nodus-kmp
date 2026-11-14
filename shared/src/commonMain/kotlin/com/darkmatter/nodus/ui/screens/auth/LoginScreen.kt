package com.darkmatter.nodus.ui.screens.auth

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.darkmatter.nodus.core.auth.rememberGoogleSignInProvider
import com.darkmatter.nodus.ui.theme.NodusColors

/** Відступи з фреймів "IOS Sign In Dark" і "MacOS Welcome Dark" (Sign In). */
@Composable
fun LoginScreen(
    onLoggedIn: () -> Unit,
    onOpenRegister: () -> Unit,
    onForgotPassword: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: LoginViewModel = viewModel { LoginViewModel() },
) {
    val state by viewModel.uiState.collectAsState()
    val googleSignInProvider = rememberGoogleSignInProvider()

    LaunchedEffect(state.isLoggedIn) {
        if (state.isLoggedIn) onLoggedIn()
    }

    AuthCardScreen(modifier = modifier) { spec ->
        Spacer(Modifier.height(if (spec.isExpanded) 58.dp else 62.dp))
        AuthTitle("SIGN IN")
        Spacer(Modifier.height(if (spec.isExpanded) 57.dp else 53.dp))
        AuthField(
            value = state.login,
            onValueChange = viewModel::onLoginChange,
            placeholder = "Email or Username",
            error = state.loginError,
            enabled = !state.isBusy,
            keyboardType = KeyboardType.Email,
        )
        Spacer(Modifier.height(13.dp))
        AuthField(
            value = state.password,
            onValueChange = viewModel::onPasswordChange,
            placeholder = "Password",
            error = state.passwordError,
            enabled = !state.isBusy,
            imeAction = ImeAction.Done,
            isPassword = true,
        )
        // Посилання немає в макеті: тимчасове місце до появи дизайну відновлення пароля
        Spacer(Modifier.height(6.dp))
        AuthSmallText(
            text = "Forgot password?",
            onClick = if (state.isBusy) null else onForgotPassword,
            modifier = Modifier.align(Alignment.End),
        )
        Spacer(Modifier.height(7.dp))
        state.errorMessage?.let {
            AuthErrorText(it)
            Spacer(Modifier.height(8.dp))
        }
        AuthPrimaryButton(
            text = "Sign in",
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
                text = "Sign in with Google",
                isLoading = state.isGoogleLoading,
                enabled = !state.isLoading,
                shadowOffsetY = if (spec.isExpanded) 2.dp else 4.dp,
                onClick = { viewModel.signInWithGoogle(googleSignInProvider) },
            )
        }
        Spacer(Modifier.height(13.dp))
        AuthSecondaryButton(
            text = "Create an Account",
            // У десктопному макеті Sign In рамка світла, у решті макетів темна
            borderColor = if (spec.isExpanded) NodusColors.SecondaryBorderLight else NodusColors.SecondaryBorderDark,
            shadowOffsetY = if (spec.isExpanded) 2.dp else 4.dp,
            enabled = !state.isBusy,
            onClick = onOpenRegister,
        )
        Spacer(Modifier.height(50.dp))
    }
}
