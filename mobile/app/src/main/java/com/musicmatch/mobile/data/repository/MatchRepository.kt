package com.musicmatch.mobile.data.repository

import com.musicmatch.mobile.data.ApiService
import com.musicmatch.mobile.model.dto.BlockRequest
import com.musicmatch.mobile.model.dto.SwipeRequest

class MatchRepository(private val api: ApiService) {

    suspend fun getCandidates(userId: Long, token: String) =
        api.getCandidates(userId, "Bearer $token")

    suspend fun swipe(token: String, request: SwipeRequest) =
        api.swipe("Bearer $token", request)

    suspend fun block(token: String, request: BlockRequest) =
        api.block("Bearer $token", request)

}