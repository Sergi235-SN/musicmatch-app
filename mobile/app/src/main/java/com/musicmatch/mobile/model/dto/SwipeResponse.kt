package com.musicmatch.mobile.model.dto

data class SwipeResponse(
    val success: Boolean,
    val match: Boolean,
    val message: String?
)