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
    @SerializedName("media_type") val mediaType: String = "movie",
    val source: String = "tmdb"
)

data class HomeCatalog(
    @SerializedName("trending_movies") val trendingMovies: List<Movie> = emptyList(),
    @SerializedName("trending_series") val trendingSeries: List<Movie> = emptyList(),
    @SerializedName("popular_movies") val popularMovies: List<Movie> = emptyList(),
    @SerializedName("popular_series") val popularSeries: List<Movie> = emptyList()
)

data class MediaGenre(val id: Int, val name: String)

data class MediaDetails(
    val id: Int,
    val title: String,
    val overview: String = "",
    @SerializedName("poster_path") val posterPath: String? = null,
    @SerializedName("backdrop_path") val backdropPath: String? = null,
    @SerializedName("release_date") val releaseDate: String? = null,
    @SerializedName("vote_average") val voteAverage: Double? = null,
    @SerializedName("media_type") val mediaType: String = "movie",
    val tagline: String = "",
    val runtime: Int? = null,
    val genres: List<MediaGenre> = emptyList(),
    val status: String? = null,
    @SerializedName("number_of_seasons") val numberOfSeasons: Int? = null,
    @SerializedName("number_of_episodes") val numberOfEpisodes: Int? = null,
    val seasons: List<SeasonSummary> = emptyList(),
    @SerializedName("watch_providers") val watchProviders: Map<String, WatchProviderRegion> = emptyMap()
)

data class SeasonSummary(
    val id: Int,
    val name: String,
    @SerializedName("season_number") val seasonNumber: Int,
    @SerializedName("episode_count") val episodeCount: Int = 0,
    @SerializedName("air_date") val airDate: String? = null
)

data class EpisodeSummary(
    val id: Int,
    @SerializedName("episode_number") val episodeNumber: Int,
    val name: String,
    val overview: String = "",
    @SerializedName("still_path") val stillPath: String? = null,
    val runtime: Int? = null,
    @SerializedName("air_date") val airDate: String? = null
)

data class SeasonDetails(
    val id: Int?,
    val name: String?,
    @SerializedName("season_number") val seasonNumber: Int?,
    val episodes: List<EpisodeSummary> = emptyList()
)

data class JellyfinItem(
    val id: String,
    val title: String,
    val year: Int? = null,
    val overview: String = "",
    @SerializedName("media_type") val mediaType: String = "Movie",
    @SerializedName("image_tag") val imageTag: String? = null,
    @SerializedName("series_id") val seriesId: String? = null
)

data class PlaybackSession(
    val provider: String,
    @SerializedName("item_id") val itemId: String,
    @SerializedName("media_url") val mediaUrl: String,
    @SerializedName("media_type") val mediaType: String,
    @SerializedName("supports_direct_play") val supportsDirectPlay: Boolean = true,
    @SerializedName("audio_tracks") val audioTracks: List<Map<String, Any?>> = emptyList(),
    @SerializedName("subtitle_tracks") val subtitleTracks: List<Map<String, Any?>> = emptyList()
)

data class StreamServer(
    val id: String,
    val name: String,
    val badge: String = "HD",
    val quality: String = "1080p",
    val language: String = "Multi-Audio",
    @SerializedName("stream_type") val streamType: String = "embed",
    @SerializedName("stream_url") val streamUrl: String = ""
)

fun isUnreleasedTitle(releaseDate: String?, status: String? = null, title: String? = null): Boolean {
    val cleanStatus = status?.lowercase() ?: ""
    if (cleanStatus in listOf("in production", "post production", "planned", "announced", "upcoming")) {
        return true
    }
    val cleanTitle = title?.lowercase() ?: ""
    if (cleanTitle.contains("doomsday") || cleanTitle.contains("secret wars") || cleanTitle.contains("vision quest")) {
        return true
    }
    if (releaseDate.isNullOrBlank()) return false
    val currentDate = "2026-11-04"
    return releaseDate.trim() > currentDate
}

object StreamServerFactory {
    fun getDefaultStreamUrl(
        tmdbId: Int,
        mediaType: String = "movie",
        season: Int? = null,
        episode: Int? = null
    ): String {
        val s = if (season != null && season > 0) season else 1
        val e = if (episode != null && episode > 0) episode else 1
        return if (mediaType == "tv") {
            "https://vidsrc.to/embed/tv/$tmdbId/$s/$e"
        } else {
            "https://vidsrc.to/embed/movie/$tmdbId"
        }
    }

    fun getServers(
        tmdbId: Int,
        mediaType: String = "movie",
        season: Int? = null,
        episode: Int? = null
    ): List<StreamServer> {
        val s = if (season != null && season > 0) season else 1
        val e = if (episode != null && episode > 0) episode else 1

        return if (mediaType == "tv") {
            listOf(
                StreamServer(
                    id = "river",
                    name = "Stream 1 (Fast CDN)",
                    badge = "Ultra HD",
                    quality = "1080p / 720p",
                    language = "Multi-Audio / Dual",
                    streamType = "embed",
                    streamUrl = "https://vidsrc.to/embed/tv/$tmdbId/$s/$e"
                ),
                StreamServer(
                    id = "vidlnx",
                    name = "Stream 2 (Full HD)",
                    badge = "1080p",
                    quality = "1080p Ultra",
                    language = "Original / Dual",
                    streamType = "embed",
                    streamUrl = "https://vidsrc.me/embed/tv?tmdb=$tmdbId&season=$s&episode=$e"
                ),
                StreamServer(
                    id = "mbply",
                    name = "Stream 3 (Hindi / Multi)",
                    badge = "Multi-Lang",
                    quality = "Adaptive",
                    language = "Hindi / Eng / Multi",
                    streamType = "embed",
                    streamUrl = "https://autoembed.cc/embed/player.php?id=$tmdbId&s=$s&e=$e"
                ),
                StreamServer(
                    id = "catflix",
                    name = "Stream 4 (Adaptive)",
                    badge = "HD",
                    quality = "HD / SD",
                    language = "Multi-Language",
                    streamType = "embed",
                    streamUrl = "https://multiembed.mov/?video_id=$tmdbId&tmdb=1&s=$s&e=$e"
                )
            )
        } else {
            listOf(
                StreamServer(
                    id = "river",
                    name = "Stream 1 (Fast CDN)",
                    badge = "Ultra HD",
                    quality = "1080p / 720p",
                    language = "Multi-Audio / Dual",
                    streamType = "embed",
                    streamUrl = "https://vidsrc.to/embed/movie/$tmdbId"
                ),
                StreamServer(
                    id = "vidlnx",
                    name = "Stream 2 (Full HD)",
                    badge = "1080p",
                    quality = "1080p Ultra",
                    language = "Original / Dual",
                    streamType = "embed",
                    streamUrl = "https://vidsrc.me/embed/movie?tmdb=$tmdbId"
                ),
                StreamServer(
                    id = "mbply",
                    name = "Stream 3 (Hindi / Multi)",
                    badge = "Multi-Lang",
                    quality = "Adaptive",
                    language = "Hindi / Eng / Multi",
                    streamType = "embed",
                    streamUrl = "https://autoembed.cc/embed/player.php?id=$tmdbId"
                ),
                StreamServer(
                    id = "catflix",
                    name = "Stream 4 (Adaptive)",
                    badge = "HD",
                    quality = "HD / SD",
                    language = "Multi-Language",
                    streamType = "embed",
                    streamUrl = "https://multiembed.mov/?video_id=$tmdbId&tmdb=1"
                )
            )
        }
    }
}

data class SourceInfo(
    val id: String,
    val name: String,
    val configured: Boolean = false,
    val type: String = "",
    val note: String? = null
)

data class HealthStatus(val status: String, val app: String? = null)

data class WatchProviderRegion(
    val link: String? = null,
    @SerializedName("flatrate") val streaming: List<WatchProvider> = emptyList(),
    val free: List<WatchProvider> = emptyList(),
    val ads: List<WatchProvider> = emptyList(),
    val rent: List<WatchProvider> = emptyList(),
    val buy: List<WatchProvider> = emptyList()
)

data class WatchProvider(
    @SerializedName("provider_id") val providerId: Int? = null,
    @SerializedName("provider_name") val providerName: String = "Streaming provider",
    @SerializedName("logo_path") val logoPath: String? = null,
    @SerializedName("display_priority") val displayPriority: Int? = null
)
