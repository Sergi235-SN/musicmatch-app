package com.musicmatch.mobile.model.dto

data class ResetPasswordRequest(
    val token: String,
    val password: String
)