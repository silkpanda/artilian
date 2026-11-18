# AGENTS.md — Adventures in Artilian

Instructions for every coding agent working in this repo (**Claude Code**, **Antigravity**, and **OpenCode**). Keep this file short: it points to the real docs rather than repeating them. If something here disagrees with those docs, the docs win; fix this file.

## What this is
*Adventures in Artilian* is a custom overhaul mod for Minecraft: Java Edition (`1.21.1`) built in playable phases for my kids. Stack: Java `21`, NeoForge `21.1.x` (`ModDevGradle`), GeckoLib `4.9.3`, TerraBlender `4.1.0.8`.

- `README.md`: setup and how to run it. Read it first.
- `PRODUCT.md`: who it's for and what it must do.
- `PLAN.md`: the single consolidated plan organized by phases and stages within each phase (sub-stage tables, decisions, red-proofs, evidence), plus the **Now** section showing what is happening right now.
- `qa/`: QA findings (`qa/FINDINGS.md`), client verification screenshots (`qa/evidence/`), and reference drawings/specs (`qa/reference/`).
- `DESIGN.md`: visual direction, UI colors, HUD/entity style rules. Read it before building any screen or model.
- `qa/FINDINGS.md`, `security/FINDINGS.md`, `TECH_DEBT.md`: the registers. Log a finding the moment it's found.
- `deploy/README.md`: how mod JARs are packaged and distributed for family co-op play.

## Commands (run from the project root)
```bash
# Once Gradle scaffold is added in Phase 1 Stage 1:
./gradlew runClient          # Launch dev Minecraft 1.21.1 client
./gradlew runServer          # Launch dev dedicated server (port 25565)
./gradlew runData            # Run NeoForge data generators (models, recipes, loot, worldgen)
./gradlew runGameTestServer  # Run NeoForge GameTests
./gradlew build              # Full compile, datagen check & JAR build (CI runs this on every push/PR)
```

## Who does what
- **Claude Code, Antigravity, and OpenCode** collaborate across phases on feature branches: pick up the active task from `PLAN.md`'s **Now** section, follow or expand the relevant Phase & Stage directly inside `PLAN.md`, implement on a dedicated branch/worktree, and verify all Gradle checks before opening a PR.

## Start and end of every session
- **Start:** read the **Now** section near the top of `PLAN.md` before anything else. It says what's in progress, what's next, and what's waiting on the owner. If it disagrees with git (`git status`, `git worktree list`, open PRs), trust git and say so.
- **End:** before you finish, rewrite the Now section so the next session (any agent) can pick up from it alone. Replace it; don't append. Date it and name yourself. Keep it to the table: in progress, next, waiting on the owner, other work in flight, loose ends. Commit it on your branch with your work, so it reaches `main` with your PR. If you made no other change, put it on a `plan/<yyyy-mm-dd>-now` branch and open a PR, never a commit on `main`.
- Short questions that change nothing don't need an update.

## How work is done here
1. **Plan first.** Every stage (and sub-stage) lives directly in `PLAN.md` under its Phase. Build one stage at a time, and record what each stage actually did (commits, red-proofs, evidence) in `PLAN.md`.
2. **Tests prove the rule.** For each new rule or control (e.g., Chester UUID ownership, non-lethal pet fainting, TNT block-damage config), write the GameTest/unit test, switch the control off, confirm the test goes red, and switch it back on. Record which tests turned red in that stage's entry in `PLAN.md`.
3. **Check it in-game** (`./gradlew runClient` / LAN co-op) for anything a player sees, hears, or interacts with.
4. **Security & ownership gate.** Anything that touches player inventory ownership (Chester), server permissions/commands, or world protection (custom TNT griefing controls) gets probes/GameTests that are red-proven first and then HELD, and an entry in `security/FINDINGS.md`.
5. **Commits** look like `<Feature> Stage 2: <what changed, in plain words>`, or `Plan: ...` for plan-only changes. One branch per feature, merged into `main` by pull request once CI is green. Fill in every section of the PR template.

## Rules that are easy to break
- **Nothing is committed on `main`, not even a `Plan:` change.** The baseline commit from project prep is the only exception. Every change, docs and plans included, goes on a branch and reaches `main` only through a pull request on GitHub. Don't merge into `main` locally and don't push to it. If you're on `main` when it's time to commit, make a branch first (`plan/<yyyy-mm-dd>-<topic>` for plan-only changes), commit there, open a PR, and say that's what you did. This holds when the owner says "commit" or "push" while on `main`: he wants the branch.
- **Never commit secrets:** `.env`, local databases, credentials and tokens stay gitignored. Private machine details (server addresses, local paths, SSH aliases) go in `CLAUDE.local.md`, which is gitignored. `AGENTS.md` and `CLAUDE.md` are committed.
- **Never leave the baseline red:** every task ends with the tests green.
- **Keep kid-safe defaults intact:** never remove non-lethal pet fainting, player-bound Chester storage, or configurable TNT protection without explicit instruction from the owner.

## Working alongside other agents
More than one agent may work on this repo. To avoid overwriting each other:
- Work only on the branch or task you were given, ideally in your own worktree.
- Don't edit files outside your task's area. If you need to, stop and say so.
- Don't rewrite, reformat or "tidy" code you weren't asked to touch.
- If the working tree has uncommitted changes you didn't make, leave them alone.
