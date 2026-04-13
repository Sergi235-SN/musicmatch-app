package com.musicmatch.mobile.model.dto

import com.musicmatch.mobile.model.ExperienceLevel

data class UserProfileResponse(
    val id: Long,
    val username: String,
    val biography: String?,
    val cityId: Long?,
    val cityName: String?,
    val profilePicture: String?,
    val experienceLevel: ExperienceLevel?,
    val styleIds: List<Long>?,
    val instruments: List<InstrumentLevelResponse>?
)