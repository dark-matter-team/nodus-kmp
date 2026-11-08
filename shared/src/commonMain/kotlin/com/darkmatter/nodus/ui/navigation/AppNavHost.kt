package com.darkmatter.nodus.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.darkmatter.nodus.ui.screens.CatalogScreen
import com.darkmatter.nodus.ui.screens.SettingsScreen
import com.darkmatter.nodus.ui.screens.auth.ForgotPasswordScreen
import com.darkmatter.nodus.ui.screens.auth.LoginScreen
import com.darkmatter.nodus.ui.screens.auth.RegisterScreen
import com.darkmatter.nodus.ui.screens.auth.ResetPasswordScreen

@Composable
fun AppNavHost() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = LoginRoute,
    ) {
        composable<LoginRoute> {
            LoginScreen(
                onLoggedIn = { navController.navigateToMain() },
                onOpenRegister = { navController.navigate(RegisterRoute) },
                onForgotPassword = { navController.navigate(ForgotPasswordRoute) },
            )
        }

        composable<RegisterRoute> {
            RegisterScreen(
                onRegistered = { navController.navigateToMain() },
                onOpenLogin = { navController.popBackStack() },
            )
        }

        composable<ForgotPasswordRoute> {
            ForgotPasswordScreen(
                onCodeSent = { email -> navController.navigate(ResetPasswordRoute(email)) },
                onBack = { navController.popBackStack() },
            )
        }

        composable<ResetPasswordRoute> { entry ->
            val route = entry.toRoute<ResetPasswordRoute>()
            ResetPasswordScreen(
                email = route.email,
                // Повертаємось на вже наявний у стеку екран входу, прибираючи екрани відновлення
                onDone = { navController.popBackStack<LoginRoute>(inclusive = false) },
                onBack = { navController.popBackStack() },
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

/** Після входу чи реєстрації auth-екрани прибираються зі стеку, щоб "назад" не повертало на них. */
private fun NavHostController.navigateToMain() {
    navigate(CatalogRoute) {
        popUpTo<LoginRoute> { inclusive = true }
    }
}
