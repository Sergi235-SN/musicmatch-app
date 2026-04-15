package com.musicmatch.mobile.viewmodel

import android.content.Context
import android.util.Base64
import android.util.Log
import android.widget.Toast
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.musicmatch.mobile.data.ApiService
import com.musicmatch.mobile.data.repository.UserRepository
import com.musicmatch.mobile.model.dto.LoginRequest
import com.musicmatch.mobile.utils.TokenManager
import kotlinx.coroutines.launch
import org.json.JSONObject

class LoginViewModel : ViewModel() {

    private val tokenManager = TokenManager()

    var email = mutableStateOf("")
    var password = mutableStateOf("")

    private val repository = UserRepository(ApiService.create())

    fun onEmailChange(newEmail: String) {
        email.value = newEmail
    }

    fun onPasswordChange(newPassword: String) {
        password.value = newPassword
    }

    fun onLoginClicked(
        context: Context,
        onSuccess: (Long) -> Unit
    ) {
        if (email.value.isBlank() || password.value.isBlank()) {
            Toast.makeText(context, "Campos obligatorios", Toast.LENGTH_SHORT).show()
            return
        }

        viewModelScope.launch {
            try {
                val response = repository.loginUser(
                    LoginRequest(email.value, password.value)
                )

                if (response.success) {

                    val username = response.data?.username ?: ""
                    val token = response.data?.token ?: ""
                    val userId = response.data?.id ?: 0L

                    tokenManager.saveToken(context, token)

                    Toast.makeText(
                        context,
                        "Bienvenido $username",
                        Toast.LENGTH_SHORT
                    ).show()

                    Log.d("DEBUG_LOGIN", "Token: $token")

                    onSuccess(userId)

                } else {
                    Toast.makeText(context, response.message, Toast.LENGTH_SHORT).show()
                }

            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(context, "Error de conexión", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun checkLogin(
        context: Context,
        onLoggedIn: (String) -> Unit,
        onNotLoggedIn: () -> Unit
    ) {
        viewModelScope.launch {
            validateToken(
                context,
                onValid = { username -> onLoggedIn(username) },
                onInvalid = { onNotLoggedIn() }
            )
        }
    }

    fun validateToken(
        context: Context,
        onValid: (String) -> Unit,
        onInvalid: () -> Unit
    ) {
        val token = tokenManager.getToken(context)

        if (token.isNullOrEmpty()) {
            onInvalid()
            return
        }

        viewModelScope.launch {
            try {
                val response = repository.getCurrentUser(token)

                if (response.success && response.data != null) {
                    onValid(response.data.username)
                } else {
                    tokenManager.clearToken(context)
                    onInvalid()
                }

            } catch (e: Exception) {
                tokenManager.clearToken(context)
                onInvalid()
            }
        }
    }

}