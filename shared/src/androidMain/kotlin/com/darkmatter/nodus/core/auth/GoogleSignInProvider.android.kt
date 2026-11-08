package com.darkmatter.nodus.core.auth

import android.content.Context
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

        return try {
            val credential = CredentialManager.create(activityContext)
                .getCredential(activityContext, request)
                .credential
            if (credential is CustomCredential &&
                credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
            ) {
                GoogleSignInResult.Success(GoogleIdTokenCredential.createFrom(credential.data).idToken)
            } else {
                GoogleSignInResult.Error("Отримано непідтримуваний тип облікових даних")
            }
        } catch (e: GetCredentialCancellationException) {
            GoogleSignInResult.Cancelled
        } catch (e: NoCredentialException) {
            GoogleSignInResult.Error(
                "На пристрої немає Google-акаунта. Додайте акаунт у налаштуваннях пристрою і спробуйте ще раз",
            )
        } catch (e: GoogleIdTokenParsingException) {
            GoogleSignInResult.Error("Не вдалося прочитати відповідь Google")
        } catch (e: GetCredentialException) {
            GoogleSignInResult.Error("Не вдалося увійти через Google. Спробуйте ще раз")
        }
    }
}
