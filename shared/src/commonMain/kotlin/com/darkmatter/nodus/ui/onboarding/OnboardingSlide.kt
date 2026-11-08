package com.darkmatter.nodus.ui.onboarding

data class OnboardingSlide(
    val title: String,
    val description: String,
)

// TEMP: тексти тимчасові, замінити на фінальні від дизайнера
val onboardingSlides = listOf(
    OnboardingSlide(
        title = "Describe your goal",
        description = "Describe any goal, and AI will generate your personal skill path.",
    ),
    OnboardingSlide(
        title = "Get your skill tree",
        description = "Your goal becomes a tree of steps. Complete a step to unlock the next ones.",
    ),
    OnboardingSlide(
        title = "Track your progress",
        description = "Check off micro-steps, finish nodes and watch your tree grow.",
    ),
)