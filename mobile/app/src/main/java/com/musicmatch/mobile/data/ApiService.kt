package com.musicmatch.mobile.data

import com.musicmatch.mobile.model.dto.ApiResponse
import com.musicmatch.mobile.model.dto.CityResponse
import com.musicmatch.mobile.model.dto.LoginRequest
import com.musicmatch.mobile.model.dto.LoginResponse
import com.musicmatch.mobile.model.dto.MusicalOptionsResponse
import com.musicmatch.mobile.model.dto.RegisterRequest
import com.musicmatch.mobile.model.dto.UserBasicResponse
import com.musicmatch.mobile.model.dto.UserResponse
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Header

interface ApiService {

    @POST("api/auth/register")
    suspend fun register(@Body request: RegisterRequest): ApiResponse<UserResponse>

    @POST("api/auth/login")
    suspend fun login(@Body request: LoginRequest): ApiResponse<LoginResponse>

    @GET("api/profile/musical-options")
    suspend fun getMusicalOptions(@Header("Authorization") token: String): ApiResponse<MusicalOptionsResponse>

    @GET("api/profile/cities")
    suspend fun getCities(@Header("Authorization") token: String): ApiResponse<CityResponse>
    @GET("api/auth/me")
    suspend fun getCurrentUser(@Header("Authorization") token: String): ApiResponse<UserBasicResponse>
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