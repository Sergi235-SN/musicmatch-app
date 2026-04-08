package com.musicmatch.mobile.model.dto

data class LoginResponse(
    val success: Boolean,
    val message: String,
    val userId: Long?,
    val username: String?
)