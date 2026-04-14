package com.musicmatch.mobile.model.dto

import com.musicmatch.mobile.model.ExperienceLevel

data class ProfileCardDTO(
    val id: Long,
    val username: String,
    val bio: String?,
    val city: String?,
    val profilePicture: String?,
    val styles: List<Long>?,
    val instruments: List<InstrumentLevelResponse>,
    val score: Double,
    val profileLevel: ExperienceLevel
)