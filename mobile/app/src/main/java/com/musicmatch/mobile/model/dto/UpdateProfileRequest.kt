package com.musicmatch.mobile.model.dto

import com.musicmatch.mobile.model.ExperienceLevel

data class UpdateProfileRequest(
    val biography: String? = null,
    val cityId: Long? = null,
    val experienceLevel: ExperienceLevel? = null,
    val styleIds: List<Long>? = null,
    val instruments: List<InstrumentLevelRequest>? = null
)