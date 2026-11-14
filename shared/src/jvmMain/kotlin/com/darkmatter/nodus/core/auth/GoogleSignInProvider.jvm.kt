package com.darkmatter.nodus.core.auth

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import com.sun.net.httpserver.HttpExchange
import com.sun.net.httpserver.HttpServer
import io.ktor.client.HttpClient
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import java.awt.Desktop
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.URI
import java.net.URLDecoder
import kotlin.time.Duration.Companion.minutes

/**
 * OAuth-клієнт Google типу "Desktop app". Значення приходять з local.properties через desktopApp
 * (jvmArgs у build.gradle.kts) і передаються в shared через CompositionLocal.
 */
data class GoogleDesktopClient(val clientId: String, val clientSecret: String)

val LocalGoogleDesktopClient = staticCompositionLocalOf { GoogleDesktopClient(clientId = "", clientSecret = "") }

@Composable
actual fun rememberGoogleSignInProvider(): GoogleSignInProvider {
    val client = LocalGoogleDesktopClient.current
    return remember(client) { DesktopGoogleSignInProvider(client) }
}

/**
 * Вхід через Google на Desktop: системний браузер і локальна адреса 127.0.0.1 на випадковому порту,
 * куди Google повертає користувача з кодом (схема "loopback" для desktop-застосунків).
 */
class DesktopGoogleSignInProvider(private val client: GoogleDesktopClient) : GoogleSignInProvider {

    override val isSupported: Boolean = true

    override suspend fun getIdToken(): GoogleSignInResult {
        if (client.clientId.isBlank() || client.clientSecret.isBlank()) {
            return GoogleSignInResult.Error("Google sign-in is not configured: desktop client ID is missing")
        }
        if (!Desktop.isDesktopSupported() || !Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
            return GoogleSignInResult.Error("Can't open a browser on this computer")
        }

        val pkce = GoogleOAuth.createPkce()
        val state = GoogleOAuth.newState()
        val callback = CompletableDeferred<Map<String, String>>()
        val server = HttpServer.create(InetSocketAddress(InetAddress.getByName(LOOPBACK_HOST), 0), 0).apply {
            createContext("/") { exchange -> handleRedirect(exchange, callback) }
            start()
        }

        try {
            val redirectUri = "http://$LOOPBACK_HOST:${server.address.port}"
            val authUrl = GoogleOAuth.authorizationUrl(client.clientId, redirectUri, pkce.challenge, state)
            withContext(Dispatchers.IO) { Desktop.getDesktop().browse(URI(authUrl)) }

            // Користувач міг просто закрити вкладку: не чекаємо вічно
            val params = withTimeoutOrNull(5.minutes) { callback.await() }
                ?: return GoogleSignInResult.Error("Google sign-in timed out. Try again")

            return when (val result = GoogleOAuth.readCallback(params, state)) {
                is GoogleOAuth.CallbackResult.Done -> result.result
                is GoogleOAuth.CallbackResult.Code -> HttpClient().use { http ->
                    GoogleOAuth.exchangeCode(http, client.clientId, client.clientSecret, result.code, redirectUri, pkce.verifier)
                }
            }
        } finally {
            server.stop(0)
        }
    }

    private fun handleRedirect(exchange: HttpExchange, callback: CompletableDeferred<Map<String, String>>) {
        val params = parseQuery(exchange.requestURI.rawQuery)
        // Браузер може окремо запитати /favicon.ico: такі запити без code/error пропускаємо
        if ("code" in params || "error" in params) callback.complete(params)

        val page = REDIRECT_PAGE.toByteArray(Charsets.UTF_8)
        exchange.responseHeaders.add("Content-Type", "text/html; charset=utf-8")
        exchange.sendResponseHeaders(200, page.size.toLong())
        exchange.responseBody.use { it.write(page) }
    }

    private fun parseQuery(rawQuery: String?): Map<String, String> =
        rawQuery.orEmpty()
            .split('&')
            .filter { it.isNotEmpty() }
            .associate { pair ->
                val key = pair.substringBefore('=')
                val value = pair.substringAfter('=', missingDelimiterValue = "")
                URLDecoder.decode(key, Charsets.UTF_8) to URLDecoder.decode(value, Charsets.UTF_8)
            }

    private companion object {
        const val LOOPBACK_HOST = "127.0.0.1"

        const val REDIRECT_PAGE = """<!doctype html>
<html><head><meta charset="utf-8"><title>Nodus</title></head>
<body style="font-family:sans-serif;background:#010952;color:#D0D9FF;text-align:center;padding-top:20vh">
<h2>You can return to Nodus</h2><p>This tab can be closed.</p>
</body></html>"""
    }
}
