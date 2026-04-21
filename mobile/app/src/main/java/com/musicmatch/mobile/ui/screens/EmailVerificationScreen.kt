package com.musicmatch.mobile.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.MarkEmailRead
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.musicmatch.mobile.ui.theme.ColorFondo
import com.musicmatch.mobile.ui.theme.ColorPrincipal
import com.musicmatch.mobile.ui.theme.ColorSecundario
import com.musicmatch.mobile.viewmodel.EmailVerificationViewModel

@Composable
fun EmailVerificationScreen(
    email: String,
    password: String,
    viewModel: EmailVerificationViewModel = viewModel(),
    onContinue: (Long) -> Unit = {},
    onGoToLogin: () -> Unit = {}
) {
    val context = LocalContext.current
    val state = viewModel.uiState.value

    LaunchedEffect(email, password) {
        viewModel.startPolling(context, email, password)
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
                text = "Verificación",
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
                when {
                    state.isSuccess -> {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Verificado",
                            tint = ColorSecundario,
                            modifier = Modifier.size(90.dp)
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        Text(
                            text = "Verificado correctamente",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = ColorPrincipal
                        )

                        Spacer(modifier = Modifier.height(28.dp))

                        Button(
                            onClick = {
                                state.userId?.let { onContinue(it) }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = ColorSecundario),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = "Continuar",
                                fontSize = 16.sp,
                                color = Color.White
                            )
                        }
                    }

                    else -> {
                        Icon(
                            imageVector = Icons.Default.MarkEmailRead,
                            contentDescription = "Email enviado",
                            tint = ColorSecundario,
                            modifier = Modifier.size(90.dp)
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        Text(
                            text = "Código de verificación enviado a",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = ColorPrincipal
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = email,
                            fontSize = 16.sp,
                            color = Color.DarkGray
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        when {
                            state.isLoggingIn -> {
                                CircularProgressIndicator(color = ColorSecundario)
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = "Correo verificado. Iniciando sesión...",
                                    fontSize = 14.sp,
                                    color = Color.Gray
                                )
                            }

                            else -> {
                                CircularProgressIndicator(color = ColorSecundario)
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = "Estamos esperando a que verifiques tu correo...",
                                    fontSize = 14.sp,
                                    color = Color.Gray
                                )
                            }
                        }

                        state.errorMessage?.let {
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = it,
                                fontSize = 13.sp,
                                color = Color.Red
                            )

                            Spacer(modifier = Modifier.height(20.dp))

                            OutlinedButton(
                                onClick = onGoToLogin,
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("Ir a iniciar sesión")
                            }
                        }
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