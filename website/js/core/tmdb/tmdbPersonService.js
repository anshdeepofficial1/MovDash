import {
  getEffectiveTmdbApiKey,
  normalizeTmdbLanguageCode,
  TmdbSettingsStore
} from "../../data/local/tmdbSettingsStore.js";
import { normalizePersonCredits } from "./tmdbPersonCreditUtils.js";

export {
  calculatePersonAge,
  normalizePersonCredits,
  sortPersonCreditsByLatest,
  sortPersonCreditsByPopularity
} from "./tmdbPersonCreditUtils.js";

const TMDB_BASE_URL = "https://api.themoviedb.org/3";
const TMDB_PROFILE_BASE_URL = "https://image.tmdb.org/t/p/w500";

function imageUrl(path, baseUrl) {
  const value = String(path || "").trim();
  if (!value) return "";
  if (/^https?:\/\//i.test(value)) return value;
  return value.startsWith("/") ? `${baseUrl}${value}` : value;
}


export const TmdbPersonService = {
  isAvailable() {
    const settings = TmdbSettingsStore.get();
    return Boolean(settings.enabled !== false && settings.useCredits !== false && getEffectiveTmdbApiKey());
  },

  async searchPersonId(name) {
    const trimmed = String(name || "").trim();
    if (!trimmed || !this.isAvailable()) return null;
    const settings = TmdbSettingsStore.get();
    const apiKey = getEffectiveTmdbApiKey();
    const lang = normalizeTmdbLanguageCode(settings.language);
    const url = `${TMDB_BASE_URL}/search/person?api_key=${encodeURIComponent(apiKey)}&language=${encodeURIComponent(lang)}&query=${encodeURIComponent(trimmed)}`;
    const response = await fetch(url).catch(() => null);
    if (!response || !response.ok) return null;
    const data = await response.json().catch(() => null);
    const first = Array.isArray(data?.results) ? data.results[0] : null;
    return first?.id ? String(first.id) : null;
  },

  async fetchPerson({ personId, name = null, language = null } = {}) {
    let normalizedId = String(personId || "").trim();
    if (!/^\d+$/.test(normalizedId) && name) {
      const searched = await this.searchPersonId(name);
      if (searched) normalizedId = searched;
    }
    if (!/^\d+$/.test(normalizedId) || !this.isAvailable()) return null;
    const settings = TmdbSettingsStore.get();
    const apiKey = getEffectiveTmdbApiKey();
    const lang = normalizeTmdbLanguageCode(language || settings.language);
    const url = `${TMDB_BASE_URL}/person/${encodeURIComponent(normalizedId)}?api_key=${encodeURIComponent(apiKey)}&language=${encodeURIComponent(lang)}&append_to_response=combined_credits`;
    const response = await fetch(url).catch(() => null);
    if (!response || !response.ok) return null;
    const data = await response.json().catch(() => null);
    const credits = normalizePersonCredits(data?.combined_credits);
    return {
      id: String(data?.id || normalizedId),
      name: String(data?.name || name || "").trim(),
      biography: String(data?.biography || "").trim(),
      birthday: String(data?.birthday || "").trim(),
      deathday: String(data?.deathday || "").trim(),
      placeOfBirth: String(data?.place_of_birth || "").trim(),
      knownForDepartment: String(data?.known_for_department || "").trim(),
      profile: imageUrl(data?.profile_path, TMDB_PROFILE_BASE_URL),
      credits
    };
  }
};
