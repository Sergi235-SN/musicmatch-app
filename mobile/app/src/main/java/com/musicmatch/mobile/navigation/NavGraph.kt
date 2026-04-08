package com.musicmatch.mobile.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.musicmatch.mobile.ui.screens.RegisterScreen
import com.musicmatch.mobile.ui.screens.login.LoginScreen

sealed class Screen(val route: String) {
    object Register : Screen("register")
    object Login : Screen("login")
    object Home : Screen("home")
}

@Composable
fun NavGraph(navController: NavHostController) {
    NavHost(navController = navController, startDestination = Screen.Register.route) {
        composable(Screen.Register.route) {
            RegisterScreen(onNavigateLogin = { navController.navigate(Screen.Login.route) })
        }
        composable(Screen.Login.route) {
            LoginScreen(onNavigateBack = { navController.popBackStack() })
        }
        composable(Screen.Home.route) {
            // Pantalla futura
        }
    }
}