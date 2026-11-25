package com.darkmatter.nodus.core.network

import com.darkmatter.nodus.core.logging.logWarning
import io.ktor.client.network.sockets.ConnectTimeoutException
import io.ktor.client.network.sockets.SocketTimeoutException
import io.ktor.client.plugins.HttpRequestTimeoutException
import io.ktor.client.plugins.ResponseException
import io.ktor.client.statement.bodyAsText
import io.ktor.client.statement.request
import io.ktor.http.encodedPath
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlin.coroutines.cancellation.CancellationException

private const val LOG_TAG = "Nodus.Api"

/**
 * Виконує запит до бекенда і перетворює результат на [ApiResult]: помилка має тип ([ErrorKind]),
 * код HTTP і текст бекенда з {"error"}, якщо він є. Кожна помилка пишеться в лог (метод, шлях,
 * статус або тип винятку) без тіл і заголовків, тож токени й паролі туди не потрапляють.
 * CancellationException прокидається далі, щоб скасування корутини не стало "помилкою".
 */
suspend fun <T> apiCall(block: suspend () -> T): ApiResult<T> = try {
    ApiResult.Success(block())
} catch (e: CancellationException) {
    throw e
} catch (e: ResponseException) {
    val code = e.response.status.value
    val request = e.response.request
    logWarning(LOG_TAG, "HTTP $code ${request.method.value} ${request.url.encodedPath}")
    ApiResult.Error(kind = ErrorKind.fromStatus(code), code = code, message = e.serverMessage())
} catch (e: Exception) {
    val kind = if (e.isTimeout()) ErrorKind.Timeout else ErrorKind.Network
    logWarning(LOG_TAG, "$kind: ${e::class.simpleName}: ${e.message}")
    ApiResult.Error(kind = kind)
}

/** Тайм-аути плагіна HttpTimeout: запиту, з'єднання і читання. */
private fun Exception.isTimeout(): Boolean =
    this is HttpRequestTimeoutException || this is ConnectTimeoutException || this is SocketTimeoutException

/** Тіло будь-якої помилки бекенда: {"error": "текст"}. */
@Serializable
private class ErrorBody(val error: String? = null)

private val errorJson = Json { ignoreUnknownKeys = true }

private suspend fun ResponseException.serverMessage(): String? = try {
    errorJson.decodeFromString<ErrorBody>(response.bodyAsText()).error?.takeIf { it.isNotBlank() }
} catch (e: CancellationException) {
    throw e
} catch (e: Exception) {
    null
}
