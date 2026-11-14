import { fetchTmdbDiscover } from "../../../core/tmdb/tmdbDiscover.js";
import { TMDB_API_KEY } from "../../../config.js";

export function isPublicHomeEntry(pathname) {
  return ["/", "/home"].includes(String(pathname || "/").replace(/\/+$/, "") || "/");
}
export function guestProfilePresentation() {
  return { guest: true, activeProfileName: "Sign In", activeProfileInitial: "", showProfileSelector: true };
}
const categories = [
  { key:"popular", title:"Popular Movies", language:"" },
  { key:"bollywood", title:"Bollywood & Hindi Hits", language:"hi" },
  { key:"south", title:"South Indian Cinema", language:"te|ta|ml|kn" },
  { key:"punjabi", title:"Punjabi Cinema", language:"pa" }
];
function image(path,size) {
  return typeof path === "string" && /^\/[a-zA-Z0-9_.-]+$/.test(path) ? "https://image.tmdb.org/t/p/" + size + path : "";
}
export function mapPublicMovie(item) {
  if (!/^\d+$/.test(String(item?.id || ""))) return null;
  return { id:"tmdb:"+item.id, name:item.title || item.original_title || "Untitled", type:"movie", poster:image(item.poster_path,"w500"), background:image(item.backdrop_path,"w1280"), backdrop:image(item.backdrop_path,"w1280"), landscapePoster:image(item.backdrop_path,"w1280"), description:item.overview || "", releaseInfo:String(item.release_date || "").slice(0,4), released:item.release_date || "", rating:typeof item.vote_average === "number" ? item.vote_average : null };
}
function row(key,title,items) {
  return { addonId:"public-tmdb", catalogId:key, catalogName:title, type:"movie", result:{ status:"success", data:{items} } };
}
export async function loadGuestHomeRows({ signal, query = "", onRow } = {}) {
  if (!TMDB_API_KEY) throw Error("Public catalog is not configured");
  if(query.trim()) {
    const params=new URLSearchParams({api_key:TMDB_API_KEY,query:query.trim(),include_adult:"false"});
    const response=await fetch("https://api.themoviedb.org/3/search/movie?"+params,{signal});
    if(!response.ok) throw Error("Movie search unavailable");
    const payload=await response.json();
    const result=row("search","Search results",(payload.results||[]).map(mapPublicMovie).filter(Boolean));
    onRow?.(result);
    return [result];
  }
  const results=await Promise.allSettled(categories.map(async category=>{
    const params=new URLSearchParams({api_key:TMDB_API_KEY,sort_by:"popularity.desc",page:"1",include_adult:"false"});
    if(category.language){ params.set("with_original_language",category.language);params.set("with_origin_country","IN"); }
    const payload=await fetchTmdbDiscover({params,signal});
    const result=row(category.key,category.title,payload.results.map(mapPublicMovie).filter(Boolean));
    onRow?.(result);
    return result;
  }));
  const rows=results.filter(r=>r.status==="fulfilled").map(r=>r.value);
  if(!rows.length)throw Error("Public catalog unavailable");
  return rows;
}
