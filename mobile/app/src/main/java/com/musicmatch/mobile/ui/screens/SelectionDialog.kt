package com.musicmatch.mobile.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.musicmatch.mobile.model.ExperienceLevel
import com.musicmatch.mobile.ui.components.SmartChip
import com.musicmatch.mobile.ui.theme.ColorFondo
import com.musicmatch.mobile.ui.theme.ColorPrincipal
import com.musicmatch.mobile.ui.theme.ColorSecundario
import com.musicmatch.mobile.ui.theme.ColorTexto

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun SelectionDialog(
    title: String,
    availableOptions: List<String>,
    currentSelectedNames: List<String>,
    isInstrumentMode: Boolean,
    currentInstrumentsWithLevel: List<Pair<String, ExperienceLevel>> = emptyList(),
    onAddInstrument: (String, ExperienceLevel) -> Unit = { _, _ -> },
    onAddStyle: (String) -> Unit = {},
    onRemove: (String) -> Unit,
    onDismiss: () -> Unit
) {

    var searchQuery by remember { mutableStateOf("") }
    var tempSelectedByClick by remember { mutableStateOf<String?>(null) }
    var tempLevel by remember { mutableStateOf(ExperienceLevel.PRINCIPIANTE) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = ColorFondo,
        shape = RoundedCornerShape(16.dp),
        title = {
            Column {
                Text(
                    title,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.SansSerif,
                    color = ColorPrincipal
                )

                Spacer(Modifier.height(6.dp))

                Text(
                    "Selecciona y gestiona tus opciones",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.Gray
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    "Cerrar",
                    color = ColorSecundario,
                    fontFamily = FontFamily.SansSerif
                )
            }
        },
        text = {

            Column(modifier = Modifier.fillMaxHeight(0.9f)) {

                // ---------------- SELECTED (CON SCROLL) ----------------
                Text(
                    "Seleccionados",
                    fontWeight = FontWeight.SemiBold,
                    fontFamily = FontFamily.SansSerif,
                    color = ColorTexto
                )

                Spacer(Modifier.height(8.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 140.dp)
                ) {
                    FlowRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .verticalScroll(rememberScrollState())
                            .padding(bottom = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {

                        if (isInstrumentMode) {
                            currentInstrumentsWithLevel.forEach { (name, level) ->
                                SmartChip(
                                    text = name,
                                    level = level,
                                    onRemove = { onRemove(name) }
                                )
                            }
                        } else {
                            currentSelectedNames.forEach { name ->
                                SmartChip(name, onRemove = { onRemove(name) })
                            }
                        }
                    }
                }

                HorizontalDivider()

                Spacer(Modifier.height(10.dp))

                // ---------------- SEARCH ----------------
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    label = { Text("Buscar") },
                    modifier = Modifier.fillMaxWidth(),
                    trailingIcon = {
                        Icon(Icons.Default.Search, null, tint = ColorSecundario)
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )

                val filteredOptions =
                    availableOptions.filter { it.contains(searchQuery, true) }

                Spacer(Modifier.height(10.dp))

                // ---------------- LIST ----------------
                LazyColumn(
                    modifier = Modifier.weight(1f)
                ) {

                    items(filteredOptions) { option ->

                        val isSelected = currentSelectedNames.contains(option)

                        ListItem(
                            headlineContent = {
                                Text(
                                    option,
                                    fontFamily = FontFamily.SansSerif,
                                    color = ColorTexto
                                )
                            },
                            modifier = Modifier.clickable {
                                if (isInstrumentMode) tempSelectedByClick = option
                                else onAddStyle(option)
                            },
                            trailingContent = {
                                if (isSelected) {
                                    Icon(
                                        Icons.Default.Check,
                                        contentDescription = null,
                                        tint = ColorSecundario
                                    )
                                }
                            }
                        )
                    }
                }

                // ---------------- LEVEL SELECTOR ----------------
                if (isInstrumentMode && tempSelectedByClick != null) {

                    Spacer(Modifier.height(10.dp))

                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color.White
                        ),
                        elevation = CardDefaults.cardElevation(4.dp)
                    ) {

                        Column(modifier = Modifier.padding(12.dp)) {

                            Text(
                                "Nivel de $tempSelectedByClick",
                                fontWeight = FontWeight.Bold,
                                color = ColorPrincipal
                            )

                            Spacer(Modifier.height(10.dp))

                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {

                                ExperienceLevel.entries.forEach { level ->

                                    FilterChip(
                                        selected = tempLevel == level,
                                        onClick = { tempLevel = level },
                                        label = {
                                            Text(
                                                level.name.lowercase()
                                                    .replaceFirstChar { it.uppercase() }
                                            )
                                        },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = ColorSecundario,
                                            selectedLabelColor = Color.White
                                        )
                                    )
                                }
                            }

                            Spacer(Modifier.height(12.dp))

                            Button(
                                onClick = {
                                    onAddInstrument(tempSelectedByClick!!, tempLevel)
                                    tempSelectedByClick = null
                                },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = ColorSecundario
                                )
                            ) {
                                Text("Confirmar", color = Color.White)
                            }
                        }
                    }
                }
            }
        }
    )
}