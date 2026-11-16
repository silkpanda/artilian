# Adventures in Artilian — Scaffold Plan (2026-11-15)

<!-- PLAN.md Phase 1 Stage 1. One stage, one PR, one jar. -->

> **Status: Stage 1 built and verified locally, in PR.** Record each stage's commit here.
>
> | Stage | Done |
> |---|---|
> | 1 | `7d4fa9c`. Local: `runData`, `build`, `runGameTestServer` green (1/1 required tests). Red-proof: inverting the `modIsLoaded` assert made `runGameTestServer` exit 1 ("1 required tests failed"), restored to green. `runClient` reached the title screen with *Adventures in Artilian 0.1.0 (artilian)* loaded, zero ERROR log lines. Review fix: the MDK's `.cache` exclude never matched, so the datagen cache shipped in the jar; fixed in `build.gradle`. |

## Why

There is no mod yet: no Gradle project, no `artilian` mod id, nothing CI can build or test. Every later stage (Chester, ores, TNTs, wands) needs a project that compiles, generates data, runs GameTests, and produces a jar the kids can drop into `mods/`. This stage builds that and nothing else.

## Stages

### Stage 1: Scaffold
- **Changes:**
  - Gradle wrapper `9.2.1` and ModDevGradle `2.0.148`, taken from the official NeoForge `MDK-1.21.1-ModDevGradle`. The wrapper jar is the MDK's (SHA-256 `7d3a4ac4…6172`, a published Gradle 8.14.x wrapper checksum; any wrapper jar can launch any distribution). Gradle 9.2.1 runs on the local JDK 25; the Foojay resolver provisions JDK 21 for compiling and for every run config.
  - NeoForge `21.1.266`, Parchment `2024.11.17`. Mod id `artilian`, display name *Adventures in Artilian*, package `com.silkpanda.artilian`, jar `adventures-in-artilian-<version>.jar` (matches `deploy/README.md`), version `0.1.0`.
  - `ArtilianMod.java` (the `@Mod` entry point), `ArtilianBlocks` / `ArtilianItems` deferred registers wired to the mod bus, empty for now.
  - Datagen: one provider that writes the GameTest structure `data/artilian/structure/empty.nbt` into `src/generated/resources/`. Generated output is committed; the datagen `.cache` is not.
  - One GameTest, `ScaffoldGameTests.modIsLoaded`, on the `artilian:empty` template.
  - `.github/workflows/tests.yml` runs `runData` (and fails if `src/generated/` changed, meaning someone forgot to commit datagen output), `build`, and `runGameTestServer`, then uploads the jar as a workflow artifact.
  - GeckoLib and TerraBlender are *not* added.
- **Tests, written first and seen failing:** `ScaffoldGameTests.modIsLoaded` asserts the `artilian` mod is loaded and its template placed. Before datagen ran, it failed on the missing `artilian:empty` structure.
- **Red-proof:** the control here is the pipeline itself: a failing GameTest must fail the build. Switch the test to `helper.fail(...)`, run `runGameTestServer`, and confirm a non-zero exit; switch it back. Also confirm the log reports exactly 1 required test, so a test silently not being discovered cannot pass as green.
- **Done When:** `./gradlew build`, `runData`, and `runGameTestServer` are green locally and in CI, and `./gradlew runClient` reaches the title screen with *Adventures in Artilian* in the mod list.

## Security gate

n/a: no ownership, permissions, commands, or world-protection code in this stage.

## Out of scope

- GeckoLib, TerraBlender, any block, item, entity, creative tab, or config file. Those arrive with the stage that first needs them.
- Publishing to CurseForge or Modrinth.
