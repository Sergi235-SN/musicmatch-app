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
import kotlinx.coroutines.launch

class RegisterViewModel : ViewModel() {

    var user = mutableStateOf(User())
        private set

    private val repository = UserRepository(ApiService.create())

    fun onUsernameChange(newUsername: String) {
        user.value = user.value.copy(username = newUsername)
    }

    fun onEmailChange(newEmail: String) {
        user.value = user.value.copy(email = newEmail)
    }

    fun onPasswordChange(newPassword: String) {
        user.value = user.value.copy(password = newPassword)
    }

    fun onRegisterClicked(context: Context, onSuccess: () -> Unit) {
        val currentUser = user.value
        if (currentUser.username.isBlank() || currentUser.email.isBlank() || currentUser.password.isBlank()) {
            Toast.makeText(context, "Todos los campos son obligatorios", Toast.LENGTH_SHORT).show()
            return
        }

        viewModelScope.launch {
            try {
                val response = repository.registerUser(
                    RegisterRequest(
                        username = currentUser.username,
                        email = currentUser.email,
                        password = currentUser.password
                    )
                )

                if(response.success){
                    Toast.makeText(context, "Bienvenido ${response.username}", Toast.LENGTH_SHORT).show()
                    onSuccess()
                }else{
                    Toast.makeText(context, response.message, Toast.LENGTH_SHORT).show()
                }

            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(context, "Error al registrar usuario", Toast.LENGTH_SHORT).show()
            }
        }
    }
}