package com.darkmatter.nodus.core.auth

import io.ktor.client.HttpClient
import io.ktor.client.request.forms.submitForm
import io.ktor.client.statement.bodyAsText
import io.ktor.http.URLBuilder
import io.ktor.http.isSuccess
import io.ktor.http.parameters
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlin.coroutines.cancellation.CancellationException
import kotlin.io.encoding.Base64

/** SHA-256 і криптостійкі випадкові байти: на кожній платформі через її системні засоби. */
internal expect fun sha256(bytes: ByteArray): ByteArray

internal expect fun secureRandomBytes(size: Int): ByteArray

/**
 * Вхід через Google для Desktop та iOS за схемою OAuth 2.0 для застосунків (authorization code + PKCE):
 * браузер показує вхід Google, повертає код на redirect URI, а застосунок обмінює код на ID token.
 * Android цим не користується: там є Credential Manager.
 */
internal object GoogleOAuth {

    private const val AUTH_ENDPOINT = "https://accounts.google.com/o/oauth2/v2/auth"
    private const val TOKEN_ENDPOINT = "https://oauth2.googleapis.com/token"

    private val base64Url = Base64.UrlSafe.withPadding(Base64.PaddingOption.ABSENT)
    private val json = Json { ignoreUnknownKeys = true }

    /** PKCE (RFC 7636): навіть перехоплений код без verifier обміняти не вийде. */
    class Pkce(val verifier: String, val challenge: String)

    fun createPkce(): Pkce {
        val verifier = base64Url.encode(secureRandomBytes(32))
        return Pkce(verifier, codeChallengeFor(verifier))
    }

    fun codeChallengeFor(verifier: String): String = base64Url.encode(sha256(verifier.encodeToByteArray()))

    /** Випадковий state захищає redirect від чужих відповідей. */
    fun newState(): String = base64Url.encode(secureRandomBytes(16))

    fun authorizationUrl(clientId: String, redirectUri: String, codeChallenge: String, state: String): String =
        URLBuilder(AUTH_ENDPOINT).apply {
            parameters.append("client_id", clientId)
            parameters.append("redirect_uri", redirectUri)
            parameters.append("response_type", "code")
            parameters.append("scope", "openid email profile")
            parameters.append("code_challenge", codeChallenge)
            parameters.append("code_challenge_method", "S256")
            parameters.append("state", state)
            parameters.append("prompt", "select_account")
        }.buildString()

    /**
     * Перевіряє параметри, з якими Google повернув користувача на redirect URI.
     * Повертає код авторизації або результат для екрана (скасування чи помилку).
     */
    fun readCallback(params: Map<String, String>, expectedState: String): CallbackResult {
        val error = params["error"]
        return when {
            error == "access_denied" -> CallbackResult.Done(GoogleSignInResult.Cancelled)
            error != null -> CallbackResult.Done(GoogleSignInResult.Error("Google sign-in failed: $error"))
            params["state"] != expectedState -> CallbackResult.Done(GoogleSignInResult.Error("Google sign-in failed: invalid response"))
            else -> params["code"]?.let { CallbackResult.Code(it) }
                ?: CallbackResult.Done(GoogleSignInResult.Error("Google sign-in failed: no authorization code"))
        }
    }

    sealed interface CallbackResult {
        data class Code(val code: String) : CallbackResult
        data class Done(val result: GoogleSignInResult) : CallbackResult
    }

    /**
     * Обмінює код на ID token. Окремий [client] без токена бекенда: запит іде до Google,
     * і Authorization нашого API туди потрапити не повинен. [clientSecret] є лише в Desktop-клієнта.
     */
    suspend fun exchangeCode(
        client: HttpClient,
        clientId: String,
        clientSecret: String?,
        code: String,
        redirectUri: String,
        codeVerifier: String,
    ): GoogleSignInResult = try {
        val response = client.submitForm(
            url = TOKEN_ENDPOINT,
            formParameters = parameters {
                append("grant_type", "authorization_code")
                append("code", code)
                append("client_id", clientId)
                if (clientSecret != null) append("client_secret", clientSecret)
                append("redirect_uri", redirectUri)
                append("code_verifier", codeVerifier)
            },
        )
        val body = json.decodeFromString<TokenResponse>(response.bodyAsText())
        val idToken = body.idToken
        when {
            response.status.isSuccess() && idToken != null -> GoogleSignInResult.Success(idToken)
            else -> GoogleSignInResult.Error("Google sign-in failed: ${body.error ?: response.status.value}")
        }
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        GoogleSignInResult.Error("Couldn't reach Google. Check your internet connection")
    }

    @Serializable
    private class TokenResponse(
        @SerialName("id_token") val idToken: String? = null,
        val error: String? = null,
    )
}
