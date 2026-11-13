import test from "node:test";
import assert from "node:assert/strict";
import { readFileSync } from "node:fs";
import { renderLoginQr } from "../../../core/qr/renderLoginQr.js";

function fixture({ fail = false, exportFail = false, emptyPng = false } = {}) {
  const codeText = {};
  const frame = {
    children: [],
    replaceChildren(node) { this.children = [node]; },
    contains(node) { return this.children.includes(node); }
  };
  let rendered;
  const document = {
    createElement(tag) {
      return tag === "canvas" ? {
        tag, setAttribute() {},
        toDataURL() {
          if (exportFail) throw Error("Canvas export failed");
          return emptyPng ? "data:," : "data:image/png;base64,local";
        }
      } : { tag };
    }
  };
  const generator = { generate(_canvas, value, size) {
    if (fail) throw Error("QR rendering failed");
    rendered = { value, size };
  } };
  const render = (input) => renderLoginQr({
    ...input, container: frame, codeText, document, generator,
    translate: (key, data) => data?.code || key
  });
  return { render, codeText, frame, get rendered() { return rendered; } };
}
const valid = { loginUrl: "https://example.test/approve?code=ABC", code: "ABC" };
test("service-issued HTTPS URL is rendered locally", () => {
  const f = fixture();
  assert.equal(f.render(valid), true);
  assert.deepEqual(f.rendered, { value: valid.loginUrl, size: 512 });
  assert.equal(f.frame.children[0].src, "data:image/png;base64,local");
  assert.equal(f.codeText.innerText, "ABC");
});
test("invalid, missing, non-HTTPS and credential-bearing URLs fail closed", () => {
  for (const loginUrl of ["javascript:alert(1)", "http://example.test", "https://user:pass@example.test/", "", "not-a-url"]) {
    const f = fixture();
    assert.equal(f.render({ ...valid, loginUrl, qrImageUrl: "https://example.test/qr.png" }), false);
    assert.equal(f.rendered, undefined);
    assert.match(f.frame.textContent, /Refresh to retry/);
  }
  assert.equal(fixture().render({ ...valid, code: "" }), false);
});
test("generation failure provides a visible retry state", () => {
  const f = fixture({ fail: true });
  assert.equal(f.render(valid), false);
  assert.match(f.frame.textContent, /Refresh to retry/);
});
test("canvas export failure or empty PNG preserves the generated QR", () => {
  for (const options of [{ exportFail: true }, { emptyPng: true }]) {
    const f = fixture(options);
    assert.equal(f.render(valid), true);
    assert.equal(f.frame.children[0].tag, "canvas");
  }
});
test("image load failure falls back to canvas but stale errors do not replace a refreshed QR", () => {
  const f = fixture();
  f.render(valid);
  const first = f.frame.children[0];
  first.onerror();
  assert.equal(f.frame.children[0].tag, "canvas");
  f.render(valid);
  const second = f.frame.children[0];
  first.onerror();
  assert.equal(f.frame.children[0], second);
});
test("screen uses the tested local renderer and does not construct an approval URL", () => {
  const screen = readFileSync(new URL("./authQrSignInScreen.js", import.meta.url), "utf8");
  assert.match(screen, /return renderLoginQr\(\{/);
  assert.doesNotMatch(screen, /qrImageUrl|api\.nuvio\.tv\/auth\/v1\/tv\?/);
});
