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
        } else {
            // Fallback embeds if direct scraper was unreachable
            val effectiveId = tmdbId ?: imdbId ?: rawId
            if (isTv) {
                items.add(
                    StreamItem(
                        name = "ZYLO Direct • Server 1",
                        title = "NetMirror Dual Audio (S${s}E${e})",
                        description = "Fast Adaptive • Multi-Audio (Hindi / Dual / Eng)",
                        url = "https://autoembed.cc/embed/player.php?id=$effectiveId&s=$s&e=$e",
                        externalUrl = "https://autoembed.cc/embed/player.php?id=$effectiveId&s=$s&e=$e",
                        addonName = "ZYLO Direct",
                        addonId = "zylo_direct",
                        streamType = "direct",
                    )
                )
                items.add(
                    StreamItem(
                        name = "ZYLO Direct • Server 2",
                        title = "VidLink Ultra HD (S${s}E${e})",
                        description = "1080p Ultra HD • Multi-Subtitles",
                        url = "https://vidlink.pro/tv/$effectiveId/$s/$e",
                        externalUrl = "https://vidlink.pro/tv/$effectiveId/$s/$e",
                        addonName = "ZYLO Direct",
                        addonId = "zylo_direct",
                        streamType = "direct",
                    )
                )
                items.add(
                    StreamItem(
                        name = "ZYLO Direct • Server 3",
                        title = "MovieBox Fast Stream (S${s}E${e})",
                        description = "Fast HLS • Multi-Language / Dual",
                        url = "https://vidsrc.cc/v2/embed/tv/$effectiveId/$s/$e",
                        externalUrl = "https://vidsrc.cc/v2/embed/tv/$effectiveId/$s/$e",
                        addonName = "ZYLO Direct",
                        addonId = "zylo_direct",
                        streamType = "direct",
                    )
                )
                items.add(
                    StreamItem(
                        name = "ZYLO Direct • Server 4",
                        title = "DoFlakes Adaptive (S${s}E${e})",
                        description = "Adaptive HD • Global Multi-Mirror",
                        url = "https://multiembed.mov/?video_id=$effectiveId&tmdb=1&s=$s&e=$e",
                        externalUrl = "https://multiembed.mov/?video_id=$effectiveId&tmdb=1&s=$s&e=$e",
                        addonName = "ZYLO Direct",
                        addonId = "zylo_direct",
                        streamType = "direct",
                    )
                )
                items.add(
                    StreamItem(
                        name = "ZYLO Direct • Server 5",
                        title = "NXSH Direct Stream (S${s}E${e})",
                        description = "Direct 1080p Stream • High Bitrate",
                        url = "https://embed.su/embed/tv/$effectiveId/$s/$e",
                        externalUrl = "https://embed.su/embed/tv/$effectiveId/$s/$e",
                        addonName = "ZYLO Direct",
                        addonId = "zylo_direct",
                        streamType = "direct",
                    )
                )
            } else {
                items.add(
                    StreamItem(
                        name = "ZYLO Direct • Server 1",
                        title = "NetMirror Dual Audio HD",
                        description = "Fast Adaptive • Multi-Audio (Hindi / Dual / Eng)",
                        url = "https://autoembed.cc/embed/player.php?id=$effectiveId",
                        externalUrl = "https://autoembed.cc/embed/player.php?id=$effectiveId",
                        addonName = "ZYLO Direct",
                        addonId = "zylo_direct",
                        streamType = "direct",
                    )
                )
                items.add(
                    StreamItem(
                        name = "ZYLO Direct • Server 2",
                        title = "VidLink Ultra HD",
                        description = "1080p Ultra HD • Multi-Subtitles",
                        url = "https://vidlink.pro/movie/$effectiveId",
                        externalUrl = "https://vidlink.pro/movie/$effectiveId",
                        addonName = "ZYLO Direct",
                        addonId = "zylo_direct",
                        streamType = "direct",
                    )
                )
                items.add(
                    StreamItem(
                        name = "ZYLO Direct • Server 3",
                        title = "MovieBox Fast Stream",
                        description = "Fast HLS • Multi-Language / Dual",
                        url = "https://vidsrc.cc/v2/embed/movie/$effectiveId",
                        externalUrl = "https://vidsrc.cc/v2/embed/movie/$effectiveId",
                        addonName = "ZYLO Direct",
                        addonId = "zylo_direct",
                        streamType = "direct",
                    )
                )
                items.add(
                    StreamItem(
                        name = "ZYLO Direct • Server 4",
                        title = "DoFlakes Adaptive",
                        description = "Adaptive HD • Global Multi-Mirror",
                        url = "https://multiembed.mov/?video_id=$effectiveId&tmdb=1",
                        externalUrl = "https://multiembed.mov/?video_id=$effectiveId&tmdb=1",
                        addonName = "ZYLO Direct",
                        addonId = "zylo_direct",
                        streamType = "direct",
                    )
                )
                items.add(
                    StreamItem(
                        name = "ZYLO Direct • Server 5",
                        title = "NXSH Direct Stream",
                        description = "Direct 1080p High Bitrate",
                        url = "https://embed.su/embed/movie/$effectiveId",
                        externalUrl = "https://embed.su/embed/movie/$effectiveId",
                        addonName = "ZYLO Direct",
                        addonId = "zylo_direct",
                        streamType = "direct",
                    )
                )
            }
        }

        return AddonStreamGroup(
            addonName = "ZYLO Direct",
            addonId = "zylo_direct",
            streams = items,
            isLoading = false,
        )
    }
}
