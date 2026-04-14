package com.musicmatch.mobile.data

import com.musicmatch.mobile.model.City
import com.musicmatch.mobile.model.dto.ApiResponse
import com.musicmatch.mobile.model.dto.BlockRequest
import com.musicmatch.mobile.model.dto.LoginRequest
import com.musicmatch.mobile.model.dto.LoginResponse
import com.musicmatch.mobile.model.dto.MatchCandidatesResponse
import com.musicmatch.mobile.model.dto.MusicalOptionsResponse
import com.musicmatch.mobile.model.dto.RegisterRequest
import com.musicmatch.mobile.model.dto.SwipeRequest
import com.musicmatch.mobile.model.dto.SwipeResponse
import com.musicmatch.mobile.model.dto.UpdateProfileRequest
import com.musicmatch.mobile.model.dto.UserProfileResponse
import com.musicmatch.mobile.model.dto.UserResponse
import com.musicmatch.mobile.utils.NetworkConfig
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.PATCH
import retrofit2.http.Part
import retrofit2.http.Path

interface ApiService {

    @POST("api/auth/register")
    suspend fun register(@Body request: RegisterRequest): ApiResponse<UserResponse>

    @POST("api/auth/login")
    suspend fun login(@Body request: LoginRequest): ApiResponse<LoginResponse>

    @GET("api/profile/musical-options")
    suspend fun getMusicalOptions(@Header("Authorization") token: String): MusicalOptionsResponse

    @GET("api/profile/cities")
    suspend fun getCities(@Header("Authorization") token: String): List<City>
    @GET("api/auth/me")
    suspend fun getCurrentUser(@Header("Authorization") token: String): ApiResponse<UserProfileResponse>

    @PATCH("api/profile/{userId}")
    suspend fun updateProfile(
        @Header("Authorization") token: String,
        @Path("userId") userId: Long,
        @Body request: UpdateProfileRequest
    ): Response<Void>

    @Multipart
    @POST("api/profile/{userId}/avatar")
    suspend fun uploadAvatar(
        @Header("Authorization") token: String,
        @Path("userId") userId: Long,
        @Part file: MultipartBody.Part
    ): String

    @GET("api/matches/{userId}/candidates")
    suspend fun getCandidates(
        @Path("userId") userId: Long,
        @Header("Authorization") token: String
    ): MatchCandidatesResponse

    @POST("api/matches/swipe")
    suspend fun swipe(
        @Header("Authorization") token: String,
        @Body request: SwipeRequest
    ): SwipeResponse

    @POST("api/matches/block")
    suspend fun block(
        @Header("Authorization") token: String,
        @Body request: BlockRequest
    )

    @GET("api/profile/public/{userId}")
    suspend fun getPublicProfile(
        @Path("userId") userId: Long
    ): UserProfileResponse

    companion object {
        fun create(): ApiService {
            val retrofit = Retrofit.Builder()
                .baseUrl(NetworkConfig.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build()

            return retrofit.create(ApiService::class.java)
        }
    }
}