// Render the service-issued login URL locally. Never synthesize an approval URL
// or send a session code to an external QR-image provider.
export function renderLoginQr({ loginUrl, code, container, codeText, document, generator, translate }) {
  if (!container || !codeText) return false;
  codeText.innerText = translate("auth.qr.codeLabel", { code });
  const fail = () => {
    container.textContent = "Unable to generate QR. Select Refresh to retry.";
    return false;
  };
  let url;
  try {
    url = new URL(String(loginUrl || "").trim());
    if (url.protocol !== "https:" || url.username || url.password || !String(code || "").trim()) return fail();
  } catch {
    return fail();
  }
  let canvas;
  try {
    canvas = document.createElement("canvas");
    canvas.className = "qr-image qr-image-canvas";
    canvas.setAttribute("aria-label", translate("auth.qr.qrImageAlt"));
    generator.generate(canvas, url.href, 512);
  } catch {
    return fail();
  }
  try {
    const image = document.createElement("img");
    image.className = "qr-image";
    image.alt = translate("auth.qr.qrImageAlt");
    const png = canvas.toDataURL("image/png");
    if (!png.startsWith("data:image/png;base64,")) throw new Error("Invalid local QR image");
    image.onerror = () => {
      // Ignore stale image errors after refresh or navigation.
      if (container.contains(image)) container.replaceChildren(canvas);
    };
    image.src = png;
    container.replaceChildren(image);
  } catch {
    container.replaceChildren(canvas);
  }
  return true;
}
