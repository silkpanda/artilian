# QA findings — Adventures in Artilian

Whether the mod works smoothly for Alice, Bob, and the owner. Security/ownership/anti-griefing findings go in `security/FINDINGS.md`, and code-health items in `TECH_DEBT.md`.

Log each finding the moment it's found, with an ID (`QA-01`, `QA-02`, ...) and a status in its heading: OPEN, FIXED (with the commit), or ACCEPTED (with the reason).

Template for an entry:

### QA-01 — [short description] — OPEN
- **Steps:** [as which player, what you did]
- **Expected:** [what should happen]
- **Actual:** [what happened]
- **Where:** [file:line, entity, or in-game scenario]
- **Fix:** [what changed, the commit, and the test that covers it]

---

### QA-01 — Spark Wand hits the nearest animal or golem, and reaches through walls — FIXED
- **Steps:** As a player, aim the Spark Wand at a zombie with a sheep standing closer on the same line; at a village iron golem; at a zombie behind a stone wall.
- **Expected:** The bolt hits the zombie, never a golem, and stops at the wall.
- **Actual:** The bolt took the closest non-protected mob, so the sheep was hit and the zombie was not. An iron golem took 6 damage attributed to the player, which turns a village golem hostile. A zombie behind a wall took 6 damage.
- **Where:** `mana/ManaService.java` (`castSparkWand`, `isNearRay`)
- **Fix:** The ray is clipped at the first solid block, targets need line of sight, monsters sort ahead of animals before distance, and iron and snow golems are protected in `safety/KidSafeTargets`. Stage 1 on `phase-1-stage-7-review-fixes`. Tests: `ReviewFixGameTests.sparkWandPrefersMonsterOverCloserAnimal`, `sparkWandBlockedByWall`, `sparkWandSparesIronGolem` (all three red against the old code).

### QA-02 — Party TNT gives monsters Speed and Regeneration — FIXED
- **Steps:** Light Party TNT with a zombie or creeper within 6 blocks.
- **Expected:** Players and friendly mobs get the party effects. Monsters get nothing.
- **Actual:** Every living entity in range got Speed II and Regeneration, monsters included.
- **Where:** `tnt/ArtilianPrimedTnt.java` (`explodeParty`)
- **Fix:** `explodeParty` skips `KidSafeTargets#isHostile` entities. Stage 1 on `phase-1-stage-7-review-fixes`. Test: `ReviewFixGameTests.partyTntDoesNotBuffMonsters` (red against the old code).

### QA-03 — Frost Nova TNT turns flowing water into permanent water sources — FIXED
- **Steps:** Light Frost Nova TNT beside flowing water (a stream running off a single source).
- **Expected:** Still water freezes and melts back to what it was.
- **Actual:** Flowing water became frosted ice, and frosted ice melts into a water source, so each flowing block came back as a new spring.
- **Where:** `tnt/ArtilianPrimedTnt.java` (`explodeFrostNova`)
- **Fix:** Only water source blocks freeze. Stage 1 on `phase-1-stage-7-review-fixes`. Test: `ReviewFixGameTests.frostNovaFreezesOnlySourceWater` (red against the old code).

### QA-04 — Sleeping Chester does not match a real chest — FIXED
- **Steps:** Put Chester to sleep next to a placed chest and look at both.
- **Expected:** At a glance they are the same object (`PRODUCT.md`: he "disguises as a normal chest").
- **Actual:** Chester's own texture was more orange, had an inset lid panel, and a different latch. Side by side he read as a chest from another texture pack.
- **Where:** `client/ChesterModel.java`, `textures/entity/chester.png`
- **Fix:** Asleep, Chester is drawn from `geo/entity/chester_sleep.geo.json` using the game's own `minecraft:textures/entity/chest/normal.png`, so he matches exactly and follows any resource pack. No vanilla pixels are copied into the mod. Stage 2 on `phase-1-stage-7-review-fixes`. Test: `Phase1ArtGameTests.sleepingChesterModelIsAPlainChest` (red with a bone renamed). Evidence: `qa/evidence/chester-sleep-matches-chest-artfix-2026-11-15.png`.

### QA-05 — 3D armor reads as pixel noise — FIXED (superseded by PR #15)
- **Steps:** Put the Sunsteel and Starlight sets on armor stands and look from ten blocks away.
- **Expected:** `DESIGN.md` high-contrast silhouettes a young kid can tell apart.
- **Actual:** The sheets were one-pixel filigree with transparent gaps. Stretched over 3D plates it became a teal-and-purple checker and orange-and-red static.
- **Where:** `textures/item/armor/sunsteel_armor.png`, `textures/item/armor/starlight_armor.png`
- **Fix:** Originally fixed in Stage 2 of `phase-1-stage-7-review-fixes`, then superseded during rebase onto `main` by PR #15 (`art/2026-11-15-armor-pass`, merged to `main`), which sealed the 64x64 GeckoLib armor sheets in `tools/art/make_ores_and_gear_art.py`, audited UVs with `tools/art/check_armor_sheets.py`, de-purpled Starlight, and raised helmet crests/spires. Evidence on `main`: `qa/evidence/armor-pass-sunsteel-2026-11-15.png`, `armor-pass-starlight-2026-11-15.png`.

### QA-06 — Wands and TNTs are told apart by colour alone — FIXED
- **Steps:** Look at the three wands in the hotbar, and at the four TNT blocks from twelve blocks away.
- **Expected:** Each has its own shape.
- **Actual:** The wands were the same diagonal stick with a six-pixel head, and each TNT emblem was a four-pixel-tall mark that vanished at a distance.
- **Where:** `tools/art/make_wand_art.py`, `tools/art/make_tnt_art.py`
- **Fix:** Wand heads now fill about half the icon (a crystal, a heart, a wing). Each TNT side carries one 8x8 emblem on a tall band (burst, up arrow, snowflake, four bomblets). Stage 2 on `phase-1-stage-7-review-fixes`. Evidence: `qa/evidence/custom-tnts-emblems-artfix-2026-11-15.png`, `custom-tnts-far-artfix-2026-11-15.png`.
