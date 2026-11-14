// Only browse public metadata before sign-in; explicit account and device-link
// entry points must retain the existing authentication flow.
export function shouldOpenPublicHome({ isBrowser = false, pathname = "/", hasLegacyGuestChoice = false } = {}) {
  if (!isBrowser || hasLegacyGuestChoice) return false;
  const path = String(pathname || "/").replace(/\/+$/, "") || "/";
  return path === "/" || path === "/home" || path === "/browse";
}
