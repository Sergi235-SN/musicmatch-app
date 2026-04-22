package com.musicmatch.mobile.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.musicmatch.mobile.data.ApiService
import com.musicmatch.mobile.data.repository.MatchRepository
import com.musicmatch.mobile.data.repository.UserRepository
import com.musicmatch.mobile.ui.components.*
import com.musicmatch.mobile.ui.theme.*
import com.musicmatch.mobile.utils.TokenManager
import com.musicmatch.mobile.viewmodel.MatchViewModel

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
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 28.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.WarningAmber,
                    contentDescription = "Perfil incompleto",
                    tint = Color(0xFFFF9800),
                    modifier = Modifier.size(64.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Perfil incompleto",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = ColorPrincipal,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "Completa tu perfil para poder ver matches.",
                    color = Color.Gray,
                    fontSize = 15.sp,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}