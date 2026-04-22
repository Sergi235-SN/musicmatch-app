package com.musicmatch.mobile.data

import com.google.gson.GsonBuilder
import com.musicmatch.mobile.utils.NetworkConfig
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object ApiClient {

    @Volatile
    private var apiService: ApiService? = null

    @Volatile
    private var currentBaseUrl: String? = null

    fun getService(): ApiService {
        val baseUrl = normalizeBaseUrl(NetworkConfig.BASE_URL)

        if (apiService == null || currentBaseUrl != baseUrl) {
            synchronized(this) {
                if (apiService == null || currentBaseUrl != baseUrl) {
                    apiService = buildRetrofit(baseUrl).create(ApiService::class.java)
                    currentBaseUrl = baseUrl
                }
            }
        }

        return apiService!!
    }

    private fun buildRetrofit(baseUrl: String): Retrofit {
        val client = OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .writeTimeout(15, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)
            .build()

        val gson = GsonBuilder().create()

        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
    }

    private fun normalizeBaseUrl(url: String): String {
        return if (url.endsWith("/")) url else "$url/"
    }
}