package com.example.baseapp.api

import com.example.baseapp.model.AnimeResponse
import com.google.gson.JsonElement
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface ApiService {
    @GET("anime/home")
    suspend fun getHome(): Response<JsonElement>

    @GET("anime/latest")
    suspend fun getLatestAnime(@Query("page") page: Int = 1): Response<AnimeResponse>

    @GET("anime/ongoing")
    suspend fun getOngoingAnime(@Query("page") page: Int = 1): Response<AnimeResponse>

    @GET("anime/completed")
    suspend fun getCompletedAnime(@Query("page") page: Int = 1): Response<AnimeResponse>

    @GET("anime/movie")
    suspend fun getMovieAnime(@Query("page") page: Int = 1): Response<AnimeResponse>

    @GET("anime/search")
    suspend fun searchAnime(@Query("query") query: String, @Query("page") page: Int = 1): Response<AnimeResponse>

    @GET("anime/detail")
    suspend fun getAnimeDetail(@Query("slug") slug: String): Response<JsonElement>

    @GET("anime/streaming")
    suspend fun getStreamingVideo(@Query("slug") slug: String): Response<JsonElement>
}
