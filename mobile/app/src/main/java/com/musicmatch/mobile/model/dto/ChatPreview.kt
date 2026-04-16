package com.musicmatch.mobile.model.dto

data class ChatPreview(
    val chatId: Long,
    val otherUserId: Long,
    val otherUsername: String,
    val otherProfileImage: String?,
    val lastMessage: String?,
    val status: String
)