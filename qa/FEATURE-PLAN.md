# Adventures in Artilian — [Feature] Plan ([YYYY-MM-DD])

<!-- Copy this file to `qa/Adventures in Artilian - [Feature] Plan [YYYY-MM-DD].md` for each
feature. Don't edit this copy. When every stage is done, move the plan to qa/archive/. -->

> **Status: [Not started / Stage N in progress / All stages complete].** Record each stage's commit here.
>
> | Stage | Done |
> |---|---|
> | 1 | |
> | 2 | |

## Why

[The problem, in the owner's, Alex's, or Steve's words where possible. What's wrong or missing today.]

## Stages

### Stage 1: [Name]
- **Changes:** [files, entities, items, blocks, data attachments, worldgen]
- **Tests, written first and seen failing:** [GameTests / unit tests or what they check]
- **Red-proof:** [which control gets switched off, and which tests should turn red]
- **Done When:** [a concrete, checkable condition]

### Stage 2: [Name]
- **Changes:**
- **Tests, written first and seen failing:**
- **Red-proof:**
- **Done When:**

## Security gate

[Only if this feature touches ownership/permissions (Chester UUID lock, server commands, anti-griefing TNT config); otherwise write "n/a" and why.] Probes/GameTests in `security/[area]/`, with IDs. Each probe is shown EXPLOITED against a build with the control switched off, then HELD with it on. Record them in `security/FINDINGS.md`.

## Out of scope

- [What this feature deliberately doesn't do, so nobody builds it by accident.]
