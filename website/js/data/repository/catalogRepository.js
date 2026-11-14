import { safeApiCall } from "../../core/network/safeApiCall.js";
import { CatalogApi } from "../remote/api/catalogApi.js";
import { addonRepository } from "./addonRepository.js";
import { getEffectiveTmdbApiKey } from "../local/tmdbSettingsStore.js";
import { fetchTmdbDiscover } from "../../core/tmdb/tmdbDiscover.js";

const ADDON_POSTER_SHAPES = ["poster", "landscape", "square"];

/**
 * The shape an addon declares for a catalog entry, or "" when it declares
 * nothing.
 *
 * Empty is the useful answer for silence: most addons say nothing at all, and
 * a default invented here would be indistinguishable from an addon actually
 * asking for portrait. Whoever reads this needs to tell those apart.
 */
export function normalizeAddonPosterShape(value) {
  const normalized = String(value || "")
    .trim()
    .toLowerCase();
  return ADDON_POSTER_SHAPES.includes(normalized) ? normalized : "";
}

class CatalogRepository {
  constructor() {
    this.catalogCache = new Map();
  }

  async getCatalog({
    addonBaseUrl,
    addonId,
    addonName,
    catalogId,
    catalogName,
    type,
    skip = 0,
    extraArgs = {},
    supportsSkip = true,
    signal = null
  }) {
    const cacheKey = this.buildCacheKey({
      addonId,
      type,
      catalogId,
      skip,
      extraArgs
    });

    const cached = this.catalogCache.get(cacheKey);
    if (cached) {
      return {
        status: "success",
        data: cached
      };
    }

    if (String(addonId || "").startsWith("zylo-")) {
      return safeApiCall(async () => {
        const apiKey = getEffectiveTmdbApiKey();
        if (!apiKey) throw new Error("TMDB API key is not configured");
        const isCartoons = catalogId === "zylo_cartoons";
        const language = catalogId === "zylo_south" ? "te|ta|ml|kn" : catalogId === "zylo_punjabi" ? "pa" : "hi";
        const page = Math.floor(skip / 20) + 1;
        const params = new URLSearchParams({ api_key: apiKey, page: String(page), sort_by: "popularity.desc" });
        if (isCartoons) params.set("with_genres", "16,10751");
        else {
          params.set("with_original_language", language);
          params.set("with_origin_country", "IN");
        }
        const json = await fetchTmdbDiscover({ mediaType: isCartoons ? "tv" : "movie", params, signal });
        const items = (Array.isArray(json?.results) ? json.results : []).map((m) => ({
          id: `tmdb:${m.id}`,
          type: isCartoons ? "series" : "movie",
          name: m.title || m.name || m.original_title || m.original_name || "Untitled",
          poster: m.poster_path ? `https://image.tmdb.org/t/p/w500${m.poster_path}` : null,
          background: m.backdrop_path ? `https://image.tmdb.org/t/p/w1280${m.backdrop_path}` : null,
          landscapePoster: m.backdrop_path ? `https://image.tmdb.org/t/p/w1280${m.backdrop_path}` : null,
          description: m.overview || "",
          releaseInfo: String(m.release_date || m.first_air_date || "").slice(0, 4),
          released: m.release_date || m.first_air_date || "",
          imdbRating: typeof m.vote_average === "number" ? m.vote_average : null,
          rating: typeof m.vote_average === "number" ? m.vote_average : null,
          addonId,
          addonName,
          catalogType: isCartoons ? "series" : type
        }));
        const row = {
          addonId,
          addonName,
          addonBaseUrl: "https://zylo.app",
          catalogId,
          catalogName,
          apiType: type,
          items,
          isLoading: false,
          hasMore: page < json.total_pages,
          currentPage: page,
          supportsSkip: true
        };
        this.catalogCache.set(cacheKey, row);
        return row;
      });
    }

    const url = this.buildCatalogUrl({
      baseUrl: addonBaseUrl,
      type,
      catalogId,
      skip,
      extraArgs
    });

    return safeApiCall(() =>
      CatalogApi.getCatalog(url, signal ? { signal } : {}).then((dto) => {
        const metas = Array.isArray(dto?.metas) ? dto.metas : [];
        const items = metas
          .filter(
            (meta) =>
              meta &&
              typeof meta === "object" &&
              String(meta.id || "").trim() &&
              String(meta.name || "").trim()
          )
          .map((meta) => ({
            ...this.mapMeta(meta),
            addonBaseUrl,
            addonId,
            addonName,
            catalogType: type
          }));

        const row = {
          addonId,
          addonName,
          addonBaseUrl,
          catalogId,
          catalogName,
          apiType: type,
          items,
          isLoading: false,
          hasMore: Boolean(supportsSkip && metas.length > 0),
          currentPage: Math.floor(skip / 100),
          supportsSkip
        };

        this.catalogCache.set(cacheKey, row);
        return row;
      })
    );
  }

  buildCatalogUrl({ baseUrl, type, catalogId, skip = 0, extraArgs = {} }) {
    const cleanBaseUrl = addonRepository.canonicalizeUrl(baseUrl);
    const queryStart = cleanBaseUrl.indexOf("?");
    const basePath =
      queryStart >= 0 ? cleanBaseUrl.slice(0, queryStart).replace(/\/+$/, "") : cleanBaseUrl;
    const baseQuery = queryStart >= 0 ? cleanBaseUrl.slice(queryStart) : "";
    const args = { ...extraArgs };

    if (Object.keys(args).length === 0) {
      return skip > 0
        ? `${basePath}/catalog/${type}/${catalogId}/skip=${skip}.json${baseQuery}`
        : `${basePath}/catalog/${type}/${catalogId}.json${baseQuery}`;
    }

    if (skip > 0 && !Object.prototype.hasOwnProperty.call(args, "skip")) {
      args.skip = String(skip);
    }

    const query = Object.entries(args)
      .map(([key, value]) => `${this.encodeArg(key)}=${this.encodeArg(String(value))}`)
      .join("&");

    return `${basePath}/catalog/${type}/${catalogId}/${query}.json${baseQuery}`;
  }

  buildCacheKey({ addonId, type, catalogId, skip = 0, extraArgs = {} }) {
    const normalizedArgs = Object.entries(extraArgs)
      .sort(([left], [right]) => left.localeCompare(right))
      .map(([key, value]) => `${key}=${value}`)
      .join("&");

    return `${addonId}_${type}_${catalogId}_${skip}_${normalizedArgs}`;
  }

  encodeArg(value) {
    return encodeURIComponent(value).replace(/\+/g, "%20");
  }

  mapMeta(meta = {}) {
    return {
      id: meta.id || "",
      name: meta.name || "Untitled",
      type: meta.type || "",
      poster: meta.poster || null,
      background: meta.background || null,
      logo: meta.logo || null,
      description: meta.description || "",
      releaseInfo: meta.releaseInfo || "",
      genres: Array.isArray(meta.genres) ? meta.genres : [],
      // The shape an addon says its artwork is. It is part of the meta object
      // every catalog returns, and this mapper used to drop it along with
      // everything else it did not name -- so an addon could ask for landscape
      // artwork all it liked and nothing downstream could ever have known.
      posterShape: normalizeAddonPosterShape(meta.posterShape)
    };
  }
}

export const catalogRepository = new CatalogRepository();
