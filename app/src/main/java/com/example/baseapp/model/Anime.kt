package com.example.baseapp.model

import com.google.gson.annotations.SerializedName

data class Anime(
    @SerializedName("title") val title: String?,
    @SerializedName("slug") val slug: String?,
    @SerializedName("image") val imageUrl: String?,
    @SerializedName("episode") val currentEpisode: String?
)

data class AnimeResult(
    @SerializedName("page") val page: Int?,
    @SerializedName("episodes") val episodes: List<Anime>?
)

data class AnimeResponse(
    @SerializedName("result") val result: AnimeResult?
)\n