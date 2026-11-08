package com.darkmatter.nodus.ui.screens.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.darkmatter.nodus.core.auth.GoogleSignInProvider
import com.darkmatter.nodus.core.di.AppContainer
import com.darkmatter.nodus.core.network.ApiResult
import com.darkmatter.nodus.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class RegisterUiState(
    val email: String = "",
    val nickname: String = "",
    val password: String = "",
    val passwordConfirmation: String = "",
    val emailError: String? = null,
    val nicknameError: String? = null,
    val passwordError: String? = null,
    val passwordConfirmationError: String? = null,
    val isLoading: Boolean = false,
    val isGoogleLoading: Boolean = false,
    val errorMessage: String? = null,
    val isRegistered: Boolean = false,
) {
    val isBusy: Boolean get() = isLoading || isGoogleLoading
}

class RegisterViewModel(
    private val repository: AuthRepository = AppContainer.authRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(RegisterUiState())
    val uiState: StateFlow<RegisterUiState> = _uiState.asStateFlow()

    fun onEmailChange(value: String) =
        _uiState.update { it.copy(email = value, emailError = null, errorMessage = null) }

    fun onNicknameChange(value: String) =
        _uiState.update { it.copy(nickname = value, nicknameError = null, errorMessage = null) }

    fun onPasswordChange(value: String) =
        _uiState.update { it.copy(password = value, passwordError = null, errorMessage = null) }

    fun onPasswordConfirmationChange(value: String) =
        _uiState.update { it.copy(passwordConfirmation = value, passwordConfirmationError = null, errorMessage = null) }

    fun submit() {
        val state = _uiState.value
        if (state.isBusy) return

        val validated = state.copy(
            emailError = AuthValidation.validateEmail(state.email),
            nicknameError = AuthValidation.validateNickname(state.nickname),
            passwordError = AuthValidation.validateNewPassword(state.password),
            passwordConfirmationError = AuthValidation.validatePasswordConfirmation(
                state.password,
                state.passwordConfirmation,
            ),
        )
        val hasErrors = listOf(
            validated.emailError,
            validated.nicknameError,
            validated.passwordError,
            validated.passwordConfirmationError,
        ).any { it != null }
        if (hasErrors) {
            _uiState.value = validated
            return
        }

        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        viewModelScope.launch {
            when (val result = repository.register(state.email, state.nickname, state.password)) {
                is ApiResult.Success -> _uiState.update { it.copy(isLoading = false, isRegistered = true) }
                is ApiResult.Error -> _uiState.update { it.copy(isLoading = false, errorMessage = result.message) }
            }
        }
    }

    /** Бекенд сам створює акаунт для нового Google-користувача, тож це і реєстрація, і вхід. */
    fun signInWithGoogle(provider: GoogleSignInProvider) {
        if (_uiState.value.isBusy) return

        _uiState.update { it.copy(isGoogleLoading = true, errorMessage = null) }
        viewModelScope.launch {
            when (val result = repository.signInWithGoogle(provider)) {
                null -> _uiState.update { it.copy(isGoogleLoading = false) }
                is ApiResult.Success -> _uiState.update { it.copy(isGoogleLoading = false, isRegistered = true) }
                is ApiResult.Error -> _uiState.update { it.copy(isGoogleLoading = false, errorMessage = result.message) }
            }
        }
    }
}
