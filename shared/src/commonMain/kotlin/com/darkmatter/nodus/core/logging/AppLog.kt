package com.darkmatter.nodus.core.logging

/**
 * Попередження в системний лог платформи: Logcat на Android, stderr на Desktop, консоль на iOS.
 * Не передавайте сюди паролі, токени, коди відновлення й тіла запитів.
 */
expect fun logWarning(tag: String, message: String)
