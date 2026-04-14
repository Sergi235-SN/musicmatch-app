package com.musicmatch.mobile.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import com.musicmatch.mobile.model.ExperienceLevel
import com.musicmatch.mobile.ui.theme.ColorPrincipal
import com.musicmatch.mobile.ui.theme.ColorSecundario

@Composable
fun SmartChip(
    text: String,
    modifier: Modifier = Modifier,
    level: ExperienceLevel? = null,
    isEditIcon: Boolean = false,
    onRemove: (() -> Unit)? = null,
    onClick: () -> Unit = {}
) {

    val containerColor = when {
        isEditIcon -> ColorSecundario.copy(alpha = 0.15f)
        level == null -> Color(0xFFE0E0E0)
        level == ExperienceLevel.PRINCIPIANTE -> Color(0xFFF1F8E9)
        level == ExperienceLevel.INTERMEDIO -> Color(0xFFFFF9C4)
        level == ExperienceLevel.AVANZADO -> Color(0xFFFFEBEE)
        else -> Color(0xFFE0E0E0)
    }

    val contentColor =
        if (level == ExperienceLevel.AVANZADO) Color(0xFFC62828)
        else ColorPrincipal

    Surface(
        modifier = modifier
            .padding(4.dp)
            .clickable { onClick() }, // Se ejecuta la lambda onClick
        shape = RoundedCornerShape(20.dp),
        color = containerColor,
        contentColor = contentColor,
        tonalElevation = 1.dp
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.labelLarge,
                fontFamily = FontFamily.SansSerif
            )

            if (onRemove != null) {
                Spacer(Modifier.width(6.dp))
                Icon(
                    Icons.Default.Close,
                    contentDescription = "Eliminar",
                    modifier = Modifier
                        .size(16.dp)
                        .clickable { onRemove() },
                    tint = contentColor
                )
            }
        }
    }
}