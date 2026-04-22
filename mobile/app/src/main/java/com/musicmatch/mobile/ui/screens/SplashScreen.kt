package com.musicmatch.mobile.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.musicmatch.mobile.R
import com.musicmatch.mobile.navigation.Screen
import com.musicmatch.mobile.ui.theme.ColorFondo
import com.musicmatch.mobile.ui.theme.ColorPrincipal
import com.musicmatch.mobile.ui.theme.ColorSecundario
import com.musicmatch.mobile.utils.NetworkConfig
import com.musicmatch.mobile.utils.ServerConfig
import com.musicmatch.mobile.viewmodel.LoginViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL

@Composable
fun SplashScreen(
    navController: NavController,
    loginViewModel: LoginViewModel
) {
    val context = androidx.compose.ui.platform.LocalContext.current

    LaunchedEffect(Unit) {
        NetworkConfig.init(context)

        val ip = ServerConfig.getIp(context)

        if (ip == null) {
            navController.navigate(Screen.ServerConfig.route) {
                popUpTo(Screen.Splash.route) { inclusive = true }
            }
            return@LaunchedEffect
        }

        val serverOk = withContext(Dispatchers.IO) {
            try {
                val url = URL("http://$ip:8080/api/auth/ping")
                val conn = url.openConnection() as HttpURLConnection
                conn.connectTimeout = 2000
                conn.readTimeout = 2000
                conn.requestMethod = "GET"
                conn.connect()
                conn.responseCode == 200
            } catch (e: Exception) {
                false
            }
        }

        if (!serverOk) {
            navController.navigate(Screen.ServerConfig.route) {
                popUpTo(Screen.Splash.route) { inclusive = true }
            }
            return@LaunchedEffect
        }

        val hasValidSession = loginViewModel.hasValidSession(context)

        if (hasValidSession) {
            navController.navigate(Screen.Home.route) {
                popUpTo(Screen.Splash.route) { inclusive = true }
            }
        } else {
            navController.navigate(Screen.Login.route) {
                popUpTo(Screen.Splash.route) { inclusive = true }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ColorFondo)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(108.dp)
                .background(ColorPrincipal)
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(horizontal = 24.dp),
            contentAlignment = Alignment.Center
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(30.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 34.dp, vertical = 44.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.logo_musicmatch),
                        contentDescription = "Logo MusicMatch",
                        modifier = Modifier
                            .size(150.dp)
                            .clip(RoundedCornerShape(28.dp)),
                        contentScale = ContentScale.Fit
                    )

                    Spacer(modifier = Modifier.height(28.dp))

                    Text(
                        text = "MusicMatch",
                        fontSize = 34.sp,
                        fontWeight = FontWeight.Bold,
                        color = ColorPrincipal,
                        fontFamily = FontFamily.SansSerif,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "Preparando tu experiencia musical...",
                        fontSize = 17.sp,
                        color = Color.Gray,
                        textAlign = TextAlign.Center,
                        lineHeight = 24.sp
                    )

                    Spacer(modifier = Modifier.height(34.dp))

                    LinearProgressIndicator(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(10.dp)
                            .clip(RoundedCornerShape(50)),
                        color = ColorSecundario,
                        trackColor = ColorSecundario.copy(alpha = 0.18f)
                    )
                }
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(88.dp)
                .background(ColorPrincipal)
        )
    }
}