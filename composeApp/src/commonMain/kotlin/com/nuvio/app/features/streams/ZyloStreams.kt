package com.nuvio.app.features.streams

import com.nuvio.app.features.addons.httpRequestRaw
import com.nuvio.app.features.details.MetaDetailsRepository
import com.nuvio.app.features.tmdb.TmdbService
import kotlinx.coroutines.withTimeoutOrNull

object ZyloStreams {

    suspend fun resolveDirectStreams(
        type: String,
        videoId: String,
        parentMetaId: String? = null,
        season: Int? = null,
        episode: Int? = null,
    ): AddonStreamGroup {
        val meta = MetaDetailsRepository.peek(type = type, id = parentMetaId ?: videoId.substringBefore(":"))
        val rawId = videoId.removePrefix("tmdb:").removePrefix("movie:").removePrefix("series:").substringBefore(":")
        
        var imdbId = if (rawId.startsWith("tt", ignoreCase = true)) rawId else meta?.imdbId
        var tmdbId = if (rawId.all { it.isDigit() }) rawId else null

        if (tmdbId == null && imdbId != null) {
            tmdbId = TmdbService.ensureTmdbId(imdbId, type)
        }
        if (imdbId == null && tmdbId != null && tmdbId.all { it.isDigit() }) {
            imdbId = tmdbId.toIntOrNull()?.let { TmdbService.tmdbToImdb(it, type) }
        }

        val s = season ?: 1
        val e = episode ?: 1
        val isTv = type.equals("series", ignoreCase = true) || type.equals("tv", ignoreCase = true) || season != null
        val streamType = if (isTv) "series" else "movie"

        // Candidate query IDs to try on direct extractors (PlavioHD / NetMirror / MovieBox)
        val queryIds = mutableListOf<String>()
        if (isTv) {
            if (!imdbId.isNullOrBlank()) queryIds.add("$imdbId:$s:$e")
            if (!tmdbId.isNullOrBlank()) queryIds.add("tmdb:$tmdbId:$s:$e")
            if (rawId.isNotBlank() && rawId != imdbId && rawId != tmdbId) queryIds.add("$rawId:$s:$e")
        } else {
            if (!imdbId.isNullOrBlank()) queryIds.add(imdbId)
            if (!tmdbId.isNullOrBlank()) queryIds.add("tmdb:$tmdbId")
            if (rawId.isNotBlank() && rawId != imdbId && rawId != tmdbId) queryIds.add(rawId)
        }

        var fetchedStreams: List<StreamItem> = emptyList()

        for (qid in queryIds) {
            val url = "https://hd.plavio.site/stream/$streamType/$qid.json"
            val response = withTimeoutOrNull(6500L) {
                runCatching {
                    httpRequestRaw(
                        method = "GET",
                        url = url,
                        headers = mapOf(
                            "Accept" to "application/json",
                            "User-Agent" to "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36",
                        ),
                        body = "",
                    )
                }.getOrNull()
            }
            if (response != null && response.status in 200..299 && response.body.isNotBlank()) {
                val parsed = StreamParser.parse(response.body, "ZYLO Direct", "zylo_direct")
                val validDirect = parsed.filter { it.url != null && it.url.isNotBlank() }
                if (validDirect.isNotEmpty()) {
                    fetchedStreams = validDirect
                    break
                }
            }
        }

        val items = mutableListOf<StreamItem>()

        if (fetchedStreams.isNotEmpty()) {
            fetchedStreams.forEachIndexed { index, stream ->
                val serverNum = index + 1
                val cleanTitle = stream.name?.takeIf { it.isNotBlank() } ?: stream.title ?: "Server $serverNum"
                val qualityDesc = stream.description ?: stream.title ?: "Direct Fast Stream"
                items.add(
                    stream.copy(
                        name = "ZYLO Direct • Server $serverNum",
                        title = cleanTitle,
                        description = qualityDesc,
                        addonName = "ZYLO Direct",
                        addonId = "zylo_direct",
                        streamType = "direct",
                    )
                )
            }
        }
        // An empty resolver result is not a playable stream. Do not invent
        // webpage URLs or advertise an unverified embed as native media.

        return AddonStreamGroup(
            addonName = "ZYLO Direct",
            addonId = "zylo_direct",
            streams = items,
            isLoading = false,
        )
    }
}
