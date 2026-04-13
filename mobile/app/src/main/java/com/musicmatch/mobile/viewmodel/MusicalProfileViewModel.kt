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

    fun loadData(context: Context) {
        val token = tokenManager.getToken(context) ?: return

        viewModelScope.launch {
            val options = repository.getMusicalOptions(token)
            val cities = repository.getCities(token)
            val profile = repository.getCurrentUser(token)

            availableInstruments = options.instruments
            availableStyles = options.styles
            availableCities = cities

            val data = profile.data

            if (data != null) {

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
                    "http://10.0.2.2:8080/api/profile/avatar/$it"
                }
            }
        }
    }


    fun saveStep1(context: Context, userId: Long, onNext: () -> Unit) {
        val token = tokenManager.getToken(context) ?: return

        viewModelScope.launch {
            isLoading = true

            val request = UpdateProfileRequest(
                experienceLevel = globalExperience,
                styleIds = selectedStyles.map { it.id },
                instruments = selectedInstruments.map {
                    InstrumentLevelRequest(it.first.id, it.second)
                }
            )

            repository.updateProfile(token, userId, request)

            isLoading = false
            onNext()
        }
    }

    fun saveStep2(context: Context, userId: Long, onFinish: () -> Unit) {
        val token = tokenManager.getToken(context) ?: return

        viewModelScope.launch {
            isLoading = true

            // ================= UPLOAD AVATAR =================
            imageUri?.let { uri ->
                val file = uriToFile(context, uri)

                val requestFile = file
                    .asRequestBody("image/*".toMediaTypeOrNull())

                val body = MultipartBody.Part.createFormData(
                    "file",
                    file.name,
                    requestFile
                )

                val filename = repository.uploadAvatar(token, userId, body)

                if (!filename.isNullOrEmpty()) {
                    currentAvatarUrl =
                        "http://10.0.2.2:8080/api/profile/avatar/$filename"
                }
            }

            // ================= UPDATE PROFILE =================
            val request = UpdateProfileRequest(
                biography = biography,
                cityId = selectedCityId
            )

            repository.updateProfile(token, userId, request)

            isLoading = false
            onFinish()
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