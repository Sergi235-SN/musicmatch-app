package com.musicmatch.mobile.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.musicmatch.mobile.data.ApiService
import com.musicmatch.mobile.data.repository.ChatRepository
import com.musicmatch.mobile.model.dto.*
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

    private var token: String = ""

    fun load(context: Context) {
        viewModelScope.launch {

            token = tokenManager.getToken(context) ?: return@launch
            _userId.value = tokenManager.getUserIdFromToken(context)

            refreshAll()
        }
    }

    private fun refreshAll() {
        viewModelScope.launch {
            _chats.value = repo.getChats(token)
            _pendingChats.value = repo.getPendingChats(token)
        }
    }

    fun openChat(chatId: Long) {
        viewModelScope.launch {
            _messages.value = repo.getMessages(token, chatId)
        }
    }

    fun sendMessage(chatId: Long, text: String) {
        viewModelScope.launch {
            repo.sendMessage(token, MessageRequest(chatId, text))
            _messages.value = repo.getMessages(token, chatId)
        }
    }

    fun acceptChat(context: Context, chatId: Long) {
        viewModelScope.launch {
            repo.acceptChat(token, chatId)
            refreshAll()
        }
    }

    fun rejectChat(context: Context, chatId: Long) {
        viewModelScope.launch {
            repo.rejectChat(token, chatId)
            refreshAll()
        }
    }

    suspend fun chatExists(chatId: Long): Boolean {
        return try {
            repo.getMessages(token, chatId)
            true
        } catch (e: Exception) {
            false
        }
    }

    fun reset() {
        _chats.value = emptyList()
        _pendingChats.value = emptyList()
        _messages.value = emptyList()
        _userId.value = null
        token = ""
    }
}