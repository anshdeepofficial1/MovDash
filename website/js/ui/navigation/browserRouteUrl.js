// Public navigation identifiers only. Never serialize playback URLs, tokens or
// arbitrary route state into the address bar.
const ROUTES = {
  home: "home", search: "search", discover: "discover", library: "library",
  settings: "settings", settingsSection: "settings/section", detail: "detail",
  stream: "streams", player: "watch", moviPlayer: "watch", account: "account",
  authSignIn: "sign-in", authQrSignIn: "sign-in/qr", syncCode: "sign-in/code",
  profileSelection: "profiles", essentialAddonSetup: "setup/addons",
  quickSetup: "setup", debugConsole: "settings/debug", trakt: "settings/trakt",
  supportersContributors: "settings/contributors", licensesAttributions: "settings/licenses",
  plugin: "addons", plugins: "plugins", catalogOrder: "settings/catalogs",
  castDetail: "cast", catalogSeeAll: "catalog", folderDetail: "library/folder",
  collectionEdit: "library/collection/edit", collectionFolderEdit: "library/folder/edit",
  link: "link"
};
const FIELDS = {
  settingsSection: ["section"], search: ["query"],
  detail: ["itemId", "itemType"], stream: ["itemId", "itemType", "videoId", "season", "episode"],
  player: ["itemId", "itemType"], moviPlayer: ["itemId", "itemType"],
  castDetail: ["tmdbId", "personId", "name"], catalogSeeAll: ["addonId", "catalogId", "type", "name"],
  folderDetail: ["folderId"], collectionEdit: ["collectionId"], collectionFolderEdit: ["folderId"],
  link: ["code"]
};
const RESTORABLE = new Set(["home", "search", "discover", "library", "settings", "settingsSection",
  "detail", "account", "plugin", "plugins", "catalogOrder", "licensesAttributions", "debugConsole",
  "folderDetail", "link"]);
function safeValue(value) {
  if (!["string", "number"].includes(typeof value)) return "";
  const text = String(value).trim();
  if (text.length > 250 || /https?:|magnet:|data:|[\u0000-\u001f]/i.test(text)) return "";
  return text;
}
export function browserRouteUrl(route, params = {}) {
  const path = ROUTES[route] || "home";
  const query = new URLSearchParams();
  for (const key of FIELDS[route] || []) {
    const value = safeValue(params[key]);
    if (value) query.set(key, value);
  }
  return "/" + path + (query.size ? "?" + query.toString() : "");
}
export function readBrowserRoute(location = {}) {
  const path = String(location.pathname || "/").replace(/^\/+|\/+$/g, "");
  if (!path) return null;
  let route = Object.keys(ROUTES).find((key) => ROUTES[key] === path);
  if (!route) return null;
  const query = new URLSearchParams(location.search || "");
  const params = {};
  for (const key of FIELDS[route] || []) {
    const value = safeValue(query.get(key));
    if (value) params[key] = value;
  }
  // Refreshing an active player or picker restores its detail page, never
  // a transient stream credential or an expired playback session.
  if (["player", "moviPlayer", "stream"].includes(route)) route = "detail";
  if (!RESTORABLE.has(route)) return null;
  if (route === "detail" && (!params.itemId || !params.itemType)) return null;
  if (route === "settingsSection" && !params.section) return { route: "settings", params: {} };
  if (route === "folderDetail" && !params.folderId) return { route: "library", params: {} };
  return { route, params };
}

// Keep a startup deep link across the sign-in/profile gate, but consume it
// only after the chosen profile is actually activated.
let pendingRoute = null;
export function stageBrowserRoute(location, profileId = null) {
  pendingRoute = { target: readBrowserRoute(location), profileId: profileId == null ? null : String(profileId) };
}
export function takeBrowserRoute(profileId) {
  const pending = pendingRoute;
  pendingRoute = null;
  if (!pending || (pending.profileId != null && pending.profileId !== String(profileId))) return null;
  return pending.target;
}
