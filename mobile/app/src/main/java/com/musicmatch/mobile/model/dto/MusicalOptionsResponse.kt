package com.musicmatch.mobile.model.dto

data class MusicalOptionsResponse(
    val instruments: List<MusicalOptionDTO>,
    val styles: List<MusicalOptionDTO>
)