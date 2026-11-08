package com.darkmatter.nodus.ui.screens.auth

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class AuthValidationTest {

    @Test
    fun validEmailPasses() {
        assertNull(AuthValidation.validateEmail("user@example.com"))
        assertNull(AuthValidation.validateEmail("  user.name+tag@mail.example.ua  "))
    }

    @Test
    fun emptyEmailAsksToEnterIt() {
        assertEquals("Введіть email", AuthValidation.validateEmail("   "))
    }

    @Test
    fun malformedEmailIsRejected() {
        listOf("user", "user@", "@example.com", "user@example", "us er@example.com", "a@b@c.com").forEach {
            assertEquals("Некоректний формат email", AuthValidation.validateEmail(it), "email: '$it'")
        }
    }

    @Test
    fun tooLongEmailIsRejected() {
        val email = "a".repeat(250) + "@x.ua"
        assertEquals("Некоректний формат email", AuthValidation.validateEmail(email))
    }

    @Test
    fun newPasswordLengthMustBeBetween8And72() {
        assertNotNull(AuthValidation.validateNewPassword(""))
        assertEquals(
            "Пароль має містити щонайменше 8 символів",
            AuthValidation.validateNewPassword("a".repeat(7)),
        )
        assertNull(AuthValidation.validateNewPassword("a".repeat(8)))
        assertNull(AuthValidation.validateNewPassword("a".repeat(72)))
        assertEquals(
            "Пароль не може бути довшим за 72 символи",
            AuthValidation.validateNewPassword("a".repeat(73)),
        )
    }

    @Test
    fun loginPasswordOnlyHasToBeEntered() {
        assertEquals("Введіть пароль", AuthValidation.validateLoginPassword(""))
        assertNull(AuthValidation.validateLoginPassword("short"))
    }

    @Test
    fun nicknameLengthMustBeBetween2And50AfterTrim() {
        assertNotNull(AuthValidation.validateNickname(""))
        assertNotNull(AuthValidation.validateNickname("a"))
        assertNotNull(AuthValidation.validateNickname("  a  "))
        assertNull(AuthValidation.validateNickname("ab"))
        assertNull(AuthValidation.validateNickname("a".repeat(50)))
        assertNotNull(AuthValidation.validateNickname("a".repeat(51)))
    }

    @Test
    fun nicknameMustNotContainAt() {
        assertEquals(
            "Нікнейм має містити від 2 до 50 символів і не містити '@'",
            AuthValidation.validateNickname("nick@name"),
        )
    }

    @Test
    fun loginAcceptsNicknameOrValidEmail() {
        assertEquals("Введіть email або нікнейм", AuthValidation.validateLogin("  "))
        assertNull(AuthValidation.validateLogin("tester1"))
        assertNull(AuthValidation.validateLogin("test1@example.com"))
        assertEquals("Некоректний формат email", AuthValidation.validateLogin("test1@"))
    }

    @Test
    fun passwordConfirmationMustMatch() {
        assertNull(AuthValidation.validatePasswordConfirmation("Passw0rd!", "Passw0rd!"))
        assertEquals("Паролі не збігаються", AuthValidation.validatePasswordConfirmation("Passw0rd!", "Passw0rd"))
    }

    @Test
    fun resetCodeMustBeSixDigits() {
        assertNull(AuthValidation.validateResetCode("123456"))
        assertNull(AuthValidation.validateResetCode(" 123456 "))
        assertNotNull(AuthValidation.validateResetCode("12345"))
        assertNotNull(AuthValidation.validateResetCode("1234567"))
        assertNotNull(AuthValidation.validateResetCode("12345a"))
    }
}
