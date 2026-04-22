package com.musicmatch.mobile.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.musicmatch.mobile.data.ApiService
import com.musicmatch.mobile.data.repository.ChatRepository
import com.musicmatch.mobile.model.dto.ChatPreview
import com.musicmatch.mobile.model.dto.MessageRequest
import com.musicmatch.mobile.model.dto.MessageResponse
import com.musicmatch.mobile.utils.TokenManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ChatViewModel : ViewModel() {

    private val api = ApiService.create()
    private val repo = ChatRepository(api)
    private val tokenManager = TokenManager()

    private val _chats = MutableStateFlow<List<ChatPreview>>(emptyList())
    val chats = _chats.asStateFlow()

    private val _pendingChats = MutableStateFlow<List<ChatPreview>>(emptyList())
    val pendingChats = _pendingChats.asStateFlow()

    private val _messages = MutableStateFlow<List<MessageResponse>>(emptyList())
    val messages = _messages.asStateFlow()

    private val _userId = MutableStateFlow<Long?>(null)
    val userId = _userId.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage = _errorMessage.asStateFlow()

    private var token: String = ""

    fun load(context: Context) {
        viewModelScope.launch {
            token = tokenManager.getToken(context) ?: return@launch
            _userId.value = tokenManager.getUserIdFromToken(context)

            try {
                refreshAll()
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "No se pudieron cargar los chats"
            }
        }
    }

    private suspend fun refreshAll() {
        _chats.value = repo.getChats(token)
        _pendingChats.value = repo.getPendingChats(token)
    }

    fun openChat(chatId: Long) {
        viewModelScope.launch {
            try {
                _messages.value = repo.getMessages(token, chatId)
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "No se pudieron cargar los mensajes"
            }
        }
    }

    suspend fun refreshChat(chatId: Long): Boolean {
        return try {
            _messages.value = repo.getMessages(token, chatId)
            true
        } catch (e: Exception) {
            _errorMessage.value = e.message ?: "No se pudo actualizar el chat"
            false
        }
    }

    fun sendMessage(chatId: Long, text: String) {
        if (text.isBlank()) return

        viewModelScope.launch {
            try {
                repo.sendMessage(token, MessageRequest(chatId, text.trim()))
                _messages.value = repo.getMessages(token, chatId)
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "No se pudo enviar el mensaje"
            }
        }
    }

    fun acceptChat(chatId: Long) {
        viewModelScope.launch {
            try {
                repo.acceptChat(token, chatId)
                refreshAll()
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "No se pudo aceptar la solicitud"
            }
        }
    }

    fun rejectChat(chatId: Long) {
        viewModelScope.launch {
            try {
                repo.rejectChat(token, chatId)
                refreshAll()
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "No se pudo rechazar la solicitud"
            }
        }
    }

    fun clearError() {
        _errorMessage.value = null
    }

    fun reset() {
        _chats.value = emptyList()
        _pendingChats.value = emptyList()
        _messages.value = emptyList()
        _userId.value = null
        _errorMessage.value = null
        token = ""
    }
}