// TMDB discover supports a single original-language code per request.
export async function fetchTmdbDiscover({ mediaType = "movie", params, signal, fetchImpl = globalThis.fetch }) {
  const base = new URLSearchParams(params);
  const languages = [...new Set(String(base.get("with_original_language") || "").split(/[|,]/).map(value => value.trim()).filter(Boolean))];
  const page = Math.max(1, Number(base.get("page") || 1));
  const requests = languages.length ? languages : [null];
  const payloads = await Promise.all(requests.map(async language => {
    const query = new URLSearchParams(base);
    if (language) query.set("with_original_language", language);
    const response = await fetchImpl(`https://api.themoviedb.org/3/discover/${mediaType}?${query}`, { signal });
    const body = await response.json().catch(() => null);
    if (!response.ok) {
      throw new Error(`TMDB discover failed (HTTP ${response.status})`);
    }
    if (!Array.isArray(body?.results)) throw new Error("TMDB discover returned an invalid response");
    return body;
  }));
  const byId = new Map();
  for (const payload of payloads) {
    for (const item of payload.results) {
      if (item?.id != null && !byId.has(item.id)) byId.set(item.id, item);
    }
  }
  const results = [...byId.values()];
  if (requests.length > 1 && base.get("sort_by") === "popularity.desc") {
    results.sort((a,b) => Number(b.popularity || 0) - Number(a.popularity || 0));
  }
  return { results, page, total_pages: Math.max(0, ...payloads.map(p => Number(p.total_pages || 0))) };
}
