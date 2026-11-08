package com.darkmatter.nodus.data.storage

import com.russhwolf.settings.Settings

class OnboardingStorage(
    private val settings: Settings = Settings(),
) {

    fun isCompleted(): Boolean =
        settings.getBoolean(KEY_COMPLETED, false)

    fun setCompleted() {
        settings.putBoolean(KEY_COMPLETED, true)
    }

    // TEMP: для повторних перевірок, видалити перед PR
    fun reset() {
        settings.putBoolean(KEY_COMPLETED, false)
    }

    private companion object {
        const val KEY_COMPLETED = "onboarding_completed"
    }
}