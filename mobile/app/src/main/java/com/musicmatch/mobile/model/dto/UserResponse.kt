package com.musicmatch.mobile.model.dto

data class UserResponse(
    val id: Long,
    val username: String,
    val email: String,
    val token: String
)