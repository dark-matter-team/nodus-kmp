package com.darkmatter.nodus.core.auth

import com.darkmatter.nodus.testutil.FakeHttpEngine
import com.darkmatter.nodus.testutil.FakeResponse
import io.ktor.client.HttpClient
import io.ktor.http.HttpStatusCode
import io.ktor.http.Url
import io.ktor.http.content.OutgoingContent
import io.ktor.http.parseUrlEncodedParameters
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNotEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class GoogleOAuthTest {

    @Test
    fun codeChallengeMatchesRfc7636Example() {
        // Приклад з додатка B RFC 7636
        assertEquals(
            "E9Melhoa2OwvFrEMTJguCHaoeK1t8URWbuGJSstw-cM",
            GoogleOAuth.codeChallengeFor("dBjftJeZ4CVP-mB92K27uhbUJU1p1r_wW1gFWFOEjXk"),
        )
    }

    @Test
    fun pkceVerifierIsRandomAndUrlSafe() {
        val first = GoogleOAuth.createPkce()
        val second = GoogleOAuth.createPkce()

        assertNotEquals(first.verifier, second.verifier)
        // 32 байти в base64url без доповнення: 43 символи, мінімум за RFC 7636
        assertEquals(43, first.verifier.length)
        assertTrue(first.verifier.all { it.isLetterOrDigit() || it == '-' || it == '_' })
        assertEquals(GoogleOAuth.codeChallengeFor(first.verifier), first.challenge)
    }

    @Test
    fun authorizationUrlCarriesPkceAndState() {
        val url = Url(GoogleOAuth.authorizationUrl("client-id", "http://127.0.0.1:5000", "challenge", "state-1"))

        assertEquals("accounts.google.com", url.host)
        assertEquals("client-id", url.parameters["client_id"])
        assertEquals("http://127.0.0.1:5000", url.parameters["redirect_uri"])
        assertEquals("code", url.parameters["response_type"])
        assertEquals("openid email profile", url.parameters["scope"])
        assertEquals("challenge", url.parameters["code_challenge"])
        assertEquals("S256", url.parameters["code_challenge_method"])
        assertEquals("state-1", url.parameters["state"])
    }

    @Test
    fun callbackWithCodeAndMatchingState() {
        val result = GoogleOAuth.readCallback(mapOf("code" to "auth-code", "state" to "s"), expectedState = "s")

        assertEquals(GoogleOAuth.CallbackResult.Code("auth-code"), result)
    }

    @Test
    fun callbackWithForeignStateIsRejected() {
        val result = GoogleOAuth.readCallback(mapOf("code" to "auth-code", "state" to "other"), expectedState = "s")

        assertIs<GoogleSignInResult.Error>(assertIs<GoogleOAuth.CallbackResult.Done>(result).result)
    }

    @Test
    fun deniedConsentIsCancellation() {
        val result = GoogleOAuth.readCallback(mapOf("error" to "access_denied", "state" to "s"), expectedState = "s")

        assertEquals(GoogleOAuth.CallbackResult.Done(GoogleSignInResult.Cancelled), result)
    }

    @Test
    fun callbackWithoutCodeIsError() {
        val result = GoogleOAuth.readCallback(mapOf("state" to "s"), expectedState = "s")

        assertIs<GoogleSignInResult.Error>(assertIs<GoogleOAuth.CallbackResult.Done>(result).result)
    }

    @Test
    fun codeIsExchangedForIdToken() = runBlocking {
        val engine = FakeHttpEngine { FakeResponse(HttpStatusCode.OK, """{"id_token":"google-id-token","access_token":"a"}""") }

        val result = HttpClient(engine).use {
            GoogleOAuth.exchangeCode(it, "client-id", "secret", "auth-code", "http://127.0.0.1:5000", "verifier")
        }

        assertEquals(GoogleSignInResult.Success("google-id-token"), result)
        val request = engine.requests.single()
        assertEquals("https://oauth2.googleapis.com/token", request.url.toString())
        val form = (request.body as OutgoingContent.ByteArrayContent).bytes().decodeToString().parseUrlEncodedParameters()
        assertEquals("authorization_code", form["grant_type"])
        assertEquals("auth-code", form["code"])
        assertEquals("client-id", form["client_id"])
        assertEquals("secret", form["client_secret"])
        assertEquals("http://127.0.0.1:5000", form["redirect_uri"])
        assertEquals("verifier", form["code_verifier"])
    }

    @Test
    fun iosExchangeSendsNoClientSecret() = runBlocking {
        val engine = FakeHttpEngine { FakeResponse(HttpStatusCode.OK, """{"id_token":"google-id-token"}""") }

        HttpClient(engine).use {
            GoogleOAuth.exchangeCode(it, "client-id", null, "auth-code", "scheme:/oauth2redirect", "verifier")
        }

        val form = (engine.requests.single().body as OutgoingContent.ByteArrayContent).bytes().decodeToString()
            .parseUrlEncodedParameters()
        assertNull(form["client_secret"])
    }

    @Test
    fun rejectedCodeIsError() = runBlocking {
        val engine = FakeHttpEngine { FakeResponse(HttpStatusCode.BadRequest, """{"error":"invalid_grant"}""") }

        val result = HttpClient(engine).use {
            GoogleOAuth.exchangeCode(it, "client-id", "secret", "auth-code", "http://127.0.0.1:5000", "verifier")
        }

        assertEquals(GoogleSignInResult.Error("Google sign-in failed: invalid_grant"), result)
    }

    @Test
    fun networkFailureIsError() = runBlocking {
        val engine = FakeHttpEngine { throw IllegalStateException("offline") }

        val result = HttpClient(engine).use {
            GoogleOAuth.exchangeCode(it, "client-id", "secret", "auth-code", "http://127.0.0.1:5000", "verifier")
        }

        val error = assertIs<GoogleSignInResult.Error>(result)
        assertFalse(error.message.contains("offline"))
    }
}
