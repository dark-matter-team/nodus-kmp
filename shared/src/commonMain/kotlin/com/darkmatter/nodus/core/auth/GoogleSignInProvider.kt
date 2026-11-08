package com.darkmatter.nodus.core.auth

import androidx.compose.runtime.Composable

sealed interface GoogleSignInResult {
    data class Success(val idToken: String) : GoogleSignInResult {
        // idToken не має потрапити в логи через випадковий toString()
        override fun toString(): String = "Success(idToken=***)"
    }

    /** Користувач закрив вибір акаунта. Це не помилка, повідомлення не показуємо. */
    data object Cancelled : GoogleSignInResult

    data class Error(val message: String) : GoogleSignInResult
}

/** Отримує Google ID token на пристрої; обмін на токен бекенда робить AuthRepository. */
interface GoogleSignInProvider {
    /** false означає, що на цій платформі Google-вхід не підтримується і кнопку не показуємо. */
    val isSupported: Boolean

    suspend fun getIdToken(): GoogleSignInResult
}

/** Реалізація для платформ, де Google-вхід ще не зроблено (Desktop, iOS). */
object UnsupportedGoogleSignInProvider : GoogleSignInProvider {
    override val isSupported: Boolean = false

    override suspend fun getIdToken(): GoogleSignInResult =
        GoogleSignInResult.Error("Вхід через Google недоступний на цій платформі")
}

/**
 * Провайдер, прив'язаний до поточного екрана: на Android йому потрібна Activity,
 * щоб показати вибір акаунта. Передається у ViewModel лише як аргумент виклику.
 */
@Composable
expect fun rememberGoogleSignInProvider(): GoogleSignInProvider
