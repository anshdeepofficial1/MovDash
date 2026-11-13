import assert from "node:assert/strict";
import test from "node:test";
import { readFile } from "node:fs/promises";
import { readAppMetadata } from "./appMetadata.mjs";

test("application metadata supplies the independent fork version and repository identity", async () => {
  const metadata = await readAppMetadata();
  // Read rather than written out: pinning the number here made every release
  // edit a test to say the same thing twice. What matters is that the version
  // the app reports is the manifest's, and that it looks like a version.
  const { version } = JSON.parse(
    await readFile(new URL("../package.json", import.meta.url), "utf8")
  );

  assert.match(metadata.version, /^\d+\.\d+\.\d+$/);
  assert.equal(metadata.version, version);
  assert.equal(metadata.identity.channel, "local");
  assert.equal(metadata.identity.commit, "");
  assert.equal(metadata.identity.upstreamVersion, "0.3.35");
  assert.equal(metadata.identity.maintainer, "anshdeepofficial1");
  assert.equal(metadata.identity.sourceRepositoryUrl, "https://github.com/anshdeepofficial1/ZYLO");
  assert.equal(metadata.identity.issuesUrl, "https://github.com/anshdeepofficial1/ZYLO/issues");
  assert.equal(
    metadata.identity.latestReleaseUrl,
    "https://api.github.com/repos/anshdeepofficial1/ZYLO/releases/latest"
  );
});
