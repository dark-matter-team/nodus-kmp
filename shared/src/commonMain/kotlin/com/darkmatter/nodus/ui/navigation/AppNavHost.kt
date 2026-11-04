package com.darkmatter.nodus.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.darkmatter.nodus.ui.screens.CatalogScreen
import com.darkmatter.nodus.ui.screens.LoginScreen
import com.darkmatter.nodus.ui.screens.SettingsScreen

@Composable
fun AppNavHost() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = LoginRoute,
    ) {
        composable<LoginRoute> {
            LoginScreen(
                onLoggedIn = {
                    navController.navigate(CatalogRoute) {
                        // Прибираємо екран входу зі стеку, щоб "назад" не повертало на нього
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