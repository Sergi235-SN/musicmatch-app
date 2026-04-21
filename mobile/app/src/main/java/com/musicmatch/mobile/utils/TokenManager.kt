package com.musicmatch.mobile.utils

import android.content.Context
import androidx.core.content.edit

class TokenManager {

    fun saveTokens(context: Context, accessToken: String, refreshToken: String) {
        val prefs = context.getSharedPreferences("musicmatch_prefs", Context.MODE_PRIVATE)
        prefs.edit {
            putString("jwt_token", accessToken)
            putString("refresh_token", refreshToken)
        }
    }

    fun saveAccessToken(context: Context, token: String) {
        val prefs = context.getSharedPreferences("musicmatch_prefs", Context.MODE_PRIVATE)
        prefs.edit { putString("jwt_token", token) }
    }

    fun saveRefreshToken(context: Context, refreshToken: String) {
        val prefs = context.getSharedPreferences("musicmatch_prefs", Context.MODE_PRIVATE)
        prefs.edit { putString("refresh_token", refreshToken) }
    }

    fun saveToken(context: Context, token: String) {
        saveAccessToken(context, token)
    }

    fun getAccessToken(context: Context): String? {
        val prefs = context.getSharedPreferences("musicmatch_prefs", Context.MODE_PRIVATE)
        return prefs.getString("jwt_token", null)
    }

    fun getToken(context: Context): String? {
        return getAccessToken(context)
    }

    fun getRefreshToken(context: Context): String? {
        val prefs = context.getSharedPreferences("musicmatch_prefs", Context.MODE_PRIVATE)
        return prefs.getString("refresh_token", null)
    }

    fun clearTokens(context: Context) {
        val prefs = context.getSharedPreferences("musicmatch_prefs", Context.MODE_PRIVATE)
        prefs.edit {
            remove("jwt_token")
            remove("refresh_token")
        }
    }

    fun clearToken(context: Context) {
        clearTokens(context)
    }

    fun getUserIdFromToken(context: Context): Long? {
        val token = getAccessToken(context) ?: return null
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