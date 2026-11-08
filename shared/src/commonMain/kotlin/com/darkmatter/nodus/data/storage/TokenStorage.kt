package com.darkmatter.nodus.data.storage

import com.russhwolf.settings.Settings

/**
 * Зберігає JWT між запусками застосунку.
 * Обмеження MVP: Settings пише значення відкритим текстом
 * (SharedPreferences / java.util.prefs / NSUserDefaults), без шифрування.
 */
class TokenStorage(private val settings: Settings) {

    fun getToken(): String? = settings.getStringOrNull(KEY_ACCESS_TOKEN)

    fun saveToken(token: String) = settings.putString(KEY_ACCESS_TOKEN, token)

    fun clear() = settings.remove(KEY_ACCESS_TOKEN)

    private companion object {
        const val KEY_ACCESS_TOKEN = "auth.access_token"
    }
}
