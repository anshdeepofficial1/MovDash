import { writeFile, rm } from "node:fs/promises";
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
const settings = [
  "PROBE_MODE=off", "REMUX_MODE=off", "TRANSCODE_MODE=none",
  "SIMKL_APP_NAME=ZYLO"
];
for (const key of publicKeys) {
  const value = process.env[key];
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
