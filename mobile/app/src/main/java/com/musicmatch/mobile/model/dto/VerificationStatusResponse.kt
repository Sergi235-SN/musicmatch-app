package com.musicmatch.mobile.model.dto

data class VerificationStatusResponse(
    val userId: Long,
    val email: String,
    val emailVerified: Boolean
)