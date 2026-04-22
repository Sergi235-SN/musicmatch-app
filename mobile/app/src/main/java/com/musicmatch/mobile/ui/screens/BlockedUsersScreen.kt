package com.musicmatch.mobile.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.musicmatch.mobile.data.ApiService
import com.musicmatch.mobile.data.repository.MatchRepository
import com.musicmatch.mobile.model.dto.BlockedUserDTO
import com.musicmatch.mobile.ui.theme.ColorFondo
import com.musicmatch.mobile.ui.theme.ColorPrincipal
import com.musicmatch.mobile.ui.theme.ColorSecundario
import com.musicmatch.mobile.utils.TokenManager
import com.musicmatch.mobile.viewmodel.BlockedUsersViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BlockedUsersScreen(
    navController: NavController
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val tokenManager = remember { TokenManager() }
    val apiService = remember { ApiService.create() }
    val matchRepository = remember { MatchRepository(apiService) }

    val viewModel: BlockedUsersViewModel = viewModel(
        factory = remember {
            object : androidx.lifecycle.ViewModelProvider.Factory {
                override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                    return BlockedUsersViewModel(
                        matchRepository = matchRepository,
                        tokenManager = tokenManager
                    ) as T
                }
            }
        }
    )

    LaunchedEffect(Unit) {
        viewModel.load(context)
    }

    LaunchedEffect(viewModel.message) {
        viewModel.message?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.clearMessage()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Usuarios bloqueados", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = "Atrás",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = ColorPrincipal
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(ColorFondo)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .background(ColorFondo)
            ) {
                when {
                    viewModel.loading -> {
                        CircularProgressIndicator(
                            modifier = Modifier.align(Alignment.Center),
                            color = ColorSecundario
                        )
                    }

                    viewModel.blockedUsers.isEmpty() -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("No has bloqueado a ningún usuario")
                        }
                    }

                    else -> {
                        LazyColumn(
                            contentPadding = PaddingValues(12.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            items(viewModel.blockedUsers) { user ->
                                BlockedUserItem(
                                    user = user,
                                    onUnblock = { viewModel.unblock(user.id) }
                                )
                            }
                        }
                    }
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp)
                    .background(ColorPrincipal)
            )
        }
    }
}

@Composable
private fun BlockedUserItem(
    user: BlockedUserDTO,
    onUnblock: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = user.username,
                modifier = Modifier.weight(1f),
                color = ColorPrincipal,
                fontWeight = FontWeight.SemiBold
            )

            OutlinedButton(onClick = onUnblock) {
                Text("Desbloquear")
            }
        }
    }
}