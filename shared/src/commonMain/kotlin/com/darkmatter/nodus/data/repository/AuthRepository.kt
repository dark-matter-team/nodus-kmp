package com.darkmatter.nodus.data.repository

import com.darkmatter.nodus.core.network.ApiResult
import com.darkmatter.nodus.core.session.SessionManager
import com.darkmatter.nodus.data.api.AuthApi
import com.darkmatter.nodus.data.model.AuthResponse
import com.darkmatter.nodus.data.model.ErrorResponse
import com.darkmatter.nodus.data.model.ForgotPasswordRequest
import com.darkmatter.nodus.data.model.GoogleAuthRequest
import com.darkmatter.nodus.data.model.LoginRequest
import com.darkmatter.nodus.data.model.RegisterRequest
import com.darkmatter.nodus.data.model.ResetPasswordRequest
import com.darkmatter.nodus.data.model.UserDto
import io.ktor.client.plugins.ResponseException
import io.ktor.client.statement.bodyAsText
import kotlinx.serialization.json.Json
import kotlin.coroutines.cancellation.CancellationException

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
        safeCall { api.forgotPassword(ForgotPasswordRequest(email.trim())).message }

    suspend fun resetPassword(email: String, code: String, newPassword: String): ApiResult<String?> =
        safeCall { api.resetPassword(ResetPasswordRequest(email.trim(), code.trim(), newPassword)).message }

    suspend fun getCurrentUser(): ApiResult<UserDto> = safeCall { api.getCurrentUser() }

    fun logout() = sessionManager.endSession()

    /** Успішна відповідь з токеном одразу відкриває сесію. */
    private suspend fun authCall(block: suspend () -> AuthResponse): ApiResult<UserDto> =
        when (val result = safeCall(block)) {
            is ApiResult.Success -> {
                sessionManager.startSession(result.data.accessToken)
                ApiResult.Success(result.data.user)
            }
            is ApiResult.Error -> result
        }

    private suspend fun <T> safeCall(block: suspend () -> T): ApiResult<T> {
        return try {
            ApiResult.Success(block())
        } catch (e: CancellationException) {
            throw e
        } catch (e: ResponseException) {
            val code = e.response.status.value
            ApiResult.Error(e.serverMessage() ?: "Помилка сервера: $code", code)
        } catch (e: Exception) {
            ApiResult.Error("Не вдалося підключитись до сервера")
        }
    }

    /** Бекенд повертає {"error": "..."} українською, цей текст можна показати користувачу. */
    private suspend fun ResponseException.serverMessage(): String? = try {
        json.decodeFromString<ErrorResponse>(response.bodyAsText()).error?.takeIf { it.isNotBlank() }
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        null
    }

    private companion object {
        val json = Json { ignoreUnknownKeys = true }
    }
}
