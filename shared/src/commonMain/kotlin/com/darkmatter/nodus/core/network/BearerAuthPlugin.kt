package com.darkmatter.nodus.core.network

import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.plugins.api.Send
import io.ktor.client.plugins.api.createClientPlugin
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.bearerAuth
import io.ktor.client.statement.HttpResponse
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.encodedPath

class BearerAuthConfig {
    var tokenProvider: () -> String? = { null }
    var onUnauthorized: () -> Unit = {}
}

/**
 * Додає Authorization: Bearer до запитів (крім ендпоінтів /api/auth/...) і завершує сесію,
 * якщо запит із токеном отримав 401. Refresh-токенів бекенд не видає,
 * тож після 401 єдиний вихід — повторний вхід.
 */
val BearerAuthPlugin = createClientPlugin("BearerAuthPlugin", ::BearerAuthConfig) {
    val tokenProvider = pluginConfig.tokenProvider
    val onUnauthorized = pluginConfig.onUnauthorized

    fun checkUnauthorized(request: HttpRequestBuilder, response: HttpResponse) {
        if (response.status == HttpStatusCode.Unauthorized &&
            request.headers.contains(HttpHeaders.Authorization)
        ) {
            onUnauthorized()
        }
    }

    onRequest { request, _ ->
        val isAuthEndpoint = request.url.encodedPath.startsWith("/api/auth/")
        if (!isAuthEndpoint && !request.headers.contains(HttpHeaders.Authorization)) {
            tokenProvider()?.let { request.bearerAuth(it) }
        }
    }

    // При expectSuccess = true 401 може дійти сюди як відповідь або як виняток,
    // залежно від порядку плагінів, тому обробляємо обидва варіанти.
    on(Send) { request ->
        try {
            proceed(request).also { call -> checkUnauthorized(request, call.response) }
        } catch (e: ClientRequestException) {
            checkUnauthorized(request, e.response)
            throw e
        }
    }
}
