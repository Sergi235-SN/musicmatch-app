package com.musicmatch.mobile.viewmodel

import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.musicmatch.mobile.data.repository.MatchRepository
import com.musicmatch.mobile.data.repository.UserRepository
import com.musicmatch.mobile.model.dto.*
import com.musicmatch.mobile.utils.NetworkConfig
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class PublicProfileViewModel(
    private val repository: UserRepository,
    private val matchRepository: MatchRepository
) : ViewModel() {

    var profile by mutableStateOf<PublicProfileResponse?>(null)
        private set

    var profileImageUrl by mutableStateOf<String?>(null)
        private set

    var availableInstruments by mutableStateOf<List<MusicalOptionDTO>>(emptyList())
        private set

    var availableStyles by mutableStateOf<List<MusicalOptionDTO>>(emptyList())
        private set

    var isLoading by mutableStateOf(false)
        private set

    var error by mutableStateOf<String?>(null)
        private set

    var message by mutableStateOf<String?>(null)
        private set

    var chatStatus by mutableStateOf<String?>(null)
        private set

    var chatId by mutableStateOf<Long?>(null)
        private set

    private val _navigateToChat = MutableStateFlow<Long?>(null)
    val navigateToChat = _navigateToChat.asStateFlow()

    // ✔ NUEVO: eventos para Toast
    private val _toastMessage = MutableStateFlow<String?>(null)
    val toastMessage = _toastMessage.asStateFlow()

    fun load(userId: Long, token: String) {
        viewModelScope.launch {
            isLoading = true
            error = null

            try {
                val options = repository.getMusicalOptions(token)
                availableInstruments = options.instruments
                availableStyles = options.styles

                val result = repository.getPublicProfile(userId, token)
                profile = result

                profileImageUrl = result.profilePicture?.let {
                    NetworkConfig.getAvatarUrl(it)
                }

                chatStatus = result.chatStatus
                chatId = result.chatId

            } catch (e: Exception) {
                _toastMessage.value = e.message ?: "Error al cargar perfil"
            } finally {
                isLoading = false
            }
        }
    }

    fun block(token: String, targetId: Long) {
        viewModelScope.launch {
            try {
                matchRepository.block(token, BlockRequest(targetId))
                profile = profile?.copy(blockedByMe = true)
                _toastMessage.value = "Usuario bloqueado"
            } catch (e: Exception) {
                _toastMessage.value = e.message ?: "Error bloqueando usuario"
            }
        }
    }

    fun unblock(token: String, targetId: Long) {
        viewModelScope.launch {
            try {
                matchRepository.unblock(token, BlockRequest(targetId))
                profile = profile?.copy(blockedByMe = false)
                _toastMessage.value = "Usuario desbloqueado"
            } catch (e: Exception) {
                _toastMessage.value = e.message ?: "Error desbloqueando usuario"
            }
        }
    }

    fun onChatClicked(targetUserId: Long, token: String) {
        viewModelScope.launch {
            try {
                val response = matchRepository.requestOrGetChat(
                    token = token,
                    targetId = targetUserId
                )

                when (response.status) {

                    "ACTIVE" -> {
                        chatStatus = "ACTIVE"
                        chatId = response.chatId
                        _navigateToChat.value = response.chatId
                    }

                    "PENDING" -> {
                        chatStatus = "PENDING"
                        message = "Solicitud enviada. Esperando respuesta."
                        _toastMessage.value = message
                    }

                    else -> {
                        _toastMessage.value = "Estado desconocido del chat"
                    }
                }

            } catch (e: Exception) {
                _toastMessage.value = e.message ?: "Error al iniciar chat"
            }
        }
    }

    fun clearNavigation() {
        _navigateToChat.value = null
    }

    fun clearToast() {
        _toastMessage.value = null
    }

    class Factory(
        private val repository: UserRepository,
        private val matchRepository: MatchRepository
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return PublicProfileViewModel(repository, matchRepository) as T
        }
    }
}