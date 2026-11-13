import test from "node:test";
import assert from "node:assert/strict";
import { browserRouteUrl, readBrowserRoute } from "./browserRouteUrl.js";
function decode(path) { const url = new URL(path, "https://example.test"); return readBrowserRoute(url); }
test("settings and details retain refreshable URLs", () => {
  assert.deepEqual(decode(browserRouteUrl("settingsSection", { section: "about" })), { route: "settingsSection", params: { section: "about" } });
  assert.deepEqual(decode(browserRouteUrl("detail", { itemId: "tt1234", itemType: "movie" })), { route: "detail", params: { itemId: "tt1234", itemType: "movie" } });
});
test("playback URLs and credentials never enter browser address", () => {
  const url = browserRouteUrl("player", { itemId: "tt1234", itemType: "movie", url: "https://example.test/secret", token: "private" });
  assert.equal(url, "/watch?itemId=tt1234&itemType=movie");
  assert.equal(decode(url).route, "detail");
  assert.equal(decode("/detail?itemId=https%3A%2F%2Fexample.test&itemType=movie"), null);
});
test("unknown or authentication routes cannot be restored as authenticated content", () => {
  assert.equal(decode("/sign-in/qr"), null);
  assert.equal(decode("/bogus"), null);
});
