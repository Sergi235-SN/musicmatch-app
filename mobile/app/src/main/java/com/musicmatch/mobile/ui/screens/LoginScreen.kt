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
import com.musicmatch.mobile.viewmodel.LoginViewModel

@Composable
fun LoginScreen(
    viewModel: LoginViewModel = viewModel(),
    onNavigateBack: () -> Unit = {}
) {
    val email = viewModel.email.value
    val password = viewModel.password.value
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ColorFondo) // Usando la constante de Registro
    ) {
        // --- Cabecera (Igual que Registro) ---
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp)
                .background(ColorPrincipal),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Iniciar Sesión",
                color = Color.White,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.SansSerif
            )
        }

        // --- Cuerpo Centrado ---
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
                // Sección de Campos
                Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
                    CustomLabelledTextField(
                        label = "Correo",
                        value = email,
                        onValueChange = viewModel::onEmailChange
                    )

                    PasswordLabelledTextField(
                        label = "Contraseña",
                        value = password,
                        onValueChange = viewModel::onPasswordChange
                    )
                }

                // --- SEPARACIÓN MÁXIMA (100.dp como en Registro) ---
                Spacer(modifier = Modifier.height(100.dp))

                // Sección de Botones
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Button(
                        onClick = { viewModel.onLoginClicked(context) {} },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = ColorSecundario),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Entrar", fontSize = 16.sp, color = Color.White)
                    }

                    TextButton(onClick = onNavigateBack) {
                        Text(
                            "¿No tienes cuenta? Regístrate",
                            color = ColorPrincipal,
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