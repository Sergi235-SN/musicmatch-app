package com.musicmatch.mobile.model.dto

data class MatchCandidatesResponse(
    val profileComplete: Boolean,
    val message: String?,
    val candidates: List<ProfileCardDTO>,
    val noMoreCandidates: Boolean
)