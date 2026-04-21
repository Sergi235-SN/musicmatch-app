package com.musicmatch.mobile.viewmodel

import android.content.Context
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.musicmatch.mobile.data.ApiService
import com.musicmatch.mobile.data.repository.UserRepository
import com.musicmatch.mobile.model.dto.LoginRequest
import com.musicmatch.mobile.utils.TokenManager
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

data class EmailVerificationUiState(
    val isChecking: Boolean = true,
    val isVerified: Boolean = false,
    val isLoggingIn: Boolean = false,
    val isSuccess: Boolean = false,
    val userId: Long? = null,
    val errorMessage: String? = null
)

class EmailVerificationViewModel : ViewModel() {

    private val repository = UserRepository(ApiService.create())
    private val tokenManager = TokenManager()
    private var pollingJob: Job? = null

    var uiState = mutableStateOf(EmailVerificationUiState())
        private set

    fun startPolling(
        context: Context,
        email: String,
        password: String
    ) {
        if (email.isBlank()) return
        if (pollingJob != null) return

        uiState.value = EmailVerificationUiState(
            isChecking = true,
            isVerified = false,
            isLoggingIn = false,
            isSuccess = false,
            userId = null,
            errorMessage = null
        )

        pollingJob = viewModelScope.launch {
            while (isActive && !uiState.value.isSuccess) {
                try {
                    val verificationResponse = repository.getVerificationStatus(email)

                    if (verificationResponse.success && verificationResponse.data != null) {
                        val verified = verificationResponse.data.emailVerified

                        if (verified) {
                            uiState.value = uiState.value.copy(
                                isChecking = false,
                                isVerified = true,
                                isLoggingIn = true,
                                errorMessage = null
                            )

                            val loginResponse = repository.loginUser(
                                LoginRequest(email, password)
                            )

                            if (loginResponse.success && loginResponse.data != null) {
                                val accessToken = loginResponse.data.token
                                val refreshToken = loginResponse.data.refreshToken
                                val userId = loginResponse.data.id

                                if (!accessToken.isNullOrBlank() && !refreshToken.isNullOrBlank()) {
                                    tokenManager.saveTokens(context, accessToken, refreshToken)

                                    uiState.value = uiState.value.copy(
                                        isChecking = false,
                                        isVerified = true,
                                        isLoggingIn = false,
                                        isSuccess = true,
                                        userId = userId,
                                        errorMessage = null
                                    )
                                    break
                                } else {
                                    uiState.value = uiState.value.copy(
                                        isChecking = false,
                                        isVerified = true,
                                        isLoggingIn = false,
                                        isSuccess = false,
                                        errorMessage = "No se pudo iniciar sesión automáticamente"
                                    )
                                }
                            } else {
                                uiState.value = uiState.value.copy(
                                    isChecking = false,
                                    isVerified = true,
                                    isLoggingIn = false,
                                    isSuccess = false,
                                    errorMessage = loginResponse.message
                                )
                            }
                        } else {
                            uiState.value = uiState.value.copy(
                                isChecking = false,
                                isVerified = false,
                                isLoggingIn = false,
                                errorMessage = null
                            )
                        }
                    } else {
                        uiState.value = uiState.value.copy(
                            isChecking = false,
                            errorMessage = verificationResponse.message
                        )
                    }
                } catch (e: Exception) {
                    uiState.value = uiState.value.copy(
                        isChecking = false,
                        isLoggingIn = false,
                        errorMessage = "No se pudo comprobar la verificación"
                    )
                }

                delay(3000)
            }
        }
    }

    fun retry(context: Context, email: String, password: String) {
        pollingJob?.cancel()
        pollingJob = null
        startPolling(context, email, password)
    }

    override fun onCleared() {
        pollingJob?.cancel()
        super.onCleared()
    }
}