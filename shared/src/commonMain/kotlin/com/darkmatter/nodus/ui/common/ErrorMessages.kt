package com.darkmatter.nodus.ui.common

import com.darkmatter.nodus.core.network.ApiResult
import com.darkmatter.nodus.core.network.ErrorKind
import nodus.shared.generated.resources.Res
import nodus.shared.generated.resources.error_http
import nodus.shared.generated.resources.error_network
import nodus.shared.generated.resources.error_timeout
import nodus.shared.generated.resources.error_unknown
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.getString

/**
 * Текст помилки для користувача. Готовий текст (від бекенда чи сервісу входу) показується як є,
 * інакше рядок з ресурсів за типом помилки. Suspend, бо ViewModel кладе вже готовий текст у стан екрана.
 */
suspend fun ApiResult.Error.userMessage(): String {
    message?.let { return it }
    val text = fallbackText()
    return getString(text.resource, *text.args.toTypedArray())
}

/** Рядок з ресурсів і його аргументи, коли готового тексту немає. */
internal class ErrorText(val resource: StringResource, val args: List<Any> = emptyList())

internal fun ApiResult.Error.fallbackText(): ErrorText {
    val code = code
    return when {
        kind == ErrorKind.Network -> ErrorText(Res.string.error_network)
        kind == ErrorKind.Timeout -> ErrorText(Res.string.error_timeout)
        code != null -> ErrorText(Res.string.error_http, listOf(code))
        else -> ErrorText(Res.string.error_unknown)
    }
}
