package com.musicmatch.mobile.model.dto

data class SwipeRequest(
    val userId: Long,
    val targetId: Long,
    val liked: Boolean
)