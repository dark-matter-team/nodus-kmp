package com.darkmatter.nodus.core.auth

import java.security.MessageDigest
import java.security.SecureRandom

private val secureRandom = SecureRandom()

internal actual fun sha256(bytes: ByteArray): ByteArray = MessageDigest.getInstance("SHA-256").digest(bytes)

internal actual fun secureRandomBytes(size: Int): ByteArray = ByteArray(size).also(secureRandom::nextBytes)
