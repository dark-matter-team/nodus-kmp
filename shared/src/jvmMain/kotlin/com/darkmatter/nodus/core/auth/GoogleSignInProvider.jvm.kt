package com.darkmatter.nodus.core.auth

import androidx.compose.runtime.Composable

@Composable
actual fun rememberGoogleSignInProvider(): GoogleSignInProvider = UnsupportedGoogleSignInProvider
