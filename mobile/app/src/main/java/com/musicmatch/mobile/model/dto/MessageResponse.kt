package com.musicmatch.mobile.model.dto

data class MessageResponse(
    val id: Long,
    val chatId: Long,
    val senderId: Long,
    val content: String
)