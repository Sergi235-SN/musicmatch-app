package com.musicmatch.mobile.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
import com.musicmatch.mobile.ui.theme.*
import com.musicmatch.mobile.utils.NetworkConfig
import com.musicmatch.mobile.viewmodel.ChatViewModel
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatDetailScreen(
    chatId: Long,
    navController: NavController,
    viewModel: ChatViewModel = viewModel()
) {

    val context = LocalContext.current

    val userId by viewModel.userId.collectAsState()
    val messages by viewModel.messages.collectAsState()
    val chats by viewModel.chats.collectAsState()

    var text by remember { mutableStateOf("") }
    var showMenu by remember { mutableStateOf(false) }

    val chat = chats.find { it.chatId == chatId }
    val otherUserId = chat?.otherUserId

    LaunchedEffect(chatId) {
        viewModel.load(context)

        while (true) {

            val exists = viewModel.chatExists(chatId)

            if (!exists) {
                navController.popBackStack()
                break
            }

            viewModel.openChat(chatId)
            delay(2000)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {

                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(Color.LightGray),
                            contentAlignment = Alignment.Center
                        ) {

                            if (!chat?.otherProfileImage.isNullOrEmpty()) {
                                AsyncImage(
                                    model = NetworkConfig.getAvatarUrl(chat.otherProfileImage),
                                    contentDescription = null,
                                    modifier = Modifier.fillMaxSize()
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = null,
                                    tint = Color.Gray
                                )
                            }
                        }

                        Spacer(Modifier.width(10.dp))

                        Text(
                            text = chat?.otherUsername ?: "Chat",
                            color = Color.White
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, null, tint = Color.White)
                    }
                },
                actions = {
                    Box {

                        IconButton(onClick = { showMenu = true }) {
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = "Opciones",
                                tint = Color.White
                            )
                        }

                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Ver perfil") },
                                onClick = {
                                    showMenu = false
                                    otherUserId?.let {
                                        navController.navigate("public_profile/$it")
                                    }
                                }
                            )
                        }
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

            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp),
                contentPadding = PaddingValues(vertical = 12.dp)
            ) {

                items(messages) { msg ->

                    val isMe = msg.senderId == userId

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        contentAlignment =
                            if (isMe) Alignment.CenterEnd else Alignment.CenterStart
                    ) {

                        Card(
                            shape = RoundedCornerShape(
                                topStart = 18.dp,
                                topEnd = 18.dp,
                                bottomStart = if (isMe) 18.dp else 6.dp,
                                bottomEnd = if (isMe) 6.dp else 18.dp
                            ),
                            colors = CardDefaults.cardColors(
                                containerColor =
                                    if (isMe) ColorPrincipal else Color.White
                            )
                        ) {

                            Text(
                                text = msg.content ?: "",
                                modifier = Modifier.padding(10.dp),
                                color = if (isMe) Color.White else Color.Black
                            )
                        }
                    }
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {

                TextField(
                    value = text,
                    onValueChange = { text = it },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("Escribe un mensaje...") },
                    singleLine = true,
                    shape = RoundedCornerShape(24.dp),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White
                    )
                )

                Spacer(Modifier.width(8.dp))

                IconButton(
                    onClick = {
                        if (text.isNotBlank()) {
                            viewModel.sendMessage(chatId, text)
                            text = ""
                        }
                    },
                    enabled = text.isNotBlank(),
                    modifier = Modifier
                        .size(48.dp)
                        .background(
                            if (text.isNotBlank()) ColorSecundario else Color.Gray,
                            shape = RoundedCornerShape(50)
                        )
                ) {
                    Icon(Icons.Default.Send, null, tint = Color.White)
                }
            }
        }
    }
}