package com.darkmatter.nodus.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.darkmatter.nodus.core.di.AppContainer
import com.darkmatter.nodus.core.session.SessionManager
import com.darkmatter.nodus.data.storage.OnboardingStorage
import com.darkmatter.nodus.ui.onboarding.OnboardingScreen
import com.darkmatter.nodus.ui.screens.CatalogScreen
import com.darkmatter.nodus.ui.screens.SettingsScreen
import com.darkmatter.nodus.ui.screens.auth.ForgotPasswordScreen
import com.darkmatter.nodus.ui.screens.auth.LoginScreen
import com.darkmatter.nodus.ui.screens.auth.NewPasswordScreen
import com.darkmatter.nodus.ui.screens.auth.PasswordChangedScreen
import com.darkmatter.nodus.ui.screens.auth.RegisterScreen
import com.darkmatter.nodus.ui.screens.auth.VerifyCodeScreen
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.filter
import com.darkmatter.nodus.ui.shell.AppShell

@Composable
fun AppNavHost(
    sessionManager: SessionManager = AppContainer.sessionManager,
    onboardingStorage: OnboardingStorage = AppContainer.onboardingStorage,
) {
    val navController = rememberNavController()

    // Обчислюється один раз: зміна стартового екрана під час роботи перебудувала б весь граф
    val startDestination = remember {
        resolveStartDestination(
            onboardingCompleted = onboardingStorage.isCompleted(),
            isLoggedIn = sessionManager.isLoggedIn.value,
        )
    }

    // Вихід з акаунта або 401 на будь-якому екрані: сесія завершилась, ведемо на вхід
    LaunchedEffect(navController) {
        sessionManager.isLoggedIn
            .drop(1)
            .filter { isLoggedIn -> !isLoggedIn }
            .collect { navController.navigateToLogin() }
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
                    val next = resolveStartDestination(
                        onboardingCompleted = true,
                        isLoggedIn = sessionManager.isLoggedIn.value,
                    )
                    navController.navigate(next) {
                        // Прибираємо онбординг зі стеку, щоб "назад" не повертало на слайди
                        popUpTo<OnboardingRoute> { inclusive = true }
                    }
                },
            )
        }

        composable<LoginRoute> {
            LoginScreen(
                onLoggedIn = { navController.navigateToMain() },
                onOpenRegister = { navController.navigate(RegisterRoute) },
                onForgotPassword = { navController.navigate(ForgotPasswordRoute) },
            )
        }
        composable<MainRoute> {
            AppShell(onOpenSettings = { navController.navigate(SettingsRoute) })
        }


        composable<RegisterRoute> {
            RegisterScreen(
                onRegistered = { navController.navigateToMain() },
                onOpenLogin = { navController.popBackStack() },
            )
        }

        composable<ForgotPasswordRoute> {
            ForgotPasswordScreen(
                onCodeSent = { email -> navController.navigate(VerifyCodeRoute(email)) },
                onBack = { navController.popBackStack() },
            )
        }

        composable<VerifyCodeRoute> { entry ->
            val route = entry.toRoute<VerifyCodeRoute>()
            VerifyCodeScreen(
                email = route.email,
                onCodeVerified = { code -> navController.navigate(NewPasswordRoute(route.email, code)) },
                // "Change Email" повертає на введення email
                onChangeEmail = { navController.popBackStack() },
            )
        }

        composable<NewPasswordRoute> { entry ->
            val route = entry.toRoute<NewPasswordRoute>()
            NewPasswordScreen(
                email = route.email,
                code = route.code,
                onPasswordReset = {
                    // Після зміни пароля назад до коду чи нового пароля вже не повертаємось
                    navController.navigate(PasswordChangedRoute) {
                        popUpTo<ForgotPasswordRoute> { inclusive = true }
                    }
                },
            )
        }

        composable<PasswordChangedRoute> {
            PasswordChangedScreen(
                // Повертаємось на вже наявний у стеку екран входу
                onSignIn = { navController.popBackStack<LoginRoute>(inclusive = false) },
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

/**
 * Стартовий екран без мережевих запитів, щоб застосунок запускався офлайн:
 * спершу онбординг (лише при першому запуску), потім вхід або одразу каталог, якщо є сесія.
 */
private fun resolveStartDestination(onboardingCompleted: Boolean, isLoggedIn: Boolean): Any = when {
    !onboardingCompleted -> OnboardingRoute
    isLoggedIn -> MainRoute
    else -> LoginRoute
}

/** Очищає весь стек: після виходу "назад" не має повертати на захищені екрани. */
private fun NavHostController.navigateToLogin() {
    navigate(LoginRoute) {
        popUpTo(graph.id) { inclusive = true }
    }
}

/** Після входу чи реєстрації auth-екрани прибираються зі стеку, щоб "назад" не повертало на них. */
private fun NavHostController.navigateToMain() {
    navigate(MainRoute) {
        popUpTo<LoginRoute> { inclusive = true }
    }
}
