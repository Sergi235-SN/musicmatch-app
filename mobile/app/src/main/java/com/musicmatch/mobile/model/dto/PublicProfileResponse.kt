package com.musicmatch.mobile.model.dto

import com.musicmatch.mobile.model.ExperienceLevel

data class PublicProfileResponse(
    val id: Long,
    val username: String,
    val biography: String?,
    val cityName: String?,
    val cityId: Long?,
    val profilePicture: String?,
    val styleIds: List<Long>,
    val instruments: List<InstrumentLevelResponse>,
    val experienceLevel: ExperienceLevel,

    val blockedByMe: Boolean,
    val blockedMe: Boolean,

    val chatStatus: String?,
    val chatId: Long?
)