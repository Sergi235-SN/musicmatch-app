package com.musicmatch.mobile.data

import com.musicmatch.mobile.model.dto.LoginRequest
import com.musicmatch.mobile.model.dto.LoginResponse
import com.musicmatch.mobile.model.dto.RegisterRequest
import com.musicmatch.mobile.model.dto.UserResponse
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

interface ApiService {

    @POST("api/auth/register")
    suspend fun register(@Body request: RegisterRequest): UserResponse

    @POST("api/auth/login")
    suspend fun login(@Body request: LoginRequest): LoginResponse

    companion object {
        fun create(): ApiService {
            val retrofit = Retrofit.Builder()
                .baseUrl("http://10.0.2.2:8080/")
                .addConverterFactory(GsonConverterFactory.create())
                .build()
            return retrofit.create(ApiService::class.java)
        }
    }
}