package com.darkmatter.nodus.ui.screens.auth

import com.darkmatter.nodus.core.auth.GoogleSignInProvider
import com.darkmatter.nodus.core.auth.GoogleSignInResult
import com.darkmatter.nodus.core.network.ApiResult
import com.darkmatter.nodus.core.network.ErrorKind
import com.darkmatter.nodus.data.model.UserDto
import com.darkmatter.nodus.data.repository.AuthRepository

/**
 * Повний Google-вхід: ID token з пристрою, потім обмін на токен бекенда.
 * Повертає null, якщо користувач скасував вибір акаунта: тоді нічого не показуємо.
 */
internal suspend fun AuthRepository.signInWithGoogle(provider: GoogleSignInProvider): ApiResult<UserDto>? =
    when (val result = provider.getIdToken()) {
        is GoogleSignInResult.Success -> loginWithGoogle(result.idToken)
        is GoogleSignInResult.Cancelled -> null
        // Помилка самого входу Google (до запиту на бекенд): її текст показується як є
        is GoogleSignInResult.Error -> ApiResult.Error(kind = ErrorKind.Unknown, message = result.message)
    }
