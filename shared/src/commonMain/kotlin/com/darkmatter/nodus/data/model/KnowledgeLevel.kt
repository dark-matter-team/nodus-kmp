package com.darkmatter.nodus.data.model

import kotlinx.serialization.Serializable

/**
 * Рівень знань дорожньої карти. Бекенд серіалізує enum рядком (JsonStringEnumConverter),
 * значення в JSON збігаються з іменами констант.
 */
@Serializable
enum class KnowledgeLevel {
    Beginner,
    Intermediate,
    Advanced,
}
