# Tech debt — Adventures in Artilian

This register lists the code-health problems that aren't bugs or security findings. Security/ownership findings go in `security/FINDINGS.md`, and feature work goes in `PLAN.md`.

**Severity**
- **High:** costs something on every tick/render, or will mislead the next person to change the code.
- **Medium:** slows work down or lets problems through without anyone noticing.
- **Low:** tidy-up.

**Status:** OPEN → FIXED (with the commit) or ACCEPTED (with the reason).

When you fix an item, update its row and add the commit to its section. An item that adds or changes a rule follows the usual practice: write the test, show it going red, then fix.

## Summary

| ID | Severity | Area | Item | Effort | Status |
|---|---|---|---|---|---|
| `TD-01` | Medium | `tools/art/` | Extract shared v2 texture painters (`art_lib.py`) and add entity art linter + headless previewer (`lint_art.py`, `preview_geo.py`) | M | OPEN |

Effort: **S** takes under half a day, **M** takes one to three days, **L** takes longer.

---

## TD-01: Shared v2 art painters and automated entity art linter/previewer

**Where:** `tools/art/make_pets_art.py`, `tools/art/make_bosses_art.py`, `tools/art/check_armor_sheets.py`

**What:** Six features across Phases 1–3 required full v2 art redos because v1 scripts defaulted to flat `paint_box()` / `outline_box()` helpers (single-color faces, uniform 1px borders, cramped `64×64` sheets) while the rich v2 techniques (6–8 stop ramps, stone strata/cracks, bevels, UV-overlap assertions) were written as private closures inside individual creature builders. Only `check_armor_sheets.py` validates UVs and flat faces, and it only checks the two armor sets.

**Fix:**
1. Extract reusable v2 painters (`paint_weathered_stone`, `paint_forged_metal`, `paint_faceted_crystal`, `paint_organic_shaded`, `assert_uv_layout_clean`) into `tools/art/art_lib.py`.
2. Generalize `check_armor_sheets.py` into `tools/art/lint_art.py` covering all `.geo.json` + `.png` pairs (checking UV bounds, UV overlaps, transparent holes, flat single-color faces, and sheet size minimums).
3. Add a fast headless Pillow 3D previewer (`tools/art/preview_geo.py`) that renders front/side/3-quarter/back contact sheets so agents can inspect silhouettes and shading before booting `./gradlew runClient`.

**Verify:** Running `python tools/art/lint_art.py` exits `0` across all shipped assets, and `python tools/art/preview_geo.py` outputs a readable 4-angle contact sheet for any entity `.geo.json`.

<!-- One section per item:

## TD-01: [short description]

**Where:** [files]

**What:** [the problem, with numbers if it was measured]

**Fix:** [what to change]

**Verify:** [the test or check that proves it's fixed]
-->
