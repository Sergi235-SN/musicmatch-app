package com.musicmatch.mobile.data.repository

import com.musicmatch.mobile.data.ApiService
import com.musicmatch.mobile.model.dto.ChatRequest
import com.musicmatch.mobile.model.dto.ChatResponse
import com.musicmatch.mobile.model.dto.MessageRequest

class ChatRepository(private val api: ApiService) {

    suspend fun getChats(token: String) =
        api.getChats("Bearer $token")

    suspend fun getMessages(token: String, chatId: Long) =
        api.getMessages("Bearer $token", chatId)

    suspend fun sendMessage(token: String, request: MessageRequest) =
        api.sendMessage("Bearer $token", request)


    suspend fun getPendingChats(token: String) =
        api.getPendingChats("Bearer $token")

    suspend fun acceptChat(token: String, chatId: Long) =
        api.acceptChat("Bearer $token", chatId)

    suspend fun rejectChat(token: String, chatId: Long) =
        api.rejectChat("Bearer $token", chatId)
}