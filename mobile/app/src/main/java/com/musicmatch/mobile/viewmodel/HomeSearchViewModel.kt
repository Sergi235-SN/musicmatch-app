package com.musicmatch.mobile.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.musicmatch.mobile.data.repository.ProfileSearchRepository
import com.musicmatch.mobile.data.repository.UserRepository
import com.musicmatch.mobile.model.dto.MusicalOptionDTO
import com.musicmatch.mobile.model.dto.ProfileSearchCardDTO
import com.musicmatch.mobile.model.dto.ProfileSearchRequest
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class HomeSearchViewModel(
    private val searchRepository: ProfileSearchRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    // =========================
    // RESULTS
    // =========================
    private val _profiles = MutableStateFlow<List<ProfileSearchCardDTO>>(emptyList())
    val profiles: StateFlow<List<ProfileSearchCardDTO>> = _profiles

    // =========================
    // SEARCH STATE
    // =========================
    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query

    private val _experience = MutableStateFlow<String?>(null)
    val experience: StateFlow<String?> = _experience

    // =========================
    // FILTER OPTIONS (FROM DB)
    // =========================
    private val _availableInstruments = MutableStateFlow<List<MusicalOptionDTO>>(emptyList())
    val availableInstruments: StateFlow<List<MusicalOptionDTO>> = _availableInstruments

    private val _availableStyles = MutableStateFlow<List<MusicalOptionDTO>>(emptyList())
    val availableStyles: StateFlow<List<MusicalOptionDTO>> = _availableStyles

    // =========================
    // SELECTED FILTERS
    // =========================
    private val _selectedInstruments = MutableStateFlow<Set<Long>>(emptySet())
    val selectedInstruments: StateFlow<Set<Long>> = _selectedInstruments

    private val _selectedStyles = MutableStateFlow<Set<Long>>(emptySet())
    val selectedStyles: StateFlow<Set<Long>> = _selectedStyles

    private var searchJob: Job? = null

    // =========================
    // LOAD FILTERS (DB)
    // =========================
    fun loadFilters(token: String) {
        viewModelScope.launch {
            runCatching {
                val options = userRepository.getMusicalOptions(token)
                _availableInstruments.value = options.instruments
                _availableStyles.value = options.styles
            }
        }
    }

    // =========================
    // SEARCH TRIGGERS
    // =========================
    fun setQuery(value: String, token: String) {
        _query.value = value
        triggerSearch(token)
    }

    fun setExperience(level: String?, token: String) {
        _experience.value = level
        triggerSearch(token)
    }

    fun toggleInstrument(id: Long, token: String) {
        _selectedInstruments.value =
            if (_selectedInstruments.value.contains(id))
                _selectedInstruments.value - id
            else
                _selectedInstruments.value + id

        triggerSearch(token)
    }

    fun toggleStyle(id: Long, token: String) {
        _selectedStyles.value =
            if (_selectedStyles.value.contains(id))
                _selectedStyles.value - id
            else
                _selectedStyles.value + id

        triggerSearch(token)
    }

    fun search(token: String) {
        triggerSearch(token)
    }

    // =========================
    // CORE SEARCH
    // =========================
    private fun triggerSearch(token: String) {

        searchJob?.cancel()

        searchJob = viewModelScope.launch {
            delay(300)

            val request = ProfileSearchRequest(
                query = _query.value.ifBlank { null },
                instrumentIds = _selectedInstruments.value.takeIf { it.isNotEmpty() },
                styleIds = _selectedStyles.value.takeIf { it.isNotEmpty() },
                experienceLevel = _experience.value
            )

            _profiles.value = searchRepository.searchProfiles(token, request)
        }
    }

    // =========================
    // FACTORY
    // =========================
    class Factory(
        private val searchRepository: ProfileSearchRepository,
        private val userRepository: UserRepository
    ) : ViewModelProvider.Factory {

        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return HomeSearchViewModel(searchRepository, userRepository) as T
        }
    }
}