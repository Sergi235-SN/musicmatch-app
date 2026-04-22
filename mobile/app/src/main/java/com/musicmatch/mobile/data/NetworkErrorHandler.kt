package com.musicmatch.mobile.data

import com.google.gson.Gson
import retrofit2.HttpException
import java.io.IOException
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

class ApiException(message: String) : Exception(message)

private data class ErrorEnvelope(
    val success: Boolean? = null,
    val message: String? = null
)

object NetworkErrorHandler {

    private val gson = Gson()

    fun toException(throwable: Throwable): Exception {
        return when (throwable) {
            is ApiException -> throwable

            is HttpException -> {
                val backendMessage = parseBackendMessage(throwable)
                ApiException(backendMessage ?: defaultHttpMessage(throwable.code()))
            }

            is SocketTimeoutException -> {
                ApiException("El servidor está tardando demasiado en responder")
            }

            is UnknownHostException, is ConnectException -> {
                ApiException("No se pudo conectar con el servidor")
            }

            is IOException -> {
                ApiException("Error de red. Revisa tu conexión")
            }

            else -> {
                ApiException(throwable.message ?: "Ha ocurrido un error inesperado")
            }
        }
    }

    private fun parseBackendMessage(exception: HttpException): String? {
        return try {
            val body = exception.response()?.errorBody()?.string()
            if (body.isNullOrBlank()) return null

            gson.fromJson(body, ErrorEnvelope::class.java)?.message
        } catch (_: Exception) {
            null
        }
    }

    private fun defaultHttpMessage(code: Int): String {
        return when (code) {
            400 -> "Datos no válidos"
            401 -> "Sesión expirada o no válida"
            403 -> "No tienes permiso para esta acción"
            404 -> "Recurso no encontrado"
            409 -> "Conflicto de datos"
            500 -> "Error interno del servidor"
            else -> "No se pudo completar la operación"
        }
    }
}

suspend inline fun <T> safeApiCall(crossinline block: suspend () -> T): T {
    return try {
        block()
    } catch (t: Throwable) {
        throw NetworkErrorHandler.toException(t)
    }
}