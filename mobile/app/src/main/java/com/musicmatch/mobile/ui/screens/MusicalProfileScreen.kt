package com.musicmatch.mobile.ui.screens

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.musicmatch.mobile.data.ApiService
import com.musicmatch.mobile.data.repository.UserRepository
import com.musicmatch.mobile.model.ExperienceLevel
import com.musicmatch.mobile.ui.components.SmartChip
import com.musicmatch.mobile.utils.TokenManager
import com.musicmatch.mobile.viewmodel.MusicalProfileViewModel
import com.musicmatch.mobile.ui.theme.ColorFondo
import com.musicmatch.mobile.ui.theme.ColorPrincipal
import com.musicmatch.mobile.ui.theme.ColorSecundario
import com.musicmatch.mobile.ui.theme.ColorTexto

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun MusicalProfileScreen(
    onNavigateNext: () -> Unit
) {

    val context = LocalContext.current

    val viewModel: MusicalProfileViewModel = viewModel(
        factory = remember {
            MusicalProfileViewModel.Factory(
                repository = UserRepository(ApiService.create()),
                tokenManager = TokenManager()
            )
        }
    )

    var showInstrumentDialog by remember { mutableStateOf(false) }
    var showStyleDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.loadData(context)
    }

    if (showInstrumentDialog) {
        SelectionDialog(
            title = "Editar Instrumentos",
            availableOptions = viewModel.availableInstruments.map { it.name },
            isInstrumentMode = true,
            currentSelectedNames = viewModel.selectedInstruments.map { it.first.name },
            currentInstrumentsWithLevel = viewModel.selectedInstruments.map {
                it.first.name to it.second
            },
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
            title = "Editar Estilos",
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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ColorFondo)
    ) {

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp)
                .background(ColorPrincipal),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Perfil Musical",
                color = Color.White,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.SansSerif
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            contentAlignment = Alignment.Center
        ) {

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 40.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(26.dp)
            ) {

                Spacer(modifier = Modifier.height(12.dp))

                SectionHeader("Instrumentos", onEdit = { showInstrumentDialog = true })

                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    viewModel.selectedInstruments.take(6).forEach { (item, level) ->
                        SmartChip(text = item.name, level = level)
                    }

                    if (viewModel.selectedInstruments.size > 6) {
                        SmartChip(text = "+${viewModel.selectedInstruments.size - 6}")
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                SectionHeader("Estilos", onEdit = { showStyleDialog = true })

                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    viewModel.selectedStyles.take(6).forEach { item ->
                        SmartChip(text = item.name)
                    }

                    if (viewModel.selectedStyles.size > 6) {
                        SmartChip(text = "+${viewModel.selectedStyles.size - 6}")
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "Nivel de experiencia global",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = ColorTexto
                )

                ExperienceDropdown(viewModel)

                Spacer(modifier = Modifier.height(14.dp))

                Button(
                    onClick = {
                        viewModel.saveStep1(context, onNavigateNext)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = ColorSecundario),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    if (viewModel.isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(22.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text("Continuar", fontSize = 16.sp, color = Color.White)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
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

@Composable
fun SectionHeader(title: String, onEdit: () -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            title,
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            color = ColorTexto
        )

        Spacer(modifier = Modifier.weight(1f))

        IconButton(onClick = onEdit) {
            Icon(
                imageVector = Icons.Default.Edit,
                contentDescription = null,
                tint = ColorSecundario
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExperienceDropdown(viewModel: MusicalProfileViewModel) {

    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {

        OutlinedTextField(
            value = viewModel.globalExperience.name.lowercase()
                .replaceFirstChar { it.uppercase() },
            onValueChange = {},
            readOnly = true,
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth(),
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded)
            }
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {

            ExperienceLevel.entries.forEach { level ->

                val color = when (level) {
                    ExperienceLevel.PRINCIPIANTE -> Color(0xFF4CAF50)
                    ExperienceLevel.INTERMEDIO -> Color(0xFFFFC107)
                    ExperienceLevel.AVANZADO -> Color(0xFFF44336)
                }

                DropdownMenuItem(
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(12.dp)
                                    .background(color, RoundedCornerShape(50))
                            )

                            Spacer(modifier = Modifier.width(8.dp))

                            Text(
                                level.name.lowercase().replaceFirstChar { it.uppercase() }
                            )
                        }
                    },
                    onClick = {
                        viewModel.globalExperience = level
                        expanded = false
                    }
                )
            }
        }
    }
}