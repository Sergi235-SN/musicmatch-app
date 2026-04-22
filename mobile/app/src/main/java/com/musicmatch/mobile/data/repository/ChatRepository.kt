package com.musicmatch.mobile.data.repository

import com.musicmatch.mobile.data.ApiService
import com.musicmatch.mobile.data.safeApiCall
import com.musicmatch.mobile.model.dto.MessageRequest

class ChatRepository(private val api: ApiService) {

    suspend fun getChats(token: String) =
        safeApiCall { api.getChats("Bearer $token") }

    suspend fun getMessages(token: String, chatId: Long) =
        safeApiCall { api.getMessages("Bearer $token", chatId) }

    suspend fun sendMessage(token: String, request: MessageRequest) =
        safeApiCall { api.sendMessage("Bearer $token", request) }

    suspend fun getPendingChats(token: String) =
        safeApiCall { api.getPendingChats("Bearer $token") }

    suspend fun acceptChat(token: String, chatId: Long) =
        safeApiCall { api.acceptChat("Bearer $token", chatId) }

    suspend fun rejectChat(token: String, chatId: Long) =
        safeApiCall { api.rejectChat("Bearer $token", chatId) }
}