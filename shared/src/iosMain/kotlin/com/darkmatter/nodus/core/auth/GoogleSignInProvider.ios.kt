package com.darkmatter.nodus.core.auth

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import io.ktor.client.HttpClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import platform.AuthenticationServices.ASWebAuthenticationPresentationContextProvidingProtocol
import platform.AuthenticationServices.ASWebAuthenticationSession
import platform.AuthenticationServices.ASWebAuthenticationSessionErrorDomain
import platform.Foundation.NSBundle
import platform.Foundation.NSError
import platform.Foundation.NSURL
import platform.Foundation.NSURLComponents
import platform.Foundation.NSURLQueryItem
import platform.UIKit.UIApplication
import platform.UIKit.UIWindow
import platform.UIKit.UIWindowScene
import platform.darwin.NSObject
import kotlin.coroutines.resume

@Composable
actual fun rememberGoogleSignInProvider(): GoogleSignInProvider = remember {
    // Ключ задається в Info.plist з Secrets.xcconfig (не в git)
    val clientId = NSBundle.mainBundle.objectForInfoDictionaryKey("GoogleIOSClientID") as? String
    IosGoogleSignInProvider(clientId.orEmpty().trim())
}

/**
 * Вхід через Google на iOS: системне вікно входу (ASWebAuthenticationSession) без сторонніх SDK.
 * Google повертає код на схему reversed client ID, далі код обмінюється на ID token без client secret.
 */
class IosGoogleSignInProvider(private val clientId: String) : GoogleSignInProvider {

    override val isSupported: Boolean = true

    // Сесія тримає провайдер вікна слабким посиланням, тож тримаємо його самі на час входу
    private var contextProvider: PresentationContextProvider? = null

    override suspend fun getIdToken(): GoogleSignInResult {
        if (clientId.isEmpty()) {
            return GoogleSignInResult.Error("Google sign-in is not configured: iOS client ID is missing")
        }

        // "<цифри>-<рядок>.apps.googleusercontent.com" -> "com.googleusercontent.apps.<цифри>-<рядок>"
        val scheme = "com.googleusercontent.apps." + clientId.removeSuffix(".apps.googleusercontent.com")
        val redirectUri = "$scheme:/oauth2redirect"
        val pkce = GoogleOAuth.createPkce()
        val state = GoogleOAuth.newState()
        val authUrl = NSURL.URLWithString(GoogleOAuth.authorizationUrl(clientId, redirectUri, pkce.challenge, state))
            ?: return GoogleSignInResult.Error("Google sign-in failed: invalid request")

        val callbackUrl = when (val outcome = withContext(Dispatchers.Main) { authenticate(authUrl, scheme) }) {
            is SessionOutcome.Redirected -> outcome.url
            is SessionOutcome.Finished -> return outcome.result
        }

        return when (val result = GoogleOAuth.readCallback(queryParams(callbackUrl), state)) {
            is GoogleOAuth.CallbackResult.Done -> result.result
            is GoogleOAuth.CallbackResult.Code -> HttpClient().use { http ->
                GoogleOAuth.exchangeCode(http, clientId, clientSecret = null, result.code, redirectUri, pkce.verifier)
            }
        }
    }

    private suspend fun authenticate(authUrl: NSURL, scheme: String): SessionOutcome =
        suspendCancellableCoroutine { continuation ->
            val provider = PresentationContextProvider()
            contextProvider = provider

            val session = ASWebAuthenticationSession(authUrl, scheme) { url: NSURL?, error: NSError? ->
                contextProvider = null
                val outcome = when {
                    url != null -> SessionOutcome.Redirected(url)
                    error.isCanceledLogin() -> SessionOutcome.Finished(GoogleSignInResult.Cancelled)
                    else -> SessionOutcome.Finished(GoogleSignInResult.Error("Google sign-in failed. Try again"))
                }
                if (continuation.isActive) continuation.resume(outcome)
            }
            session.presentationContextProvider = provider

            continuation.invokeOnCancellation {
                session.cancel()
                contextProvider = null
            }

            if (!session.start()) {
                contextProvider = null
                continuation.resume(SessionOutcome.Finished(GoogleSignInResult.Error("Can't open Google sign-in")))
            }
        }

    private fun NSError?.isCanceledLogin(): Boolean =
        this != null && domain == ASWebAuthenticationSessionErrorDomain && code == CANCELED_LOGIN_CODE

    private fun queryParams(url: NSURL): Map<String, String> =
        NSURLComponents(url, false).queryItems.orEmpty()
            .filterIsInstance<NSURLQueryItem>()
            .associate { it.name to it.value.orEmpty() }

    private sealed interface SessionOutcome {
        class Redirected(val url: NSURL) : SessionOutcome
        class Finished(val result: GoogleSignInResult) : SessionOutcome
    }

    private class PresentationContextProvider : NSObject(), ASWebAuthenticationPresentationContextProvidingProtocol {
        override fun presentationAnchorForWebAuthenticationSession(session: ASWebAuthenticationSession): UIWindow =
            UIApplication.sharedApplication.connectedScenes
                .filterIsInstance<UIWindowScene>()
                .firstNotNullOfOrNull { it.keyWindow }
                ?: UIWindow()
    }

    private companion object {
        // ASWebAuthenticationSessionErrorCodeCanceledLogin: користувач закрив вікно входу
        const val CANCELED_LOGIN_CODE = 1L
    }
}
