package com.musicmatch.mobile.data.repository

import com.musicmatch.mobile.data.ApiService
import com.musicmatch.mobile.model.dto.RegisterRequest
import com.musicmatch.mobile.model.dto.UserResponse

class UserRepository(private val api: ApiService) {
    suspend fun registerUser(request: RegisterRequest): UserResponse {
        return api.register(request)
    }
}