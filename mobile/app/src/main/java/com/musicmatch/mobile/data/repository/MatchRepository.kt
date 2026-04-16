package com.musicmatch.mobile.data.repository

import com.musicmatch.mobile.data.ApiService
import com.musicmatch.mobile.model.dto.BlockRequest
import com.musicmatch.mobile.model.dto.ChatRequest
import com.musicmatch.mobile.model.dto.ChatResponse
import com.musicmatch.mobile.model.dto.SwipeRequest

class MatchRepository(private val api: ApiService) {

    suspend fun getCandidates(token: String) =
        api.getCandidates("Bearer $token")

    suspend fun swipe(token: String, request: SwipeRequest) =
        api.swipe("Bearer $token", request)

    suspend fun block(token: String, request: BlockRequest) =
        api.block("Bearer $token", request)

    suspend fun unblock(token: String, request: BlockRequest) =
        api.unblock("Bearer $token", request)


    suspend fun requestOrGetChat(
        token: String,
        targetId: Long
    ): ChatResponse {

        return api.requestOrGetChat(
            token = "Bearer $token",
            request = ChatRequest(targetId)
        )
    }
}