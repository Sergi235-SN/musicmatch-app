package com.musicmatch.mobile.viewmodel

import android.content.Context
import android.widget.Toast
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.musicmatch.mobile.data.ApiService
import com.musicmatch.mobile.data.repository.UserRepository
import com.musicmatch.mobile.model.dto.LoginRequest
import com.musicmatch.mobile.utils.TokenManager
import kotlinx.coroutines.launch

class LoginViewModel : ViewModel() {

    private val tokenManager = TokenManager()

    var email = mutableStateOf("")
    var password = mutableStateOf("")

    private fun repository(): UserRepository {
        return UserRepository(ApiService.create())
    }

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
                val response = repository().loginUser(
                    LoginRequest(email.value, password.value)
                )

                if (response.success && response.data != null) {
                    val username = response.data.username
                    val accessToken = response.data.token
                    val refreshToken = response.data.refreshToken
                    val userId = response.data.id

                    if (accessToken.isNullOrBlank() || refreshToken.isNullOrBlank()) {
                        Toast.makeText(
                            context,
                            "No se pudo iniciar sesión. Revisa tu correo o vuelve a intentarlo.",
                            Toast.LENGTH_SHORT
                        ).show()
                        return@launch
                    }

                    tokenManager.saveTokens(context, accessToken, refreshToken)

                    Toast.makeText(
                        context,
                        "Bienvenido $username",
                        Toast.LENGTH_SHORT
                    ).show()

                    onSuccess(userId)

                } else {
                    Toast.makeText(
                        context,
                        response.message ?: "No se pudo iniciar sesión",
                        Toast.LENGTH_SHORT
                    ).show()
                }

            } catch (e: Exception) {
                Toast.makeText(
                    context,
                    e.message ?: "No se pudo conectar con el servidor",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    fun checkLogin(
        context: Context,
        onLoggedIn: (String) -> Unit,
        onNotLoggedIn: () -> Unit
    ) {
        validateToken(
            context,
            onValid = { username -> onLoggedIn(username) },
            onInvalid = { onNotLoggedIn() }
        )
    }

    fun validateToken(
        context: Context,
        onValid: (String) -> Unit,
        onInvalid: () -> Unit
    ) {
        val accessToken = tokenManager.getAccessToken(context)

        if (accessToken.isNullOrEmpty()) {
            onInvalid()
            return
        }

        viewModelScope.launch {
            try {
                val username = getCurrentUsername(accessToken)

                if (username != null) {
                    onValid(username)
                    return@launch
                }

                val refreshed = refreshSession(context)
                if (refreshed) {
                    val newAccessToken = tokenManager.getAccessToken(context)
                    val refreshedUsername = newAccessToken?.let { getCurrentUsername(it) }

                    if (refreshedUsername != null) {
                        onValid(refreshedUsername)
                        return@launch
                    }
                }

                tokenManager.clearTokens(context)
                onInvalid()

            } catch (e: Exception) {
                tokenManager.clearTokens(context)
                onInvalid()
            }
        }
    }

    private suspend fun getCurrentUsername(token: String): String? {
        return try {
            val response = repository().getCurrentUser(token)
            if (response.success && response.data != null) {
                response.data.username
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }

    private suspend fun refreshSession(context: Context): Boolean {
        val refreshToken = tokenManager.getRefreshToken(context) ?: return false

        return try {
            val response = repository().refreshAccessToken(refreshToken)
            val newAccessToken = response.data?.token

            if (response.success && !newAccessToken.isNullOrBlank()) {
                tokenManager.saveAccessToken(context, newAccessToken)
                true
            } else {
                false
            }
        } catch (e: Exception) {
            false
        }
    }

    suspend fun hasValidSession(context: Context): Boolean {
        val accessToken = tokenManager.getAccessToken(context)

        if (accessToken.isNullOrEmpty()) {
            return false
        }

        return try {
            val username = getCurrentUsername(accessToken)

            if (username != null) {
                true
            } else {
                val refreshed = refreshSession(context)
                if (refreshed) {
                    val newAccessToken = tokenManager.getAccessToken(context)
                    val refreshedUsername = newAccessToken?.let { getCurrentUsername(it) }
                    refreshedUsername != null
                } else {
                    tokenManager.clearTokens(context)
                    false
                }
            }
        } catch (e: Exception) {
            tokenManager.clearTokens(context)
            false
        }
    }

}