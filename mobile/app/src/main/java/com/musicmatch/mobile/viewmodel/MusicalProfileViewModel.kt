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
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File

class MusicalProfileViewModel(
    private val repository: UserRepository,
    private val tokenManager: TokenManager
) : ViewModel() {

    companion object {
        private const val MAX_AVATAR_SIZE_BYTES = 5_000_000L
    }

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
    var errorMessage by mutableStateOf<String?>(null)

    var username by mutableStateOf<String?>(null)
    var email by mutableStateOf<String?>(null)

    private fun getToken(context: Context): String? {
        return tokenManager.getToken(context)
    }

    fun loadData(context: Context) {
        val token = getToken(context) ?: return

        viewModelScope.launch {
            try {
                errorMessage = null

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

            } catch (e: Exception) {
                errorMessage = e.message ?: "No se pudo cargar el perfil"
            }
        }
    }

    fun saveStep1(context: Context, onNext: () -> Unit) {
        val token = getToken(context) ?: return

        viewModelScope.launch {
            isLoading = true
            errorMessage = null

            try {
                val request = UpdateProfileRequest(
                    experienceLevel = globalExperience,
                    styleIds = selectedStyles.map { it.id },
                    instruments = selectedInstruments.map {
                        InstrumentLevelRequest(it.first.id, it.second)
                    }
                )

                repository.updateProfile(token, request)
                onNext()

            } catch (e: Exception) {
                errorMessage = e.message ?: "No se pudieron guardar los cambios"
            } finally {
                isLoading = false
            }
        }
    }

    fun saveStep2(context: Context, onFinish: () -> Unit) {
        val token = getToken(context) ?: return

        viewModelScope.launch {
            isLoading = true
            errorMessage = null

            try {
                imageUri?.let { uri ->
                    val uploadedPath = uploadAvatarIfNeeded(context, token, uri)
                    currentAvatarUrl = NetworkConfig.getAvatarUrl(uploadedPath)
                }

                val request = UpdateProfileRequest(
                    biography = biography,
                    cityId = selectedCityId
                )

                repository.updateProfile(token, request)
                onFinish()

            } catch (e: Exception) {
                errorMessage = e.message ?: "No se pudo completar el perfil"
            } finally {
                isLoading = false
            }
        }
    }

    fun saveFullProfile(context: Context, onFinish: () -> Unit) {
        val token = getToken(context) ?: return

        viewModelScope.launch {
            isLoading = true
            errorMessage = null

            try {
                imageUri?.let { uri ->
                    val uploadedPath = uploadAvatarIfNeeded(context, token, uri)
                    currentAvatarUrl = NetworkConfig.getAvatarUrl(uploadedPath)
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

                repository.updateProfile(token, request)
                onFinish()

            } catch (e: Exception) {
                errorMessage = e.message ?: "No se pudieron guardar los cambios"
            } finally {
                isLoading = false
            }
        }
    }

    fun clearError() {
        errorMessage = null
    }

    private suspend fun uploadAvatarIfNeeded(
        context: Context,
        token: String,
        uri: Uri
    ): String {
        val file = uriToFile(context, uri)

        if (file.length() > MAX_AVATAR_SIZE_BYTES) {
            throw IllegalArgumentException("La imagen supera el tamaño máximo de 5 MB")
        }

        val body = MultipartBody.Part.createFormData(
            "file",
            file.name,
            file.asRequestBody("image/*".toMediaTypeOrNull())
        )

        return repository.uploadAvatar(token, body)
    }

    private fun uriToFile(context: Context, uri: Uri): File {
        val inputStream = context.contentResolver.openInputStream(uri)
            ?: throw IllegalStateException("No se pudo abrir la imagen seleccionada")

        val file = File.createTempFile("upload", ".jpg", context.cacheDir)

        inputStream.use { input ->
            file.outputStream().use { output ->
                input.copyTo(output)
            }
        }

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