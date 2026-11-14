import assert from "node:assert/strict";
import test from "node:test";
import { shouldOpenPublicHome } from "./publicHomePolicy.js";

test("signed-out browsers start with metadata browsing only", () => {
  for(const pathname of ["/","/home","/browse/"]) assert.equal(shouldOpenPublicHome({isBrowser:true,pathname}),true);
  for(const pathname of ["/sign-in","/sign-in/qr","/link","/settings","/watch","/detail"]) assert.equal(shouldOpenPublicHome({isBrowser:true,pathname}),false);
  assert.equal(shouldOpenPublicHome({isBrowser:false}),false);
  assert.equal(shouldOpenPublicHome({isBrowser:true,hasLegacyGuestChoice:true}),false);
});
