package com.example.baseapp.api

import com.example.baseapp.model.AnimeResponse
import retrofit2.Response
import retrofit2.http.GET

interface ApiService {
    // TODO: Ganti "anime/latest" dengan endpoint asli Anda nanti
    @GET("anime/latest")
    suspend fun getLatestAnime(): Response<AnimeResponse>
}
