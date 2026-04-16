package com.musicmatch.mobile.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.musicmatch.mobile.data.ApiService
import com.musicmatch.mobile.data.repository.MatchRepository
import com.musicmatch.mobile.data.repository.UserRepository
import com.musicmatch.mobile.model.ExperienceLevel
import com.musicmatch.mobile.ui.components.SmartChip
import com.musicmatch.mobile.ui.theme.*
import com.musicmatch.mobile.utils.TokenManager
import com.musicmatch.mobile.viewmodel.PublicProfileViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun PublicProfileScreen(
    userId: Long,
    navController: NavController
) {

    val context = LocalContext.current
    val tokenManager = remember { TokenManager() }

    val apiService = remember { ApiService.create() }
    val userRepository = remember { UserRepository(apiService) }
    val matchRepository = remember { MatchRepository(apiService) }

    val viewModel: PublicProfileViewModel = viewModel(
        factory = remember {
            PublicProfileViewModel.Factory(userRepository, matchRepository)
        }
    )

    var showBlockDialog by remember { mutableStateOf(false) }
    var showUnblockDialog by remember { mutableStateOf(false) }

    LaunchedEffect(userId) {
        val token = tokenManager.getToken(context) ?: ""
        viewModel.load(userId, token)
    }

    val profile = viewModel.profile
    val token = tokenManager.getToken(context) ?: ""
    val footerHeight = 72.dp

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Perfil", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, null, tint = Color.White)
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
                .background(ColorFondo)
        ) {

            when {

                viewModel.isLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                profile == null -> {
                    Text(
                        viewModel.error ?: "Usuario no encontrado",
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                profile?.blockedMe == true -> {
                    Text(
                        text = "Este usuario te ha bloqueado",
                        color = Color.Gray,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                else -> {

                    val status = viewModel.chatStatus
                    val isBlocked = profile.blockedByMe == true

                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(
                                top = padding.calculateTopPadding(),
                                bottom = footerHeight
                            ),
                        contentAlignment = Alignment.Center
                    ) {

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp)
                                .verticalScroll(rememberScrollState())
                        ) {

                            Spacer(modifier = Modifier.height(24.dp))

                            Row(verticalAlignment = Alignment.CenterVertically) {

                                Box(
                                    modifier = Modifier
                                        .size(90.dp)
                                        .clip(CircleShape)
                                        .background(Color.LightGray),
                                    contentAlignment = Alignment.Center
                                ) {

                                    val image = viewModel.profileImageUrl

                                    if (!image.isNullOrEmpty()) {
                                        AsyncImage(
                                            model = image,
                                            contentDescription = null,
                                            modifier = Modifier.fillMaxSize(),
                                            contentScale = ContentScale.Crop
                                        )
                                    } else {
                                        Icon(
                                            Icons.Default.Person,
                                            null,
                                            modifier = Modifier.size(40.dp),
                                            tint = Color.Gray
                                        )
                                    }
                                }

                                Spacer(Modifier.width(16.dp))

                                Column {
                                    Text(
                                        profile.username,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 20.sp,
                                        color = ColorTexto
                                    )

                                    Text(profile.cityName ?: "", color = Color.Gray)

                                    Text(
                                        profile.experienceLevel.name,
                                        color = Color.Gray
                                    )
                                }
                            }

                            Spacer(Modifier.height(28.dp))

                            Text("Instrumentos", fontWeight = FontWeight.Bold)

                            Spacer(Modifier.height(8.dp))

                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                profile.instruments?.take(6)?.forEach { inst ->

                                    val name = viewModel.availableInstruments
                                        .find { it.id == inst.instrumentId }
                                        ?.name ?: "Instrumento"

                                    SmartChip(
                                        text = name,
                                        level = inst.level ?: ExperienceLevel.PRINCIPIANTE
                                    )
                                }
                            }

                            Spacer(Modifier.height(16.dp))

                            Text("Estilos", fontWeight = FontWeight.Bold)

                            Spacer(Modifier.height(8.dp))

                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                profile.styleIds?.take(6)?.forEach { id ->

                                    val name = viewModel.availableStyles
                                        .find { it.id == id }
                                        ?.name ?: "Estilo"

                                    SmartChip(text = name)
                                }
                            }

                            Spacer(Modifier.height(28.dp))

                            when {

                                isBlocked -> {
                                    Button(
                                        onClick = {},
                                        enabled = false,
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = Color.DarkGray
                                        )
                                    ) {
                                        Text("Usuario bloqueado")
                                    }
                                }

                                status == "PENDING" -> {
                                    Button(
                                        onClick = {},
                                        enabled = false,
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = Color.Gray
                                        )
                                    ) {
                                        Text("Solicitud pendiente")
                                    }
                                }

                                status == "ACTIVE" -> {
                                    Button(
                                        onClick = {
                                            viewModel.chatId?.let {
                                                navController.navigate("chat_detail/$it")
                                            }
                                        },
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = ColorSecundario
                                        )
                                    ) {
                                        Text("Ir al chat")
                                    }
                                }

                                else -> {
                                    Button(
                                        onClick = {
                                            viewModel.onChatClicked(profile.id, token)
                                        },
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = ColorSecundario
                                        )
                                    ) {
                                        Text("Iniciar chat")
                                    }
                                }
                            }

                            Spacer(Modifier.height(12.dp))

                            Button(
                                onClick = {
                                    if (isBlocked) showUnblockDialog = true
                                    else showBlockDialog = true
                                },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isBlocked)
                                        Color(0xFF2E7D32)
                                    else
                                        Color.Red
                                )
                            ) {
                                Text(if (isBlocked) "Desbloquear" else "Bloquear")
                            }

                            Spacer(modifier = Modifier.height(24.dp))
                        }
                    }
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(footerHeight)
                    .align(Alignment.BottomCenter)
                    .background(ColorPrincipal)
            )

            if (showBlockDialog) {
                AlertDialog(
                    onDismissRequest = { showBlockDialog = false },
                    confirmButton = {
                        TextButton(onClick = {
                            viewModel.block(token, profile!!.id)
                            showBlockDialog = false
                        }) {
                            Text("Bloquear")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showBlockDialog = false }) {
                            Text("Cancelar")
                        }
                    },
                    title = { Text("Bloquear usuario") },
                    text = { Text("¿Seguro que quieres bloquear a este usuario?") }
                )
            }

            if (showUnblockDialog) {
                AlertDialog(
                    onDismissRequest = { showUnblockDialog = false },
                    confirmButton = {
                        TextButton(onClick = {
                            viewModel.unblock(token, profile!!.id)
                            showUnblockDialog = false
                        }) {
                            Text("Desbloquear")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showUnblockDialog = false }) {
                            Text("Cancelar")
                        }
                    },
                    title = { Text("Desbloquear usuario") },
                    text = { Text("¿Quieres desbloquear a este usuario?") }
                )
            }
        }
    }
}