package com.darkmatter.nodus.core.network

import com.darkmatter.nodus.testutil.AuthTestEnvironment
import com.darkmatter.nodus.testutil.FakeResponse
import com.darkmatter.nodus.testutil.authResponseJson
import com.darkmatter.nodus.testutil.ok
import com.darkmatter.nodus.testutil.userJson
import io.ktor.http.encodedPath
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class HttpLoggingTest {

    private val password = "Secret-Pass-42"
    private val newPassword = "Brand-New-Pass-77"
    private val token = "eyJhbGciOiJIUzI1NiJ9.secret-payload.secret-signature"
    private val resetCode = "924615"
    private val googleIdToken = "google-id-token-secret-value"

    @Test
    fun logsContainNoPasswordsTokensOrResetCodes() = runBlocking {
        val env = AuthTestEnvironment { request ->
            when (request.url.encodedPath) {
                "/api/auth/login", "/api/auth/register", "/api/auth/google" ->
                    FakeResponse(ok, authResponseJson(token))
                "/api/users/me" -> FakeResponse(ok, userJson())
                else -> FakeResponse(ok, """{"message":"ok"}""")
            }
        }

        env.repository.register("new@example.com", "newbie", password)
        env.repository.login("tester1", password)
        env.repository.getCurrentUser()
        env.repository.forgotPassword("test1@example.com")
        env.repository.resetPassword("test1@example.com", resetCode, newPassword)
        env.repository.loginWithGoogle(googleIdToken)

        val log = env.logs.joinToString("\n")
        assertTrue(log.contains("/api/users/me"), "логування має працювати:\n$log")
        listOf(password, newPassword, token, resetCode, "secret-payload", googleIdToken).forEach { secret ->
            assertFalse(log.contains(secret), "у логах знайдено '$secret':\n$log")
        }
    }

    @Test
    fun authorizationHeaderIsMasked() = runBlocking {
        val env = AuthTestEnvironment { FakeResponse(ok, userJson()) }
        env.sessionManager.startSession(token)

        env.repository.getCurrentUser()

        val log = env.logs.joinToString("\n")
        assertTrue(log.contains("Authorization: ***"), "заголовок Authorization має бути замаскований:\n$log")
        assertFalse(log.contains(token))
    }
}
