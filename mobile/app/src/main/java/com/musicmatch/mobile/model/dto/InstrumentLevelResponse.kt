package com.musicmatch.mobile.model.dto

import com.musicmatch.mobile.model.ExperienceLevel

data class InstrumentLevelResponse(
    val instrumentId: Long,
    val level: ExperienceLevel
)