package com.example.baseapp.model

import com.google.gson.annotations.SerializedName

data class Anime(
    @SerializedName("id") val id: String,
    @SerializedName("title") val title: String,
    @SerializedName("image_url") val imageUrl: String,
    @SerializedName("episode") val currentEpisode: String
)

data class AnimeResponse(
    @SerializedName("status") val status: String,
    @SerializedName("data") val data: List<Anime>
)
