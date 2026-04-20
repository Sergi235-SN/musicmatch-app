package com.musicmatch.mobile

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.compose.rememberNavController
import com.musicmatch.mobile.navigation.NavGraph
import com.musicmatch.mobile.navigation.Screen
import com.musicmatch.mobile.utils.NetworkConfig
import com.musicmatch.mobile.utils.ServerConfig
import com.musicmatch.mobile.viewmodel.LoginViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL

class MainActivity : ComponentActivity() {

    private val loginViewModel = LoginViewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            MaterialTheme {

                val navController = rememberNavController()

                LaunchedEffect(Unit) {

                    NetworkConfig.init(this@MainActivity)

                    val ip = ServerConfig.getIp(this@MainActivity)

                    if (ip == null) {
                        navController.navigate(Screen.ServerConfig.route) {
                            popUpTo(0)
                        }
                        return@LaunchedEffect
                    }

                    val serverOk = withContext(Dispatchers.IO) {
                        try {
                            val url = URL("http://$ip:8080/api/auth/ping")
                            val conn = url.openConnection() as HttpURLConnection
                            conn.connectTimeout = 2000
                            conn.connect()
                            conn.responseCode == 200
                        } catch (e: Exception) {
                            false
                        }
                    }

                    if (!serverOk) {
                        navController.navigate(Screen.ServerConfig.route) {
                            popUpTo(0)
                        }
                        return@LaunchedEffect
                    }

                    loginViewModel.validateToken(
                        context = this@MainActivity,
                        onValid = {
                            navController.navigate(Screen.Home.route) {
                                popUpTo(0)
                            }
                        },
                        onInvalid = {
                            navController.navigate(Screen.Login.route) {
                                popUpTo(0)
                            }
                        }
                    )
                }

                NavGraph(navController = navController)
            }
        }
    }
}