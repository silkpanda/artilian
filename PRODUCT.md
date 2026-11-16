# Product

<!-- impeccable:product-schema 1 -->

## Platform

Minecraft: Java Edition (`1.21.1`) client & dedicated/LAN server mod (`artilian`).

## Stack

- **Mod Loader & Build:** NeoForge `21.1.x` (`ModDevGradle`), Java `21` toolchain
- **Planned Libraries:** GeckoLib `4.9.3` (cartoony/animated Chester, pets, bosses, dragons), TerraBlender `4.1.0.8` (Overworld biome injection)
- **Local Dev / Co-op Port:** Minecraft client & LAN/dedicated server (`25565` default when hosting locally)

## Users

- **Alice:** Wants biome-specific pets, pet capture & battling, a personal cartoony living chest friend named **Chester** (who sleeps looking like a normal chest), an **Eye Dimension** covered in eyes everywhere, custom biome-specific structures, and custom structure enemies.
- **Bob:** Wants custom bosses, custom weapons/armor/tools, custom TNTs, new biomes, and a distinct second dimension (**Giant Jungle of Dragons** with multiple dragon species).
- **The owner (Creator / Parent Admin):** Builds and curates the mod in playable phases for co-op family sessions.

## Product Purpose

Deliver a cohesive, kid-friendly Minecraft Java overhaul mod—**Adventures in Artilian**—that unites Alice's and Bob's wishlists into one clear, rewarding progression loop, shipped in playable milestones so the kids can play each phase as it's built.

## Positioning

Unlike hardcore RPG or grindy tech overhaul packs, *Adventures in Artilian* prioritizes immediate wonder, readable mechanics, expressive cartoony companions, and zero-frustration safety nets tailored for ages 5 and 7.

## Operating Context

- Runs on family Windows PCs (Minecraft Java `1.21.1` + NeoForge) in single-player or local LAN co-op.
- Built in strict **branch-and-PR phases** with automated builds (`./gradlew build`), data generation (`./gradlew runData`), and NeoForge GameTests (`./gradlew runGameTestServer`) before each playable `.jar` release.

## Capabilities and Constraints

- **Chester Companion:** Auto-spawns bound to each player on first login, re-summonable anytime with a **Chester Whistle**, stores 27 player-bound slots safely (survives entity loss/player death), and disguises as a normal chest both when the owner sleeps in a bed and when told to **Sit & Sleep**.
- **Pet Capture & Battles:** Biome pets can be captured with **Capture Crystals** or befriended with **Biome Treats**, summoned to battle alongside the player, and safely **faint** back into their crystal instead of dying permanently.
- **Magic System:** Auto-regenerating Mana bar paired with **Wands & Staves** (no frustrating consumable scroll ammo).
- **Custom TNTs & Arsenal:** Playful non-destructive TNTs (Party, Bounce, Frost) plus configurable demolition TNTs (`allowDestructiveCustomTnt` toggle in config to protect kids' bases).
- **Two Distinct Dimensions:** Alice's **Eye Realm** (eyes across terrain, trees, sky, mobs, pet, and boss) and Bob's **Dragon Jungle** (giant prehistoric jungle with multiple dragon species and dragonscale endgame).
- **Hard Constraint:** Every phase must remain kid-accessible, readable (telegraphed boss attacks, clear audio/visual cues), and playable without waiting for all 5 phases to finish.

## Brand Commitments

- Cartoony, vibrant, storybook-adventure aesthetic with high-contrast silhouettes so young kids can immediately tell friend from foe.
- Clear visual/audio feedback on every spell, capture, boss telegraph, and Chester state change.

## Evidence on Hand

- Master phased plan in `PLAN.md`.
- Intake confirmed for both Alice's and Bob's wishlists (2026-11-15).
