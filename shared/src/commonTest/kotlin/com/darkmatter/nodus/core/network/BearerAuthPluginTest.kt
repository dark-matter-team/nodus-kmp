package com.darkmatter.nodus.core.network

import com.darkmatter.nodus.testutil.AuthTestEnvironment
import com.darkmatter.nodus.testutil.FakeResponse
import com.darkmatter.nodus.testutil.errorJson
import com.darkmatter.nodus.testutil.ok
import com.darkmatter.nodus.testutil.unauthorized
import com.darkmatter.nodus.testutil.userJson
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertTrue

class BearerAuthPluginTest {

    @Test
    fun protectedRequestCarriesBearerToken() = runBlocking {
        val env = AuthTestEnvironment { FakeResponse(ok, userJson()) }
        env.sessionManager.startSession("token-123")

        val result = env.repository.getCurrentUser()

        assertIs<ApiResult.Success<*>>(result)
        assertEquals("Bearer token-123", env.engine.requests.single().headers[HttpHeaders.Authorization])
    }

    @Test
    fun authEndpointsAreSentWithoutToken() = runBlocking {
        val env = AuthTestEnvironment { FakeResponse(unauthorized, errorJson("Невірний email/нікнейм або пароль")) }
        env.sessionManager.startSession("token-123")

        env.repository.login("tester1", "wrong-password")

        assertNull(env.engine.requests.single().headers[HttpHeaders.Authorization])
    }

    @Test
    fun unauthorizedOnRequestWithTokenEndsSession() = runBlocking {
        val env = AuthTestEnvironment { FakeResponse(unauthorized) }
        env.sessionManager.startSession("expired-token")

        val result = env.repository.getCurrentUser()

        assertEquals(401, (result as ApiResult.Error).code)
        assertFalse(env.sessionManager.isLoggedIn.value)
        assertNull(env.tokenStorage.getToken())
    }

    @Test
    fun unauthorizedFromLoginKeepsExistingSession() = runBlocking {
        val env = AuthTestEnvironment { FakeResponse(unauthorized, errorJson("Невірний email/нікнейм або пароль")) }
        env.sessionManager.startSession("token-123")

        val result = env.repository.login("tester1", "wrong-password")

        assertEquals(ApiResult.Error("Невірний email/нікнейм або пароль", 401), result)
        assertTrue(env.sessionManager.isLoggedIn.value)
        assertEquals("token-123", env.tokenStorage.getToken())
    }

    @Test
    fun otherErrorsOnProtectedRequestKeepSession() = runBlocking {
        val env = AuthTestEnvironment { FakeResponse(HttpStatusCode.NotFound, errorJson("Користувача не знайдено")) }
        env.sessionManager.startSession("token-123")

        env.repository.getCurrentUser()

        assertTrue(env.sessionManager.isLoggedIn.value)
    }
}
