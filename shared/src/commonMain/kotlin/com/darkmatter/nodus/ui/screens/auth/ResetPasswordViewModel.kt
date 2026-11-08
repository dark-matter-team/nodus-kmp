package com.darkmatter.nodus.ui.screens.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.darkmatter.nodus.core.di.AppContainer
import com.darkmatter.nodus.core.network.ApiResult
import com.darkmatter.nodus.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ResetPasswordUiState(
    val email: String,
    val code: String = "",
    val newPassword: String = "",
    val passwordConfirmation: String = "",
    val codeError: String? = null,
    val newPasswordError: String? = null,
    val passwordConfirmationError: String? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    /** Повідомлення про успішне повторне надсилання коду. */
    val infoMessage: String? = null,
    val isPasswordReset: Boolean = false,
)

class ResetPasswordViewModel(
    email: String,
    private val repository: AuthRepository = AppContainer.authRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ResetPasswordUiState(email = email))
    val uiState: StateFlow<ResetPasswordUiState> = _uiState.asStateFlow()

    fun onCodeChange(value: String) {
        // Код складається лише з цифр, тому решту символів не пропускаємо
        val digits = value.filter { it.isDigit() }.take(AuthValidation.RESET_CODE_LENGTH)
        _uiState.update { it.copy(code = digits, codeError = null, errorMessage = null) }
    }

    fun onNewPasswordChange(value: String) =
        _uiState.update { it.copy(newPassword = value, newPasswordError = null, errorMessage = null) }

    fun onPasswordConfirmationChange(value: String) =
        _uiState.update { it.copy(passwordConfirmation = value, passwordConfirmationError = null, errorMessage = null) }

    fun submit() {
        val state = _uiState.value
        if (state.isLoading) return

        val validated = state.copy(
            codeError = AuthValidation.validateResetCode(state.code),
            newPasswordError = AuthValidation.validateNewPassword(state.newPassword),
            passwordConfirmationError = AuthValidation.validatePasswordConfirmation(
                state.newPassword,
                state.passwordConfirmation,
            ),
        )
        if (validated.codeError != null || validated.newPasswordError != null ||
            validated.passwordConfirmationError != null
        ) {
            _uiState.value = validated
            return
        }

        _uiState.update { it.copy(isLoading = true, errorMessage = null, infoMessage = null) }
        viewModelScope.launch {
            when (val result = repository.resetPassword(state.email, state.code, state.newPassword)) {
                is ApiResult.Success -> _uiState.update { it.copy(isLoading = false, isPasswordReset = true) }
                is ApiResult.Error -> _uiState.update { it.copy(isLoading = false, errorMessage = result.message) }
            }
        }
    }

    fun resendCode() {
        val state = _uiState.value
        if (state.isLoading) return

        _uiState.update { it.copy(isLoading = true, errorMessage = null, infoMessage = null) }
        viewModelScope.launch {
            when (val result = repository.forgotPassword(state.email)) {
                is ApiResult.Success -> _uiState.update {
                    it.copy(isLoading = false, code = "", infoMessage = "Новий код надіслано на ${state.email}")
                }
                is ApiResult.Error -> _uiState.update { it.copy(isLoading = false, errorMessage = result.message) }
            }
        }
    }
}
