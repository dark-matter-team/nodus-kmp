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

data class ForgotPasswordUiState(
    val email: String = "",
    val emailError: String? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    /** Email, на який надіслано код; не null означає, що можна переходити до введення коду. */
    val codeSentTo: String? = null,
)

class ForgotPasswordViewModel(
    private val repository: AuthRepository = AppContainer.authRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ForgotPasswordUiState())
    val uiState: StateFlow<ForgotPasswordUiState> = _uiState.asStateFlow()

    fun onEmailChange(value: String) =
        _uiState.update { it.copy(email = value, emailError = null, errorMessage = null) }

    /** Викликається після переходу на наступний екран, щоб при поверненні "назад" не перейти знову. */
    fun onNavigated() = _uiState.update { it.copy(codeSentTo = null) }

    fun submit() {
        val state = _uiState.value
        if (state.isLoading) return

        val emailError = AuthValidation.validateEmail(state.email)
        if (emailError != null) {
            _uiState.update { it.copy(emailError = emailError) }
            return
        }

        val email = state.email.trim()
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        viewModelScope.launch {
            when (val result = repository.forgotPassword(email)) {
                is ApiResult.Success -> _uiState.update { it.copy(isLoading = false, codeSentTo = email) }
                is ApiResult.Error -> _uiState.update { it.copy(isLoading = false, errorMessage = result.message) }
            }
        }
    }
}
