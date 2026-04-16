package com.musicmatch.mobile.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.musicmatch.mobile.data.ApiService
import com.musicmatch.mobile.data.repository.MatchRepository
import com.musicmatch.mobile.data.repository.UserRepository
import com.musicmatch.mobile.ui.components.*
import com.musicmatch.mobile.ui.theme.*
import com.musicmatch.mobile.viewmodel.MatchViewModel
import com.musicmatch.mobile.utils.TokenManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MatchesScreen(navController: NavController) {

    val context = LocalContext.current

    val tokenManager = remember { TokenManager() }
    val apiService = remember { ApiService.create() }

    val matchRepository = remember { MatchRepository(apiService) }
    val userRepository = remember { UserRepository(apiService) }

    val viewModel: MatchViewModel = viewModel(
        factory = remember {
            object : androidx.lifecycle.ViewModelProvider.Factory {
                override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                    return MatchViewModel(
                        matchRepository = matchRepository,
                        userRepository = userRepository,
                        tokenManager = tokenManager
                    ) as T
                }
            }
        }
    )

    LaunchedEffect(Unit) {
        viewModel.load(context)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Matches", color = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = ColorPrincipal
                )
            )
        }
    ) { padding ->

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(ColorFondo),
            contentAlignment = Alignment.Center
        ) {

            when {

                !viewModel.profileComplete -> {
                    ProfileIncompleteState()
                }

                viewModel.loading -> {
                    CircularProgressIndicator(color = ColorSecundario)
                }

                viewModel.currentProfile == null -> {
                    EmptyMatchesState(viewModel.message ?: "No hay más músicos")
                }

                else -> {
                    SwipeCardStack(
                        viewModel = viewModel,
                        navController = navController
                    )
                }
            }
        }
    }
}

@Composable
fun ProfileIncompleteState() {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("Completa tu perfil para ver matches")
    }
}