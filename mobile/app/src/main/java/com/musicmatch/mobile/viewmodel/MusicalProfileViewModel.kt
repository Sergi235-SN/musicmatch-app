package com.musicmatch.mobile.viewmodel

import android.content.Context
import android.net.Uri
import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.musicmatch.mobile.data.repository.UserRepository
import com.musicmatch.mobile.model.City
import com.musicmatch.mobile.model.ExperienceLevel
import com.musicmatch.mobile.model.dto.*
import com.musicmatch.mobile.utils.NetworkConfig
import com.musicmatch.mobile.utils.TokenManager
import kotlinx.coroutines.launch
import okhttp3.MultipartBody
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File

class MusicalProfileViewModel(
    private val repository: UserRepository,
    private val tokenManager: TokenManager
) : ViewModel() {


    val selectedInstruments =
        mutableStateListOf<Pair<MusicalOptionDTO, ExperienceLevel>>()

    val selectedStyles = mutableStateListOf<MusicalOptionDTO>()

    var globalExperience by mutableStateOf(ExperienceLevel.PRINCIPIANTE)

    var biography by mutableStateOf("")
    var selectedCityId by mutableStateOf<Long?>(null)

    var imageUri by mutableStateOf<Uri?>(null)
    var currentAvatarUrl by mutableStateOf<String?>(null)

    var availableCities by mutableStateOf<List<City>>(emptyList())
    var availableInstruments by mutableStateOf<List<MusicalOptionDTO>>(emptyList())
    var availableStyles by mutableStateOf<List<MusicalOptionDTO>>(emptyList())

    var isLoading by mutableStateOf(false)

    var username by mutableStateOf<String?>(null)
    var email by mutableStateOf<String?>(null)


    private fun getToken(context: Context): String? {
        return tokenManager.getToken(context)
    }

    private fun getUserId(context: Context): Long {
        return tokenManager.getUserIdFromToken(context)
            ?: throw IllegalStateException("Token inválido o sin userId")
    }


    fun loadData(context: Context) {
        val token = getToken(context) ?: return

        viewModelScope.launch {
            val options = repository.getMusicalOptions(token)
            val cities = repository.getCities(token)
            val profile = repository.getCurrentUser(token)

            availableInstruments = options.instruments
            availableStyles = options.styles
            availableCities = cities

            val data = profile.data ?: return@launch

            username = data.username
            email = data.email
            biography = data.biography ?: ""
            selectedCityId = data.cityId
            globalExperience = data.experienceLevel ?: ExperienceLevel.PRINCIPIANTE

            selectedStyles.clear()
            data.styleIds?.forEach { id ->
                availableStyles.find { it.id == id }?.let {
                    selectedStyles.add(it)
                }
            }

            selectedInstruments.clear()
            data.instruments?.forEach { inst ->
                availableInstruments.find { it.id == inst.instrumentId }?.let { option ->
                    selectedInstruments.add(
                        option to (inst.level ?: ExperienceLevel.PRINCIPIANTE)
                    )
                }
            }

            currentAvatarUrl = data.profilePicture?.let {
                NetworkConfig.getAvatarUrl(it)
            }
        }
    }


    fun saveStep1(context: Context, onNext: () -> Unit) {
        val token = getToken(context) ?: return
        val userId = getUserId(context)

        viewModelScope.launch {
            isLoading = true

            try {
                val request = UpdateProfileRequest(
                    experienceLevel = globalExperience,
                    styleIds = selectedStyles.map { it.id },
                    instruments = selectedInstruments.map {
                        InstrumentLevelRequest(it.first.id, it.second)
                    }
                )

                repository.updateProfile(token, userId, request)
                onNext()

            } finally {
                isLoading = false
            }
        }
    }


    fun saveStep2(context: Context, onFinish: () -> Unit) {
        val token = getToken(context) ?: return
        val userId = getUserId(context)

        viewModelScope.launch {
            isLoading = true

            try {

                imageUri?.let { uri ->
                    val file = uriToFile(context, uri)

                    val body = MultipartBody.Part.createFormData(
                        "file",
                        file.name,
                        file.asRequestBody("image/*".toMediaTypeOrNull())
                    )

                    val filename = repository.uploadAvatar(token, userId, body)

                    if (!filename.isNullOrEmpty()) {
                        currentAvatarUrl = NetworkConfig.getAvatarUrl(filename)
                    }
                }

                val request = UpdateProfileRequest(
                    biography = biography,
                    cityId = selectedCityId
                )

                repository.updateProfile(token, userId, request)

                onFinish()

            } finally {
                isLoading = false
            }
        }
    }


    fun saveFullProfile(context: Context, onFinish: () -> Unit) {
        val token = getToken(context) ?: return
        val userId = getUserId(context)

        viewModelScope.launch {
            isLoading = true

            try {

                var uploadedFilename: String? = null

                imageUri?.let { uri ->
                    val file = uriToFile(context, uri)

                    val body = MultipartBody.Part.createFormData(
                        "file",
                        file.name,
                        file.asRequestBody("image/*".toMediaTypeOrNull())
                    )

                    uploadedFilename = repository.uploadAvatar(token, userId, body)
                }

                val request = UpdateProfileRequest(
                    biography = biography,
                    cityId = selectedCityId,
                    experienceLevel = globalExperience,
                    styleIds = selectedStyles.map { it.id },
                    instruments = selectedInstruments.map {
                        InstrumentLevelRequest(it.first.id, it.second)
                    }
                )

                repository.updateProfile(token, userId, request)

                uploadedFilename?.let {
                    currentAvatarUrl = NetworkConfig.getAvatarUrl(it)
                }

                onFinish()

            } finally {
                isLoading = false
            }
        }
    }


    private fun uriToFile(context: Context, uri: Uri): File {
        val inputStream = context.contentResolver.openInputStream(uri)!!
        val file = File.createTempFile("upload", ".jpg", context.cacheDir)
        file.outputStream().use { inputStream.copyTo(it) }
        return file
    }


    class Factory(
        private val repository: UserRepository,
        private val tokenManager: TokenManager
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return MusicalProfileViewModel(repository, tokenManager) as T
        }
    }
}