package com.darkmatter.nodus.ui.common

import com.darkmatter.nodus.core.network.ApiResult
import com.darkmatter.nodus.core.network.ErrorKind
import kotlinx.coroutines.runBlocking
import nodus.shared.generated.resources.Res
import nodus.shared.generated.resources.error_http
import nodus.shared.generated.resources.error_network
import nodus.shared.generated.resources.error_timeout
import nodus.shared.generated.resources.error_unknown
import kotlin.test.Test
import kotlin.test.assertEquals

/** Сам рядок з ресурсів у юніт-тестах не завантажити (потрібні Skia або контекст Android), тож перевіряємо вибір. */
class ErrorMessagesTest {

    @Test
    fun backendTextIsShownAsIs() = runBlocking {
        val error = ApiResult.Error(ErrorKind.Client, 404, "Дерево не знайдено")

        assertEquals("Дерево не знайдено", error.userMessage())
    }

    @Test
    fun networkAndTimeoutHaveOwnTexts() {
        assertEquals(Res.string.error_network, ApiResult.Error(ErrorKind.Network).fallbackText().resource)
        assertEquals(Res.string.error_timeout, ApiResult.Error(ErrorKind.Timeout).fallbackText().resource)
    }

    @Test
    fun httpErrorWithoutTextShowsStatusCode() {
        val text = ApiResult.Error(ErrorKind.Server, 500).fallbackText()

        assertEquals(Res.string.error_http, text.resource)
        assertEquals(listOf<Any>(500), text.args)
    }

    @Test
    fun errorWithoutCodeOrTextIsGeneric() {
        assertEquals(Res.string.error_unknown, ApiResult.Error(ErrorKind.Unknown).fallbackText().resource)
    }
}
