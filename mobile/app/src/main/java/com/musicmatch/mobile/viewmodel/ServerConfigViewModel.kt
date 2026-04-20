package com.musicmatch.mobile.viewmodel

import android.content.Context
import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.musicmatch.mobile.utils.NetworkConfig
import com.musicmatch.mobile.utils.ServerConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL


class ServerConfigViewModel : ViewModel() {

    var ip = mutableStateOf("")
    var errorMessage = mutableStateOf<String?>(null)
    var isLoading = mutableStateOf(false)

    fun onIpChange(newIp: String) {
        ip.value = newIp
        errorMessage.value = null
    }

    fun connect(
        context: Context,
        onSuccess: () -> Unit
    ) {
        if (ip.value.isBlank()) {
            errorMessage.value = "Introduce una IP válida"
            return
        }

        viewModelScope.launch(Dispatchers.IO) {

            isLoading.value = true
            errorMessage.value = null

            try {
                val url = "http://${ip.value}:8080/api/auth/ping"

                val connection = URL(url).openConnection() as HttpURLConnection
                connection.connectTimeout = 3000
                connection.readTimeout = 3000
                connection.requestMethod = "GET"

                val responseCode = connection.responseCode

                withContext(Dispatchers.Main) {
                    if (responseCode == 200) {
                        ServerConfig.saveIp(context, ip.value)
                        NetworkConfig.init(context)
                        onSuccess()
                    } else {
                        errorMessage.value = "El servidor responde pero con error"
                    }
                }

            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    errorMessage.value = "No se puede conectar al servidor"
                }
            } finally {
                withContext(Dispatchers.Main) {
                    isLoading.value = false
                }
            }
        }
    }
}