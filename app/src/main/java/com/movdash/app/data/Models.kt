package com.movdash.app.data

import com.google.gson.annotations.SerializedName

data class Movie(
    val id: Int,
    val title: String,
    val overview: String = "",
    @SerializedName("poster_path") val posterPath: String? = null,
    @SerializedName("backdrop_path") val backdropPath: String? = null,
    @SerializedName("release_date") val releaseDate: String? = null,
    @SerializedName("vote_average") val voteAverage: Double? = null,
    val source: String = "tmdb"
)

data class SourceInfo(
    val id: String,
    val name: String,
    val configured: Boolean = false,
    val type: String = "",
    val note: String? = null
)

data class HealthStatus(val status: String, val app: String? = null)
