package com.darkmatter.nodus.ui.screens.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.darkmatter.nodus.core.auth.GoogleSignInProvider
import com.darkmatter.nodus.core.di.AppContainer
import com.darkmatter.nodus.core.network.ApiResult
import com.darkmatter.nodus.data.repository.AuthRepository
import com.darkmatter.nodus.ui.common.userMessage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** Форма тримає введені значення в будь-якому стані, тому це data class, а не sealed interface. */
data class LoginUiState(
    val login: String = "",
    val password: String = "",
    val loginError: String? = null,
    val passwordError: String? = null,
    val isLoading: Boolean = false,
    val isGoogleLoading: Boolean = false,
    val errorMessage: String? = null,
    val isLoggedIn: Boolean = false,
) {
    /** Поки йде будь-який запит, форма і обидві кнопки заблоковані. */
    val isBusy: Boolean get() = isLoading || isGoogleLoading
}

class LoginViewModel(
    private val repository: AuthRepository = AppContainer.authRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun onLoginChange(value: String) =
        _uiState.update { it.copy(login = value, loginError = null, errorMessage = null) }

    fun onPasswordChange(value: String) =
        _uiState.update { it.copy(password = value, passwordError = null, errorMessage = null) }

    fun submit() {
        val state = _uiState.value
        if (state.isBusy) return

        val loginError = AuthValidation.validateLogin(state.login)
        val passwordError = AuthValidation.validateLoginPassword(state.password)
        if (loginError != null || passwordError != null) {
            _uiState.update { it.copy(loginError = loginError, passwordError = passwordError) }
            return
        }

        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        viewModelScope.launch {
            when (val result = repository.login(state.login, state.password)) {
                is ApiResult.Success -> _uiState.update { it.copy(isLoading = false, isLoggedIn = true) }
                is ApiResult.Error -> {
                    val message = result.userMessage()
                    _uiState.update { it.copy(isLoading = false, errorMessage = message) }
                }
            }
        }
    }

    /** [provider] прив'язаний до екрана (Activity), тому передається у виклик, а не зберігається. */
    fun signInWithGoogle(provider: GoogleSignInProvider) {
        if (_uiState.value.isBusy) return

        _uiState.update { it.copy(isGoogleLoading = true, errorMessage = null) }
        viewModelScope.launch {
            when (val result = repository.signInWithGoogle(provider)) {
                null -> _uiState.update { it.copy(isGoogleLoading = false) }
                is ApiResult.Success -> _uiState.update { it.copy(isGoogleLoading = false, isLoggedIn = true) }
                is ApiResult.Error -> {
                    val message = result.userMessage()
                    _uiState.update { it.copy(isGoogleLoading = false, errorMessage = message) }
                }
            }
        }
    }
}
