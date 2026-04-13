package com.musicmatch.mobile.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.musicmatch.mobile.ui.screens.*
import com.musicmatch.mobile.viewmodel.*

sealed class Screen(val route: String) {
    object Register : Screen("register")
    object Login : Screen("login")
    object Home : Screen("home")
    object MusicalProfile : Screen("musical_profile/{userId}") {
        fun createRoute(userId: Long) = "musical_profile/$userId"
    }

    object MusicalProfileStep2 : Screen("musical_profile_step2/{userId}") {
        fun createRoute(userId: Long) = "musical_profile_step2/$userId"
    }
}

@Composable
fun NavGraph(navController: NavHostController) {
    NavHost(navController = navController, startDestination = Screen.Register.route) {

        composable(Screen.Register.route) {
            val regViewModel: RegisterViewModel = viewModel()

            RegisterScreen(
                viewModel = regViewModel,
                onNavigateLogin = {
                    navController.navigate(Screen.Login.route)
                },
                onNavigateToMusicalProfile = { userId ->
                    navController.navigate(Screen.MusicalProfile.createRoute(userId)) {
                        popUpTo(Screen.Register.route) { inclusive = true }
                    }
                }
            )
        }

        composable(
            route = Screen.MusicalProfile.route,
            arguments = listOf(navArgument("userId") { type = NavType.LongType })
        ) { backStackEntry ->
            val userId = backStackEntry.arguments?.getLong("userId") ?: 0L
            MusicalProfileScreen(
                userId = userId,
                onNavigateNext = {
                    navController.navigate(Screen.MusicalProfileStep2.createRoute(userId))
                }
            )
        }

        composable(Screen.Login.route) {
            LoginScreen(
                onNavigateBack = { navController.popBackStack() },
                // CORRECCIÓN: Pasamos la acción de navegar a Registro
                onNavigateRegister = {
                    navController.navigate(Screen.Register.route) {
                        // Evita crear múltiples instancias de Register en el stack
                        launchSingleTop = true
                    }
                }
            )
        }

        composable(
            route = Screen.MusicalProfileStep2.route,
            arguments = listOf(navArgument("userId") { type = NavType.LongType })
        ) { backStackEntry ->

            val userId = backStackEntry.arguments?.getLong("userId") ?: 0L

            MusicalProfileStep2Screen(
                userId = userId,
                onFinish = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.MusicalProfileStep2.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Home.route) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("¡Bienvenido a Home!")
            }
        }
    }
}