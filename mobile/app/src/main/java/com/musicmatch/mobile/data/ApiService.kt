package com.musicmatch.mobile.data

import com.musicmatch.mobile.model.City
import com.musicmatch.mobile.model.dto.ApiResponse
import com.musicmatch.mobile.model.dto.BlockRequest
import com.musicmatch.mobile.model.dto.ChatPreview
import com.musicmatch.mobile.model.dto.ChatRequest
import com.musicmatch.mobile.model.dto.ChatResponse
import com.musicmatch.mobile.model.dto.LoginRequest
import com.musicmatch.mobile.model.dto.LoginResponse
import com.musicmatch.mobile.model.dto.MatchCandidatesResponse
import com.musicmatch.mobile.model.dto.MessageRequest
import com.musicmatch.mobile.model.dto.MessageResponse
import com.musicmatch.mobile.model.dto.MusicalOptionsResponse
import com.musicmatch.mobile.model.dto.PublicProfileResponse
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

    @GET("api/auth/me")
    suspend fun getCurrentUser(
        @Header("Authorization") token: String
    ): ApiResponse<UserProfileResponse>

    @GET("api/profile/musical-options")
    suspend fun getMusicalOptions(
        @Header("Authorization") token: String
    ): MusicalOptionsResponse

    @GET("api/profile/cities")
    suspend fun getCities(
        @Header("Authorization") token: String
    ): List<City>

    @PATCH("api/profile")
    suspend fun updateProfile(
        @Header("Authorization") token: String,
        @Body request: UpdateProfileRequest
    ): Response<Void>

    @Multipart
    @POST("api/profile/avatar")
    suspend fun uploadAvatar(
        @Header("Authorization") token: String,
        @Part file: MultipartBody.Part
    ): String

    @GET("api/profile/public/{userId}")
    suspend fun getPublicProfile(
        @Path("userId") userId: Long,
        @Header("Authorization") token: String
    ): PublicProfileResponse

    @GET("api/matches/candidates")
    suspend fun getCandidates(
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

    @POST("api/matches/unblock")
    suspend fun unblock(
        @Header("Authorization") token: String,
        @Body request: BlockRequest
    )

    @POST("api/chats/request-or-get")
    suspend fun requestOrGetChat(
        @Header("Authorization") token: String,
        @Body request: ChatRequest
    ): ChatResponse

    @GET("api/chats/pending")
    suspend fun getPendingChats(
        @Header("Authorization") token: String
    ): List<ChatPreview>

    @POST("api/chats/{chatId}/accept")
    suspend fun acceptChat(
        @Header("Authorization") token: String,
        @Path("chatId") chatId: Long
    )

    @POST("api/chats/{chatId}/reject")
    suspend fun rejectChat(
        @Header("Authorization") token: String,
        @Path("chatId") chatId: Long
    )

    @POST("api/chats/message")
    suspend fun sendMessage(
        @Header("Authorization") token: String,
        @Body request: MessageRequest
    )

    @GET("api/chats")
    suspend fun getChats(
        @Header("Authorization") token: String
    ): List<ChatPreview>

    @GET("api/chats/{chatId}/messages")
    suspend fun getMessages(
        @Header("Authorization") token: String,
        @Path("chatId") chatId: Long
    ): List<MessageResponse>

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