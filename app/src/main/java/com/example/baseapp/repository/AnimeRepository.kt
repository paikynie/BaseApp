package com.example.baseapp.repository

import com.example.baseapp.api.ApiClient

class AnimeRepository {
    private val api = ApiClient.instance

    suspend fun getHome() = api.getHome()
    suspend fun getLatestAnime(page: Int = 1) = api.getLatestAnime(page)
    suspend fun getOngoingAnime(page: Int = 1) = api.getOngoingAnime(page)
    suspend fun getCompletedAnime(page: Int = 1) = api.getCompletedAnime(page)
    suspend fun getMovieAnime(page: Int = 1) = api.getMovieAnime(page)
    suspend fun searchAnime(query: String, page: Int = 1) = api.searchAnime(query, page)
    suspend fun getAnimeDetail(slug: String) = api.getAnimeDetail(slug)
    suspend fun getStreamingVideo(slug: String) = api.getStreamingVideo(slug)
}
