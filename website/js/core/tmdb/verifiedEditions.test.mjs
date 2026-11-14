import assert from "node:assert/strict";
import test from "node:test";
import {findVerifiedEditions} from "./verifiedEditions.js";
test("Encore remains a separate official edition search result",()=>{
  for(const q of ["Avengers: Endgame Encore","endgame encore","Avengers Endgame: Encore 2026"]){
    const [r] = findVerifiedEditions(q);
    assert.equal(r.title,"Avengers Endgame: Encore");
    assert.equal(r.digitalRelease,"2026-11-13");
    assert.match(r.source,/^https:\/\/www\.disneyplus\.com\//);
    assert.equal(r.id,undefined);
  }
  assert.deepEqual(findVerifiedEditions("Avengers"),[]);
  assert.deepEqual(findVerifiedEditions("Jailer"),[]);
});
