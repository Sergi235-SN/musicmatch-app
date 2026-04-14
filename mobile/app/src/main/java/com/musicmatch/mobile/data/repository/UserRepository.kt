package com.musicmatch.mobile.data.repository

import com.musicmatch.mobile.data.ApiService
import com.musicmatch.mobile.model.City
import com.musicmatch.mobile.model.dto.ApiResponse
import com.musicmatch.mobile.model.dto.LoginRequest
import com.musicmatch.mobile.model.dto.LoginResponse
import com.musicmatch.mobile.model.dto.MusicalOptionsResponse
import com.musicmatch.mobile.model.dto.RegisterRequest
import com.musicmatch.mobile.model.dto.UpdateProfileRequest
import com.musicmatch.mobile.model.dto.UserProfileResponse
import com.musicmatch.mobile.model.dto.UserResponse
import okhttp3.MultipartBody

class UserRepository(private val api: ApiService) {
    suspend fun registerUser(request: RegisterRequest): ApiResponse<UserResponse> {
        return api.register(request)
    }

    suspend fun loginUser(request: LoginRequest): ApiResponse<LoginResponse> {
        return api.login(request)
    }

    suspend fun getMusicalOptions(token: String): MusicalOptionsResponse {
        return api.getMusicalOptions("Bearer $token")
    }

    suspend fun getCities(token: String): List<City> {
        return api.getCities("Bearer $token")
    }

    suspend fun getCurrentUser(token: String): ApiResponse<UserProfileResponse> {
        return api.getCurrentUser("Bearer $token")
    }

    suspend fun updateProfile(token: String, userId: Long, request: UpdateProfileRequest): Boolean {
        return try {
            api.updateProfile("Bearer $token", userId, request).isSuccessful
        } catch (e: Exception) {
            false
        }
    }

    suspend fun uploadAvatar(token: String, userId: Long, file: MultipartBody.Part): String? {
        return try {
            api.uploadAvatar("Bearer $token", userId, file)
        } catch (e: Exception) {
            null
        }
    }

    suspend fun getPublicProfile(userId: Long): UserProfileResponse {
        return api.getPublicProfile(userId)
    }

}