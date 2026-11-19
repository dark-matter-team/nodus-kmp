package com.darkmatter.nodus.core.network

import com.darkmatter.nodus.core.config.getBaseUrl
import com.darkmatter.nodus.core.session.SessionManager
import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.plugins.logging.SIMPLE
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

/**
 * [engine] і [logger] замінюються лише в тестах; за замовчуванням використовується
 * платформний рушій (OkHttp / Darwin) і вивід у консоль.
 */
fun createHttpClient(
    sessionManager: SessionManager,
    engine: HttpClientEngine? = null,
    logger: Logger = Logger.SIMPLE,
): HttpClient {
    val config: HttpClientConfig<*>.() -> Unit = { configure(sessionManager, logger) }
    return if (engine != null) HttpClient(engine, config) else HttpClient(config)
}

private fun HttpClientConfig<*>.configure(sessionManager: SessionManager, httpLogger: Logger) {
    install(ContentNegotiation){
        json(Json { ignoreUnknownKeys = true })
    }
    install(HttpTimeout) {
        requestTimeoutMillis = 15_000
        connectTimeoutMillis = 15_000
        socketTimeoutMillis = 15_000
    }
    defaultRequest {
        url(getBaseUrl())
        contentType(ContentType.Application.Json)
    }
    install(BearerAuthPlugin) {
        tokenProvider = { sessionManager.accessToken }
        onUnauthorized = { sessionManager.endSession() }
    }
    install(Logging) {
        // HEADERS без тіл: у тілах auth-запитів і відповідей є паролі, токени та коди відновлення
        level = LogLevel.HEADERS
        logger = httpLogger
        sanitizeHeader { header -> header == HttpHeaders.Authorization }
    }
    expectSuccess = true
}
