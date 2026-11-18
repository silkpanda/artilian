# Adventures in Artilian — Zapkin Electric Pet Plan (2026-11-17)

Branch: `phase-2-lightning-guy-pet` (from `main` at `9a1655b`).
Relates to: `PLAN.md` Phase 2, `PRODUCT.md`, `DESIGN.md`, `qa/Adventures in Artilian - Dragon and Eye Pets Plan 2026-11-16.md`.

> **Status: Stages 1–3 complete (2026-11-17, Antigravity).** Record each stage's commit here.
>
> | Stage | Done |
> |---|---|
> | 1 Zapkin entity, spawns, GeckoLib model/texture/glowmask/animations & datagen | Done. `ZapkinEntity` (pure `ELECTRIC`, `0.95 x 1.55` hitbox, eye height `1.10`, client `ELECTRIC_SPARK` particles), `ZapkinModel`/`ZapkinRenderer` (`1.25F` scale + `AutoGlowingGeoLayer`), `zapkin.geo.json`, `zapkin.animation.json` (`idle`, `walk`, `sit`, `attack`), `make_zapkin()` in `tools/art/make_pets_art.py` (`zapkin.png` + `zapkin_glowmask.png`), registered in `ArtilianEntities`/`PetEvents`/`ArtilianItems`/`ArtilianCreativeTabs`/`ArtilianBiomes` (Starlight Highlands weight 4, group 1–2), datagen (`en_us.json`, `starlight_highlands.json`, `zapkin_spawn_egg.json`). |
> | 2 GameTests, red-proofs, capture/faint verification | Done. Three `PetRosterGameTests` added (`zapkinIsPureElectricSizedAndSpawnsInStarlightHighlands`, `zapkinBattlesWithElectricMatchups`, `zapkinCapturesBefriendsSummonsAndFaintsSafelyIntoCrystal`). Red-proof: `ZapkinEntity#getPrimaryElement` -> `NATURE` turned `zapkinIsPureElectricSizedAndSpawnsInStarlightHighlands` and `zapkinCapturesBefriendsSummonsAndFaintsSafelyIntoCrystal` red; restored `ELECTRIC`, `104/104` green. |
> | 3 Client verification, live evidence, PR | Done. `./gradlew runClient` verified v2 redesign (storm-navy armor, glowing electric-cyan/white-hot visor, cheek lightning fins, back Tesla coils, spinning multi-ring crimson/cyan/yellow plasma orb hands, floating static spark shards, sharper glowing head bolts); zero missing-model/texture warnings in `run/logs/latest.log`; evidence saved to `qa/evidence/zapkin-2026-11-17.png`. |

## Why

The kids drew a lightning creature (`qa/reference/pets/lightning-guy-drawing-2026-11-17.jpg`): a bright yellow-and-red electric buddy with jagged lightning spikes on top of his head, round yellow-and-red orb hands at the ends of his arms, and two legs with feet.

**Name:** `Zapkin` (`artilian:zapkin`) is a placeholder display name. The kids pick the real name; only the `en_us` display name changes when they do. The registry id stays `zapkin` so captured crystals keep working.

### Art spec (from the drawing)

Source: `qa/reference/pets/lightning-guy-drawing-2026-11-17.jpg`.
Notes (`qa/reference/pets/NOTES.md`):
- A lightning guy, with lightning on his head (the jagged yellow-filled spikes on top).
- Red outline, yellow fill. Round yellow-and-red things at the ends of both arms. Two legs with feet.
- "He doesn't need to be square, take some liberties on that one." Free to reshape the body.

Model translation (sculpted, energetic bipedal electric buddy):
- **Body & Head:** Reshaped from a plain square into a compact, expressive torso (`waist`/`torso`) topped by a slightly wider sculpted `head` with expressive round yellow eyes, red rims, and a zig-zag electric grin/chest spark.
- **Lightning Head Spikes:** Three jagged yellow-filled, red-outlined lightning spikes rising from the top of the head (two shorter jagged spikes on the left/center and a taller crooked bolt on the right, matching the drawing's silhouette).
- **Arms & Orb Hands:** Thin red-and-yellow arms (`arm_left`, `arm_right`) ending in chunky round/octagonal yellow-and-red energy orb hands (`orb_left`, `orb_right`) that pulse and glow.
- **Legs & Feet:** Two sturdy yellow-filled, red-outlined legs (`leg_left`, `leg_right`) with distinct forward-pointing feet (`foot_left`, `foot_right`).
- **Glowmask:** `zapkin_glowmask.png` via `AutoGlowingGeoLayer` so the lightning head spikes, eyes, chest spark, and orb hand cores glow vibrantly.

### Rules

- **Elements:** `ELECTRIC` primary, no secondary (pure Electric; Zaphox is `ELECTRIC / AIR`).
- **Size:** Hitbox `0.95F x 1.55F`, eye height `1.10F`, `ZapkinRenderer` scale `1.25F` (about 1.5 blocks tall to the top of the lightning spikes).
- **Stats:** Regular roster: `22.0` health, `4.5` attack damage, `0.32` movement speed, `24.0` follow range.
- **Ambient particles:** `ParticleTypes.ELECTRIC_SPARK` crackling around the head spikes and orb hands on client.
- **Spawning:** `Starlight Highlands` only, on `ANIMALS_SPAWNABLE_ON`, `STARLIGHT_SLATE`, or `STARLIGHT_TURF`, spawn weight `4`, group size `1–2`.
- **Sounds:** happy `SoundEvents.AMETHYST_BLOCK_CHIME`, ambient `SoundEvents.ALLAY_AMBIENT_WITHOUT_ITEM`, hurt `SoundEvents.ALLAY_HURT`.

## Stages

### Stage 1: Zapkin entity, spawns, GeckoLib model/texture/glowmask/animations & datagen
- **Changes:**
  - `ZapkinEntity` extending `ArtilianPetEntity` (`ELECTRIC`, stats above, `checkZapkinSpawnRules`, client `ELECTRIC_SPARK` particles, sounds, GeckoLib controllers `idle`, `walk`, `sit`, `attack`).
  - Registered in `ArtilianEntities` (`ZAPKIN`), attributes & spawn placement in `PetEvents`, spawn egg in `ArtilianItems` + `ArtilianCreativeTabs`, biome spawn in `ArtilianBiomes` (Starlight Highlands, weight 4, group 1–2).
  - `ZapkinModel` / `ZapkinRenderer` (with `AutoGlowingGeoLayer`) wired in `ArtilianClient`; `zapkin.geo.json`, `zapkin.animation.json`.
  - `make_zapkin()` in `tools/art/make_pets_art.py` generating `zapkin.png` and `zapkin_glowmask.png` (restoring `crystal_fox.png` after running).
  - `ArtilianLanguageProvider` (`Zapkin`, `Zapkin Spawn Egg`), `ArtilianItemModelProvider` spawn egg, `./gradlew runData`.
- **Tests, written first and seen failing:** written in Stage 2.
- **Red-proof:** Stage 2.
- **Done When:** `./gradlew runData` and `./gradlew build` are clean.

### Stage 2: GameTests, red-proofs, capture/faint verification
- **Changes:** `PetRosterGameTests` gains:
  - `zapkinIsPureElectricSizedAndSpawnsInStarlightHighlands`
  - `zapkinBattlesWithElectricMatchups`
  - `zapkinCapturesBefriendsSummonsAndFaintsSafelyIntoCrystal`
- **Red-proof:**
  - Change `ZapkinEntity#getPrimaryElement` to `NATURE` -> `zapkinIsPureElectricSizedAndSpawnsInStarlightHighlands` and `zapkinBattlesWithElectricMatchups` turn red -> restore.
- **Done When:** `./gradlew runData`, `./gradlew build`, and `./gradlew runGameTestServer` pass 100%.

### Stage 3: Client verification, live evidence, PR
- **Changes:** Run `./gradlew runClient`, spawn Zapkin in-game, verify animations (`idle`, `walk`, `sit`, `attack`), glowmask, and zero missing-model/texture warnings in `run/logs/latest.log`, and capture `qa/evidence/zapkin-2026-11-17.png`. Update `PLAN.md` **Now**, push branch, and open PR.
- **Done When:** CI/local checks green, evidence screenshot saved, PR open.

## Security gate

n/a for new controls. `ZapkinEntity` extends `ArtilianPetEntity` and inherits `P2-01`–`P2-04` (crystal UUID ownership, non-lethal fainting, anti-duplication roster, friendly-target filtering). `PetSecurityProbes` must stay green.

## Out of scope

- The kids' official name for Zapkin (placeholder until they pick).
- New `PetElement` enum values or changes to matchup tables.
