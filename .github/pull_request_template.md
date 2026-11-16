## What

Plan: `qa/[plan file]`. Stages in this PR:

- **Stage [N]:** [what changed, in plain words]

## Things to review

- [Decisions a reviewer should check, risky spots, anything public-facing or gameplay-breaking. Write "none" if there are none.]

## Checks

- **Tests:** [modules / `./gradlew build` / GameTests run locally and their result]. The full suite runs in CI.
- **Red-proven:** [each control switched off and the tests that turned red, or "none: no new rule"]
- **Security gate:** [probes / ownership & anti-grief checks and their result, or "n/a: no permissions, input or public URL"]
- **In-game / client:** [what was tested in `./gradlew runClient` or LAN co-op, or "n/a: docs/config only"]
- **Migrations / data:** [worldgen, DataComponents, or Attachment schema changes, or "none"]
- **Not checked:** [anything left unverified, and why]
