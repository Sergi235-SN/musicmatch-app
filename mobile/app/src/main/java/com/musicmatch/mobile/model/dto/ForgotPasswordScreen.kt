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
import com.musicmatch.mobile.ui.components.CustomLabelledTextField
import com.musicmatch.mobile.ui.theme.ColorFondo
import com.musicmatch.mobile.ui.theme.ColorPrincipal
import com.musicmatch.mobile.ui.theme.ColorSecundario
import kotlinx.coroutines.launch

@Composable
fun ForgotPasswordScreen(
    onNavigateBack: () -> Unit = {}
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val repository = remember { UserRepository(ApiService.create()) }

    var email by remember { mutableStateOf("") }
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
                text = "Recuperar contraseña",
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
                    text = "Introduce tu correo y te enviaremos un enlace para restablecer la contraseña.",
                    fontSize = 15.sp,
                    color = Color.DarkGray
                )

                Spacer(modifier = Modifier.height(24.dp))

                CustomLabelledTextField(
                    label = "Correo electrónico",
                    value = email,
                    onValueChange = { email = it }
                )

                Spacer(modifier = Modifier.height(32.dp))

                Button(
                    onClick = {
                        if (email.isBlank()) {
                            Toast.makeText(context, "Introduce tu correo", Toast.LENGTH_SHORT).show()
                            return@Button
                        }

                        scope.launch {
                            loading = true
                            try {
                                val response = repository.forgotPassword(email)
                                Toast.makeText(context, response.message, Toast.LENGTH_LONG).show()
                                if (response.success) {
                                    onNavigateBack()
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
                        text = if (loading) "Enviando..." else "Enviar enlace",
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