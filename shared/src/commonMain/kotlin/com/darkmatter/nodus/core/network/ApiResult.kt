package com.darkmatter.nodus.core.network

sealed interface ApiResult<out T> {
    data class Success<T>(val data: T) : ApiResult<T>

    /**
     * Мережевий шар не формує текстів для користувача: екран сам обирає повідомлення за [kind]
     * (див. ui/common/ErrorMessages.kt). [message] є лише тоді, коли готовий текст прийшов ззовні:
     * з тіла {"error"} бекенда або від сервісу входу Google. [code] є лише в помилок HTTP.
     */
    data class Error(
        val kind: ErrorKind,
        val code: Int? = null,
        val message: String? = null,
    ) : ApiResult<Nothing>
}

enum class ErrorKind {
    /** Сервер недоступний або немає інтернету. */
    Network,

    /** Сервер не відповів вчасно. */
    Timeout,

    /** 4xx: помилка в запиті, бекенд зазвичай пояснює причину. */
    Client,

    /** 5xx: збій на сервері. */
    Server,

    /** Усе інше: несподіваний статус HTTP або помилка поза мережевим шаром. */
    Unknown,

    ;

    companion object {
        fun fromStatus(code: Int): ErrorKind = when (code) {
            in 400..499 -> Client
            in 500..599 -> Server
            else -> Unknown
        }
    }
}
