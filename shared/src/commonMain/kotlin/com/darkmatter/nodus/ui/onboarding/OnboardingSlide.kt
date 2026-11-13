package com.darkmatter.nodus.ui.onboarding

import nodus.shared.generated.resources.Res
import nodus.shared.generated.resources.onboarding_slide1
import nodus.shared.generated.resources.onboarding_slide1_wide
import nodus.shared.generated.resources.onboarding_slide2
import nodus.shared.generated.resources.onboarding_slide2_wide
import nodus.shared.generated.resources.onboarding_slide3
import nodus.shared.generated.resources.onboarding_slide3_wide
import org.jetbrains.compose.resources.DrawableResource

/**
 * [image] вертикальна ілюстрація для телефона, [wideImage] альбомна для широкого екрана.
 * Тексти дослівно з макета Figma.
 */
data class OnboardingSlide(
    val title: String,
    val description: String,
    val image: DrawableResource,
    val wideImage: DrawableResource,
)

val onboardingSlides = listOf(
    OnboardingSlide(
        title = "DESCRIBE YOUR GOAL",
        description = "Describe any goal, and AI will generate your personal skill path.",
        image = Res.drawable.onboarding_slide1,
        wideImage = Res.drawable.onboarding_slide1_wide,
    ),
    OnboardingSlide(
        title = "GET YOUR SKILL TREE",
        description = "Break down your global goal into clear, step-by-step interactive nodes.",
        image = Res.drawable.onboarding_slide2,
        wideImage = Res.drawable.onboarding_slide2_wide,
    ),
    OnboardingSlide(
        title = "LEVEL UP EVERY DAY",
        description = "Boost your RPG stars, maintain daily streaks, and unblock cosmic rewards.",
        image = Res.drawable.onboarding_slide3,
        wideImage = Res.drawable.onboarding_slide3_wide,
    ),
)
