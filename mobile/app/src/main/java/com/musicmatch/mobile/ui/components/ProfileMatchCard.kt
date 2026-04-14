package com.musicmatch.mobile.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.musicmatch.mobile.model.dto.ProfileCardDTO
import com.musicmatch.mobile.viewmodel.MatchViewModel
import com.musicmatch.mobile.utils.NetworkConfig

@Composable
fun ProfileMatchCard(
    profile: ProfileCardDTO,
    viewModel: MatchViewModel,
    onLike: () -> Unit,
    onDislike: () -> Unit,
    onOpenProfile: () -> Unit
) {

    var showInstruments by remember { mutableStateOf(false) }
    var showStyles by remember { mutableStateOf(false) }

    val imageUrl = profile.profilePicture?.let {
        NetworkConfig.getAvatarUrl(it)
    }

    val instruments = profile.instruments ?: emptyList()
    val styles = profile.styles ?: emptyList()

    val maxVisible = 3

    Card(
        modifier = Modifier
            .fillMaxWidth(0.92f)
            .height(600.dp)
            .clickable { onOpenProfile() },
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(10.dp)
    ) {

        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxSize()
        ) {

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(240.dp),
                contentAlignment = Alignment.Center
            ) {

                if (!imageUrl.isNullOrEmpty()) {
                    AsyncImage(
                        model = imageUrl,
                        contentDescription = null,
                        modifier = Modifier
                            .size(130.dp)
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(
                        Icons.Default.Person,
                        null,
                        modifier = Modifier.size(130.dp),
                        tint = Color.Gray
                    )
                }
            }

            Spacer(Modifier.height(10.dp))

            Text(profile.username, fontSize = 22.sp)
            Text(profile.city ?: "", style = MaterialTheme.typography.bodySmall)

            Text(
                text = profile.profileLevel.toString(),
                fontSize = 13.sp,
                color = Color.Gray
            )

            Spacer(Modifier.height(10.dp))

            Text("Instrumentos")

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {

                instruments.take(maxVisible).forEach { inst ->
                    SmartChip(
                        text = viewModel.getInstrumentName(inst.instrumentId),
                        level = inst.level
                    )
                }

                if (instruments.size > maxVisible) {
                    Box(
                        modifier = Modifier
                            .wrapContentWidth()
                            .height(IntrinsicSize.Min)
                    ) {
                        SmartChip(
                            text = "+${instruments.size - maxVisible}",
                            onClick = { showInstruments = true }
                        )
                    }
                }
            }

            Spacer(Modifier.height(10.dp))

            Text("Estilos")

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {

                styles.take(maxVisible).forEach { id ->
                    SmartChip(
                        text = viewModel.getStyleName(id),
                        level = null
                    )
                }

                if (styles.size > maxVisible) {
                    Box(
                        modifier = Modifier
                            .wrapContentWidth()
                            .height(IntrinsicSize.Min)
                    ) {
                        SmartChip(
                            text = "+${styles.size - maxVisible}",
                            onClick = { showStyles = true }
                        )
                    }
                }
            }

            Spacer(Modifier.weight(1f))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {

                FloatingActionButton(
                    onClick = onDislike,
                    containerColor = Color(0xFFE53935)
                ) {
                    Icon(Icons.Default.Close, null, tint = Color.White)
                }

                FloatingActionButton(
                    onClick = onLike,
                    containerColor = Color(0xFF43A047)
                ) {
                    Icon(Icons.Default.Favorite, null, tint = Color.White)
                }
            }
        }
    }

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
                Column(
                    modifier = Modifier
                        .heightIn(max = 400.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    instruments.forEach { inst ->
                        SmartChip(
                            text = viewModel.getInstrumentName(inst.instrumentId),
                            level = inst.level
                        )
                        Spacer(Modifier.height(6.dp))
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
                Column(
                    modifier = Modifier
                        .heightIn(max = 400.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    styles.forEach { id ->
                        SmartChip(
                            text = viewModel.getStyleName(id),
                            level = null
                        )
                        Spacer(Modifier.height(6.dp))
                    }
                }
            }
        )
    }
}