# Captain Brimstone — Silhouette & Visual Reference Spec

- **Role:** Phase 3 — Stage 3 Boss (`artilian:captain_brimstone`), Fire / Shadow pirate captain of **Emberbloom Meadows** (`artilian:brimstone_bastion`).
- **Target Scale:** `2.0 × 3.4` block hitbox (`~3.6` blocks tall including tricorne hat & shoulder cannon-pauldrons; `visible_bounds_width: 4.2`, `visible_bounds_height: 4.2`).
- **Silhouette Inspiration:** Towering magma-forged dread-pirate captain — broad-shouldered charred-iron greatcoat with flared coat-tails, wide sweeping tricorne hat with glowing ember-gold trim and front skull-crest, hollowed furnace-rib chest cavity with a glowing magma heart core, heavy iron-cannon right gauntlet, and curved notched cutlass-blade left forearm.
- **Color Palette & Anchor:**
  - **Dark Structural Anchor:** Charred Nether-Iron & Deep Coal-Slate (`#181216` – `#42323A`) for the greatcoat, boots, hat body, and armor plates, with weathered Crimson-Oxide coat lining (`#521E24` – `#7A2B30`).
  - **Metallic Trim:** Sunsteel Gold (`#B87424` – `#FFE08A`) for pauldrons, belt buckle, coat buttons, hat trim, and cannon muzzle rings.
  - **Glowing Accents (`_glowmask.png`):** Blazing Magma Orange (`#FF6B1A`), Ember Gold (`#FFB84D`), and White-Hot Core (`#FFF2B2`) in the recessed eye sockets, furnace-rib chest core, cannon barrel bore, cutlass edge runes, and hat skull emblem.
- **Key 3D Recessed / Angled Features:**
  - **Tricorne Hat:** 3-part angled brim flaps (`front`, `left`, `right` pitched outward) + tall crown + front 3D Sunsteel skull/ember crest + trailing ember-feather plume.
  - **Head:** Recessed dark skull-visor face with 3D brow ridge, angled cheekbones/jaw, glowing magma eye sockets, and braided/chunky ash-grey & ember-tipped beard.
  - **Torso:** Tapered greatcoat torso with 3D flared coat-tails (`tasset_left`, `tasset_right`, `coat_back`), Sunsteel belt + oversized buckle, and a Warden-style hollowed chest furnace with 3D iron rib bars framing a recessed glowing magma core.
  - **Arms:** Asymmetric pirate silhouette — right arm (`arm_right`) ends in a heavy banded iron hand-cannon gauntlet with flared muzzle rim; left arm (`arm_left`) wields an integrated notched cutlass blade extending from the bracer.
  - **Legs:** Heavy cuffed sea-boots with Sunsteel knee-guards and toe-caps.
- **Required Animations (`captain_brimstone.animation.json`):** `idle`, `walk`, `attack`, `telegraph` (2.0 s / 40-tick windup raising cannon & cutlass).
