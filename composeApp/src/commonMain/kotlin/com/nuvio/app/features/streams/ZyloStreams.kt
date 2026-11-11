package com.nuvio.app.features.streams

import com.nuvio.app.features.details.MetaDetailsRepository
import com.nuvio.app.features.tmdb.TmdbService

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

        val effectiveId = tmdbId ?: imdbId ?: rawId
        val s = season ?: 1
        val e = episode ?: 1

        val isTv = type.equals("series", ignoreCase = true) || type.equals("tv", ignoreCase = true) || season != null

        val items = mutableListOf<StreamItem>()

        if (isTv) {
            items.add(
                StreamItem(
                    name = "ZYLO Direct • Server 1",
                    title = "AutoEmbed Multi-Audio (S${s}E${e})",
                    description = "Fast Adaptive • Multi-Language (Hindi / Dual / Eng)",
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
                    description = "1080p Ultra • Original / Subtitles",
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
                    title = "VidSrc CC Stream (S${s}E${e})",
                    description = "Fast HLS • Multi-Audio / Subtitles",
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
                    title = "MultiEmbed Adaptive (S${s}E${e})",
                    description = "Adaptive HD • Global Mirrors",
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
                    title = "Embed.su HD (S${s}E${e})",
                    description = "Direct 1080p Stream",
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
                    title = "AutoEmbed Multi-Audio HD",
                    description = "Fast Adaptive • Multi-Language (Hindi / Dual / Eng)",
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
                    description = "1080p Ultra • Original / Subtitles",
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
                    title = "VidSrc CC Stream",
                    description = "Fast HLS • Multi-Audio / Dual",
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
                    title = "MultiEmbed Adaptive",
                    description = "Adaptive HD / SD • Multi-Language",
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
                    title = "Embed.su Direct",
                    description = "Direct 1080p High Stream",
                    url = "https://embed.su/embed/movie/$effectiveId",
                    externalUrl = "https://embed.su/embed/movie/$effectiveId",
                    addonName = "ZYLO Direct",
                    addonId = "zylo_direct",
                    streamType = "direct",
                )
            )
        }

        return AddonStreamGroup(
            addonName = "ZYLO Direct",
            addonId = "zylo_direct",
            streams = items,
            isLoading = false,
        )
    }
}
