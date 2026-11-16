package com.silkpanda.artilian.pet;

import java.util.Optional;

/**
 * Implemented by pets and elemental enemies so battle matchup math works uniformly across
 * Artilian creatures and future Stage 5–6 roster mobs.
 */
public interface ElementalMob {
    PetElement getPrimaryElement();

    default Optional<PetElement> getSecondaryElement() {
        return Optional.empty();
    }
}
