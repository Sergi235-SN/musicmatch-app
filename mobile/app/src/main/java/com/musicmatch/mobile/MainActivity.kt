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
import com.musicmatch.mobile.viewmodel.LoginViewModel

class MainActivity : ComponentActivity() {

    private val loginViewModel = LoginViewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            MaterialTheme {

                val navController = rememberNavController()

                LaunchedEffect(Unit) {

                    loginViewModel.validateToken(
                        context = this@MainActivity,
                        onValid = { username ->

                            Toast.makeText(
                                this@MainActivity,
                                "Bienvenido $username",
                                Toast.LENGTH_SHORT
                            ).show()

                            // ✅ IR A HOME (NO LOGIN)
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