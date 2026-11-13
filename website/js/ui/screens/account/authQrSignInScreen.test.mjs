import test from "node:test";
import assert from "node:assert/strict";
import { readFileSync } from "node:fs";
const screen = readFileSync(new URL("./authQrSignInScreen.js", import.meta.url), "utf8");
const start = screen.indexOf("  renderQr({ loginUrl, code }) {");
const end = screen.indexOf("\n  clearQr() {", start);
assert.ok(start >= 0 && end > start);
function fixture({ fail = false } = {}) {
  const codeText = {};
  const frame = { children: [], replaceChildren(node) { this.children = [node]; } };
  let rendered;
  const document = {
    createElement(tag) { return tag === "canvas" ? { toDataURL: () => "data:image/png;base64,local" } : {}; }
  };
  const generator = { generate(_canvas, value, size) {
    if (fail) throw Error("QR rendering failed");
    rendered = { value, size };
  } };
  const methods = new Function("document", "QrCodeGenerator", "I18n",
    "return ({" + screen.slice(start, end) + "});")(document, generator, { t: (key, data) => data?.code || key });
  methods.container = { querySelector: (id) => id === "#qr-container" ? frame : codeText };
  return { methods, codeText, frame, get rendered() { return rendered; } };
}
test("HTTPS login URL is rendered locally, not sent to an external QR image service", () => {
  const f = fixture();
  assert.equal(f.methods.renderQr({ loginUrl: "https://example.test/approve?code=ABC", code: "ABC" }), true);
  assert.deepEqual(f.rendered, { value: "https://example.test/approve?code=ABC", size: 512 });
  assert.equal(f.frame.children[0].src, "data:image/png;base64,local");
  assert.equal(f.codeText.innerText, "ABC");
});
test("invalid URL and failed rendering provide a visible retry state", () => {
  for (const [url, fail] of [["javascript:alert(1)", false], ["https://example.test/approve", true]]) {
    const f = fixture({ fail });
    assert.equal(f.methods.renderQr({ loginUrl: url, code: "ABC" }), false);
    assert.match(f.frame.textContent, /Refresh to retry/);
  }
});
