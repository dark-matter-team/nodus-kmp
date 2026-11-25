package com.darkmatter.nodus.ui.screens.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.darkmatter.nodus.core.di.AppContainer
import com.darkmatter.nodus.core.network.ApiResult
import com.darkmatter.nodus.data.repository.AuthRepository
import com.darkmatter.nodus.ui.common.userMessage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class NewPasswordUiState(
    val newPassword: String = "",
    val passwordConfirmation: String = "",
    val newPasswordError: String? = null,
    val passwordConfirmationError: String? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isPasswordReset: Boolean = false,
) {
    /** Для підказки "✓ At least 8 characters". */
    val isLengthMet: Boolean get() = newPassword.length >= AuthValidation.PASSWORD_MIN_LENGTH

    /** Одна помилка на місці підказки: спершу поля, потім відповідь сервера. */
    val shownError: String? get() = newPasswordError ?: passwordConfirmationError ?: errorMessage
}

/** Код уже перевірено на попередньому екрані; бекенд перевіряє його ще раз разом з новим паролем. */
class NewPasswordViewModel(
    private val email: String,
    private val code: String,
    private val repository: AuthRepository = AppContainer.authRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(NewPasswordUiState())
    val uiState: StateFlow<NewPasswordUiState> = _uiState.asStateFlow()

    fun onNewPasswordChange(value: String) =
        _uiState.update { it.copy(newPassword = value, newPasswordError = null, errorMessage = null) }

    fun onPasswordConfirmationChange(value: String) =
        _uiState.update { it.copy(passwordConfirmation = value, passwordConfirmationError = null, errorMessage = null) }

    /** Викликається після переходу на екран успіху. */
    fun onNavigated() = _uiState.update { it.copy(isPasswordReset = false) }

    fun submit() {
        val state = _uiState.value
        if (state.isLoading) return

        val newPasswordError = AuthValidation.validateNewPassword(state.newPassword)
        val confirmationError = AuthValidation.validatePasswordConfirmation(state.newPassword, state.passwordConfirmation)
        if (newPasswordError != null || confirmationError != null) {
            _uiState.update {
                it.copy(newPasswordError = newPasswordError, passwordConfirmationError = confirmationError)
            }
            return
        }

        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        viewModelScope.launch {
            when (val result = repository.resetPassword(email, code, state.newPassword)) {
                is ApiResult.Success -> _uiState.update { it.copy(isLoading = false, isPasswordReset = true) }
                is ApiResult.Error -> {
                    val message = result.userMessage()
                    _uiState.update { it.copy(isLoading = false, errorMessage = message) }
                }
            }
        }
    }
}
