# Captain Brimstone — Silhouette & Visual Reference Spec (v2: Non-Humanoid Pirate Galleon-Walker)

- **Role:** Phase 3 — Stage 3 Boss (`artilian:captain_brimstone`), Fire / Shadow cursed pirate boss of **Emberbloom Meadows** (`artilian:brimstone_bastion`).
- **Target Scale:** `2.4 × 3.4` block hitbox (`~4.0` blocks tall to top of mast/Jolly-Roger flag; `visible_bounds_width: 4.8`, `visible_bounds_height: 4.6`).
- **Silhouette Inspiration (0% Humanoid, 100% Pirate):** A living, scuttling **Cursed Pirate Galleon-Crab** — a curved dark-timber & iron-banded pirate ship hull walking on **4 articulated iron anchor/crab legs**, featuring a **pointed bowsprit with a glowing skull figurehead**, **port & starboard broadside cannons**, a **spoked ship's wheel** on the quarterdeck, a **tall mainmast with a tattered Crimson Jolly-Roger Sail (pixel-art skull & crossbones)**, and a **giant Tricorne-hatted Pirate Skull-Captain** (with a **black eyepatch** and **one blazing magma eye**) fused into the helm!
- **Color Palette & Anchor:**
  - **Ship Hull & Anchor Legs:** Dark Weathered Emberwood & Charred Oak (`#221518` – `#5A3228`), iron hull bands (`#1E1920` – `#4A3E4C`), and Sunsteel Gold trim (`#B87424` – `#FFE08A`).
  - **Sail & Jolly Roger:** Tattered Crimson-Oxide sailcloth (`#5E1A22` – `#9A2C34`) with bone-white (`#E8DFD0`) skull & crossbones emblem.
  - **Glowing Accents (`_glowmask.png`):** Blazing Magma Orange (`#FF6B1A`), Ember Gold (`#FFB84D`), and White-Hot Core (`#FFF2B2`) in the glowing hull portholes, cannon bores, captain's single uncovered magma eye, bowsprit figurehead eyes, and crow's-nest mast lantern.
- **Key 3D Recessed / Angled Features:**
  - **4 Articulated Crab-Anchor Legs:** `leg_left` & `leg_right` (front pair) + `leg_back_left` & `leg_back_right` (rear pair), each with upper timber-iron femur + angled curved anchor-claw tibia.
  - **Galleon Hull & Bowsprit:** Stepped flared ship hull with raised quarterdeck stern, glowing porthole row, and a `25°` upward-angled bowsprit + skull figurehead at the bow.
  - **Broadside Cannons (`arm_left`, `arm_right`):** Heavy banded iron ship cannons on wooden carriages flanking the port & starboard gunwales, recoiling on attack and tilting upward during `telegraph`.
  - **Helm & Mast (`head`, `mast`):** Large spoked wooden ship's wheel behind a wide **Tricorne-hatted Pirate Skull** (with diagonal **black eyepatch** + glowing magma eye), backed by a tall mast flying a **Jolly-Roger Skull & Crossbones sail** and crow's-nest lantern.
- **Required Animations (`captain_brimstone.animation.json`):** `idle` (hull bobs, sail sways, cannons breathe), `walk` (4-leg crab scuttle), `attack` (cannon recoil + bowsprit lunge), `telegraph` (2.0 s / 40-tick windup aiming cannons skyward & rocking hull).
