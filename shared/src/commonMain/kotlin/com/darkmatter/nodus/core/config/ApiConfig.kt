package com.darkmatter.nodus.core.config

/** Адреса локального бекенда для розробки на цій платформі (емулятор Android бачить комп'ютер як 10.0.2.2). */
expect fun getBaseUrl(): String

/**
 * Адреса бекенда для поточної збірки. Застосунок задає її на старті з налаштувань збірки:
 * Android і Desktop з api.baseUrl у local.properties (BuildConfig і jvmArgs), iOS з API_BASE_URL
 * у xcconfig через Info.plist. Якщо нічого не задано, використовується локальна адреса [getBaseUrl].
 */
object ApiConfig {

    private var configuredBaseUrl: String? = null

    val baseUrl: String
        get() = configuredBaseUrl ?: getBaseUrl()

    /** Порожнє значення ігнорується; завершальний "/" прибирається, щоб шляхи не подвоювали його. */
    fun setBaseUrl(url: String) {
        configuredBaseUrl = url.trim().trimEnd('/').takeIf { it.isNotEmpty() }
    }
}
