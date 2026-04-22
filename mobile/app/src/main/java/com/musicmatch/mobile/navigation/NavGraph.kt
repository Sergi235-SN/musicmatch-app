package com.musicmatch.mobile.navigation

import android.net.Uri
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.navArgument
import androidx.navigation.navDeepLink
import com.musicmatch.mobile.ui.screens.BlockedUsersScreen
import com.musicmatch.mobile.ui.screens.ChatDetailScreen
import com.musicmatch.mobile.ui.screens.ChatRequestsScreen
import com.musicmatch.mobile.ui.screens.ChatScreen
import com.musicmatch.mobile.ui.screens.ForgotPasswordScreen
import com.musicmatch.mobile.ui.screens.HomeScreen
import com.musicmatch.mobile.ui.screens.LoginScreen
import com.musicmatch.mobile.ui.screens.MatchesScreen
import com.musicmatch.mobile.ui.screens.MusicalProfileScreen
import com.musicmatch.mobile.ui.screens.MusicalProfileStep2Screen
import com.musicmatch.mobile.ui.screens.ProfileScreen
import com.musicmatch.mobile.ui.screens.PublicProfileScreen
import com.musicmatch.mobile.ui.screens.RegisterScreen
import com.musicmatch.mobile.ui.screens.ResetPasswordScreen
import com.musicmatch.mobile.ui.screens.ServerConfigScreen
import com.musicmatch.mobile.ui.screens.SplashScreen
import com.musicmatch.mobile.ui.theme.ColorPrincipal
import com.musicmatch.mobile.ui.theme.ColorSecundario
import com.musicmatch.mobile.viewmodel.LoginViewModel
import com.musicmatch.mobile.viewmodel.RegisterViewModel

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object ServerConfig : Screen("server_config")
    object Register : Screen("register")
    object Login : Screen("login")
    object Home : Screen("home")

    object Matches : Screen("matches")
    object Chat : Screen("chat")
    object Profile : Screen("profile")
    object BlockedUsers : Screen("blocked_users")

    object ForgotPassword : Screen("forgot_password")

    object ResetPassword : Screen("reset_password?token={token}") {
        fun createRoute(token: String) = "reset_password?token=$token"
    }

    object EmailVerification : Screen("email_verification?email={email}") {
        fun createRoute(email: String) = "email_verification?email=${Uri.encode(email)}"
    }

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

object ChatDetail : Screen("chat_detail/{chatId}") {
    fun createRoute(chatId: Long) = "chat_detail/$chatId"
}

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

@Composable
fun NavGraph(
    navController: NavHostController,
    loginViewModel: LoginViewModel
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Splash.route
    ) {

        composable(Screen.Splash.route) {
            SplashScreen(
                navController = navController,
                loginViewModel = loginViewModel
            )
        }

        composable(Screen.ServerConfig.route) {
            ServerConfigScreen(navController)
        }

        composable(Screen.Register.route) {
            val regViewModel: RegisterViewModel = viewModel()

            RegisterScreen(
                viewModel = regViewModel,
                onNavigateLogin = {
                    navController.navigate(Screen.Login.route)
                },
                onRegisterSuccess = { email, password ->
                    navController.currentBackStackEntry
                        ?.savedStateHandle
                        ?.set("pending_password", password)

                    navController.navigate(Screen.EmailVerification.createRoute(email))
                }
            )
        }

        composable(Screen.Login.route) {
            LoginScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateRegister = {
                    navController.navigate(Screen.Register.route)
                },
                onNavigateForgotPassword = {
                    navController.navigate(Screen.ForgotPassword.route)
                },
                onLoginSuccess = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            )
        }

        composable(
            route = Screen.EmailVerification.route,
            arguments = listOf(
                navArgument("email") {
                    type = NavType.StringType
                    defaultValue = ""
                }
            )
        ) { backStackEntry ->
            val email = backStackEntry.arguments?.getString("email").orEmpty()

            val password = navController.previousBackStackEntry
                ?.savedStateHandle
                ?.get<String>("pending_password")
                .orEmpty()

            com.musicmatch.mobile.ui.screens.EmailVerificationScreen(
                email = email,
                password = password,
                onContinue = { userId ->
                    navController.previousBackStackEntry
                        ?.savedStateHandle
                        ?.remove<String>("pending_password")

                    navController.navigate(Screen.MusicalProfile.createRoute(userId)) {
                        popUpTo(Screen.Register.route) { inclusive = true }
                    }
                },
                onGoToLogin = {
                    navController.previousBackStackEntry
                        ?.savedStateHandle
                        ?.remove<String>("pending_password")

                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Register.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.ForgotPassword.route) {
            ForgotPasswordScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.ResetPassword.route,
            arguments = listOf(
                navArgument("token") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = ""
                }
            ),
            deepLinks = listOf(
                navDeepLink {
                    uriPattern = "musicmatch://reset-password?token={token}"
                }
            )
        ) { backStackEntry ->

            val token = backStackEntry.arguments?.getString("token").orEmpty()

            ResetPasswordScreen(
                token = token,
                onPasswordResetSuccess = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Home.route) {
            MainScaffold(navController) {
                HomeScreen(navController = navController)
            }
        }

        composable(Screen.Matches.route) {
            MainScaffold(navController) {
                MatchesScreen(navController = navController)
            }
        }

        composable(Screen.Chat.route) {
            MainScaffold(navController) {
                ChatScreen(navController)
            }
        }

        composable("chat_requests") {
            MainScaffold(navController) {
                ChatRequestsScreen(navController)
            }
        }

        composable(
            route = ChatDetail.route,
            arguments = listOf(navArgument("chatId") { type = NavType.LongType })
        ) { backStackEntry ->

            val chatId = backStackEntry.arguments?.getLong("chatId") ?: 0L

            MainScaffold(navController) {
                ChatDetailScreen(
                    chatId = chatId,
                    navController = navController
                )
            }
        }

        composable(Screen.Profile.route) {
            MainScaffold(navController) {
                ProfileScreen(navController = navController)
            }
        }

        composable(Screen.BlockedUsers.route) {
            BlockedUsersScreen(navController = navController)
        }

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

        composable(
            route = Screen.MusicalProfileStep2.route,
            arguments = listOf(navArgument("userId") { type = NavType.LongType })
        ) {
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

@Composable
fun MainScaffold(
    navController: NavHostController,
    content: @Composable () -> Unit
) {
    val currentRoute by navController.currentBackStackEntryAsState()

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = ColorPrincipal,
                contentColor = Color.White
            ) {
                bottomItems.forEach { item ->

                    val selected = currentRoute?.destination?.route == item.route

                    NavigationBarItem(
                        selected = selected,
                        onClick = {
                            if (currentRoute?.destination?.route != item.route) {
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