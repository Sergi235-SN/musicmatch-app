package com.musicmatch.mobile.model.dto

data class LoginResponse(
    val id: Long,
    val username: String,
    val token: String
)