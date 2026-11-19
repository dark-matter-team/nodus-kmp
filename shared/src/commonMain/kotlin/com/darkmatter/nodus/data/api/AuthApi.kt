package com.darkmatter.nodus.data.api

import com.darkmatter.nodus.data.model.AuthResponse
import com.darkmatter.nodus.data.model.ForgotPasswordRequest
import com.darkmatter.nodus.data.model.GoogleAuthRequest
import com.darkmatter.nodus.data.model.LoginRequest
import com.darkmatter.nodus.data.model.MessageResponse
import com.darkmatter.nodus.data.model.RegisterRequest
import com.darkmatter.nodus.data.model.ResetPasswordRequest
import com.darkmatter.nodus.data.model.UserDto
import com.darkmatter.nodus.data.model.VerifyResetCodeRequest
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody

class AuthApi(private val client: HttpClient) {

    suspend fun register(request: RegisterRequest): AuthResponse =
        client.post("/api/auth/register") { setBody(request) }.body()

    suspend fun login(request: LoginRequest): AuthResponse =
        client.post("/api/auth/login") { setBody(request) }.body()

    suspend fun googleLogin(request: GoogleAuthRequest): AuthResponse =
        client.post("/api/auth/google") { setBody(request) }.body()

    suspend fun forgotPassword(request: ForgotPasswordRequest): MessageResponse =
        client.post("/api/auth/forgot-password") { setBody(request) }.body()

    /** Лише перевіряє код відновлення, пароль не змінює. */
    suspend fun verifyResetCode(request: VerifyResetCodeRequest): MessageResponse =
        client.post("/api/auth/verify-reset-code") { setBody(request) }.body()

    suspend fun resetPassword(request: ResetPasswordRequest): MessageResponse =
        client.post("/api/auth/reset-password") { setBody(request) }.body()

    /** Захищений ендпоінт: потребує заголовка Authorization: Bearer. */
    suspend fun getCurrentUser(): UserDto =
        client.get("/api/users/me").body()
}
