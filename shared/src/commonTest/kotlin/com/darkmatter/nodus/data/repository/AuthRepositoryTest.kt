package com.darkmatter.nodus.data.repository

import com.darkmatter.nodus.core.network.ApiResult
import com.darkmatter.nodus.testutil.AuthTestEnvironment
import com.darkmatter.nodus.testutil.FakeResponse
import com.darkmatter.nodus.testutil.authResponseJson
import com.darkmatter.nodus.testutil.badRequest
import com.darkmatter.nodus.testutil.bodyText
import com.darkmatter.nodus.testutil.errorJson
import com.darkmatter.nodus.testutil.ok
import io.ktor.http.HttpStatusCode
import io.ktor.http.encodedPath
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertTrue

class AuthRepositoryTest {

    @Test
    fun serverErrorMessageIsTakenFromErrorField() = runBlocking {
        val env = AuthTestEnvironment {
            FakeResponse(badRequest, errorJson("Користувач з таким email або нікнеймом вже існує"))
        }

        val result = env.repository.register("test1@example.com", "tester1", "Passw0rd!")

        assertEquals(
            ApiResult.Error("Користувач з таким email або нікнеймом вже існує", 400),
            result,
        )
    }

    @Test
    fun errorWithoutJsonBodyFallsBackToStatusCode() = runBlocking {
        val env = AuthTestEnvironment { FakeResponse(HttpStatusCode.InternalServerError, "Internal error") }

        val result = env.repository.forgotPassword("test1@example.com")

        assertEquals(ApiResult.Error("Помилка сервера: 500", 500), result)
    }

    @Test
    fun blankErrorFieldFallsBackToStatusCode() = runBlocking {
        val env = AuthTestEnvironment { FakeResponse(badRequest, errorJson("")) }

        val result = env.repository.resetPassword("test1@example.com", "123456", "Passw0rd!")

        assertEquals(ApiResult.Error("Помилка сервера: 400", 400), result)
    }

    @Test
    fun verifyResetCodeSendsTrimmedEmailAndCode() = runBlocking {
        val env = AuthTestEnvironment { FakeResponse(ok, """{"message":"Code is valid"}""") }

        val result = env.repository.verifyResetCode(" test1@example.com ", " 123456 ")

        assertIs<ApiResult.Success<*>>(result)
        val request = env.engine.requests.single()
        assertEquals("/api/auth/verify-reset-code", request.url.encodedPath)
        val body = Json.parseToJsonElement(request.bodyText()).jsonObject
        assertEquals("test1@example.com", body["email"]?.jsonPrimitive?.content)
        assertEquals("123456", body["code"]?.jsonPrimitive?.content)
    }

    @Test
    fun invalidResetCodeKeepsStatusForScreen() = runBlocking {
        // Зараз бекенд віддає тут {"message"} замість {"error"}: екран орієнтується на код 400, а не на текст
        val env = AuthTestEnvironment { FakeResponse(badRequest, """{"message":"Invalid or expired code"}""") }

        val result = env.repository.verifyResetCode("test1@example.com", "000000")

        assertEquals(400, assertIs<ApiResult.Error>(result).code)
    }

    @Test
    fun networkFailureGivesConnectionMessage() = runBlocking {
        val env = AuthTestEnvironment { throw IllegalStateException("connection refused") }

        val result = env.repository.login("tester1", "Test1234!")

        assertEquals(ApiResult.Error("Не вдалося підключитись до сервера"), result)
    }

    @Test
    fun loginWithNicknameSendsNicknameField() = runBlocking {
        val env = AuthTestEnvironment { FakeResponse(ok, authResponseJson("token-1")) }

        env.repository.login("  tester1 ", "Test1234!")

        val body = Json.parseToJsonElement(env.engine.requests.single().bodyText()).jsonObject
        assertEquals("tester1", body["nickname"]?.jsonPrimitive?.content)
        assertTrue(body["email"] == null || body["email"] == JsonNull)
    }

    @Test
    fun loginWithEmailSendsEmailField() = runBlocking {
        val env = AuthTestEnvironment { FakeResponse(ok, authResponseJson("token-1")) }

        env.repository.login("test1@example.com", "Test1234!")

        val body = Json.parseToJsonElement(env.engine.requests.single().bodyText()).jsonObject
        assertEquals("test1@example.com", body["email"]?.jsonPrimitive?.content)
        assertTrue(body["nickname"] == null || body["nickname"] == JsonNull)
    }

    @Test
    fun successfulLoginSavesTokenAndStartsSession() = runBlocking {
        val env = AuthTestEnvironment { FakeResponse(ok, authResponseJson("token-login")) }

        val result = env.repository.login("tester1", "Test1234!")

        assertIs<ApiResult.Success<*>>(result)
        assertEquals("token-login", env.tokenStorage.getToken())
        assertTrue(env.sessionManager.isLoggedIn.value)
    }

    @Test
    fun successfulRegistrationSavesToken() = runBlocking {
        val env = AuthTestEnvironment { FakeResponse(ok, authResponseJson("token-register", "newbie")) }

        val result = env.repository.register("new@example.com", "newbie", "Passw0rd!")

        assertEquals("newbie", (result as ApiResult.Success).data.nickname)
        assertEquals("token-register", env.tokenStorage.getToken())
    }

    @Test
    fun googleLoginSendsIdTokenAndSavesBackendToken() = runBlocking {
        val env = AuthTestEnvironment { FakeResponse(ok, authResponseJson("token-google")) }

        val result = env.repository.loginWithGoogle("google-id-token")

        assertIs<ApiResult.Success<*>>(result)
        val request = env.engine.requests.single()
        assertEquals("/api/auth/google", request.url.encodedPath)
        val body = Json.parseToJsonElement(request.bodyText()).jsonObject
        assertEquals("google-id-token", body["idToken"]?.jsonPrimitive?.content)
        assertEquals("token-google", env.tokenStorage.getToken())
        assertTrue(env.sessionManager.isLoggedIn.value)
    }

    @Test
    fun invalidGoogleTokenShowsBackendMessage() = runBlocking {
        val env = AuthTestEnvironment {
            FakeResponse(HttpStatusCode.Unauthorized, errorJson("Недійсний Google токен"))
        }

        val result = env.repository.loginWithGoogle("bad-id-token")

        assertEquals(ApiResult.Error("Недійсний Google токен", 401), result)
        assertNull(env.tokenStorage.getToken())
    }

    @Test
    fun failedLoginDoesNotSaveToken() = runBlocking {
        val env = AuthTestEnvironment {
            FakeResponse(HttpStatusCode.Unauthorized, errorJson("Невірний email/нікнейм або пароль"))
        }

        env.repository.login("tester1", "wrong-password")

        assertNull(env.tokenStorage.getToken())
        assertFalse(env.sessionManager.isLoggedIn.value)
    }

    @Test
    fun logoutRemovesTokenAndEndsSession() = runBlocking {
        val env = AuthTestEnvironment { FakeResponse(ok, authResponseJson("token-login")) }
        env.repository.login("tester1", "Test1234!")

        env.repository.logout()

        assertNull(env.tokenStorage.getToken())
        assertFalse(env.sessionManager.isLoggedIn.value)
    }
}
