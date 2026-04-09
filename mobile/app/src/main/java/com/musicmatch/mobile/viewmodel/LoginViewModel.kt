package com.musicmatch.mobile.viewmodel

import android.content.Context
import android.widget.Toast
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.musicmatch.mobile.data.ApiService
import com.musicmatch.mobile.data.repository.UserRepository
import com.musicmatch.mobile.model.dto.LoginRequest
import kotlinx.coroutines.launch
import android.util.Base64
import android.util.Log
import com.musicmatch.mobile.utils.TokenManager
import org.json.JSONObject
class LoginViewModel : ViewModel() {

    private val tokenManager = TokenManager()
    var email = mutableStateOf("")
    var password = mutableStateOf("")

    private val repository = UserRepository(ApiService.create())

    fun onEmailChange(newEmail: String) { email.value = newEmail }
    fun onPasswordChange(newPassword: String) { password.value = newPassword }



    fun checkLogin(context: Context, onLoggedIn: (username: String) -> Unit, onNotLoggedIn: () -> Unit) {
        viewModelScope.launch {
            validateToken(
                context,
                onValid = { username -> onLoggedIn(username) },
                onInvalid = { onNotLoggedIn() }
            )
        }
    }

    // Login manual
    fun onLoginClicked(context: Context, onSuccess: () -> Unit) {
        if(email.value.isBlank() || password.value.isBlank()){
            Toast.makeText(context, "Campos obligatorios", Toast.LENGTH_SHORT).show()
            return
        }

        viewModelScope.launch {
            try {
                val response = repository.loginUser(
                    LoginRequest(email.value, password.value)
                )

                if(response.success){
                    val username = response.data?.username ?: ""
                    val token = response.data?.token ?: ""
                    tokenManager.saveToken(context, token)
                    Toast.makeText(context, "Bienvenido $username", Toast.LENGTH_SHORT).show()

                    Log.d("DEBUG_LOGIN", "Token actual: $token")
                    Log.d("DEBUG_LOGIN", "Response success=${response.success}, message=${response.message}, data=${response.data}")

                    onSuccess()
                } else {
                    Toast.makeText(context, response.message, Toast.LENGTH_SHORT).show()
                }

            } catch (e: Exception){
                e.printStackTrace()
                Toast.makeText(context, "Error de conexión", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun getUsernameFromToken(token: String): String {
        return try {
            val parts = token.split(".")
            if (parts.size == 3) {
                val payload = String(Base64.decode(parts[1], Base64.URL_SAFE), Charsets.UTF_8)
                val json = JSONObject(payload)
                json.getString("username")
            } else {
                ""
            }
        } catch (e: Exception) {
            ""
        }
    }

    fun validateToken(context: Context, onValid: (username: String) -> Unit, onInvalid: () -> Unit) {
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