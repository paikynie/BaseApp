package com.example.baseapp.repository

import com.example.baseapp.api.ApiClient
import com.example.baseapp.model.AnimeResponse
import retrofit2.Response

class AnimeRepository {
    suspend fun getLatestAnime(): Response<AnimeResponse> {
        return ApiClient.instance.getLatestAnime()
    }
}
