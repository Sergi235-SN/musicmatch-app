package com.musicmatch.mobile.utils

import android.content.Context
import androidx.core.content.edit

object NetworkConfig {
    // Para el emulador de Android, 10.0.2.2 apunta al localhost de tu PC
    private var BASE_IP = "10.0.2.2"
    private const val PORT = "8080"

    fun init(context: Context) {
        val savedIp = ServerConfig.getIp(context)
        if (savedIp != null) {
            BASE_IP = savedIp
        }
    }

    val BASE_URL: String
        get() = "http://$BASE_IP:$PORT"

    // Helper para obtener la URL completa de una imagen/avatar
    fun getAvatarUrl(fileName: String): String {
        return "$BASE_URL/api/profile/avatar/$fileName"
    }
}

object ServerConfig {

    fun saveIp(context: Context, ip: String) {
        val prefs = context.getSharedPreferences("config", Context.MODE_PRIVATE)
        prefs.edit { putString("ip", ip) }
    }

    fun getIp(context: Context): String? {
        val prefs = context.getSharedPreferences("config", Context.MODE_PRIVATE)
        return prefs.getString("ip", null)
    }
}