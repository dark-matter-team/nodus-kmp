package com.darkmatter.nodus.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.darkmatter.nodus.core.di.AppContainer
import com.darkmatter.nodus.ui.onboarding.OnboardingScreen
import com.darkmatter.nodus.ui.screens.CatalogScreen
import com.darkmatter.nodus.ui.screens.LoginScreen
import com.darkmatter.nodus.ui.screens.SettingsScreen

@Composable
fun AppNavHost() {
    val navController = rememberNavController()
    val onboardingStorage = AppContainer.onboardingStorage

    // Читаємо прапорець один раз. Без remember він перечитувався б при кожній перемальовці
    val startDestination: Any = remember {
        if (onboardingStorage.isCompleted()) LoginRoute else OnboardingRoute
    }

    NavHost(
        navController = navController,
        startDestination = startDestination,
    ) {
        composable<OnboardingRoute> {
            OnboardingScreen(
                onFinished = {
                    // Спершу зберігаємо прапорець, потім переходимо далі
                    onboardingStorage.setCompleted()
                    navController.navigate(LoginRoute) {
                        // Прибираємо онбординг зі стеку, щоб "назад" не повертало на слайди
                        popUpTo<OnboardingRoute> { inclusive = true }
                    }
                },
            )
        }

        composable<LoginRoute> {
            LoginScreen(
                onLoggedIn = {
                    navController.navigate(CatalogRoute) {
                        popUpTo<LoginRoute> { inclusive = true }
                    }
                },
            )
        }

        composable<CatalogRoute> {
            CatalogScreen(
                onOpenSettings = { navController.navigate(SettingsRoute) },
            )
        }

        composable<SettingsRoute> {
            SettingsScreen(
                onBack = { navController.popBackStack() },
            )
        }
    }
}