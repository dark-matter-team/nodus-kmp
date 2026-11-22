package com.darkmatter.nodus.core.network

import io.ktor.client.plugins.ResponseException
import io.ktor.client.statement.bodyAsText
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlin.coroutines.cancellation.CancellationException

/**
 * Виконує запит до бекенда і перетворює результат на [ApiResult]:
 * помилка з тілом {"error": "..."} повертає текст бекенда (його можна показати користувачу),
 * інша помилка HTTP повертає код статусу, мережева помилка повертає загальне повідомлення.
 * CancellationException прокидається далі, щоб скасування корутини не стало "помилкою".
 */
suspend fun <T> apiCall(block: suspend () -> T): ApiResult<T> = try {
    ApiResult.Success(block())
} catch (e: CancellationException) {
    throw e
} catch (e: ResponseException) {
    val code = e.response.status.value
    ApiResult.Error(e.serverMessage() ?: "Помилка сервера: $code", code)
} catch (e: Exception) {
    ApiResult.Error("Не вдалося підключитись до сервера")
}

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
