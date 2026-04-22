package com.musicmatch.mobile.ui.screens

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
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
import com.musicmatch.mobile.navigation.Screen
import com.musicmatch.mobile.ui.components.SmartChip
import com.musicmatch.mobile.ui.theme.*
import com.musicmatch.mobile.utils.TokenManager
import com.musicmatch.mobile.viewmodel.MusicalProfileViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ProfileScreen(
    navController: NavController
) {
    val context = LocalContext.current
    val tokenManager = remember { TokenManager() }

    val viewModel: MusicalProfileViewModel = viewModel(
        factory = remember {
            MusicalProfileViewModel.Factory(
                repository = UserRepository(ApiService.create()),
                tokenManager = tokenManager
            )
        }
    )

    var editing by remember { mutableStateOf(false) }
    var menuExpanded by remember { mutableStateOf(false) }
    var showInstrumentDialog by remember { mutableStateOf(false) }
    var showStyleDialog by remember { mutableStateOf(false) }

    val scrollState = rememberScrollState()

    val imagePicker = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { if (editing) viewModel.imageUri = it }
    }

    fun logout() {
        tokenManager.clearToken(context)
        navController.navigate("login") {
            popUpTo(0) { inclusive = true }
        }
    }

    LaunchedEffect(Unit) {
        viewModel.loadData(context)
    }

    LaunchedEffect(viewModel.errorMessage) {
        viewModel.errorMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.clearError()
        }
    }

    if (showInstrumentDialog) {
        SelectionDialog(
            title = "Editar instrumentos",
            availableOptions = viewModel.availableInstruments.map { it.name },
            isInstrumentMode = true,
            currentSelectedNames = viewModel.selectedInstruments.map { it.first.name },
            currentInstrumentsWithLevel = viewModel.selectedInstruments.map { it.first.name to it.second },
            onAddInstrument = { name, level ->
                val instrument = viewModel.availableInstruments.firstOrNull { it.name == name }
                if (instrument != null) {
                    viewModel.selectedInstruments.removeAll { it.first.name == name }
                    viewModel.selectedInstruments.add(instrument to level)
                }
            },
            onRemove = { name ->
                viewModel.selectedInstruments.removeAll { it.first.name == name }
            },
            onDismiss = { showInstrumentDialog = false }
        )
    }

    if (showStyleDialog) {
        SelectionDialog(
            title = "Editar estilos",
            availableOptions = viewModel.availableStyles.map { it.name },
            isInstrumentMode = false,
            currentSelectedNames = viewModel.selectedStyles.map { it.name },
            onAddStyle = { name ->
                val style = viewModel.availableStyles.firstOrNull { it.name == name }
                if (style != null && !viewModel.selectedStyles.contains(style)) {
                    viewModel.selectedStyles.add(style)
                }
            },
            onRemove = { name ->
                viewModel.selectedStyles.removeAll { it.name == name }
            },
            onDismiss = { showStyleDialog = false }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Text("Mi Perfil", color = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = ColorPrincipal,
                    titleContentColor = Color.White
                ),
                actions = {
                    IconButton(onClick = { menuExpanded = true }) {
                        Icon(Icons.Default.Menu, null, tint = Color.White)
                    }
                    DropdownMenu(
                        expanded = menuExpanded,
                        onDismissRequest = { menuExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Usuarios bloqueados") },
                            onClick = {
                                menuExpanded = false
                                navController.navigate(Screen.BlockedUsers.route)
                            }
                        )

                        DropdownMenuItem(
                            text = { Text("Cerrar sesión") },
                            onClick = {
                                menuExpanded = false
                                logout()
                            }
                        )
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(ColorFondo)
                .verticalScroll(scrollState)
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                IconButton(
                    onClick = { editing = !editing },
                    enabled = !viewModel.isLoading
                ) {
                    Icon(
                        Icons.Default.Edit,
                        null,
                        tint = if (editing) ColorSecundario else Color.Gray
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(90.dp)
                        .clip(CircleShape)
                        .background(Color.LightGray)
                        .clickable(enabled = editing && !viewModel.isLoading) {
                            imagePicker.launch("image/*")
                        },
                    contentAlignment = Alignment.Center
                ) {
                    when {
                        viewModel.imageUri != null -> AsyncImage(
                            model = viewModel.imageUri,
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )

                        !viewModel.currentAvatarUrl.isNullOrEmpty() -> AsyncImage(
                            model = viewModel.currentAvatarUrl,
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )

                        else -> Icon(
                            Icons.Default.Person,
                            null,
                            modifier = Modifier.size(40.dp)
                        )
                    }
                }

                Spacer(Modifier.width(16.dp))

                Column(modifier = Modifier.alpha(if (editing) 1f else 0.8f)) {
                    Text(
                        viewModel.username ?: "Usuario",
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                    Text(viewModel.email ?: "email", color = Color.Gray)
                }
            }

            OutlinedTextField(
                value = viewModel.biography,
                onValueChange = { if (editing) viewModel.biography = it },
                enabled = editing && !viewModel.isLoading,
                label = { Text("Biografía") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp)
            )

            var expandedCity by remember { mutableStateOf(false) }
            ExposedDropdownMenuBox(
                expanded = expandedCity && editing && !viewModel.isLoading,
                onExpandedChange = {
                    if (editing && !viewModel.isLoading) expandedCity = !expandedCity
                }
            ) {
                OutlinedTextField(
                    value = viewModel.availableCities.find { it.id == viewModel.selectedCityId }?.name ?: "Selecciona ciudad",
                    onValueChange = {},
                    readOnly = true,
                    enabled = editing && !viewModel.isLoading,
                    label = { Text("Ciudad") },
                    modifier = Modifier.menuAnchor().fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expandedCity && editing && !viewModel.isLoading) }
                )
                ExposedDropdownMenu(
                    expanded = expandedCity,
                    onDismissRequest = { expandedCity = false }
                ) {
                    viewModel.availableCities.forEach { city ->
                        DropdownMenuItem(
                            text = { Text(city.name) },
                            onClick = {
                                viewModel.selectedCityId = city.id
                                expandedCity = false
                            }
                        )
                    }
                }
            }

            var expandedExp by remember { mutableStateOf(false) }
            ExposedDropdownMenuBox(
                expanded = expandedExp && editing && !viewModel.isLoading,
                onExpandedChange = {
                    if (editing && !viewModel.isLoading) expandedExp = !expandedExp
                }
            ) {
                OutlinedTextField(
                    value = viewModel.globalExperience.name.lowercase().replaceFirstChar { it.uppercase() },
                    onValueChange = {},
                    readOnly = true,
                    enabled = editing && !viewModel.isLoading,
                    label = { Text("Experiencia Global") },
                    modifier = Modifier.menuAnchor().fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expandedExp && editing && !viewModel.isLoading) }
                )
                ExposedDropdownMenu(
                    expanded = expandedExp,
                    onDismissRequest = { expandedExp = false }
                ) {
                    ExperienceLevel.entries.forEach { level ->
                        DropdownMenuItem(
                            text = { Text(level.name.lowercase().replaceFirstChar { it.uppercase() }) },
                            onClick = {
                                viewModel.globalExperience = level
                                expandedExp = false
                            }
                        )
                    }
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Instrumentos", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Spacer(Modifier.weight(1f))
                if (editing) {
                    IconButton(
                        onClick = { showInstrumentDialog = true },
                        enabled = !viewModel.isLoading
                    ) {
                        Icon(Icons.Default.Edit, null, tint = ColorSecundario)
                    }
                }
            }

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                viewModel.selectedInstruments.take(6).forEach { (item, level) ->
                    SmartChip(text = item.name, level = level)
                }
                if (viewModel.selectedInstruments.size > 6) {
                    SmartChip(text = "+${viewModel.selectedInstruments.size - 6}")
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Estilos", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Spacer(Modifier.weight(1f))
                if (editing) {
                    IconButton(
                        onClick = { showStyleDialog = true },
                        enabled = !viewModel.isLoading
                    ) {
                        Icon(Icons.Default.Edit, null, tint = ColorSecundario)
                    }
                }
            }

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                viewModel.selectedStyles.take(6).forEach { item ->
                    SmartChip(text = item.name)
                }
                if (viewModel.selectedStyles.size > 6) {
                    SmartChip(text = "+${viewModel.selectedStyles.size - 6}")
                }
            }

            if (editing) {
                Spacer(Modifier.height(10.dp))
                Button(
                    onClick = { viewModel.saveFullProfile(context) { editing = false } },
                    enabled = !viewModel.isLoading,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = ColorSecundario),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    if (viewModel.isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text("Guardar cambios", color = Color.White, fontSize = 16.sp)
                    }
                }
            }

            Spacer(Modifier.height(20.dp))
        }
    }
}