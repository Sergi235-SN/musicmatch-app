package com.musicmatch.mobile.model.dto

data class RegisterRequest(
    val username: String,
    val email: String,
    val password: String
)