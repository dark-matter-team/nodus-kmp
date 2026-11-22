package com.darkmatter.nodus.core.network

sealed interface ApiResult<out T> {
    data class Success<T>(val data: T) : ApiResult<T>

    /**
     * [message] можна показувати користувачу. [code] є лише в помилок HTTP;
     * [kind] дозволяє екрану реагувати на тип помилки (наприклад, запропонувати повторити спробу).
     */
    data class Error(
        val message: String,
        val code: Int? = null,
        val kind: ErrorKind = ErrorKind.fromStatus(code),
    ) : ApiResult<Nothing>
}

enum class ErrorKind {
    /** Сервер недоступний або немає інтернету. */
    Network,

    /** Сервер не відповів вчасно. */
    Timeout,

    /** 4xx: помилка в запиті, бекенд пояснює причину. */
    Client,

    /** 5xx: збій на сервері. */
    Server,

    ;

    companion object {
        fun fromStatus(code: Int?): ErrorKind = when (code) {
            null -> Network
            in 400..499 -> Client
            else -> Server
        }
    }
}
