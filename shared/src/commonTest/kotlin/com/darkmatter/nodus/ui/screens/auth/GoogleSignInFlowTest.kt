package com.darkmatter.nodus.ui.screens.auth

import com.darkmatter.nodus.core.auth.GoogleSignInProvider
import com.darkmatter.nodus.core.auth.GoogleSignInResult
import com.darkmatter.nodus.core.network.ApiResult
import com.darkmatter.nodus.testutil.AuthTestEnvironment
import com.darkmatter.nodus.testutil.FakeResponse
import com.darkmatter.nodus.testutil.authResponseJson
import com.darkmatter.nodus.testutil.ok
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertTrue

class GoogleSignInFlowTest {

    private class FakeGoogleSignInProvider(private val result: GoogleSignInResult) : GoogleSignInProvider {
        override val isSupported: Boolean = true
        override suspend fun getIdToken(): GoogleSignInResult = result
    }

    @Test
    fun cancelledSelectionIsSilentAndSendsNothing() = runBlocking {
        val env = AuthTestEnvironment { FakeResponse(ok, authResponseJson("token")) }

        val result = env.repository.signInWithGoogle(FakeGoogleSignInProvider(GoogleSignInResult.Cancelled))

        assertNull(result)
        assertTrue(env.engine.requests.isEmpty())
        assertFalse(env.sessionManager.isLoggedIn.value)
    }

    @Test
    fun providerErrorIsShownAndSendsNothing() = runBlocking {
        val env = AuthTestEnvironment { FakeResponse(ok, authResponseJson("token")) }
        val message = "No Google account on this device. Add one in the device settings and try again"

        val result = env.repository.signInWithGoogle(FakeGoogleSignInProvider(GoogleSignInResult.Error(message)))

        assertEquals(ApiResult.Error(message), result)
        assertTrue(env.engine.requests.isEmpty())
    }

    @Test
    fun idTokenIsExchangedForBackendSession() = runBlocking {
        val env = AuthTestEnvironment { FakeResponse(ok, authResponseJson("token-google")) }

        val result = env.repository.signInWithGoogle(
            FakeGoogleSignInProvider(GoogleSignInResult.Success("google-id-token")),
        )

        assertIs<ApiResult.Success<*>>(result)
        assertEquals("token-google", env.tokenStorage.getToken())
    }

    @Test
    fun successToStringHidesIdToken() {
        assertFalse(GoogleSignInResult.Success("google-id-token").toString().contains("google-id-token"))
    }
}
