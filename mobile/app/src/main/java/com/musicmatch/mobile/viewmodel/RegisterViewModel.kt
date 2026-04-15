package com.musicmatch.mobile.viewmodel

import android.content.Context
import android.widget.Toast
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.musicmatch.mobile.data.ApiService
import com.musicmatch.mobile.data.repository.UserRepository
import com.musicmatch.mobile.model.dto.RegisterRequest
import com.musicmatch.mobile.model.User
import com.musicmatch.mobile.utils.TokenManager
import kotlinx.coroutines.launch

class RegisterViewModel : ViewModel() {

    private val tokenManager = TokenManager()
    private val repository = UserRepository(ApiService.create())

    var user = mutableStateOf(User())
        private set

    fun onUsernameChange(newUsername: String) = run { user.value = user.value.copy(username = newUsername) }
    fun onEmailChange(newEmail: String) = run { user.value = user.value.copy(email = newEmail) }
    fun onPasswordChange(newPassword: String) = run { user.value = user.value.copy(password = newPassword) }

    fun onRegisterClicked(context: Context, onSuccess: (Long) -> Unit) {
        val currentUser = user.value
        if (currentUser.username.isBlank() || currentUser.email.isBlank() || currentUser.password.isBlank()) {
            Toast.makeText(context, "Campos obligatorios", Toast.LENGTH_SHORT).show()
            return
        }

        viewModelScope.launch {
            try {
                val response = repository.registerUser(
                    RegisterRequest(currentUser.username, currentUser.email, currentUser.password)
                )

                if (response.success && response.data != null) {
                    tokenManager.saveToken(context, response.data.token ?: "")

                    val userId = response.data.id ?: 0L
                    onSuccess(userId)
                } else {
                    Toast.makeText(context, response.message, Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Error de conexión", Toast.LENGTH_SHORT).show()
            }
        }
    }
}