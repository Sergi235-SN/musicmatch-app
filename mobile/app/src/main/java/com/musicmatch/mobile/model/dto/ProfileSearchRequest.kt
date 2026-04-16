package com.musicmatch.mobile.model.dto

data class ProfileSearchRequest(
    val query: String?,
    val instrumentIds: Set<Long>?,
    val styleIds: Set<Long>?,
    val experienceLevel: String?
)