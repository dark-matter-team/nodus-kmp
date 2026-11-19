package com.darkmatter.nodus.ui.navigation

import kotlinx.serialization.Serializable

@Serializable
data object OnboardingRoute

@Serializable
data object LoginRoute

@Serializable
data object RegisterRoute

@Serializable
data object ForgotPasswordRoute

@Serializable
data class VerifyCodeRoute(val email: String)

@Serializable
data class NewPasswordRoute(val email: String, val code: String)

@Serializable
data object PasswordChangedRoute

@Serializable
data object CatalogRoute

@Serializable
data object SettingsRoute
