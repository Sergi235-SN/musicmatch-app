package com.musicmatch.mobile.data.repository

import com.musicmatch.mobile.data.ApiService
import com.musicmatch.mobile.model.dto.ApiResponse
import com.musicmatch.mobile.model.dto.CityResponse
import com.musicmatch.mobile.model.dto.LoginRequest
import com.musicmatch.mobile.model.dto.LoginResponse
import com.musicmatch.mobile.model.dto.MusicalOptionsResponse
import com.musicmatch.mobile.model.dto.RegisterRequest
import com.musicmatch.mobile.model.dto.UserBasicResponse
import com.musicmatch.mobile.model.dto.UserResponse

class UserRepository(private val api: ApiService) {
    suspend fun registerUser(request: RegisterRequest): ApiResponse<UserResponse> {
        return api.register(request)
    }

    suspend fun loginUser(request: LoginRequest): ApiResponse<LoginResponse> {
        return api.login(request)
    }

    suspend fun getMusicalOptions(token: String): ApiResponse<MusicalOptionsResponse> {
        return api.getMusicalOptions("Bearer $token")
    }

    suspend fun getCities(token: String): ApiResponse<CityResponse> {
        return api.getCities("Bearer $token")
    }

    suspend fun getCurrentUser(token: String): ApiResponse<UserBasicResponse> {
        return api.getCurrentUser("Bearer $token")
    }

}