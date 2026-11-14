// Editorial fallback for verified editions not yet indexed by the catalog provider.
// This is discovery metadata only; never synthesize a TMDB ID or a media URL.
const VERIFIED_EDITIONS = [
  {
    title: "Avengers Endgame: Encore",
    aliases: ["avengers endgame encore", "endgame encore", "avengers endgame"],
    year: 2026,
    theatricalRelease: "2026-09-25",
    digitalRelease: "2026-11-13",
    provider: "Disney+",
    source: "https://www.disneyplus.com/explore/articles/avengers-endgame-encore"
  }
];
const normalize = value => String(value || "").toLowerCase().replace(/[^a-z0-9]+/g," ").trim();

export function findVerifiedEditions(query) {
  const normalized = normalize(query);
  if (!normalized) return [];
  return VERIFIED_EDITIONS.filter(entry => entry.aliases.some(alias => normalized === alias || (normalized.startsWith(alias + " ") && /^\d{4}$/.test(normalized.slice(alias.length + 1)))));
}
