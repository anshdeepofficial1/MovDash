import assert from "node:assert/strict";
import test from "node:test";
import { isPublicHomeEntry, guestProfilePresentation, mapPublicMovie } from "./guestHomeData.js";
test("root and native home are public entry routes, not sign-in or device link",()=>{
 for(const path of ["/","/home","/home/"])assert.equal(isPublicHomeEntry(path),true);
 for(const path of ["/sign-in","/sign-in/qr","/link","/watch","/settings"])assert.equal(isPublicHomeEntry(path),false);
});
test("guest profile contains no account information",()=>{
 assert.deepEqual(guestProfilePresentation(),{guest:true,activeProfileName:"Sign In",activeProfileInitial:"",showProfileSelector:true});
});
test("guest movie mapping provides metadata and no playback identifiers",()=>{
 const item=mapPublicMovie({id:123,title:"Example",poster_path:"/poster.jpg",url:"https://example.invalid/video",token:"secret"});
 assert.equal(item.id,"tmdb:123");
 assert.equal(item.poster,"https://image.tmdb.org/t/p/w500/poster.jpg");
 assert.equal(item.url,undefined);assert.equal(item.token,undefined);
 assert.equal(mapPublicMovie({id:"https://bad.invalid"}),null);
});
