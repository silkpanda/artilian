# Deploy — Adventures in Artilian

> **Not packaged or distributed yet.** *Adventures in Artilian* is a Minecraft Java (`1.21.1`, NeoForge) client/server mod distributed as a `.jar` file (not a cloud web service). Fill in exact launcher/co-op paths before the Phase 1 family playtest release.

## Where it runs

- **Target Runtime:** Minecraft Java Edition `1.21.1` with NeoForge `21.1.x`
- **Mod Dependencies:** GeckoLib (`4.9.3`) and TerraBlender (`4.1.0.8`) are both bundled inside the mod `.jar` via Jar-in-Jar (no extra `.jar` needed in `mods/`)
- **Distribution Artifact:** `build/libs/adventures-in-artilian-<version>.jar`
- **Family Co-op Play:** Installed into each player's NeoForge `mods/` folder (and the LAN host / local dedicated server `mods/` folder on port `25565`)
- **Settings:** `config/artilian-common.toml` holds `allowDestructiveCustomTnt` under `[tnt]` (defaults to `false`, shipped in Phase 1 Stage 4 so only Cluster Quarry TNT can break non-block-entity terrain when an adult/host explicitly sets it to `true`); later phases add pet fainting toggles — Chester is strictly bound to one owner and has no sharing toggle

Private machine details (launcher paths, local LAN IPs) go in `CLAUDE.local.md` if needed.

## Release order

1. Merge phase PR into `main` once CI is green.
2. Build release JAR locally via `./gradlew build`.
3. Back up the family Minecraft world folder before dropping in a new phase `.jar`.
4. Copy the new `.jar` into the host and client `mods/` folders and verify launch.

## Backups

- **World Backups:** Always copy the `saves/<world_name>` directory to an off-drive or backup folder before testing a new phase release with Alex and Steve.

## Rolling back a bad release

1. Stop Minecraft / local server.
2. Restore the previous phase `.jar` in `mods/` and restore the pre-upgrade world backup from before the test session.
