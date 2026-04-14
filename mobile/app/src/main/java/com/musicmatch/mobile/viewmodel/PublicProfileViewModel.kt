package com.musicmatch.mobile.viewmodel

import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.musicmatch.mobile.data.repository.UserRepository
import com.musicmatch.mobile.model.dto.MusicalOptionDTO
import com.musicmatch.mobile.model.dto.UserProfileResponse
import com.musicmatch.mobile.utils.NetworkConfig
import kotlinx.coroutines.launch

class PublicProfileViewModel(
    private val repository: UserRepository
) : ViewModel() {

    var profile by mutableStateOf<UserProfileResponse?>(null)

    var profileImageUrl by mutableStateOf<String?>(null)

    var availableInstruments by mutableStateOf<List<MusicalOptionDTO>>(emptyList())
    var availableStyles by mutableStateOf<List<MusicalOptionDTO>>(emptyList())

    var isLoading by mutableStateOf(false)
    var error by mutableStateOf<String?>(null)

    fun load(userId: Long, token: String) {
        viewModelScope.launch {
            isLoading = true
            error = null

            try {
                val options = repository.getMusicalOptions(token)
                availableInstruments = options.instruments
                availableStyles = options.styles

                val result = repository.getPublicProfile(userId)
                profile = result

                profileImageUrl = result.profilePicture?.let {
                    NetworkConfig.getAvatarUrl(it)
                }

            } catch (e: Exception) {
                error = e.message
            } finally {
                isLoading = false
            }
        }
    }

    class Factory(
        private val repository: UserRepository
    ) : androidx.lifecycle.ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return PublicProfileViewModel(repository) as T
        }
    }
}