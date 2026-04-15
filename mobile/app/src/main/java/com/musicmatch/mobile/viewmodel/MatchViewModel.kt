package com.musicmatch.mobile.viewmodel

import android.content.Context
import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.musicmatch.mobile.data.repository.MatchRepository
import com.musicmatch.mobile.data.repository.UserRepository
import com.musicmatch.mobile.model.dto.*
import com.musicmatch.mobile.utils.TokenManager
import kotlinx.coroutines.launch

class MatchViewModel(
    private val matchRepository: MatchRepository,
    private val userRepository: UserRepository,
    private val tokenManager: TokenManager
) : ViewModel() {

    var candidates by mutableStateOf<List<ProfileCardDTO>>(emptyList())
        private set

    var loading by mutableStateOf(false)
        private set

    var message by mutableStateOf<String?>(null)
        private set

    var profileComplete by mutableStateOf(true)
        private set

    var currentIndex by mutableStateOf(0)
        private set

    val currentProfile: ProfileCardDTO?
        get() = candidates.getOrNull(currentIndex)

    var availableInstruments by mutableStateOf<List<MusicalOptionDTO>>(emptyList())
        private set

    var availableStyles by mutableStateOf<List<MusicalOptionDTO>>(emptyList())
        private set

    private var instrumentMap = mapOf<Long, String>()
    private var styleMap = mapOf<Long, String>()

    private var userId: Long = 0L
    private var token: String = ""

    fun load(context: Context) {
        viewModelScope.launch {
            loading = true
            message = null

            try {
                token = tokenManager.getToken(context)
                    ?: throw Exception("Sesión expirada")

                userId = tokenManager.getUserIdFromToken(context)
                    ?: throw Exception("ID no encontrado")

                val options = userRepository.getMusicalOptions(token)

                availableInstruments = options.instruments
                availableStyles = options.styles

                instrumentMap = options.instruments.associate { it.id to it.name }
                styleMap = options.styles.associate { it.id to it.name }

                val res = matchRepository.getCandidates(userId, token)

                profileComplete = res.profileComplete
                candidates = res.candidates

                message = when {
                    res.candidates.isEmpty() ->
                        "No hay más músicos cerca de ti"
                    !res.message.isNullOrBlank() ->
                        res.message
                    else -> null
                }

                currentIndex = 0

            } catch (e: Exception) {
                message = "Error al cargar: ${e.localizedMessage}"
            } finally {
                loading = false
            }
        }
    }

    fun getInstrumentName(id: Long): String =
        instrumentMap[id] ?: "Instrumento"

    fun getStyleName(id: Long): String =
        styleMap[id] ?: "Estilo"

    fun swipe(
        liked: Boolean,
        context: Context,
        onMatch: (Boolean) -> Unit = {}
    ) {
        val current = currentProfile ?: return

        viewModelScope.launch {
            try {
                val res = matchRepository.swipe(
                    token,
                    SwipeRequest(userId, current.id, liked)
                )

                if (res.match) onMatch(true)

                next()

            } catch (e: Exception) {
                message = "Error en la conexión"
            }
        }
    }

    private fun next() {
        if (candidates.isNotEmpty() && currentIndex < candidates.lastIndex) {
            currentIndex++
        } else {
            candidates = emptyList()
            currentIndex = 0
        }
    }
}