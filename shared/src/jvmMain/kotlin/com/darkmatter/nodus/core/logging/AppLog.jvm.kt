package com.darkmatter.nodus.core.logging

actual fun logWarning(tag: String, message: String) {
    System.err.println("W/$tag: $message")
}
