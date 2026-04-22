package com.musicmatch.mobile.model.dto

data class VerificationStatusResponse(
    val id: Long? = null,
    val email: String? = null,
    val emailVerified: Boolean = false,
    val state: String? = null
)