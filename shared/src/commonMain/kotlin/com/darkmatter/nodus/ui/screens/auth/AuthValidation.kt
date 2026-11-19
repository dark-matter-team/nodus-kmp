package com.darkmatter.nodus.ui.screens.auth

/**
 * Клієнтська валідація, узгоджена з AuthService бекенда.
 * Кожна функція повертає текст помилки або null, якщо значення коректне.
 */
object AuthValidation {

    const val PASSWORD_MIN_LENGTH = 8
    const val PASSWORD_MAX_LENGTH = 72 // обмеження BCrypt на бекенді
    const val NICKNAME_MIN_LENGTH = 2
    const val NICKNAME_MAX_LENGTH = 50
    const val RESET_CODE_LENGTH = 6

    // Спрощена перевірка; остаточно формат перевіряє бекенд і повертає свій текст помилки
    private val emailRegex = Regex("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")

    fun validateEmail(email: String): String? {
        val value = email.trim()
        return when {
            value.isEmpty() -> "Enter your email"
            value.length > 254 || !emailRegex.matches(value) -> "Invalid email format"
            else -> null
        }
    }

    fun validateLogin(login: String): String? {
        val value = login.trim()
        return when {
            value.isEmpty() -> "Enter your email or username"
            '@' in value -> validateEmail(value)
            else -> null
        }
    }

    fun validateNickname(nickname: String): String? {
        val value = nickname.trim()
        return if (value.length !in NICKNAME_MIN_LENGTH..NICKNAME_MAX_LENGTH || '@' in value) {
            "Username must be $NICKNAME_MIN_LENGTH–$NICKNAME_MAX_LENGTH characters and must not contain '@'"
        } else {
            null
        }
    }

    fun validateNewPassword(password: String): String? = when {
        password.length < PASSWORD_MIN_LENGTH -> "Password must be at least $PASSWORD_MIN_LENGTH characters"
        password.length > PASSWORD_MAX_LENGTH -> "Password must be at most $PASSWORD_MAX_LENGTH characters"
        else -> null
    }

    /** При вході довжину не перевіряємо: достатньо, щоб пароль був введений. */
    fun validateLoginPassword(password: String): String? =
        if (password.isEmpty()) "Enter your password" else null

    fun validatePasswordConfirmation(password: String, confirmation: String): String? =
        if (password != confirmation) "Passwords do not match" else null
}
