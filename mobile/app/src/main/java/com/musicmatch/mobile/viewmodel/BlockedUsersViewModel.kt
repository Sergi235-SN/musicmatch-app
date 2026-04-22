package com.musicmatch.mobile.viewmodel

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.musicmatch.mobile.data.repository.MatchRepository
import com.musicmatch.mobile.model.dto.BlockRequest
import com.musicmatch.mobile.model.dto.BlockedUserDTO
import com.musicmatch.mobile.utils.TokenManager
import kotlinx.coroutines.launch

class BlockedUsersViewModel(
    private val matchRepository: MatchRepository,
    private val tokenManager: TokenManager
) : ViewModel() {

    var blockedUsers by mutableStateOf<List<BlockedUserDTO>>(emptyList())
        private set

    var loading by mutableStateOf(false)
        private set

    var message by mutableStateOf<String?>(null)
        private set

    private var token: String = ""

    fun load(context: Context) {
        viewModelScope.launch {
            loading = true
            message = null

            try {
                token = tokenManager.getToken(context)
                    ?: throw Exception("Sesión expirada")

                blockedUsers = matchRepository.getBlockedUsers(token)

            } catch (e: Exception) {
                message = e.message ?: "No se pudieron cargar los usuarios bloqueados"
            } finally {
                loading = false
            }
        }
    }

    fun unblock(userId: Long) {
        viewModelScope.launch {
            try {
                matchRepository.unblock(token, BlockRequest(userId))
                blockedUsers = blockedUsers.filterNot { it.id == userId }
                message = "Usuario desbloqueado"
            } catch (e: Exception) {
                message = e.message ?: "No se pudo desbloquear el usuario"
            }
        }
    }

    fun clearMessage() {
        message = null
    }
}