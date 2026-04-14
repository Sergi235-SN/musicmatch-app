package com.musicmatch.mobile.navigation

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.People
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.musicmatch.mobile.ui.screens.*
import com.musicmatch.mobile.viewmodel.*
import com.musicmatch.mobile.ui.theme.ColorPrincipal
import com.musicmatch.mobile.ui.theme.ColorSecundario
import androidx.compose.ui.graphics.Color

// ================= ROUTES =================
sealed class Screen(val route: String) {

    object Register : Screen("register")
    object Login : Screen("login")
    object Home : Screen("home")

    object Matches : Screen("matches")
    object Chat : Screen("chat")
    object Profile : Screen("profile")

    object MusicalProfile : Screen("musical_profile/{userId}") {
        fun createRoute(userId: Long) = "musical_profile/$userId"
    }

    object MusicalProfileStep2 : Screen("musical_profile_step2/{userId}") {
        fun createRoute(userId: Long) = "musical_profile_step2/$userId"
    }

    object PublicProfile : Screen("public_profile/{userId}") {
        fun createRoute(userId: Long) = "public_profile/$userId"
    }
}

// ================= BOTTOM NAV =================
sealed class BottomItem(
    val route: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val label: String
) {
    object Home : BottomItem("home", Icons.Default.Home, "Inicio")
    object Matches : BottomItem("matches", Icons.Default.People, "Matches")
    object Chat : BottomItem("chat", Icons.Default.Chat, "Chat")
    object Profile : BottomItem("profile", Icons.Default.Person, "Perfil")
}

private val bottomItems = listOf(
    BottomItem.Home,
    BottomItem.Matches,
    BottomItem.Chat,
    BottomItem.Profile
)

// ================= NAVGRAPH =================
@Composable
fun NavGraph(navController: NavHostController) {

    NavHost(
        navController = navController,
        startDestination = Screen.Register.route
    ) {

        // ================= REGISTER =================
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

        // ================= LOGIN =================
        composable(Screen.Login.route) {

            LoginScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateRegister = {
                    navController.navigate(Screen.Register.route)
                },
                onLoginSuccess = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            )
        }

        // ================= HOME =================
        composable(Screen.Home.route) {
            MainScaffold(navController) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("HOME")
                }
            }
        }

        // ================= MATCHES =================
        composable(Screen.Matches.route) {

            MainScaffold(navController) {

                MatchesScreen(navController = navController)
            }
        }

        // ================= CHAT =================
        composable(Screen.Chat.route) {
            MainScaffold(navController) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("CHAT")
                }
            }
        }

        // ================= PROFILE (IMPORTANTE) =================
        composable(Screen.Profile.route) {
            MainScaffold(navController) {
                ProfileScreen(navController = navController)
            }
        }

        // ================= ONBOARDING STEP 1 =================
        composable(
            route = Screen.MusicalProfile.route,
            arguments = listOf(navArgument("userId") { type = NavType.LongType })
        ) { backStackEntry ->

            val userId = backStackEntry.arguments?.getLong("userId") ?: 0L

            MusicalProfileScreen(
                onNavigateNext = {
                    navController.navigate(Screen.MusicalProfileStep2.createRoute(userId))
                }
            )
        }

        // ================= ONBOARDING STEP 2 =================
        composable(
            route = Screen.MusicalProfileStep2.route,
            arguments = listOf(navArgument("userId") { type = NavType.LongType })
        ) { backStackEntry ->

            val userId = backStackEntry.arguments?.getLong("userId") ?: 0L

            MusicalProfileStep2Screen(
                onFinish = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.MusicalProfileStep2.route) { inclusive = true }
                    }
                }
            )
        }

        composable(
            route = Screen.PublicProfile.route,
            arguments = listOf(navArgument("userId") { type = NavType.LongType })
        ) { backStackEntry ->

            val userId = backStackEntry.arguments?.getLong("userId") ?: 0L

            PublicProfileScreen(
                userId = userId,
                navController = navController
            )
        }

    }
}


// ================= MAIN SCAFFOLD =================
@Composable
fun MainScaffold(
    navController: NavHostController,
    content: @Composable () -> Unit
) {
    val currentRoute =
        navController.currentBackStackEntryAsState().value?.destination?.route

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = ColorPrincipal,
                contentColor = Color.White
            ) {
                bottomItems.forEach { item ->

                    val selected = currentRoute == item.route

                    NavigationBarItem(
                        selected = selected,
                        onClick = {
                            if (currentRoute != item.route) {
                                navController.navigate(item.route) {
                                    popUpTo(Screen.Home.route) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        },
                        icon = {
                            Icon(item.icon, contentDescription = item.label)
                        },
                        label = { Text(item.label) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = ColorSecundario,
                            selectedTextColor = ColorSecundario,
                            unselectedIconColor = Color.White.copy(alpha = 0.7f),
                            unselectedTextColor = Color.White.copy(alpha = 0.7f),
                            indicatorColor = Color.White.copy(alpha = 0.1f)
                        )
                    )
                }
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            content()
        }
    }
}