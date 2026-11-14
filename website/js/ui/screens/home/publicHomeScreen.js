import { Router } from "../../navigation/router.js";
import { ScreenUtils } from "../../navigation/screen.js";
import { TMDB_API_KEY } from "../../../config.js";
import { fetchTmdbDiscover } from "../../../core/tmdb/tmdbDiscover.js";
import { findVerifiedEditions } from "../../../core/tmdb/verifiedEditions.js";

// A signed-out, public metadata view. It deliberately has no stream lookup,
// player navigation, user credential reads, watch history or offline actions.
const categories = [
  { title: "Popular Movies", language: "" },
  { title: "Bollywood & Hindi Hits", language: "hi" },
  { title: "South Indian Cinema", language: "te|ta|ml|kn" },
  { title: "Punjabi Cinema", language: "pa" }
];

function element(tag, text, className) {
  const node = document.createElement(tag);
  if (text != null) node.textContent = text;
  if (className) node.className = className;
  return node;
}

function externalTitle(item) {
  const link = element("a", null, "public-home-card focusable");
  link.href = "https://www.themoviedb.org/movie/" + encodeURIComponent(String(item.id));
  link.target = "_blank";
  link.rel = "noopener noreferrer";
  if (typeof item.poster_path === "string" && /^\/[a-zA-Z0-9_.-]+$/.test(item.poster_path)) {
    const img = element("img");
    img.src = "https://image.tmdb.org/t/p/w342" + item.poster_path;
    img.alt = "";
    img.loading = "lazy";
    link.append(img);
  }
  link.append(element("strong", item.title || item.original_title || "Untitled"));
  link.append(element("small", String(item.release_date || "").slice(0,4)));
  return link;
}

export const PublicHomeScreen = {
  async mount() {
    this.container = document.getElementById("publicHome");
    ScreenUtils.show(this.container);
    this.controller?.abort();
    const controller = new AbortController();
    this.controller = controller;
    this.container.replaceChildren();
    const main = element("main", null, "public-home-shell");
    const header = element("header", null, "public-home-header");
    header.append(element("h1","ZYLO"));
    const signIn = element("button","Sign in","public-home-button focusable");
    signIn.type = "button";
    signIn.addEventListener("click", () => Router.navigate("authSignIn"));
    header.append(signIn);
    main.append(header, element("h2","Discover movies"), element("p","Browse public movie information. Sign in to access your account, profiles and saved preferences."));
    const form = element("form", null, "public-home-search");
    const input = element("input");
    input.type = "search";
    input.placeholder = "Search movies";
    input.setAttribute("aria-label","Search movies");
    const submit = element("button","Search","public-home-button focusable");
    submit.type = "submit";
    form.append(input,submit);
    main.append(form);
    const body = element("div");
    main.append(body);
    this.container.append(main);
    let searchController = null;
    const showCatalogs = async () => {
      body.replaceChildren();
      for (const category of categories) {
        const section = element("section", null, "public-home-section");
        const row = element("div", null, "public-home-row");
        const status = element("p","Loading catalog…");
        section.append(element("h2",category.title),status,row);
        body.append(section);
        void (async () => {
          try {
            if (!TMDB_API_KEY) throw new Error("Catalog configuration unavailable");
            const params = new URLSearchParams({ api_key: TMDB_API_KEY, page: "1", sort_by:"popularity.desc" });
            if (category.language) {
              params.set("with_original_language",category.language);
              params.set("with_origin_country","IN");
            }
            const data = await fetchTmdbDiscover({params,signal:controller.signal});
            if(controller.signal.aborted || !section.isConnected) return;
            status.textContent = data.results.length ? "" : "No titles found.";
            for (const item of data.results.slice(0,20)) row.append(externalTitle(item));
          } catch {
            if(!controller.signal.aborted && section.isConnected) status.textContent = "Catalog unavailable. Refresh to retry.";
          }
        })();
      }
    };
    form.addEventListener("submit", async event => {
      event.preventDefault();
      searchController?.abort();
      const query = input.value.trim();
      if (!query) { void showCatalogs(); return; }
      const activeSearch = new AbortController();
      searchController = activeSearch;
      controller.signal.addEventListener("abort",()=>activeSearch.abort(),{once:true});
      body.replaceChildren();
      const row = element("div",null,"public-home-row");
      const status = element("p","Searching…");
      body.append(status,row);
      for(const edition of findVerifiedEditions(query)) {
        const link = element("a",edition.title + " — official provider","public-home-card focusable");
        link.href = edition.source; link.target="_blank"; link.rel="noopener noreferrer";
        row.append(link);
      }
      try {
        if(!TMDB_API_KEY) throw new Error("Catalog configuration unavailable");
        const params = new URLSearchParams({api_key:TMDB_API_KEY,query,include_adult:"false"});
        const response = await fetch("https://api.themoviedb.org/3/search/movie?" + params,{signal:activeSearch.signal});
        if(!response.ok) throw new Error("Search unavailable");
        const data = await response.json();
        if(activeSearch.signal.aborted || !row.isConnected) return;
        for(const item of (Array.isArray(data.results)?data.results:[]).filter(i=>i?.id)) row.append(externalTitle(item));
        status.textContent = row.childElementCount ? "" : "No titles found.";
      } catch {
        if(!activeSearch.signal.aborted && row.isConnected) status.textContent = "Search unavailable. Try again.";
      }
    });
    void showCatalogs();
  },
  cleanup() {
    this.controller?.abort();
    this.controller = null;
    if(this.container) {
      this.container.replaceChildren();
      ScreenUtils.hide(this.container);
    }
  }
};
