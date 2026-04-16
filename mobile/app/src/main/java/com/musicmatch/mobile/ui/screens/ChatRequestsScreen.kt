package com.musicmatch.mobile.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.musicmatch.mobile.model.dto.ChatPreview
import com.musicmatch.mobile.ui.theme.*
import com.musicmatch.mobile.utils.NetworkConfig
import com.musicmatch.mobile.viewmodel.ChatViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatRequestsScreen(
    navController: NavController,
    viewModel: ChatViewModel = viewModel()
) {

    val context = LocalContext.current
    val requests by viewModel.pendingChats.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.load(context)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("Solicitudes", color = Color.White)
                },
                navigationIcon = {
                    IconButton(onClick = {
                        navController.popBackStack()
                    }) {
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

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(ColorFondo)
        ) {

            if (requests.isEmpty()) {

                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No tienes solicitudes")
                }

            } else {

                LazyColumn(
                    contentPadding = PaddingValues(10.dp)
                ) {

                    items(requests) { chat ->

                        RequestItem(
                            chat = chat,
                            onAccept = {
                                viewModel.acceptChat(context, chat.chatId)
                            },
                            onReject = {
                                viewModel.rejectChat(context, chat.chatId)
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun RequestItem(
    chat: ChatPreview,
    onAccept: () -> Unit,
    onReject: () -> Unit
) {

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(Color.LightGray),
                contentAlignment = Alignment.Center
            ) {

                if (!chat.otherProfileImage.isNullOrEmpty()) {
                    AsyncImage(
                        model = NetworkConfig.getAvatarUrl(chat.otherProfileImage),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Icon(
                        Icons.Default.Person,
                        contentDescription = null,
                        tint = Color.Gray
                    )
                }
            }

            Spacer(Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {

                Text(chat.otherUsername, color = ColorPrincipal)
                Text("Quiere iniciar chat", color = Color.Gray)
            }

            Row {

                Button(
                    onClick = onAccept,
                    colors = ButtonDefaults.buttonColors(ColorSecundario)
                ) {
                    Text("OK")
                }

                Spacer(Modifier.width(6.dp))

                OutlinedButton(onClick = onReject) {
                    Text("X")
                }
            }
        }
    }
}