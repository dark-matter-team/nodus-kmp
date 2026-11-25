package com.darkmatter.nodus.core.logging

import android.util.Log

actual fun logWarning(tag: String, message: String) {
    Log.w(tag, message)
}
