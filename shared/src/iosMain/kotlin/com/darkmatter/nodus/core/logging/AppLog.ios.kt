package com.darkmatter.nodus.core.logging

import platform.Foundation.NSLog

actual fun logWarning(tag: String, message: String) {
    // Повідомлення передається аргументом, а не форматом: символ % у ньому не зламає NSLog
    NSLog("W/%@: %@", tag, message)
}
