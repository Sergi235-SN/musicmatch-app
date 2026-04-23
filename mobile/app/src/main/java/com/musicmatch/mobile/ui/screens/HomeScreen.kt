package com.musicmatch.mobile.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FilterList
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
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.musicmatch.mobile.data.ApiService
import com.musicmatch.mobile.data.repository.ProfileSearchRepository
import com.musicmatch.mobile.data.repository.UserRepository
import com.musicmatch.mobile.navigation.Screen
import com.musicmatch.mobile.model.ExperienceLevel
import com.musicmatch.mobile.ui.theme.*
import com.musicmatch.mobile.utils.NetworkConfig
import com.musicmatch.mobile.utils.TokenManager
import com.musicmatch.mobile.viewmodel.HomeSearchViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(navController: NavController) {

    val context = LocalContext.current
    val token = remember { TokenManager().getToken(context) ?: "" }

    val api = remember { ApiService.create() }

    val searchRepo = remember { ProfileSearchRepository(api) }
    val userRepo = remember { UserRepository(api) }

    val viewModel: HomeSearchViewModel = viewModel(
        factory = HomeSearchViewModel.Factory(searchRepo, userRepo)
    )

    val profiles by viewModel.profiles.collectAsState()
    val query by viewModel.query.collectAsState()
    val selectedExperience by viewModel.experience.collectAsState()

    val instruments by viewModel.selectedInstruments.collectAsState()
    val styles by viewModel.selectedStyles.collectAsState()

    val availableInstruments by viewModel.availableInstruments.collectAsState()
    val availableStyles by viewModel.availableStyles.collectAsState()

    var showFilters by remember { mutableStateOf(false) }
    var instrumentSearch by remember { mutableStateOf("") }
    var styleSearch by remember { mutableStateOf("") }

    LaunchedEffect(token) {
        viewModel.search(token)
        viewModel.loadFilters(token)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Buscar músicos", color = Color.White)
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

            Column(Modifier.fillMaxSize().padding(12.dp)) {

                TextField(
                    value = query,
                    onValueChange = { viewModel.setQuery(it, token) },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Buscar usuario...") }
                )

                Spacer(Modifier.height(10.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.Center
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {

                        ExperienceLevel.entries.forEach { level ->

                            FilterChip(
                                selected = selectedExperience == level.name,
                                onClick = {
                                    viewModel.setExperience(
                                        if (selectedExperience == level.name) null else level.name,
                                        token
                                    )
                                },
                                label = { Text(level.name) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = Color(0xFFD7F3E3),
                                    selectedLabelColor = Color(0xFF0F5132)
                                )
                            )
                        }
                    }
                }

                Spacer(Modifier.height(10.dp))

                val activeFilters = buildList {
                    instruments.forEach { id ->
                        val name = availableInstruments.find { it.id == id }?.name ?: "Inst"
                        add(Triple("instrument", id, name))
                    }

                    styles.forEach { id ->
                        val name = availableStyles.find { it.id == id }?.name ?: "Style"
                        add(Triple("style", id, name))
                    }
                }


                val visibleFilters = activeFilters.take(2)
                val remainingCount = activeFilters.size - visibleFilters.size

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {

                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        maxItemsInEachRow = 3
                    ) {

                        visibleFilters.forEach { (type, id, name) ->

                            AssistChip(
                                onClick = {
                                    when (type) {
                                        "instrument" -> viewModel.toggleInstrument(id, token)
                                        "style" -> viewModel.toggleStyle(id, token)
                                    }
                                },
                                label = {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(name)
                                        Spacer(Modifier.width(6.dp))
                                        Text("×")
                                    }
                                },
                                shape = MaterialTheme.shapes.medium,
                                colors = AssistChipDefaults.assistChipColors(
                                    containerColor = Color(0xFFE3F2FD), 
                                    labelColor = Color(0xFF1565C0)
                                )
                            )
                        }

                        if (remainingCount > 0) {

                            AssistChip(
                                onClick = { showFilters = true },
                                label = { Text("+$remainingCount") },
                                shape = MaterialTheme.shapes.medium,
                                colors = AssistChipDefaults.assistChipColors(
                                    containerColor = Color(0xFFF1F3F4), 
                                    labelColor = Color(0xFF444444)
                                )
                            )
                        }
                    }
                }

                Spacer(Modifier.height(10.dp))

                LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {

                    items(profiles) { user ->

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    navController.navigate(
                                        Screen.PublicProfile.createRoute(user.id)
                                    )
                                }
                        ) {

                            Row(Modifier.padding(12.dp)) {

                                Box(
                                    modifier = Modifier
                                        .size(56.dp)
                                        .clip(CircleShape)
                                        .background(Color.LightGray),
                                    contentAlignment = Alignment.Center
                                ) {

                                    if (!user.profilePicture.isNullOrEmpty()) {
                                        AsyncImage(
                                            model = NetworkConfig.getAvatarUrl(user.profilePicture),
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
                                    Text(
                                        text = user.username ?: "Usuario",
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = user.city ?: "",
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                    Text(
                                        text = user.experienceLevel ?: "",
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                            }
                        }
                    }
                }
            }

            FloatingActionButton(
                onClick = { showFilters = true },
                containerColor = ColorPrincipal,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp)
            ) {
                Icon(Icons.Default.FilterList, contentDescription = "Filtros", tint = Color.White)
            }
        }
    }

    if (showFilters) {

        var selectedTab by remember { mutableStateOf(0) }
        val tabs = listOf("Instrumentos", "Estilos")

        var instrumentSearch by remember { mutableStateOf("") }
        var styleSearch by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showFilters = false },
            confirmButton = {
                TextButton(onClick = { showFilters = false }) {
                    Text("Cerrar")
                }
            },
            title = {
                Text("Filtros")
            },
            text = {

                Column(
                    modifier = Modifier
                        .height(600.dp)
                        .fillMaxWidth()
                ) {


                    TabRow(selectedTabIndex = selectedTab) {
                        tabs.forEachIndexed { index, title ->
                            Tab(
                                selected = selectedTab == index,
                                onClick = { selectedTab = index },
                                text = { Text(title) }
                            )
                        }
                    }

                    Spacer(Modifier.height(12.dp))

                    when (selectedTab) {

                        0 -> {

                            TextField(
                                value = instrumentSearch,
                                onValueChange = { instrumentSearch = it },
                                modifier = Modifier.fillMaxWidth(),
                                placeholder = { Text("Buscar instrumento...") }
                            )

                            Spacer(Modifier.height(10.dp))

                            Column(
                                modifier = Modifier.verticalScroll(rememberScrollState())
                            ) {
                                availableInstruments
                                    .filter { it.name.contains(instrumentSearch, true) }
                                    .forEach { inst ->
                                        FilterChip(
                                            selected = instruments.contains(inst.id),
                                            onClick = {
                                                viewModel.toggleInstrument(inst.id, token)
                                            },
                                            label = { Text(inst.name) }
                                        )
                                    }
                            }
                        }

                        1 -> {

                            TextField(
                                value = styleSearch,
                                onValueChange = { styleSearch = it },
                                modifier = Modifier.fillMaxWidth(),
                                placeholder = { Text("Buscar estilo...") }
                            )

                            Spacer(Modifier.height(10.dp))

                            Column(
                                modifier = Modifier.verticalScroll(rememberScrollState())
                            ) {
                                availableStyles
                                    .filter { it.name.contains(styleSearch, true) }
                                    .forEach { style ->
                                        FilterChip(
                                            selected = styles.contains(style.id),
                                            onClick = {
                                                viewModel.toggleStyle(style.id, token)
                                            },
                                            label = { Text(style.name) }
                                        )
                                    }
                            }
                        }
                    }
                }
            }
        )
    }

}