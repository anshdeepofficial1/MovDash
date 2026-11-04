package com.movdash.app.data

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

interface MovDashApi {
    @GET("health")
    suspend fun health(): HealthStatus

    @GET("api/v1/config/status")
    suspend fun configStatus(): Map<String, Any?>

    @GET("api/v1/catalog/home")
    suspend fun home(): HomeCatalog

    @GET("api/v1/catalog/trending")
    suspend fun trending(
        @Query("media_type") mediaType: String = "movie",
        @Query("time_window") timeWindow: String = "week"
    ): List<Movie>

    @GET("api/v1/catalog/popular/{mediaType}")
    suspend fun popular(@Path("mediaType") mediaType: String): List<Movie>

    @GET("api/v1/catalog/top-rated/{mediaType}")
    suspend fun topRated(@Path("mediaType") mediaType: String): List<Movie>

    @GET("api/v1/catalog/upcoming/{mediaType}")
    suspend fun upcoming(@Path("mediaType") mediaType: String): List<Movie>

    @GET("api/v1/catalog/genres/{mediaType}")
    suspend fun genres(@Path("mediaType") mediaType: String): List<MediaGenre>

    @GET("api/v1/catalog/discover/{mediaType}")
    suspend fun discover(
        @Path("mediaType") mediaType: String,
        @Query("page") page: Int = 1,
        @Query("sort_by") sortBy: String = "popularity.desc",
        @Query("with_genres") genres: String? = null,
        @Query("year") year: Int? = null
    ): List<Movie>

    @GET("api/v1/catalog/search")
    suspend fun search(
        @Query("q") query: String,
        @Query("media_type") mediaType: String = "multi",
        @Query("page") page: Int = 1
    ): List<Movie>

    @GET("api/v1/catalog/{mediaType}/{itemId}")
    suspend fun details(
        @Path("mediaType") mediaType: String,
        @Path("itemId") itemId: Int
    ): MediaDetails

    @GET("api/v1/catalog/tv/{seriesId}/season/{seasonNumber}")
    suspend fun seasonDetails(
        @Path("seriesId") seriesId: Int,
        @Path("seasonNumber") seasonNumber: Int
    ): SeasonDetails

    @GET("api/v1/stream/servers")
    suspend fun streamServers(
        @Query("tmdb_id") tmdbId: Int,
        @Query("media_type") mediaType: String = "movie",
        @Query("season") season: Int? = null,
        @Query("episode") episode: Int? = null
    ): List<StreamServer>

    @GET("api/v1/sources")
    suspend fun sources(): List<SourceInfo>

    @GET("api/v1/sources/jellyfin/health")
    suspend fun jellyfinHealth(): Map<String, Any?>

    @GET("api/v1/sources/jellyfin/items")
    suspend fun jellyfinItems(
        @Query("q") query: String? = null,
        @Query("media_type") mediaType: String = "all",
        @Query("limit") limit: Int = 100
    ): List<JellyfinItem>

    @GET("api/v1/sources/jellyfin/series/{seriesId}/seasons")
    suspend fun jellyfinSeasons(@Path("seriesId") seriesId: String): List<JellyfinItem>

    @GET("api/v1/sources/jellyfin/series/{seriesId}/episodes")
    suspend fun jellyfinEpisodes(
        @Path("seriesId") seriesId: String,
        @Query("season_id") seasonId: String? = null
    ): List<JellyfinItem>

    @GET("api/v1/sources/jellyfin/playback/{itemId}")
    suspend fun playbackSession(@Path("itemId") itemId: String): PlaybackSession
}

object ApiFactory {
    fun create(baseUrl: String): MovDashApi {
        val normalized = baseUrl.trim().let { if (it.endsWith("/")) it else "$it/" }
        val client = OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(45, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .callTimeout(60, TimeUnit.SECONDS)
            .build()
        return Retrofit.Builder()
            .baseUrl(normalized)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(MovDashApi::class.java)
    }
}
