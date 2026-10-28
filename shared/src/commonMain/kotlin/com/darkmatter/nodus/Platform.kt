package com.darkmatter.nodus

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform