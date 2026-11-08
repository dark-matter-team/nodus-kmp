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
            value.isEmpty() -> "Введіть email"
            value.length > 254 || !emailRegex.matches(value) -> "Некоректний формат email"
            else -> null
        }
    }

    fun validateLogin(login: String): String? {
        val value = login.trim()
        return when {
            value.isEmpty() -> "Введіть email або нікнейм"
            '@' in value -> validateEmail(value)
            else -> null
        }
    }

    fun validateNickname(nickname: String): String? {
        val value = nickname.trim()
        return if (value.length !in NICKNAME_MIN_LENGTH..NICKNAME_MAX_LENGTH || '@' in value) {
            "Нікнейм має містити від $NICKNAME_MIN_LENGTH до $NICKNAME_MAX_LENGTH символів і не містити '@'"
        } else {
            null
        }
    }

    fun validateNewPassword(password: String): String? = when {
        password.length < PASSWORD_MIN_LENGTH -> "Пароль має містити щонайменше $PASSWORD_MIN_LENGTH символів"
        password.length > PASSWORD_MAX_LENGTH -> "Пароль не може бути довшим за $PASSWORD_MAX_LENGTH символи"
        else -> null
    }

    /** При вході довжину не перевіряємо: достатньо, щоб пароль був введений. */
    fun validateLoginPassword(password: String): String? =
        if (password.isEmpty()) "Введіть пароль" else null

    fun validatePasswordConfirmation(password: String, confirmation: String): String? =
        if (password != confirmation) "Паролі не збігаються" else null

    fun validateResetCode(code: String): String? {
        val value = code.trim()
        return if (value.length != RESET_CODE_LENGTH || !value.all { it.isDigit() }) {
            "Код має складатися з $RESET_CODE_LENGTH цифр"
        } else {
            null
        }
    }
}
