package com.musicmatch.mobile.utils

import android.content.Context
import androidx.core.content.edit

object NetworkConfig {

    private const val DEFAULT_IP = "10.0.2.2"
    private const val PORT = "8080"

    @Volatile
    private var baseIp: String = DEFAULT_IP

    fun init(context: Context) {
        val savedIp = ServerConfig.getIp(context)
        baseIp = normalizeIp(savedIp).ifBlank { DEFAULT_IP }
    }

    val BASE_URL: String
        get() = if (
            baseIp.startsWith("http://") ||
            baseIp.startsWith("https://")
        ) {
            baseIp.removeSuffix("/")
        } else {
            "http://$baseIp:$PORT"
        }

    fun getAvatarUrl(value: String): String {
        val cleaned = value.trim()

        if (cleaned.isBlank()) return ""

        return when {
            cleaned.startsWith("http://", ignoreCase = true) ||
                    cleaned.startsWith("https://", ignoreCase = true) -> cleaned

            cleaned.startsWith("/api/") -> "$BASE_URL$cleaned"

            cleaned.startsWith("api/") -> "$BASE_URL/$cleaned"

            else -> "$BASE_URL/api/profile/avatar/$cleaned"
        }
    }

    fun updateIp(ip: String) {
        baseIp = normalizeIp(ip).ifBlank { DEFAULT_IP }
    }

    fun reset() {
        baseIp = DEFAULT_IP
    }

    private fun normalizeIp(ip: String?): String {
        if (ip.isNullOrBlank()) return ""

        return ip.trim()
            .removeSuffix("/")
    }
}

object ServerConfig {

    private const val PREFS_NAME = "config"
    private const val KEY_IP = "ip"

    fun saveIp(context: Context, ip: String) {
        val normalizedIp = ip.trim()
            .removeSuffix("/")

        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit { putString(KEY_IP, normalizedIp) }

        NetworkConfig.updateIp(normalizedIp)
    }

    fun getIp(context: Context): String? {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getString(KEY_IP, null)
    }

    fun clearIp(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit { remove(KEY_IP) }
        NetworkConfig.reset()
    }
}