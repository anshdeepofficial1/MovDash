import { readFile, writeFile, rm } from "node:fs/promises";
import { spawnSync } from "node:child_process";
import { fileURLToPath } from "node:url";
import path from "node:path";

const root = fileURLToPath(new URL("../", import.meta.url));
const config = path.join(root, ".zylo-build.properties");
const publicKeys = [
  "NUVIO_SUPABASE_URL", "NUVIO_SUPABASE_ANON_KEY",
  "NUVIO_SUPABASE_FALLBACK_URL", "TMDB_API_KEY", "TRAKT_CLIENT_ID",
  "SIMKL_CLIENT_ID", "INTRODB_API_URL"
];
// The public upstream client identifier is the same one shipped by the
// default ZYLO Android configuration. Never copy a server credential here.
const example = await readFile(path.join(root, ".env.example"), "utf8");
const exampleValue = (key) => example.match(new RegExp("^" + key + "=(.*)$", "m"))?.[1]?.trim() || "";
const defaultUrl = "https://api.nuvio.tv";
const customUrl = process.env.NUVIO_SUPABASE_URL?.trim();
const customKey = process.env.NUVIO_SUPABASE_ANON_KEY?.trim();
if (Boolean(customUrl) !== Boolean(customKey)) {
  throw new Error("Set both NUVIO_SUPABASE_URL and NUVIO_SUPABASE_ANON_KEY to use a custom account server.");
}
const accountUrl = customUrl || defaultUrl;
const accountKey = customKey || exampleValue("NUVIO_SUPABASE_ANON_KEY");
const isPublicKey = (key) => {
  if (key.startsWith("sb_publishable_")) return true;
  try { return JSON.parse(Buffer.from(key.split(".")[1], "base64url").toString()).role === "anon"; }
  catch { return false; }
};
if (new URL(accountUrl).protocol !== "https:" || !isPublicKey(accountKey)) {
  throw new Error("Account server must use HTTPS and a browser-public anon/publishable key.");
}
const accountValues = {
  NUVIO_SUPABASE_URL: accountUrl,
  NUVIO_SUPABASE_ANON_KEY: accountKey,
  NUVIO_SUPABASE_FALLBACK_URL: process.env.NUVIO_SUPABASE_FALLBACK_URL || accountUrl
};
if (new URL(accountValues.NUVIO_SUPABASE_FALLBACK_URL).protocol !== "https:") throw new Error("Account fallback requires HTTPS.");
const settings = [
  "PROBE_MODE=off", "REMUX_MODE=off", "TRANSCODE_MODE=none",
  "SIMKL_APP_NAME=ZYLO"
];
for (const key of publicKeys) {
  const value = accountValues[key] || process.env[key];
  if (!value) continue;
  if (/[\r\n]/.test(value)) throw new Error("Invalid value for " + key);
  settings.push(key + "=" + value.replace(/\\/g, "\\\\"));
}
await writeFile(config, settings.join("\n") + "\n", { mode: 0o600 });
try {
  const result = spawnSync(process.execPath, ["scripts/build.mjs"], {
    cwd: root, stdio: "inherit",
    env: { ...process.env, NUVIO_LOCAL_PROPERTIES: config }
  });
  if (result.error) throw result.error;
  if (result.status !== 0) process.exitCode = result.status || 1;
} finally {
  await rm(config, { force: true });
}
