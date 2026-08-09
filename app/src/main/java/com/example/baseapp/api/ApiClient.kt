package com.example.baseapp.api

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object ApiClient {
    // Menggunakan HTTPS (atau HTTP jika diatur di manifest usesCleartextTraffic)
    private const val BASE_URL = "https://api.pailynie.eu.cc/"

    val instance: ApiService by lazy {
        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        retrofit.create(ApiService::class.java)
    }
}
