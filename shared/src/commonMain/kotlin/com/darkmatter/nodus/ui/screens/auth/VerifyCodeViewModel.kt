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

data class VerifyCodeUiState(
    val email: String,
    val code: String = "",
    val isLoading: Boolean = false,
    /** Код не підійшов: клітинки червоні, під ними [errorMessage]. */
    val isCodeInvalid: Boolean = false,
    val errorMessage: String? = null,
    /** Повідомлення про повторно надісланий код. */
    val infoMessage: String? = null,
    /** Перевірений код; не null означає, що можна переходити до нового пароля. */
    val verifiedCode: String? = null,
)

class VerifyCodeViewModel(
    email: String,
    private val repository: AuthRepository = AppContainer.authRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(VerifyCodeUiState(email = email))
    val uiState: StateFlow<VerifyCodeUiState> = _uiState.asStateFlow()

    /** Код перевіряється одразу, щойно введено всі цифри: кнопки підтвердження в макеті немає. */
    fun onCodeChange(value: String) {
        if (_uiState.value.isLoading) return
        val digits = value.filter { it.isDigit() }.take(AuthValidation.RESET_CODE_LENGTH)
        _uiState.update {
            it.copy(code = digits, isCodeInvalid = false, errorMessage = null, infoMessage = null)
        }
        if (digits.length == AuthValidation.RESET_CODE_LENGTH) verify(digits)
    }

    /** Викликається після переходу на наступний екран, щоб при поверненні "назад" не перейти знову. */
    fun onNavigated() = _uiState.update { it.copy(verifiedCode = null) }

    fun resendCode() {
        val state = _uiState.value
        if (state.isLoading) return

        _uiState.update { it.copy(isLoading = true, isCodeInvalid = false, errorMessage = null, infoMessage = null) }
        viewModelScope.launch {
            when (val result = repository.forgotPassword(state.email)) {
                is ApiResult.Success -> _uiState.update {
                    it.copy(isLoading = false, code = "", infoMessage = "We sent a new code")
                }
                is ApiResult.Error -> {
                    val message = result.userMessage()
                    _uiState.update { it.copy(isLoading = false, errorMessage = message) }
                }
            }
        }
    }

    private fun verify(code: String) {
        val email = _uiState.value.email
        _uiState.update { it.copy(isLoading = true) }
        viewModelScope.launch {
            when (val result = repository.verifyResetCode(email, code)) {
                is ApiResult.Success -> _uiState.update { it.copy(isLoading = false, verifiedCode = code) }
                is ApiResult.Error -> {
                    // 400 означає неправильний або прострочений код: текст з макета, клітинки порожні
                    val invalid = result.code == HTTP_BAD_REQUEST
                    val message = if (invalid) INVALID_CODE_MESSAGE else result.userMessage()
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            code = if (invalid) "" else it.code,
                            isCodeInvalid = invalid,
                            errorMessage = message,
                        )
                    }
                }
            }
        }
    }

    companion object {
        const val INVALID_CODE_MESSAGE = "Invalid or expired code"
        private const val HTTP_BAD_REQUEST = 400
    }
}
