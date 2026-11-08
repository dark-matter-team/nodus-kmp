package com.darkmatter.nodus.core.session

import com.darkmatter.nodus.data.storage.TokenStorage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Єдине джерело правди про сесію. HTTP-клієнт бере звідси токен,
 * а AppNavHost стежить за isLoggedIn, щоб після виходу чи 401 відкрити екран входу.
 */
class SessionManager(private val tokenStorage: TokenStorage) {

    private val _isLoggedIn = MutableStateFlow(loadValidToken() != null)
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()

    val accessToken: String?
        get() = tokenStorage.getToken()

    fun startSession(token: String) {
        tokenStorage.saveToken(token)
        _isLoggedIn.value = true
    }

    /** Вихід з акаунта або реакція на 401: токен видаляється, isLoggedIn стає false. */
    fun endSession() {
        tokenStorage.clear()
        _isLoggedIn.value = false
    }

    private fun loadValidToken(): String? {
        val token = tokenStorage.getToken() ?: return null
        if (JwtUtils.isExpired(token)) {
            tokenStorage.clear()
            return null
        }
        return token
    }
}
