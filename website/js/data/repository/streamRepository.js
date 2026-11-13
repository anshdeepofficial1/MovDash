import { secureMediaUrl } from "../../core/streams/secureMediaUrl.js";
import { safeApiCall } from "../../core/network/safeApiCall.js";
import { addonRepository } from "./addonRepository.js";
import { StreamApi } from "../remote/api/streamApi.js";
import { MetaApi } from "../remote/api/metaApi.js";
import { PluginManager } from "../../core/player/pluginManager.js";
import {
  cleanLocalPluginVideoId,
  isLocalPluginVideoId,
  resolvePluginSeasonEpisode
} from "../../core/player/pluginModels.js";
import { PLUGIN_LIMITS } from "../../core/player/pluginWorkerRuntime.js";
import { TmdbService } from "../../core/tmdb/tmdbService.js";
import { LocalDebridAvailabilityService } from "../../core/debrid/localDebridAvailabilityService.js";
import { DebridStreamPresentation } from "../../core/debrid/directDebridStreamPresentation.js";

const ZYLO_SERVERS = [
  {
    id: "nitro",
    name: "Nitro - [Multi-Lang]",
    titleFormat: "Nitro [Multi-Lang] • 1080p Auto | Multi-Audio",
    description: "Multi-Audio (Hindi • Tamil • Telugu • English) • Ultra Fast CDN",
    referer: "https://nitro.stream/",
    isMultiLang: true
  },
  {
    id: "mahalala",
    name: "Mahalala - [Multi-Lang]",
    titleFormat: "Mahalala [Multi-Lang] • 1080p South & Bollywood Cinema",
    description: "Multi-Audio (Tamil • Telugu • Malayalam • Kannada • Hindi) • Direct",
    referer: "https://mahalala.stream/",
    isMultiLang: true
  },
  {
    id: "multi_bill",
    name: "Multi-bill-[Multi-Lang]",
    titleFormat: "Multi-bill [Multi-Lang] • 1080p Dual Audio Master",
    description: "High-Bandwidth Multi-Language Server",
    referer: "https://multibill.stream/",
    isMultiLang: true
  },
  {
    id: "aws_ply",
    name: "AwsPly-[Multi-Lang]",
    titleFormat: "AwsPly [Multi-Lang] • CloudFront Global CDN",
    description: "Low Latency • Adaptive Bitrate HLS (Multi-Audio)",
    referer: "https://awsply.stream/",
    isMultiLang: true
  },
  {
    id: "cast_vid",
    name: "CastVid",
    titleFormat: "CastVid • 1080p Fast Direct Stream",
    description: "CastVid Global Edge Network",
    referer: "https://castvid.net/"
  },
  {
    id: "strem_fx",
    name: "StremFx",
    titleFormat: "StremFx • 1080p High-Speed HLS",
    description: "StreamFX Bufferless Edge Node",
    referer: "https://stremfx.com/"
  },
  {
    id: "citadel",
    name: "Citadel",
    titleFormat: "Citadel • 1080p Resilient Multi-Source",
    description: "Citadel Resilient Cluster",
    referer: "https://citadel.stream/"
  },
  {
    id: "pure_wave",
    name: "PureWave",
    titleFormat: "PureWave • 1080p Clean Direct Stream",
    description: "PureWave High-Performance Node",
    referer: "https://purewave.to/"
  },
  {
    id: "vixo",
    name: "Vixo",
    titleFormat: "Vixo • 1080p / 720p Pro Stream",
    description: "Vixo Multi-Source Fast Cluster",
    referer: "https://vixo.to/"
  },
  {
    id: "prvibd",
    name: "Prvibd",
    titleFormat: "Prvibd • 1080p ProEmbed CDN",
    description: "Private High-Speed Embed Server",
    referer: "https://proembed.top/"
  },
  {
    id: "ophm",
    name: "Ophm",
    titleFormat: "Ophm • OpenHLS Master Stream",
    description: "Ophim Adaptive Media Network",
    referer: "https://ophim1.com/"
  },
  {
    id: "vid_stpm",
    name: "VidStpM",
    titleFormat: "VidStpM • VidStream Link Direct",
    description: "VidLink High-Bandwidth Hub",
    referer: "https://vidlink.pro/"
  },
  {
    id: "show_nitro",
    name: "ShowNitro",
    titleFormat: "ShowNitro • 4K / 1080p Cloud Drive",
    description: "Febbox Cloud Cache • Add Febbox cookie in settings for 4K",
    referer: "https://www.febbox.com/",
    isCloud: true
  },
  {
    id: "vid_hindi",
    name: "VidHindi",
    titleFormat: "VidHindi • 1080p Bollywood & Hindi Dubbed [Clean Audio]",
    description: "Hindi Dual Audio • Indian Cinema Dedicated Server",
    referer: "https://vidhindi.stream/",
    isHindi: true
  },
  {
    id: "vid_emd",
    name: "VidEmd",
    titleFormat: "VidEmd • 1080p VidEmbed v1",
    description: "VidEmbed Dedicated Edge Node",
    referer: "https://2embed.cc/"
  },
  {
    id: "vid_emt",
    name: "VidEmt",
    titleFormat: "VidEmt • 1080p VidEmbed v2",
    description: "VidEmbed Adaptive Stream Engine",
    referer: "https://www.2embed.stream/"
  },
  {
    id: "vid_fk",
    name: "VidFk",
    titleFormat: "VidFk • 1080p VidFlix Direct",
    description: "VidFlix Multi-Bitrate Cluster",
    referer: "https://vidflix.to/"
  },
  {
    id: "vid_mv",
    name: "VidMv",
    titleFormat: "VidMv • 1080p AutoEmbed VidMovie",
    description: "AutoEmbed Edge Video Network",
    referer: "https://autoembed.cc/"
  },
  {
    id: "vid_pro",
    name: "VidPro",
    titleFormat: "VidPro • 1080p VidSrc Pro HD",
    description: "VidSrc Pro Dedicated Pipeline",
    referer: "https://vidsrc.pro/"
  },
  {
    id: "vid_bo",
    name: "VidBo",
    titleFormat: "VidBo • 1080p VidBox Rive Stream",
    description: "VidBox Low-Latency Cloud",
    referer: "https://rive.stream/"
  }
];

function buildZyloServerUrl(server, isTv, tmdbId, imdbId, season, episode) {
  const effectiveId = tmdbId || imdbId || "0";
  const s = season || 1;
  const e = episode || 1;
  if (isTv) {
    switch (server.id) {
      case "vid_stpm": return `https://vidlink.pro/tv/${effectiveId}/${s}/${e}`;
      case "vixo": return `https://vidsrc.xyz/embed/tv?tmdb=${effectiveId}&season=${s}&episode=${e}`;
      case "vid_emd": return imdbId ? `https://www.2embed.cc/embedtv/${imdbId}&s=${s}&e=${e}` : `https://www.2embed.cc/embedtv/${effectiveId}&s=${s}&e=${e}`;
      case "vid_emt": return `https://www.2embed.stream/embed/tv/${effectiveId}/${s}/${e}`;
      case "vid_mv": return `https://autoembed.cc/embed/tv/${effectiveId}/${s}/${e}`;
      case "citadel": return `https://moviesapi.club/tv/${effectiveId}-${s}-${e}`;
      case "vid_fk": return `https://embed.smashystream.com/playere.php?tmdb=${effectiveId}&season=${s}&episode=${e}`;
      case "vid_bo": return `https://rive.stream/embed?type=series&id=${effectiveId}&season=${s}&episode=${e}`;
      case "prvibd": return `https://multiembed.mov/?video_id=${effectiveId}&tmdb=1&s=${s}&e=${e}`;
      case "cast_vid": return `https://castvid.net/embed/tv/${effectiveId}/${s}/${e}`;
      case "pure_wave": return `https://purewave.to/embed/tv/${effectiveId}/${s}/${e}`;
      case "strem_fx": return `https://stremfx.com/tv/${effectiveId}/${s}/${e}`;
      case "ophm": return `https://ophim1.com/tv/${effectiveId}/${s}/${e}`;
      case "vid_pro": return `https://vidsrc.pro/embed/tv/${effectiveId}/${s}/${e}`;
      case "show_nitro": return `https://febbox.com/embed/tv/${effectiveId}/${s}/${e}`;
      case "nitro": return `https://nitro.stream/embed/tv/${effectiveId}/${s}/${e}`;
      case "mahalala": return `https://mahalala.stream/embed/tv/${effectiveId}/${s}/${e}`;
      case "multi_bill": return `https://multibill.stream/embed/tv/${effectiveId}/${s}/${e}`;
      case "aws_ply": return `https://awsply.stream/embed/tv/${effectiveId}/${s}/${e}`;
      case "vid_hindi": return `https://vidhindi.stream/embed/tv/${effectiveId}/${s}/${e}`;
      default: return `https://vidlink.pro/tv/${effectiveId}/${s}/${e}`;
    }
  } else {
    switch (server.id) {
      case "vid_stpm": return `https://vidlink.pro/movie/${effectiveId}`;
      case "vixo": return `https://vidsrc.xyz/embed/movie?tmdb=${effectiveId}`;
      case "vid_emd": return imdbId ? `https://www.2embed.cc/embed/${imdbId}` : `https://www.2embed.cc/embed/${effectiveId}`;
      case "vid_emt": return `https://www.2embed.stream/embed/movie/${effectiveId}`;
      case "vid_mv": return `https://autoembed.cc/embed/movie/${effectiveId}`;
      case "citadel": return `https://moviesapi.club/movie/${effectiveId}`;
      case "vid_fk": return `https://embed.smashystream.com/playere.php?tmdb=${effectiveId}`;
      case "vid_bo": return `https://rive.stream/embed?type=movie&id=${effectiveId}`;
      case "prvibd": return `https://multiembed.mov/?video_id=${effectiveId}&tmdb=1`;
      case "cast_vid": return `https://castvid.net/embed/movie/${effectiveId}`;
      case "pure_wave": return `https://purewave.to/embed/movie/${effectiveId}`;
      case "strem_fx": return `https://stremfx.com/movie/${effectiveId}`;
      case "ophm": return `https://ophim1.com/movie/${effectiveId}`;
      case "vid_pro": return `https://vidsrc.pro/embed/movie/${effectiveId}`;
      case "show_nitro": return `https://febbox.com/embed/movie/${effectiveId}`;
      case "nitro": return `https://nitro.stream/embed/movie/${effectiveId}`;
      case "mahalala": return `https://mahalala.stream/embed/movie/${effectiveId}`;
      case "multi_bill": return `https://multibill.stream/embed/movie/${effectiveId}`;
      case "aws_ply": return `https://awsply.stream/embed/movie/${effectiveId}`;
      case "vid_hindi": return `https://vidhindi.stream/embed/movie/${effectiveId}`;
      default: return `https://vidlink.pro/movie/${effectiveId}`;
    }
  }
}

class StreamRepository {
  async getStreamsFromAddon(baseUrl, type, videoId) {
    const url = this.buildStreamUrl(baseUrl, type, videoId);
    const result = await safeApiCall(() => StreamApi.getStreams(url));
    if (result.status !== "success") {
      return result;
    }

    const streams = (result.data?.streams || []).map((stream) => this.mapStream(stream));
    return { status: "success", data: streams };
  }

  async getStreamsFromAllAddons(type, videoId, options = {}) {
    const installedAddons = (await addonRepository.getInstalledAddons()).map((addon, index) => ({
      ...addon,
      orderIndex: index
    }));
    const onAddon = typeof options?.onAddon === "function" ? options.onAddon : null;

    const onChunk = typeof options?.onChunk === "function" ? options.onChunk : null;

    const notifyChunk = (group) => {
      if (!onChunk || !group?.streams?.length) {
        return;
      }
      try {
        onChunk({
          status: "success",
          data: [group]
        });
      } catch (error) {
        console.warn("Stream chunk callback failed", error);
      }
    };

    const notifyAddon = (addon, orderIndex) => {
      if (!onAddon || !addon) {
        return;
      }
      try {
        onAddon({ ...addon, orderIndex });
      } catch (error) {
        console.warn("Stream addon callback failed", error);
      }
    };

    const prepareDebridGroup = async (group) => {
      const checkingGroup =
        DebridStreamPresentation.apply(LocalDebridAvailabilityService.markChecking([group]))[0] ||
        group;
      const checkedGroup =
        (await LocalDebridAvailabilityService.annotateCachedAvailability([checkingGroup]))[0] ||
        checkingGroup;
      const presentedGroup = DebridStreamPresentation.apply([checkedGroup])[0] || checkedGroup;
      notifyChunk(presentedGroup);
      return presentedGroup;
    };

    const sourceAddons = options.preferredPluginOnly ? [] : installedAddons;
    const addonTasks = sourceAddons.map(async (addon) => {
      try {
        // Secondary/aggregator catalogs sometimes expose a channel row while
        // the original addon owns the ID under `tv`. Exact type remains the
        // default; an explicit, unambiguous idPrefix may recover the owner type.
        const streamRequestType = addonRepository.resolveResourceRequestType(
          addon,
          "stream",
          type,
          videoId,
          { allowIdTypeFallback: true }
        );
        const metaRequestType = addonRepository.resolveResourceRequestType(
          addon,
          "meta",
          type,
          videoId,
          { allowIdTypeFallback: true }
        );
        const canStream = Boolean(streamRequestType);
        const canMeta = Boolean(metaRequestType);
        // Meta-only stream discovery is a compatibility path for debrid cloud
        // items, which are exposed through the `other` type. Regular movie/series
        // metadata addons must not be queried as stream sources.
        const canTryMetaOnlyStreams =
          canMeta &&
          String(type || "")
            .trim()
            .toLowerCase() === "other";
        if (!canStream && !canTryMetaOnlyStreams) {
          return null;
        }
        const orderIndex = Number(addon.orderIndex ?? Number.MAX_SAFE_INTEGER);
        notifyAddon(addon, orderIndex);
        let addonStreams = [];
        let streamRequestSucceeded = false;
        if (canStream) {
          const streamsResult = await this.getStreamsFromAddon(
            addon.baseUrl,
            streamRequestType,
            videoId
          );
          if (streamsResult.status === "success") {
            streamRequestSucceeded = true;
            if (streamsResult.data.length) {
              addonStreams = streamsResult.data;
            }
          }
        }
        // Match Android: when a declared stream endpoint succeeds with no
        // results, try the matching meta video's inline streams even if the
        // manifest omitted its meta resource. Keep the existing meta-only
        // compatibility path for debrid cloud `other` catalogs as well.
        if (
          addonStreams.length === 0 &&
          ((canStream && streamRequestSucceeded) || canTryMetaOnlyStreams)
        ) {
          addonStreams = await this.fetchInlineStreamsFromMeta(
            addon,
            canStream ? streamRequestType : metaRequestType,
            videoId
          );
        }
        if (addonStreams.length === 0) {
          return null;
        }

        const group = {
          addonId: addon.id,
          addonBaseUrl: addon.baseUrl,
          addonName: addon.displayName,
          addonLogo: addon.logo,
          addonOrderIndex: orderIndex,
          streamOrigin: {
            kind: "addon",
            addonId: addon.id,
            addonBaseUrl: addon.baseUrl,
            addonName: addon.displayName,
            addonOrderIndex: orderIndex
          },
          streams: addonStreams.map((stream) => ({
            ...stream,
            addonId: addon.id,
            addonBaseUrl: addon.baseUrl,
            addonName: addon.displayName,
            addonLogo: addon.logo,
            addonOrderIndex: orderIndex,
            streamOrigin: {
              ...(stream.streamOrigin || {}),
              kind: "addon",
              addonId: addon.id,
              addonBaseUrl: addon.baseUrl,
              addonName: addon.displayName,
              addonOrderIndex: orderIndex
            }
          }))
        };
        return prepareDebridGroup(group);
      } catch (_) {
        return null;
      }
    });

    const pluginTask = (async () => {
      const prepared = [];
      const controller = new AbortController();
      const abort = () => controller.abort();
      options.signal?.addEventListener("abort", abort, { once: true });
      if (options.signal?.aborted) abort();
      const timeout = setTimeout(abort, PLUGIN_LIMITS.globalTimeoutMs);
      try {
        await this.getPluginStreams(type, videoId, {
          ...options,
          signal: controller.signal,
          onGroup: (group) => prepared.push(prepareDebridGroup(group).catch(() => group))
        });
      } catch (error) {
        if (!controller.signal.aborted) console.warn("Plugin stream fetch failed", error);
      } finally {
        clearTimeout(timeout);
        options.signal?.removeEventListener("abort", abort);
      }
      const grouped = new Map();
      for (const group of await Promise.all(prepared)) {
        const previous = grouped.get(group.sourceProviderId);
        grouped.set(
          group.sourceProviderId,
          previous ? { ...previous, streams: [...previous.streams, ...group.streams] } : group
        );
      }
      return [...grouped.values()];
    })();

    const zyloServerTask = (async () => {
      if (options.preferredPluginOnly) return [];
      try {
        const group = await this.getZyloServerStreams(type, videoId, options);
        if (group?.streams?.length) {
          notifyChunk(group);
          return [group];
        }
        return [];
      } catch (_) {
        return [];
      }
    })();

    const [addonsWithStreams, pluginStreams, zyloServerStreams] = await Promise.all([
      Promise.all(addonTasks).then((items) =>
        items.filter(Boolean).sort(
          (left, right) => Number(left.addonOrderIndex || 0) - Number(right.addonOrderIndex || 0)
        )
      ),
      pluginTask,
      zyloServerTask
    ]);

    return {
      status: "success",
      data: [...zyloServerStreams, ...addonsWithStreams, ...pluginStreams]
    };
  }

  async getZyloServerStreams(type, videoId, options = {}) {
    const rawVideoId = String(videoId || "").trim();
    const isTv =
      String(type || "").toLowerCase() === "series" ||
      String(type || "").toLowerCase() === "tv" ||
      Boolean(options?.season != null);

    const parts = rawVideoId.split(":");
    let imdbId = null;
    let tmdbId = null;
    let season = options?.season != null ? Number(options.season) : 1;
    let episode = options?.episode != null ? Number(options.episode) : 1;

    for (const part of parts) {
      if (/^tt\d+$/i.test(part)) imdbId = part;
      else if (/^\d+$/.test(part) && !tmdbId) tmdbId = part;
    }
    if (parts.length >= 3 && !isNaN(parts[1]) && !isNaN(parts[2])) {
      season = Number(parts[1]);
      episode = Number(parts[2]);
    }
    if (options.imdbId && !imdbId) imdbId = options.imdbId;
    if (options.tmdbId && !tmdbId) tmdbId = String(options.tmdbId);

    if (!tmdbId && imdbId) {
      try {
        tmdbId = await TmdbService.ensureTmdbId(imdbId, isTv ? "tv" : "movie", {
          requireEnabled: false,
          signal: options.signal
        });
      } catch (_) {}
    }

    const effectiveId = tmdbId || imdbId || rawVideoId;
    if (!effectiveId) return null;

    const streams = ZYLO_SERVERS.map((server) => {
      const serverUrl = buildZyloServerUrl(server, isTv, tmdbId, imdbId, season, episode);
      return {
        id: `zylo_server_${server.id}_${effectiveId}`,
        name: server.name,
        title: server.titleFormat,
        description: server.description,
        url: serverUrl,
        externalUrl: serverUrl,
        externalFrameUrl: serverUrl,
        quality: "1080p",
        addonId: "zylo-servers",
        addonName: "ZYLO Servers",
        addonBaseUrl: "https://zylo.app",
        addonOrderIndex: -1,
        sourceName: "ZYLO Servers",
        sourceProviderId: server.id,
        streamType: "direct",
        streamOrigin: {
          kind: "server",
          addonId: "zylo-servers",
          addonName: "ZYLO Servers",
          sourceProviderId: server.id
        },
        behaviorHints: {
          notWebReady: false,
          bingeGroup: `zylo-servers|${server.id}`,
          proxyHeaders: {
            request: {
              "User-Agent": "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36",
              "Referer": server.referer
            }
          }
        }
      };
    });

    return {
      addonId: "zylo-servers",
      addonBaseUrl: "https://zylo.app",
      addonName: "ZYLO Servers",
      addonLogo: null,
      addonOrderIndex: -1,
      streamOrigin: {
        kind: "server",
        addonId: "zylo-servers",
        addonName: "ZYLO Servers"
      },
      streams
    };
  }

  async getPluginStreams(type, videoId, options = {}) {
    if (!PluginManager.pluginsEnabled || options.signal?.aborted) return [];
    const rawVideoId = String(videoId || "").trim();
    const localId = isLocalPluginVideoId(rawVideoId);
    const knownTmdbId = [options.tmdbId, options.itemId, rawVideoId]
      .map((id) => /^(?:(?:tmdb|movie|series):)*(\d+)(?::|$)/i.exec(String(id ?? "").trim())?.[1])
      .find(Boolean);
    const tmdbLookupId = localId
      ? rawVideoId
      : String(knownTmdbId || options.imdbId || options.itemId || rawVideoId).trim();
    const tmdbId = await TmdbService.ensureTmdbId(tmdbLookupId, type, {
      requireEnabled: false,
      signal: options.signal
    });
    const pluginId = tmdbId || (localId ? cleanLocalPluginVideoId(rawVideoId) : null);
    if (!pluginId) return [];
    const mapGroup = (result) => ({
      sourceProviderId: result.sourceId || result.sourceName || null,
      addonName: result.sourceName,
      addonLogo: null,
      streamOrigin: {
        kind: "plugin",
        sourceProviderId: result.sourceId || result.sourceName || null,
        addonName: result.sourceName || null
      },
      streams: (result.streams || []).map((stream) => ({
        ...stream,
        sourceProviderId: result.sourceId || result.sourceName || null,
        addonName: result.sourceName,
        addonLogo: null,
        streamOrigin: {
          ...(stream.streamOrigin || {}),
          kind: "plugin",
          sourceProviderId: result.sourceId || result.sourceName || null,
          addonName: result.sourceName || null
        }
      }))
    });
    const pluginResults = await PluginManager.executeScrapersStreaming({
      ...resolvePluginSeasonEpisode(rawVideoId, options.season, options.episode),
      tmdbId: pluginId,
      mediaType: tmdbId && (type === "series" || type === "show") ? "tv" : type,
      signal: options.signal,
      priorityBingeGroup: options.priorityBingeGroup,
      preferredPluginOnly: options.preferredPluginOnly,
      onGroup: options.onGroup ? (group) => options.onGroup(mapGroup(group)) : undefined
    });
    return pluginResults.map(mapGroup);
  }

  buildStreamUrl(baseUrl, type, videoId) {
    const cleanBaseUrl = addonRepository.canonicalizeUrl(baseUrl);
    const queryStart = cleanBaseUrl.indexOf("?");
    const basePath =
      queryStart >= 0 ? cleanBaseUrl.slice(0, queryStart).replace(/\/+$/, "") : cleanBaseUrl;
    const baseQuery = queryStart >= 0 ? cleanBaseUrl.slice(queryStart) : "";
    return `${basePath}/stream/${this.encode(type)}/${this.encode(videoId)}.json${baseQuery}`;
  }

  buildMetaUrl(baseUrl, type, id) {
    const cleanBaseUrl = addonRepository.canonicalizeUrl(baseUrl);
    const queryStart = cleanBaseUrl.indexOf("?");
    const basePath =
      queryStart >= 0 ? cleanBaseUrl.slice(0, queryStart).replace(/\/+$/, "") : cleanBaseUrl;
    const baseQuery = queryStart >= 0 ? cleanBaseUrl.slice(queryStart) : "";
    return `${basePath}/meta/${this.encode(type)}/${this.encode(id)}.json${baseQuery}`;
  }

  encode(value) {
    return encodeURIComponent(String(value || "")).replace(/\+/g, "%20");
  }

  mapStream(stream = {}) {
    const sidecarSubtitles = Array.isArray(stream.subtitles)
      ? stream.subtitles
          .filter((entry) => entry && entry.url)
          .map((entry) => ({
            id: entry.id || null,
            url: entry.url,
            lang: entry.lang || "unknown"
          }))
      : [];

    return {
      name: stream.name || null,
      title: stream.title || null,
      description: stream.description || null,
      url: stream.url ? secureMediaUrl(stream.url) : null,
      ytId: stream.ytId || null,
      infoHash: stream.infoHash || null,
      fileIdx: stream.fileIdx ?? null,
      externalUrl: stream.externalUrl || null,
      behaviorHints: stream.behaviorHints || null,
      sources: Array.isArray(stream.sources) ? stream.sources : [],
      quality: stream.quality || null,
      qualityValue: Number.isFinite(Number(stream.qualityValue)) ? Number(stream.qualityValue) : -1,
      clientResolve: stream.clientResolve || null,
      debridCacheStatus: stream.debridCacheStatus || null,
      subtitles: sidecarSubtitles
    };
  }

  async fetchInlineStreamsFromMeta(addon, type, videoId) {
    const rawVideoId = String(videoId || "").trim();
    if (!addon?.baseUrl || !rawVideoId) {
      return [];
    }

    // Try the content-level id (handles series episode ids like tt123:1:2)
    // and the raw id (handles content whose clicked id is the meta id itself,
    // e.g. debrid cloud "other" items keyed dmm:<torrentId>).
    const contentLevelId = this.buildContentLevelMetaId(rawVideoId);
    const candidateMetaIds = [];
    if (contentLevelId) {
      candidateMetaIds.push(contentLevelId);
    }
    if (rawVideoId && rawVideoId !== contentLevelId) {
      candidateMetaIds.push(rawVideoId);
    }

    for (const metaId of candidateMetaIds) {
      const url = this.buildMetaUrl(addon.baseUrl, type, metaId);
      const result = await safeApiCall(() => MetaApi.getMeta(url));

      if (result.status !== "success") {
        continue;
      }

      const meta = result.data?.meta || null;
      const videos = Array.isArray(meta?.videos) ? meta.videos : [];

      if (!videos.length) {
        continue;
      }

      const matchingVideo =
        videos.find((video) => String(video?.id || "") === rawVideoId) ||
        (type !== "series" && videos.length === 1 ? videos[0] : null);

      const streams = Array.isArray(matchingVideo?.streams) ? matchingVideo.streams : [];

      const mapped = streams
        .map((stream) => this.mapStream(stream))
        .filter(
          (stream) =>
            stream.url ||
            stream.externalUrl ||
            stream.ytId ||
            stream.clientResolve ||
            stream.infoHash
        );

      if (mapped.length) {
        return mapped;
      }
    }

    return [];
  }

  buildContentLevelMetaId(videoId) {
    const raw = String(videoId || "").trim();
    if (!raw) {
      return "";
    }
    const parts = raw.split(":");
    if (parts.length <= 1) {
      return raw;
    }
    let trailingNumericCount = 0;
    for (let index = parts.length - 1; index >= 0; index -= 1) {
      if (!/^\d+$/.test(parts[index])) {
        break;
      }
      trailingNumericCount += 1;
    }
    const firstSegment = parts[0];
    const minSegments = /^tt/i.test(firstSegment) || /^\d+$/.test(firstSegment) ? 1 : 2;
    const segmentsToDrop = Math.min(trailingNumericCount, Math.max(0, parts.length - minSegments));
    return segmentsToDrop > 0 ? parts.slice(0, -segmentsToDrop).join(":") : raw;
  }
}

export const streamRepository = new StreamRepository();
