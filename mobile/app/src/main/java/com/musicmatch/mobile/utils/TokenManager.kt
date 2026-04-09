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
}