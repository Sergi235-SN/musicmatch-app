package com.musicmatch.mobile.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MailOutline
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
import androidx.compose.ui.layout.ContentScale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    navController: NavController,
    viewModel: ChatViewModel = viewModel()
) {

    val context = LocalContext.current
    val chats by viewModel.chats.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.load(context)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Box(
                        Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Chats", color = Color.White)
                    }
                },
                actions = {

                    IconButton(
                        onClick = {
                            navController.navigate("chat_requests")
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.MailOutline,
                            contentDescription = "Solicitudes",
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

            if (chats.isEmpty()) {

                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No tienes chats todavía")
                }

            } else {

                LazyColumn(
                    contentPadding = PaddingValues(10.dp)
                ) {

                    items(chats) { chat ->

                        ChatItem(chat) {
                            navController.navigate("chat_detail/${chat.chatId}")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ChatItem(
    chat: ChatPreview,
    onClick: () -> Unit
) {

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .clickable { onClick() },
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
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(Icons.Default.Person, null, tint = Color.Gray)
                }
            }

            Spacer(Modifier.width(12.dp))

            Column {

                Text(chat.otherUsername, color = ColorPrincipal)

                Text(
                    chat.lastMessage ?: "Sin mensajes aún",
                    color = Color.Black
                )

                Text(
                    chat.status,
                    color = ColorSecundario
                )
            }
        }
    }
}