package com.musicmatch.mobile.data.repository

import com.musicmatch.mobile.data.ApiService
import com.musicmatch.mobile.model.City
import com.musicmatch.mobile.model.dto.*
import okhttp3.MultipartBody

class UserRepository(private val api: ApiService) {

    suspend fun registerUser(request: RegisterRequest) =
        api.register(request)

    suspend fun loginUser(request: LoginRequest) =
        api.login(request)

    suspend fun getMusicalOptions(token: String) =
        api.getMusicalOptions("Bearer $token")

    suspend fun getCities(token: String) =
        api.getCities("Bearer $token")

    suspend fun getCurrentUser(token: String) =
        api.getCurrentUser("Bearer $token")

    suspend fun updateProfile(token: String, request: UpdateProfileRequest): Boolean {
        return try {
            api.updateProfile("Bearer $token", request).isSuccessful
        } catch (e: Exception) {
            false
        }
    }

    suspend fun uploadAvatar(token: String, file: MultipartBody.Part): String? {
        return try {
            api.uploadAvatar("Bearer $token", file)
        } catch (e: Exception) {
            null
        }
    }

    suspend fun getPublicProfile(userId: Long, token: String) =
        api.getPublicProfile(userId, "Bearer $token")
}