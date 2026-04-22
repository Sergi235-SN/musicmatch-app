package com.musicmatch.mobile.data.repository

import com.musicmatch.mobile.data.ApiService
import com.musicmatch.mobile.data.safeApiCall
import com.musicmatch.mobile.model.dto.ProfileSearchCardDTO
import com.musicmatch.mobile.model.dto.ProfileSearchRequest

class ProfileSearchRepository(
    private val api: ApiService
) {

    suspend fun searchProfiles(
        token: String,
        request: ProfileSearchRequest
    ): List<ProfileSearchCardDTO> {
        return safeApiCall {
            api.searchProfiles(
                token = "Bearer $token",
                request = request
            )
        }
    }
}