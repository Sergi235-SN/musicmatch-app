package com.musicmatch.mobile.ui.screens

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import coil.compose.AsyncImage
import com.musicmatch.mobile.viewmodel.MusicalProfileViewModel
import com.musicmatch.mobile.data.ApiService
import com.musicmatch.mobile.data.repository.UserRepository
import com.musicmatch.mobile.ui.theme.ColorFondo
import com.musicmatch.mobile.ui.theme.ColorPrincipal
import com.musicmatch.mobile.ui.theme.ColorSecundario
import com.musicmatch.mobile.utils.TokenManager
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MusicalProfileStep2Screen(
    onFinish: () -> Unit
) {
    val context = LocalContext.current

    val viewModel: MusicalProfileViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
        factory = remember {
            MusicalProfileViewModel.Factory(
                repository = UserRepository(ApiService.create()),
                tokenManager = TokenManager()
            )
        }
    )

    LaunchedEffect(Unit) {
        viewModel.loadData(context)
    }

    LaunchedEffect(viewModel.errorMessage) {
        viewModel.errorMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.clearError()
        }
    }

    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { viewModel.imageUri = it }
    }

    val bioLimit = 160
    val bioLength = viewModel.biography.length

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
                text = "Completa tu perfil",
                color = Color.White,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
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
                    .padding(horizontal = 40.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                Box(
                    modifier = Modifier
                        .size(135.dp)
                        .clip(CircleShape)
                        .background(Color.LightGray)
                        .clickable(enabled = !viewModel.isLoading) {
                            imagePicker.launch("image/*")
                        },
                    contentAlignment = Alignment.Center
                ) {
                    when {
                        viewModel.imageUri != null -> {
                            AsyncImage(
                                model = viewModel.imageUri,
                                contentDescription = null,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        }

                        !viewModel.currentAvatarUrl.isNullOrEmpty() -> {
                            AsyncImage(
                                model = viewModel.currentAvatarUrl,
                                contentDescription = null,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        }

                        else -> {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = "Perfil por defecto",
                                tint = Color.Gray,
                                modifier = Modifier.size(60.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(60.dp))

                OutlinedTextField(
                    value = viewModel.biography,
                    onValueChange = {
                        if (it.length <= bioLimit) {
                            viewModel.biography = it
                        }
                    },
                    label = { Text("Biografía") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    shape = RoundedCornerShape(10.dp),
                    enabled = !viewModel.isLoading
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 6.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    Text(
                        text = "$bioLength / $bioLimit",
                        color = if (bioLength > bioLimit - 20)
                            Color.Red
                        else
                            Color.Gray,
                        fontSize = 12.sp
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                var expanded by remember { mutableStateOf(false) }

                ExposedDropdownMenuBox(
                    expanded = expanded && !viewModel.isLoading,
                    onExpandedChange = {
                        if (!viewModel.isLoading) expanded = !expanded
                    }
                ) {
                    OutlinedTextField(
                        value = viewModel.availableCities
                            .firstOrNull { it.id == viewModel.selectedCityId }
                            ?.name ?: "Selecciona ciudad",
                        onValueChange = {},
                        readOnly = true,
                        enabled = !viewModel.isLoading,
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )

                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        viewModel.availableCities.forEach { city ->
                            DropdownMenuItem(
                                text = { Text(city.name) },
                                onClick = {
                                    viewModel.selectedCityId = city.id
                                    expanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(50.dp))

                Button(
                    onClick = {
                        viewModel.saveStep2(context, onFinish)
                    },
                    enabled = !viewModel.isLoading,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ColorSecundario
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    if (viewModel.isLoading) {
                        CircularProgressIndicator(
                            color = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    } else {
                        Text("Finalizar", color = Color.White)
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