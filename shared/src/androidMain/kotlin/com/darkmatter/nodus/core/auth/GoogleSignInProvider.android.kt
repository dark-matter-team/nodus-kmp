package com.darkmatter.nodus.core.auth

import android.content.Context
import android.content.pm.ApplicationInfo
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.platform.LocalContext
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import androidx.credentials.exceptions.NoCredentialException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException

/**
 * Web client ID (той самий, що Google:ClientId на бекенді). Задається в androidApp з BuildConfig,
 * бо shared як KMP-бібліотека не має доступу до local.properties застосунку.
 */
val LocalGoogleWebClientId = staticCompositionLocalOf { "" }

@Composable
actual fun rememberGoogleSignInProvider(): GoogleSignInProvider {
    // У Compose всередині ComponentActivity LocalContext є самою Activity
    val context = LocalContext.current
    val webClientId = LocalGoogleWebClientId.current
    return remember(context, webClientId) { AndroidGoogleSignInProvider(context, webClientId) }
}

class AndroidGoogleSignInProvider(
    private val activityContext: Context,
    private val webClientId: String,
) : GoogleSignInProvider {

    override val isSupported: Boolean = true

    override suspend fun getIdToken(): GoogleSignInResult {
        if (webClientId.isBlank()) {
            return GoogleSignInResult.Error("Вхід через Google не налаштовано: не задано Web client ID")
        }

        val googleIdOption = GetGoogleIdOption.Builder()
            .setServerClientId(webClientId)
            .setFilterByAuthorizedAccounts(false)
            .build()
        val request = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()

        // TEMP: діагностика Google-входу, прибрати після з'ясування причини помилки
        debugLog("serverClientId length=${webClientId.length} prefix=${webClientId.take(12)}")

        return try {
            val credential = CredentialManager.create(activityContext)
                .getCredential(activityContext, request)
                .credential
            if (credential is CustomCredential &&
                credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
            ) {
                GoogleSignInResult.Success(GoogleIdTokenCredential.createFrom(credential.data).idToken)
            } else {
                debugLog("unsupported credential: class=${credential::class.java.name} type=${credential.type}") // TEMP
                GoogleSignInResult.Error("Отримано непідтримуваний тип облікових даних")
            }
        } catch (e: GetCredentialCancellationException) {
            logFailure(e) // TEMP
            GoogleSignInResult.Cancelled
        } catch (e: NoCredentialException) {
            logFailure(e) // TEMP
            GoogleSignInResult.Error(
                "На пристрої немає Google-акаунта. Додайте акаунт у налаштуваннях пристрою і спробуйте ще раз",
            )
        } catch (e: GoogleIdTokenParsingException) {
            logFailure(e) // TEMP
            GoogleSignInResult.Error("Не вдалося прочитати відповідь Google")
        } catch (e: GetCredentialException) {
            logFailure(e) // TEMP
            GoogleSignInResult.Error("Не вдалося увійти через Google. Спробуйте ще раз")
        }
    }

    // TEMP: діагностика Google-входу, лише для debug-збірки. Не логує idToken і токен бекенда.
    private val isDebuggable: Boolean =
        (activityContext.applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE) != 0

    // TEMP
    private fun debugLog(message: String) {
        if (isDebuggable) Log.w(DIAGNOSTICS_TAG, message)
    }

    // TEMP: виняток і весь ланцюжок cause; для GetCredentialException також type і errorMessage
    private fun logFailure(error: Throwable) {
        if (!isDebuggable) return
        var current: Throwable? = error
        var depth = 0
        while (current != null && depth < MAX_CAUSE_DEPTH) {
            val credentialDetails = if (current is GetCredentialException) {
                " type=${current.type} errorMessage=${current.errorMessage}"
            } else {
                ""
            }
            Log.w(DIAGNOSTICS_TAG, "[$depth] ${current::class.java.name}: message=${current.message}$credentialDetails")
            current = current.cause.takeIf { it !== current }
            depth++
        }
    }

    // TEMP
    private companion object {
        const val DIAGNOSTICS_TAG = "GoogleSignIn"
        const val MAX_CAUSE_DEPTH = 10
    }
}
