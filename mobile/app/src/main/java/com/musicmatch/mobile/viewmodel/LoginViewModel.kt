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

class LoginViewModel : ViewModel() {

    var email = mutableStateOf("")
    var password = mutableStateOf("")

    private val repository = UserRepository(ApiService.create())

    fun onEmailChange(newEmail: String) {
        email.value = newEmail
    }

    fun onPasswordChange(newPassword: String) {
        password.value = newPassword
    }

    fun onLoginClicked(context: Context, onSuccess: () -> Unit) {

        if(email.value.isBlank() || password.value.isBlank()){
            Toast.makeText(context, "Campos obligatorios", Toast.LENGTH_SHORT).show()
            return
        }

        viewModelScope.launch {
            try {

                val response = repository.loginUser(
                    LoginRequest(
                        email = email.value,
                        password = password.value
                    )
                )

                if(response.success){
                    val username = response.data?.username ?: ""
                    Toast.makeText(context, "Bienvenido $username", Toast.LENGTH_SHORT).show()
                    onSuccess()
                }else{
                    Toast.makeText(context, response.message, Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception){
                e.printStackTrace()
                Toast.makeText(context, "Error de conexión", Toast.LENGTH_SHORT).show()
            }
        }
    }
}