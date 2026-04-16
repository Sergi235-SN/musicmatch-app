package com.musicmatch.mobile.ui.components

import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.*
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.ImeAction

@Composable
fun PasswordLabelledTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit
) {
    var passwordVisible by remember { mutableStateOf(false) }

    CustomLabelledTextField(
        label = label,
        value = value,
        onValueChange = onValueChange,
        visualTransformation = if (passwordVisible)
            VisualTransformation.None
        else
            PasswordVisualTransformation(),

        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Password,
            autoCorrect = false,
            imeAction = ImeAction.Done
        ),

        isPassword = true,

        trailingIcon = {
            val image =
                if (passwordVisible) Icons.Filled.Visibility
                else Icons.Filled.VisibilityOff

            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                Icon(imageVector = image, contentDescription = null)
            }
        }
    )
}