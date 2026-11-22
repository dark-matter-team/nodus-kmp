package com.darkmatter.nodus.data.repository

import com.darkmatter.nodus.core.network.ApiResult
import com.darkmatter.nodus.core.network.apiCall
import com.darkmatter.nodus.core.session.SessionManager
import com.darkmatter.nodus.data.api.AuthApi
import com.darkmatter.nodus.data.model.AuthResponse
import com.darkmatter.nodus.data.model.ForgotPasswordRequest
import com.darkmatter.nodus.data.model.GoogleAuthRequest
import com.darkmatter.nodus.data.model.LoginRequest
import com.darkmatter.nodus.data.model.RegisterRequest
import com.darkmatter.nodus.data.model.ResetPasswordRequest
import com.darkmatter.nodus.data.model.UserDto
import com.darkmatter.nodus.data.model.VerifyResetCodeRequest

class AuthRepository(
    private val api: AuthApi,
    private val sessionManager: SessionManager,
) {

    /** [login] може бути email або нікнеймом: нікнейм не може містити '@', тож розрізняємо за ним. */
    suspend fun login(login: String, password: String): ApiResult<UserDto> {
        val trimmed = login.trim()
        val request = if ('@' in trimmed) {
            LoginRequest(password = password, email = trimmed)
        } else {
            LoginRequest(password = password, nickname = trimmed)
        }
        return authCall { api.login(request) }
    }

    suspend fun register(email: String, nickname: String, password: String): ApiResult<UserDto> =
        authCall { api.register(RegisterRequest(email.trim(), password, nickname.trim())) }

    /**
     * Обмінює Google ID token на токен бекенда. Новий користувач створюється автоматично.
     * 401 "Недійсний Google токен" повертається як ApiResult.Error з текстом бекенда.
     */
    suspend fun loginWithGoogle(idToken: String): ApiResult<UserDto> =
        authCall { api.googleLogin(GoogleAuthRequest(idToken)) }

    suspend fun forgotPassword(email: String): ApiResult<String?> =
        apiCall { api.forgotPassword(ForgotPasswordRequest(email.trim())).message }

    /** Перевіряє код з листа до введення нового пароля; сам код лишається дійсним для [resetPassword]. */
    suspend fun verifyResetCode(email: String, code: String): ApiResult<String?> =
        apiCall { api.verifyResetCode(VerifyResetCodeRequest(email.trim(), code.trim())).message }

    suspend fun resetPassword(email: String, code: String, newPassword: String): ApiResult<String?> =
        apiCall { api.resetPassword(ResetPasswordRequest(email.trim(), code.trim(), newPassword)).message }

    suspend fun getCurrentUser(): ApiResult<UserDto> = apiCall { api.getCurrentUser() }

    fun logout() = sessionManager.endSession()

    /** Успішна відповідь з токеном одразу відкриває сесію. */
    private suspend fun authCall(block: suspend () -> AuthResponse): ApiResult<UserDto> =
        when (val result = apiCall(block)) {
            is ApiResult.Success -> {
                sessionManager.startSession(result.data.accessToken)
                ApiResult.Success(result.data.user)
            }
            is ApiResult.Error -> result
        }
}
