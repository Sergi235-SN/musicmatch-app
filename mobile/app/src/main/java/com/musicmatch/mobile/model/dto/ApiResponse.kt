package com.musicmatch.mobile.model.dto

data class ApiResponse<T>(
    val success: Boolean,
    val message: String,
    val data: T?
)