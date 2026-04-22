package com.musicmatch.mobile.utils

import android.content.Context
import android.util.Base64
import android.util.Log
import androidx.core.content.edit
import org.json.JSONObject

class TokenManager {

    companion object {
        private const val PREFS_NAME = "musicmatch_prefs"
        private const val KEY_ACCESS_TOKEN = "jwt_token"
        private const val KEY_REFRESH_TOKEN = "refresh_token"
        private const val TAG = "TokenManager"
    }

    private fun prefs(context: Context) =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun saveTokens(context: Context, accessToken: String, refreshToken: String) {
        prefs(context).edit {
            putString(KEY_ACCESS_TOKEN, accessToken)
            putString(KEY_REFRESH_TOKEN, refreshToken)
        }
    }

    fun saveAccessToken(context: Context, token: String) {
        prefs(context).edit {
            putString(KEY_ACCESS_TOKEN, token)
        }
    }

    fun saveRefreshToken(context: Context, refreshToken: String) {
        prefs(context).edit {
            putString(KEY_REFRESH_TOKEN, refreshToken)
        }
    }

    fun saveToken(context: Context, token: String) {
        saveAccessToken(context, token)
    }

    fun getAccessToken(context: Context): String? {
        return prefs(context).getString(KEY_ACCESS_TOKEN, null)
    }

    fun getToken(context: Context): String? {
        return getAccessToken(context)
    }

    fun getRefreshToken(context: Context): String? {
        return prefs(context).getString(KEY_REFRESH_TOKEN, null)
    }

    fun hasSession(context: Context): Boolean {
        return !getAccessToken(context).isNullOrBlank()
    }

    fun clearTokens(context: Context) {
        prefs(context).edit {
            remove(KEY_ACCESS_TOKEN)
            remove(KEY_REFRESH_TOKEN)
        }
    }

    fun clearToken(context: Context) {
        clearTokens(context)
    }

    fun getUserIdFromToken(context: Context): Long? {
        val token = getAccessToken(context) ?: return null
        return extractUserId(token)
    }

    fun extractUserId(token: String): Long? {
        val payload = decodePayload(token) ?: return null

        return try {
            val json = JSONObject(payload)
            json.optString("sub", null)?.toLongOrNull()
        } catch (e: Exception) {
            Log.e(TAG, "Error extrayendo userId del token", e)
            null
        }
    }

    fun isTokenExpired(token: String): Boolean {
        val payload = decodePayload(token) ?: return true

        return try {
            val json = JSONObject(payload)
            val expSeconds = json.optLong("exp", 0L)
            if (expSeconds <= 0L) return true

            val nowSeconds = System.currentTimeMillis() / 1000
            nowSeconds >= expSeconds
        } catch (e: Exception) {
            Log.e(TAG, "Error comprobando expiración del token", e)
            true
        }
    }

    private fun decodePayload(token: String): String? {
        val parts = token.split(".")
        if (parts.size < 2) return null

        return try {
            val payloadPart = parts[1]
            val padded = padBase64(payloadPart)

            String(
                Base64.decode(padded, Base64.URL_SAFE or Base64.NO_WRAP),
                Charsets.UTF_8
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error decodificando payload JWT", e)
            null
        }
    }

    private fun padBase64(value: String): String {
        val remainder = value.length % 4
        return if (remainder == 0) value else value + "=".repeat(4 - remainder)
    }
}