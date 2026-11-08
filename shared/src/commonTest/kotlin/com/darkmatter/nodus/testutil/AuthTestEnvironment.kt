package com.darkmatter.nodus.testutil

import com.darkmatter.nodus.core.network.createHttpClient
import com.darkmatter.nodus.core.session.SessionManager
import com.darkmatter.nodus.data.api.AuthApi
import com.darkmatter.nodus.data.repository.AuthRepository
import com.darkmatter.nodus.data.storage.TokenStorage
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.request.HttpRequestData
import io.ktor.http.HttpStatusCode

/** Зібраний як в AppContainer ланцюжок, але з фейковими сховищем і рушієм. */
class AuthTestEnvironment(handler: (HttpRequestData) -> FakeResponse) {
    val logs = mutableListOf<String>()
    val tokenStorage = TokenStorage(InMemorySettings())
    val sessionManager = SessionManager(tokenStorage)
    val engine = FakeHttpEngine(handler)
    private val client = createHttpClient(
        sessionManager = sessionManager,
        engine = engine,
        logger = object : Logger {
            override fun log(message: String) {
                logs += message
            }
        },
    )
    val repository = AuthRepository(AuthApi(client), sessionManager)
}

fun authResponseJson(token: String, nickname: String = "tester1", email: String = "test1@example.com") = """
    {"accessToken":"$token","refreshToken":"",
     "user":{"id":"7f1c0d2e-0000-0000-0000-000000000001","email":"$email","nickname":"$nickname",
             "avatarUrl":null,"createdAt":"2026-11-01T10:00:00Z"}}
""".trimIndent()

fun userJson(nickname: String = "tester1") =
    """{"id":"7f1c0d2e-0000-0000-0000-000000000001","email":"test1@example.com","nickname":"$nickname"}"""

fun errorJson(message: String) = """{"error":"$message"}"""

val ok = HttpStatusCode.OK
val badRequest = HttpStatusCode.BadRequest
val unauthorized = HttpStatusCode.Unauthorized
