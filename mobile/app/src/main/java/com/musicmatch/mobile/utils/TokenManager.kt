package com.musicmatch.mobile.utils

import android.content.Context
import androidx.core.content.edit

class TokenManager {
    // Guardar token en SharedPreferences
    public fun saveToken(context: Context, token: String) {
        val prefs = context.getSharedPreferences("musicmatch_prefs", Context.MODE_PRIVATE)
        prefs.edit { putString("jwt_token", token) }
    }

    // Obtener token
    public fun getToken(context: Context): String? {
        val prefs = context.getSharedPreferences("musicmatch_prefs", Context.MODE_PRIVATE)
        return prefs.getString("jwt_token", null)
    }

    // Borrar token (cuando expira)
    public fun clearToken(context: Context) {
        val prefs = context.getSharedPreferences("musicmatch_prefs", Context.MODE_PRIVATE)
        prefs.edit { remove("jwt_token") }
    }

    fun getUserIdFromToken(context: Context): Long? {
        val token = getToken(context) ?: return null
        val parts = token.split(".")
        if (parts.size < 2) return null

        return try {
            val payload = String(android.util.Base64.decode(parts[1], android.util.Base64.URL_SAFE))
            val json = org.json.JSONObject(payload)

            val userIdString = json.optString("sub", null)
            userIdString.toLongOrNull()
        } catch (e: Exception) {
            android.util.Log.e("TokenManager", "Error decodificando token", e)
            null
        }
    }
}