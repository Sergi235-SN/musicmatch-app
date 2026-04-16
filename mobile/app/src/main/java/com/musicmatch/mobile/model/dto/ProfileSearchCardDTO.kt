package com.musicmatch.mobile.model.dto

data class ProfileSearchCardDTO(
    val id: Long,
    val username: String,
    val city: String?,
    val profilePicture: String,
    val experienceLevel: String
)