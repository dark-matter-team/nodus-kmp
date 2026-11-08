package com.darkmatter.nodus.data.model

import kotlinx.serialization.Serializable

@Serializable
data class RegisterRequest(
    val email: String,
    val password: String,
    val nickname: String,
)

/** Бекенд шукає користувача за email, якщо він заданий, інакше за nickname. */
@Serializable
data class LoginRequest(
    val password: String,
    val email: String? = null,
    val nickname: String? = null,
)

@Serializable
data class GoogleAuthRequest(val idToken: String)

@Serializable
data class ForgotPasswordRequest(val email: String)

@Serializable
data class ResetPasswordRequest(
    val email: String,
    val code: String,
    val newPassword: String,
)

@Serializable
data class UserDto(
    val id: String,
    val email: String,
    val nickname: String,
    val avatarUrl: String? = null,
    val createdAt: String? = null,
)

@Serializable
data class AuthResponse(
    val accessToken: String,
    val user: UserDto,
    val refreshToken: String? = null,
)

@Serializable
data class MessageResponse(val message: String? = null)

/** Тіло будь-якої помилки бекенда: {"error": "текст українською"}. */
@Serializable
data class ErrorResponse(val error: String? = null)
