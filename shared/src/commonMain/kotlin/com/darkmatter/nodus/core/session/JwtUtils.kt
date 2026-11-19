package com.darkmatter.nodus.core.session

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.longOrNull
import kotlin.io.encoding.Base64
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

/**
 * Читає поле exp з payload JWT без перевірки підпису (підпис перевіряє лише бекенд).
 * Потрібно, щоб на старті не показувати головний екран із завідомо протермінованим токеном.
 */
internal object JwtUtils {

    private val base64Url = Base64.UrlSafe.withPadding(Base64.PaddingOption.ABSENT_OPTIONAL)

    fun expiresAtEpochSeconds(token: String): Long? = try {
        val payload = token.split('.').getOrNull(1) ?: return null
        val json = base64Url.decode(payload).decodeToString()
        Json.parseToJsonElement(json).jsonObject["exp"]?.jsonPrimitive?.longOrNull
    } catch (e: IllegalArgumentException) {
        // Некоректний Base64 або JSON (SerializationException теж є IllegalArgumentException)
        null
    }

    /** Токен без exp або з нечитабельним payload вважаємо дійсним: остаточно вирішить бекенд (401). */
    @OptIn(ExperimentalTime::class)
    fun isExpired(token: String): Boolean {
        val exp = expiresAtEpochSeconds(token) ?: return false
        return Clock.System.now().epochSeconds >= exp
    }
}
