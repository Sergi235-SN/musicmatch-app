package com.musicmatch.mobile.data.repository

import com.musicmatch.mobile.data.ApiService
import com.musicmatch.mobile.data.safeApiCall
import com.musicmatch.mobile.model.dto.EmailRequest
import com.musicmatch.mobile.model.dto.LoginRequest
import com.musicmatch.mobile.model.dto.RefreshTokenRequest
import com.musicmatch.mobile.model.dto.RegisterRequest
import com.musicmatch.mobile.model.dto.ResetPasswordRequest
import com.musicmatch.mobile.model.dto.UpdateProfileRequest
import okhttp3.MultipartBody

class UserRepository(private val api: ApiService) {

    suspend fun registerUser(request: RegisterRequest) =
        safeApiCall { api.register(request) }

    suspend fun loginUser(request: LoginRequest) =
        safeApiCall { api.login(request) }

    suspend fun refreshAccessToken(refreshToken: String) =
        safeApiCall { api.refresh(RefreshTokenRequest(refreshToken)) }

    suspend fun resendVerification(email: String) =
        safeApiCall { api.resendVerification(EmailRequest(email)) }

    suspend fun forgotPassword(email: String) =
        safeApiCall { api.forgotPassword(EmailRequest(email)) }

    suspend fun resetPassword(token: String, password: String) =
        safeApiCall { api.resetPassword(ResetPasswordRequest(token, password)) }

    suspend fun getMusicalOptions(token: String) =
        safeApiCall { api.getMusicalOptions("Bearer $token") }

    suspend fun getCities(token: String) =
        safeApiCall { api.getCities("Bearer $token") }

    suspend fun getCurrentUser(token: String) =
        safeApiCall { api.getCurrentUser("Bearer $token") }

    suspend fun updateProfile(token: String, request: UpdateProfileRequest): Boolean {
        return safeApiCall {
            api.updateProfile("Bearer $token", request).isSuccessful
        }
    }

    suspend fun uploadAvatar(token: String, file: MultipartBody.Part): String {
        val response = safeApiCall {
            api.uploadAvatar("Bearer $token", file)
        }

        if (!response.success || response.data.isNullOrBlank()) {
            throw Exception(response.message ?: "No se pudo subir el avatar")
        }

        return response.data
    }

    suspend fun getPublicProfile(userId: Long, token: String) =
        safeApiCall { api.getPublicProfile(userId, "Bearer $token") }

    suspend fun getVerificationStatus(email: String) =
        safeApiCall { api.getVerificationStatus(email) }
}