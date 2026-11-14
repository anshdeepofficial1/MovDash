import assert from "node:assert/strict";
import test from "node:test";
import { fetchTmdbDiscover } from "./tmdbDiscover.js";

test("regional discovery requests each original language separately and preserves page", async () => {
  const calls = [];
  const params = new URLSearchParams({with_original_language:"te|ta|ml|kn",with_origin_country:"IN",page:"2",sort_by:"popularity.desc"});
  const result = await fetchTmdbDiscover({params,fetchImpl:async url=>{
    const query = new URL(url).searchParams;
    calls.push([query.get("with_original_language"),query.get("page"),query.get("with_origin_country")]);
    const i = calls.length;
    return {ok:true,json:async()=>({results:[{id:i,popularity:i},{id:99,popularity:0}],total_pages:i+2})};
  }});
  assert.deepEqual(calls,[["te","2","IN"],["ta","2","IN"],["ml","2","IN"],["kn","2","IN"]]);
  assert.deepEqual(result.results.map(x=>x.id),[4,3,2,1,99]);
  assert.equal(result.total_pages,6);
});
test("HTTP failures are not successful empty catalogs",async()=>{
  await assert.rejects(fetchTmdbDiscover({params:new URLSearchParams({with_original_language:"hi"}),fetchImpl:async()=>({ok:false,status:401,json:async()=>({status_message:"invalid key"})})}),/HTTP 401/);
});
test("valid empty result remains empty",async()=>{
  const r=await fetchTmdbDiscover({params:new URLSearchParams({with_original_language:"hi"}),fetchImpl:async()=>({ok:true,json:async()=>({results:[],total_pages:0})})});
  assert.deepEqual(r.results,[]);
});
