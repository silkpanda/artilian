# Adventures in Artilian

A custom overhaul mod for **Minecraft: Java Edition (`1.21.1`, NeoForge)** built in playable phases for my kids — featuring Chester, biome-specific pets with Capture Crystals & Treats, Wands & Staves magic, custom TNTs, custom weapons/armor/tools, biomes, structures, bosses, the **Eye Realm**, and the **Giant Dragon Jungle**.

## Prerequisites
- **Java:** any JDK 17 or newer to launch Gradle `9.2.1` (JDK 25 works). The Foojay toolchain resolver downloads JDK 21 into `~/.gradle/jdks` on first build and uses it to compile and run the game.
- **Minecraft / Mod Loader:** Minecraft Java `1.21.1` + NeoForge `21.1.x` (`ModDevGradle`)
- **Libraries:** GeckoLib `4.9.3` (bundled inside the built `.jar` via `jarJar`), TerraBlender `4.1.0.8` (planned for Phase 2)

## Quickstart

The first build downloads Minecraft, NeoForge, and JDK 21, so give it several minutes. The mod jar lands at `build/libs/adventures-in-artilian-<version>.jar`.

1. **Build & verify mod:**
   ```powershell
   .\gradlew.bat build
   ```

2. **Environment configuration (optional publishing/dev overrides):**
   ```powershell
   Copy-Item .env.example .env
   ```

3. **Run local dev Minecraft client / server:**
   ```powershell
   .\gradlew.bat runClient
   .\gradlew.bat runServer   # Optional dedicated server on port 25565
   ```

4. **Run data generators & GameTests:**
   ```powershell
   .\gradlew.bat runData
   .\gradlew.bat runGameTestServer
   ```

## Architecture & Directory Layout
```text
adventures-in-artilian/
├── .github/                  # CI workflow (tests.yml) and PR template
├── deploy/                   # Family co-op JAR packaging & install guide
├── qa/                       # Per-feature stage plans (archive/ for completed) & QA findings
├── security/                 # Ownership & anti-griefing security findings
├── AGENTS.md / CLAUDE.md     # Agent operating rules
├── DESIGN.md                 # Visual tokens, cartoony style & HUD rules
├── PLAN.md                   # Master phased roadmap & session Now handoff
├── PRODUCT.md                # Product spec, Alice & Bob wishlists, constraints
└── TECH_DEBT.md              # Code health register
```
