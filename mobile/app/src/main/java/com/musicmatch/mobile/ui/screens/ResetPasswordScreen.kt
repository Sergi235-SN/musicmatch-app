package com.musicmatch.mobile.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.musicmatch.mobile.data.ApiService
import com.musicmatch.mobile.data.repository.UserRepository
import com.musicmatch.mobile.ui.components.PasswordLabelledTextField
import com.musicmatch.mobile.ui.theme.ColorFondo
import com.musicmatch.mobile.ui.theme.ColorPrincipal
import com.musicmatch.mobile.ui.theme.ColorSecundario
import kotlinx.coroutines.launch

@Composable
fun ResetPasswordScreen(
    token: String,
    onPasswordResetSuccess: () -> Unit = {},
    onNavigateBack: () -> Unit = {}
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val repository = remember { UserRepository(ApiService.create()) }

    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(false) }

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
                text = "Nueva contraseña",
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
                    .padding(horizontal = 40.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Escribe tu nueva contraseña.",
                    fontSize = 15.sp,
                    color = Color.DarkGray
                )

                Spacer(modifier = Modifier.height(24.dp))

                PasswordLabelledTextField(
                    label = "Nueva contraseña",
                    value = password,
                    onValueChange = { password = it }
                )

                Spacer(modifier = Modifier.height(16.dp))

                PasswordLabelledTextField(
                    label = "Repetir contraseña",
                    value = confirmPassword,
                    onValueChange = { confirmPassword = it }
                )

                Text(
                    text = "Mínimo 8 caracteres, 1 mayúscula, 1 minúscula y 1 número",
                    fontSize = 12.sp,
                    color = Color.Gray,
                    modifier = Modifier.padding(start = 4.dp, top = 8.dp)
                )

                Spacer(modifier = Modifier.height(32.dp))

                Button(
                    onClick = {
                        if (token.isBlank()) {
                            Toast.makeText(context, "Token de recuperación inválido", Toast.LENGTH_SHORT).show()
                            return@Button
                        }

                        if (password.isBlank() || confirmPassword.isBlank()) {
                            Toast.makeText(context, "Completa todos los campos", Toast.LENGTH_SHORT).show()
                            return@Button
                        }

                        if (password != confirmPassword) {
                            Toast.makeText(context, "Las contraseñas no coinciden", Toast.LENGTH_SHORT).show()
                            return@Button
                        }

                        scope.launch {
                            loading = true
                            try {
                                val response = repository.resetPassword(token, password)
                                Toast.makeText(context, response.message, Toast.LENGTH_LONG).show()

                                if (response.success) {
                                    onPasswordResetSuccess()
                                }

                            } catch (e: Exception) {
                                Toast.makeText(context, "Error de conexión", Toast.LENGTH_SHORT).show()
                            } finally {
                                loading = false
                            }
                        }
                    },
                    enabled = !loading,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = ColorSecundario),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = if (loading) "Guardando..." else "Guardar contraseña",
                        fontSize = 16.sp,
                        color = Color.White
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                TextButton(onClick = onNavigateBack) {
                    Text(
                        text = "Volver",
                        color = ColorPrincipal,
                        fontWeight = FontWeight.SemiBold
                    )
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