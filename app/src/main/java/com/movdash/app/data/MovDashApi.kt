package com.movdash.app.data

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

interface MovDashApi {
    @GET("api/v1/catalog/trending")
    suspend fun trending(): List<Movie>

    @GET("api/v1/catalog/search")
    suspend fun search(@Query("q") query: String): List<Movie>

    @GET("api/v1/sources")
    suspend fun sources(): List<SourceInfo>

    @GET("health")
    suspend fun health(): HealthStatus
}

object ApiFactory {
    fun create(baseUrl: String): MovDashApi {
        val normalized = baseUrl.trim().let { if (it.endsWith("/")) it else "$it/" }
        val client = OkHttpClient.Builder()
            .connectTimeout(12, TimeUnit.SECONDS)
            .readTimeout(20, TimeUnit.SECONDS)
            .build()
        return Retrofit.Builder()
            .baseUrl(normalized)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(MovDashApi::class.java)
    }
}
