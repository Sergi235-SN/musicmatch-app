package com.musicmatch.mobile.utils

object NetworkConfig {
    // Para el emulador de Android, 10.0.2.2 apunta al localhost de tu PC
    // Si usas un móvil físico, aquí pondrías la IP de tu red local (ej: 192.168.1.50)
    private const val BASE_IP = "192.168.1.120"
    private const val PORT = "8080"

    const val BASE_URL = "http://$BASE_IP:$PORT"

    // Helper para obtener la URL completa de una imagen/avatar
    fun getAvatarUrl(fileName: String): String {
        return "$BASE_URL/api/profile/avatar/$fileName"
    }
}