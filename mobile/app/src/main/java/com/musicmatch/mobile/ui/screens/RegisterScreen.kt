package com.musicmatch.mobile.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.musicmatch.mobile.ui.components.CustomLabelledTextField
import com.musicmatch.mobile.ui.components.PasswordLabelledTextField
import com.musicmatch.mobile.viewmodel.RegisterViewModel

// --- Paleta de Colores ---
val ColorPrincipal = Color(0xFF2C3E50)
val ColorSecundario = Color(0xFF8E44AD)
val ColorFondo = Color(0xFFF5F5F5)
val ColorTexto = Color(0xFF333333)

@Composable
fun RegisterScreen(
    viewModel: RegisterViewModel = viewModel(),
    onNavigateLogin: () -> Unit = {}
) {
    val context = LocalContext.current
    val user = viewModel.user.value

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ColorFondo)
    ) {
        // --- Cabecera ---
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp)
                .background(ColorPrincipal),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Registro",
                color = Color.White,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.SansSerif
            )
        }

        // --- Cuerpo del Formulario Centrado ---
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
                // Sección de Campos de Texto
                Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
                    CustomLabelledTextField(
                        label = "Email",
                        value = user.email,
                        onValueChange = viewModel::onEmailChange
                    )

                    CustomLabelledTextField(
                        label = "Usuario",
                        value = user.username,
                        onValueChange = viewModel::onUsernameChange
                    )

                    PasswordLabelledTextField(
                        label = "Contraseña",
                        value = user.password,
                        onValueChange = viewModel::onPasswordChange
                    )
                }

                // --- SEPARACIÓN ---
                Spacer(modifier = Modifier.height(80.dp))

                // --- SECCIÓN DE BOTONES (Vertical para evitar errores de texto) ---
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Button(
                        onClick = { viewModel.onRegisterClicked(context) {} },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = ColorSecundario),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(text = "Registrarse", fontSize = 16.sp, color = Color.White)
                    }

                    TextButton(
                        onClick = onNavigateLogin,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "¿Ya tienes una cuenta?, Inicia sesión",
                            color = ColorPrincipal,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }

        // --- Pie de página ---
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(80.dp)
                .background(ColorPrincipal)
        )
    }
}