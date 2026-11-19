package com.darkmatter.nodus.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.darkmatter.nodus.core.di.AppContainer
import com.darkmatter.nodus.core.network.ApiResult
import com.darkmatter.nodus.data.model.UserDto
import com.darkmatter.nodus.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface SettingsUiState {
    data object Loading : SettingsUiState
    data class Success(val user: UserDto) : SettingsUiState
    data class Error(val message: String) : SettingsUiState
}

class SettingsViewModel(
    private val repository: AuthRepository = AppContainer.authRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow<SettingsUiState>(SettingsUiState.Loading)
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        loadProfile()
    }

    fun retry() = loadProfile()

    /** Перехід на екран входу робить AppNavHost, коли сесія завершується. */
    fun logout() = repository.logout()

    // Захищений запит: при протермінованому токені отримаємо 401, і сесія завершиться автоматично
    private fun loadProfile() {
        _uiState.value = SettingsUiState.Loading
        viewModelScope.launch {
            _uiState.value = when (val result = repository.getCurrentUser()) {
                is ApiResult.Success -> SettingsUiState.Success(result.data)
                is ApiResult.Error -> SettingsUiState.Error(result.message)
            }
        }
    }
}
