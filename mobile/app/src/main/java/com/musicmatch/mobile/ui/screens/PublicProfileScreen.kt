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
    val token = remember { TokenManager().getToken(context) }

    val viewModel: PublicProfileViewModel = viewModel(
        factory = remember {
            PublicProfileViewModel.Factory(
                UserRepository(ApiService.create())
            )
        }
    )

    var showInstruments by remember { mutableStateOf(false) }
    var showStyles by remember { mutableStateOf(false) }

    LaunchedEffect(userId) {
        viewModel.load(userId, token ?: "")
    }

    val profile = viewModel.profile
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
    ) { scaffoldPadding ->

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

                else -> {

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(
                                start = 20.dp,
                                end = 20.dp,
                                top = scaffoldPadding.calculateTopPadding() + 20.dp,
                                bottom = footerHeight + 40.dp
                            ),
                        verticalArrangement = Arrangement.Center
                    ) {

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
                                    text = profile.experienceLevel?.toString() ?: "",
                                    color = Color.Gray
                                )

                            }
                        }

                        Spacer(Modifier.height(32.dp))

                        Text("Instrumentos", fontWeight = FontWeight.Bold, fontSize = 18.sp)

                        val instruments = profile.instruments ?: emptyList()

                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            val visible = instruments.take(6)

                            visible.forEach { inst ->
                                val name =
                                    viewModel.availableInstruments
                                        .find { it.id == inst.instrumentId }
                                        ?.name ?: "Instrumento"

                                SmartChip(
                                    text = name,
                                    level = inst.level ?: ExperienceLevel.PRINCIPIANTE
                                )
                            }

                            if (instruments.size > 6) {
                                SmartChip(
                                    text = "+${instruments.size - 6}",
                                    onClick = { showInstruments = true }
                                )
                            }
                        }

                        Spacer(Modifier.height(16.dp))

                        Text("Estilos", fontWeight = FontWeight.Bold, fontSize = 18.sp)

                        val styles = profile.styleIds ?: emptyList()

                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            val visible = styles.take(6)

                            visible.forEach { id ->
                                val name =
                                    viewModel.availableStyles
                                        .find { it.id == id }
                                        ?.name ?: "Estilo"

                                SmartChip(text = name)
                            }

                            if (styles.size > 6) {
                                SmartChip(
                                    text = "+${styles.size - 6}",
                                    onClick = { showStyles = true }
                                )
                            }
                        }

                        Spacer(Modifier.height(32.dp))

                        Button(
                            onClick = { },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = ColorSecundario)
                        ) {
                            Text("Iniciar chat", color = Color.White)
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

            if (showInstruments) {
                AlertDialog(
                    onDismissRequest = { showInstruments = false },
                    confirmButton = {
                        TextButton(onClick = { showInstruments = false }) {
                            Text("Cerrar")
                        }
                    },
                    title = { Text("Instrumentos") },
                    text = {
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            profile?.instruments?.forEach { inst ->
                                val name =
                                    viewModel.availableInstruments
                                        .find { it.id == inst.instrumentId }
                                        ?.name ?: "Instrumento"

                                SmartChip(
                                    text = name,
                                    level = inst.level ?: ExperienceLevel.PRINCIPIANTE
                                )
                            }
                        }
                    }
                )
            }

            if (showStyles) {
                AlertDialog(
                    onDismissRequest = { showStyles = false },
                    confirmButton = {
                        TextButton(onClick = { showStyles = false }) {
                            Text("Cerrar")
                        }
                    },
                    title = { Text("Estilos") },
                    text = {
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            profile?.styleIds?.forEach { id ->
                                val name =
                                    viewModel.availableStyles
                                        .find { it.id == id }
                                        ?.name ?: "Estilo"

                                SmartChip(text = name)
                            }
                        }
                    }
                )
            }
        }
    }
}