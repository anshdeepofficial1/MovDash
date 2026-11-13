import test from "node:test";
import assert from "node:assert/strict";
import { readFileSync } from "node:fs";

const source = readFileSync(new URL("./metaDetailsScreen.js", import.meta.url), "utf8");
const start = source.indexOf("function extractCast(meta = {}) {");
const end = source.indexOf("\nfunction isBackEvent", start);
assert.ok(start >= 0 && end > start);
const extractCast = new Function("Platform", source.slice(start, end) + "\nreturn extractCast;")({ isBrowser: () => true });

test("sparse addon names gain portraits and all available credited cast", () => {
  const cast = Array.from({ length: 32 }, (_, i) => ({
    id: i + 1, name: "Person " + i, character: "Role " + i, profile_path: "/person-" + i + ".jpg"
  }));
  const result = extractCast({ cast: ["Person 0", "Person 1", "Person 2"], credits: { cast } });
  assert.equal(result.length, 32);
  assert.equal(result[0].photo, "https://image.tmdb.org/t/p/w300/person-0.jpg");
  assert.equal(result[0].tmdbId, 1);
  assert.equal(result[31].character, "Role 31");
});
test("a partial portrait list still receives the other portraits and people", () => {
  const result = extractCast({ castMembers: [{ name: "A", photo: "https://example.test/a.jpg" }],
    credits: { cast: [{ id: 2, name: "A", profile_path: "/a.jpg" }, { id: 3, name: "B", profile_path: "/b.jpg" }] } });
  assert.equal(result.length, 2);
  assert.equal(result[0].tmdbId, 2);
  assert.equal(result[1].photo, "https://image.tmdb.org/t/p/w300/b.jpg");
});
