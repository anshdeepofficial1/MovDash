import test from "node:test";
import assert from "node:assert/strict";
import { readFileSync } from "node:fs";
import { DeviceLinkScreen } from "./deviceLinkScreen.js";
import { browserRouteUrl, readBrowserRoute } from "../../navigation/browserRouteUrl.js";

test("browserRouteUrl supports link route with code", () => {
  const url = browserRouteUrl("link", { code: "UQTXPW" });
  assert.equal(url, "/link?code=UQTXPW");
});

test("readBrowserRoute parses /link?code=UQTXPW cleanly", () => {
  const parsed = readBrowserRoute({ pathname: "/link", search: "?code=UQTXPW" });
  assert.deepEqual(parsed, { route: "link", params: { code: "UQTXPW" } });
});

test("DeviceLinkScreen renders link prompt and actions", () => {
  assert.equal(typeof DeviceLinkScreen.mount, "function");
  assert.equal(typeof DeviceLinkScreen.render, "function");
  assert.equal(typeof DeviceLinkScreen.handleApprove, "function");
});
