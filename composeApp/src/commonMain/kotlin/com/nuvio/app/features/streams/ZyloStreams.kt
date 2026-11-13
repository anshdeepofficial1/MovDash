package com.nuvio.app.features.streams

import com.nuvio.app.features.addons.httpRequestRaw
import com.nuvio.app.features.details.MetaDetailsRepository
import com.nuvio.app.features.tmdb.TmdbService
import kotlinx.coroutines.withTimeoutOrNull

object ZyloStreams {

    data class ServerDefinition(
        val id: String,
        val name: String,
        val titleFormat: String,
        val description: String,
        val referer: String,
        val isMultiLang: Boolean = false,
        val isHindi: Boolean = false,
        val isCloud: Boolean = false,
    )

    val SERVERS: List<ServerDefinition> = listOf(
        ServerDefinition(
            id = "nitro",
            name = "Nitro - [Multi-Lang]",
            titleFormat = "Nitro [Multi-Lang] • 1080p Auto | Multi-Audio",
            description = "Multi-Audio (Hindi • Tamil • Telugu • English) • Ultra Fast CDN",
            referer = "https://nitro.stream/",
            isMultiLang = true,
        ),
        ServerDefinition(
            id = "mahalala",
            name = "Mahalala - [Multi-Lang]",
            titleFormat = "Mahalala [Multi-Lang] • 1080p South & Bollywood Cinema",
            description = "Multi-Audio (Tamil • Telugu • Malayalam • Kannada • Hindi) • Direct",
            referer = "https://mahalala.stream/",
            isMultiLang = true,
        ),
        ServerDefinition(
            id = "multi_bill",
            name = "Multi-bill-[Multi-Lang]",
            titleFormat = "Multi-bill [Multi-Lang] • 1080p Dual Audio Master",
            description = "High-Bandwidth Multi-Language Server",
            referer = "https://multibill.stream/",
            isMultiLang = true,
        ),
        ServerDefinition(
            id = "aws_ply",
            name = "AwsPly-[Multi-Lang]",
            titleFormat = "AwsPly [Multi-Lang] • CloudFront Global CDN",
            description = "Low Latency • Adaptive Bitrate HLS (Multi-Audio)",
            referer = "https://awsply.stream/",
            isMultiLang = true,
        ),
        ServerDefinition(
            id = "cast_vid",
            name = "CastVid",
            titleFormat = "CastVid • 1080p Fast Direct Stream",
            description = "CastVid Global Edge Network",
            referer = "https://castvid.net/",
        ),
        ServerDefinition(
            id = "strem_fx",
            name = "StremFx",
            titleFormat = "StremFx • 1080p High-Speed HLS",
            description = "StreamFX Bufferless Edge Node",
            referer = "https://stremfx.com/",
        ),
        ServerDefinition(
            id = "citadel",
            name = "Citadel",
            titleFormat = "Citadel • 1080p Resilient Multi-Source",
            description = "Citadel Resilient Cluster",
            referer = "https://citadel.stream/",
        ),
        ServerDefinition(
            id = "pure_wave",
            name = "PureWave",
            titleFormat = "PureWave • 1080p Clean Direct Stream",
            description = "PureWave High-Performance Node",
            referer = "https://purewave.to/",
        ),
        ServerDefinition(
            id = "vixo",
            name = "Vixo",
            titleFormat = "Vixo • 1080p / 720p Pro Stream",
            description = "Vixo Multi-Source Fast Cluster",
            referer = "https://vixo.to/",
        ),
        ServerDefinition(
            id = "prvibd",
            name = "Prvibd",
            titleFormat = "Prvibd • 1080p ProEmbed CDN",
            description = "Private High-Speed Embed Server",
            referer = "https://proembed.top/",
        ),
        ServerDefinition(
            id = "ophm",
            name = "Ophm",
            titleFormat = "Ophm • OpenHLS Master Stream",
            description = "Ophim Adaptive Media Network",
            referer = "https://ophim1.com/",
        ),
        ServerDefinition(
            id = "vid_stpm",
            name = "VidStpM",
            titleFormat = "VidStpM • VidStream Link Direct",
            description = "VidLink High-Bandwidth Hub",
            referer = "https://vidlink.pro/",
        ),
        ServerDefinition(
            id = "show_nitro",
            name = "ShowNitro",
            titleFormat = "ShowNitro • 4K / 1080p Cloud Drive",
            description = "Febbox Cloud Cache • Add Febbox cookie in settings for 4K",
            referer = "https://www.febbox.com/",
            isCloud = true,
        ),
        ServerDefinition(
            id = "vid_hindi",
            name = "VidHindi",
            titleFormat = "VidHindi • 1080p Bollywood & Hindi Dubbed [Clean Audio]",
            description = "Hindi Dual Audio • Indian Cinema Dedicated Server",
            referer = "https://vidhindi.stream/",
            isHindi = true,
        ),
        ServerDefinition(
            id = "vid_emd",
            name = "VidEmd",
            titleFormat = "VidEmd • 1080p VidEmbed v1",
            description = "VidEmbed Dedicated Edge Node",
            referer = "https://2embed.cc/",
        ),
        ServerDefinition(
            id = "vid_emt",
            name = "VidEmt",
            titleFormat = "VidEmt • 1080p VidEmbed v2",
            description = "VidEmbed Adaptive Stream Engine",
            referer = "https://www.2embed.stream/",
        ),
        ServerDefinition(
            id = "vid_fk",
            name = "VidFk",
            titleFormat = "VidFk • 1080p VidFlix Direct",
            description = "VidFlix Multi-Bitrate Cluster",
            referer = "https://vidflix.to/",
        ),
        ServerDefinition(
            id = "vid_mv",
            name = "VidMv",
            titleFormat = "VidMv • 1080p AutoEmbed VidMovie",
            description = "AutoEmbed Edge Video Network",
            referer = "https://autoembed.cc/",
        ),
        ServerDefinition(
            id = "vid_pro",
            name = "VidPro",
            titleFormat = "VidPro • 1080p VidSrc Pro HD",
            description = "VidSrc Pro Dedicated Pipeline",
            referer = "https://vidsrc.pro/",
        ),
        ServerDefinition(
            id = "vid_bo",
            name = "VidBo",
            titleFormat = "VidBo • 1080p VidBox Rive Stream",
            description = "VidBox Low-Latency Cloud",
            referer = "https://rive.stream/",
        ),
    )

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
            val response = withTimeoutOrNull(4500L) {
                runCatching {
                    httpRequestRaw(
                        method = "GET",
                        url = url,
                        headers = mapOf(
                            "Accept" to "application/json",
                            "User-Agent" to "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36",
                        ),
                        body = "",
                    )
                }.getOrNull()
            }
            if (response != null && response.status in 200..299 && response.body.isNotBlank()) {
                val parsed = StreamParser.parse(response.body, "ZYLO Servers", "zylo_servers")
                val validDirect = parsed.filter { it.url != null && it.url.isNotBlank() }
                if (validDirect.isNotEmpty()) {
                    fetchedStreams = validDirect
                    break
                }
            }
        }

        val items = mutableListOf<StreamItem>()
        val defaultUserAgent = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36"

        SERVERS.forEachIndexed { index, server ->
            val directStream = fetchedStreams.getOrNull(index)
            val fallbackUrl = buildServerUrl(server, isTv, tmdbId, imdbId, s, e)
            val streamUrl = directStream?.url?.takeIf { it.isNotBlank() } ?: fallbackUrl

            val title = if (directStream != null) {
                "${server.name} • ${directStream.title ?: directStream.name ?: "1080p Fast Direct"}"
            } else {
                server.titleFormat
            }

            val desc = if (directStream?.description?.isNotBlank() == true) {
                "${server.description} • ${directStream.description}"
            } else {
                server.description
            }

            items.add(
                StreamItem(
                    name = server.name,
                    title = title,
                    description = desc,
                    url = streamUrl,
                    externalUrl = fallbackUrl,
                    sourceName = "ZYLO Servers",
                    addonName = "ZYLO Servers",
                    addonId = "zylo_servers",
                    streamType = "direct",
                    behaviorHints = StreamBehaviorHints(
                        bingeGroup = "zylo_servers|${server.id}",
                        proxyHeaders = StreamProxyHeaders(
                            request = mapOf(
                                "User-Agent" to defaultUserAgent,
                                "Referer" to server.referer,
                            )
                        ),
                    ),
                )
            )
        }

        return AddonStreamGroup(
            addonName = "ZYLO Servers",
            addonId = "zylo_servers",
            streams = items,
            isLoading = false,
        )
    }

    private fun buildServerUrl(
        server: ServerDefinition,
        isTv: Boolean,
        tmdbId: String?,
        imdbId: String?,
        season: Int,
        episode: Int,
    ): String {
        val effectiveId = tmdbId ?: imdbId ?: "0"
        return if (isTv) {
            when (server.id) {
                "vid_stpm" -> "https://vidlink.pro/tv/$effectiveId/$season/$episode"
                "vixo" -> "https://vidsrc.xyz/embed/tv?tmdb=$effectiveId&season=$season&episode=$episode"
                "vid_emd" -> if (!imdbId.isNullOrBlank()) "https://www.2embed.cc/embedtv/$imdbId&s=$season&e=$episode" else "https://www.2embed.cc/embedtv/$effectiveId&s=$season&e=$episode"
                "vid_emt" -> "https://www.2embed.stream/embed/tv/$effectiveId/$season/$episode"
                "vid_mv" -> "https://autoembed.cc/embed/tv/$effectiveId/$season/$episode"
                "citadel" -> "https://moviesapi.club/tv/$effectiveId-$season-$episode"
                "vid_fk" -> "https://embed.smashystream.com/playere.php?tmdb=$effectiveId&season=$season&episode=$episode"
                "vid_bo" -> "https://rive.stream/embed?type=series&id=$effectiveId&season=$season&episode=$episode"
                "prvibd" -> "https://multiembed.mov/?video_id=$effectiveId&tmdb=1&s=$season&e=$episode"
                "cast_vid" -> "https://castvid.net/embed/tv/$effectiveId/$season/$episode"
                "pure_wave" -> "https://purewave.to/embed/tv/$effectiveId/$season/$episode"
                "strem_fx" -> "https://stremfx.com/tv/$effectiveId/$season/$episode"
                "ophm" -> "https://ophim1.com/tv/$effectiveId/$season/$episode"
                "vid_pro" -> "https://vidsrc.pro/embed/tv/$effectiveId/$season/$episode"
                "show_nitro" -> "https://febbox.com/embed/tv/$effectiveId/$season/$episode"
                "nitro" -> "https://nitro.stream/embed/tv/$effectiveId/$season/$episode"
                "mahalala" -> "https://mahalala.stream/embed/tv/$effectiveId/$season/$episode"
                "multi_bill" -> "https://multibill.stream/embed/tv/$effectiveId/$season/$episode"
                "aws_ply" -> "https://awsply.stream/embed/tv/$effectiveId/$season/$episode"
                "vid_hindi" -> "https://vidhindi.stream/embed/tv/$effectiveId/$season/$episode"
                else -> "https://vidlink.pro/tv/$effectiveId/$season/$episode"
            }
        } else {
            when (server.id) {
                "vid_stpm" -> "https://vidlink.pro/movie/$effectiveId"
                "vixo" -> "https://vidsrc.xyz/embed/movie?tmdb=$effectiveId"
                "vid_emd" -> if (!imdbId.isNullOrBlank()) "https://www.2embed.cc/embed/$imdbId" else "https://www.2embed.cc/embed/$effectiveId"
                "vid_emt" -> "https://www.2embed.stream/embed/movie/$effectiveId"
                "vid_mv" -> "https://autoembed.cc/embed/movie/$effectiveId"
                "citadel" -> "https://moviesapi.club/movie/$effectiveId"
                "vid_fk" -> "https://embed.smashystream.com/playere.php?tmdb=$effectiveId"
                "vid_bo" -> "https://rive.stream/embed?type=movie&id=$effectiveId"
                "prvibd" -> "https://multiembed.mov/?video_id=$effectiveId&tmdb=1"
                "cast_vid" -> "https://castvid.net/embed/movie/$effectiveId"
                "pure_wave" -> "https://purewave.to/embed/movie/$effectiveId"
                "strem_fx" -> "https://stremfx.com/movie/$effectiveId"
                "ophm" -> "https://ophim1.com/movie/$effectiveId"
                "vid_pro" -> "https://vidsrc.pro/embed/movie/$effectiveId"
                "show_nitro" -> "https://febbox.com/embed/movie/$effectiveId"
                "nitro" -> "https://nitro.stream/embed/movie/$effectiveId"
                "mahalala" -> "https://mahalala.stream/embed/movie/$effectiveId"
                "multi_bill" -> "https://multibill.stream/embed/movie/$effectiveId"
                "aws_ply" -> "https://awsply.stream/embed/movie/$effectiveId"
                "vid_hindi" -> "https://vidhindi.stream/embed/movie/$effectiveId"
                else -> "https://vidlink.pro/movie/$effectiveId"
            }
        }
    }
}
