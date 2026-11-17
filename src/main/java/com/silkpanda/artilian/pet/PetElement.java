package com.silkpanda.artilian.pet;

import com.mojang.serialization.Codec;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.SnowGolem;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.monster.Blaze;
import net.minecraft.world.entity.monster.Bogged;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.Drowned;
import net.minecraft.world.entity.monster.ElderGuardian;
import net.minecraft.world.entity.monster.EnderMan;
import net.minecraft.world.entity.monster.Endermite;
import net.minecraft.world.entity.monster.Ghast;
import net.minecraft.world.entity.monster.Guardian;
import net.minecraft.world.entity.monster.Husk;
import net.minecraft.world.entity.monster.MagmaCube;
import net.minecraft.world.entity.monster.Phantom;
import net.minecraft.world.entity.monster.Shulker;
import net.minecraft.world.entity.monster.Silverfish;
import net.minecraft.world.entity.monster.Slime;
import net.minecraft.world.entity.monster.Stray;
import net.minecraft.world.entity.monster.Vex;
import net.minecraft.world.entity.monster.ZombifiedPiglin;
import net.minecraft.world.entity.monster.breeze.Breeze;
import net.minecraft.world.entity.monster.piglin.AbstractPiglin;
import net.minecraft.world.entity.monster.warden.Warden;

import java.util.EnumSet;
import java.util.Optional;
import java.util.Set;

/**
 * Expanded 11-element pet system combining classic adventure elements with Artilian's biomes and dimensions.
 * Pets carry a primary element and an optional secondary element, with intuitive 1.5x / 0.75x matchups.
 */
public enum PetElement implements StringRepresentable {
    FIRE("fire", 0xFF6B35, ParticleTypes.FLAME),
    WATER("water", 0x38BDF8, ParticleTypes.SPLASH),
    NATURE("nature", 0x4CD964, ParticleTypes.HAPPY_VILLAGER),
    ELECTRIC("electric", 0xFACC15, ParticleTypes.ELECTRIC_SPARK),
    ICE("ice", 0x7DF9FF, ParticleTypes.SNOWFLAKE),
    AIR("air", 0xE2E8F0, ParticleTypes.CLOUD),
    EARTH("earth", 0xD97706, ParticleTypes.WAX_OFF),
    CRYSTAL("crystal", 0x38D9C0, ParticleTypes.END_ROD),
    STARLIGHT("starlight", 0x8B7CFF, ParticleTypes.GLOW),
    SHADOW("shadow", 0xD946EF, ParticleTypes.PORTAL),
    DRAGON("dragon", 0x10B981, ParticleTypes.DRAGON_BREATH);

    public static final float SUPER_EFFECTIVE = 1.5F;
    public static final float NEUTRAL = 1.0F;
    public static final float RESISTED = 0.75F;

    public static final Codec<PetElement> CODEC = StringRepresentable.fromEnum(PetElement::values);
    public static final StreamCodec<FriendlyByteBuf, PetElement> STREAM_CODEC = StreamCodec.of(
            (buf, element) -> buf.writeEnum(element),
            buf -> buf.readEnum(PetElement.class)
    );

    private final String serializedName;
    private final int colorRgb;
    private final ParticleOptions particle;
    private Set<PetElement> strongAgainst = Set.of();
    private Set<PetElement> weakAgainst = Set.of();

    static {
        FIRE.matchups(EnumSet.of(NATURE, ICE, CRYSTAL), EnumSet.of(WATER, EARTH, DRAGON));
        WATER.matchups(EnumSet.of(FIRE, EARTH), EnumSet.of(NATURE, ELECTRIC));
        NATURE.matchups(EnumSet.of(WATER, EARTH), EnumSet.of(FIRE, ICE, AIR));
        ELECTRIC.matchups(EnumSet.of(WATER, AIR), EnumSet.of(EARTH, CRYSTAL));
        ICE.matchups(EnumSet.of(NATURE, AIR, DRAGON), EnumSet.of(FIRE, EARTH));
        AIR.matchups(EnumSet.of(NATURE), EnumSet.of(ELECTRIC, ICE, CRYSTAL));
        EARTH.matchups(EnumSet.of(FIRE, ELECTRIC, CRYSTAL), EnumSet.of(WATER, NATURE, ICE));
        CRYSTAL.matchups(EnumSet.of(ELECTRIC, AIR, SHADOW), EnumSet.of(EARTH, FIRE));
        STARLIGHT.matchups(EnumSet.of(SHADOW, DRAGON), EnumSet.of(CRYSTAL));
        SHADOW.matchups(EnumSet.of(STARLIGHT, NATURE), EnumSet.of(CRYSTAL, STARLIGHT));
        DRAGON.matchups(EnumSet.of(FIRE, WATER, NATURE, ELECTRIC), EnumSet.of(ICE, STARLIGHT, DRAGON));
    }

    PetElement(String serializedName, int colorRgb, ParticleOptions particle) {
        this.serializedName = serializedName;
        this.colorRgb = colorRgb;
        this.particle = particle;
    }

    private void matchups(Set<PetElement> strong, Set<PetElement> weak) {
        this.strongAgainst = Set.copyOf(strong);
        this.weakAgainst = Set.copyOf(weak);
    }

    @Override
    public String getSerializedName() {
        return this.serializedName;
    }

    public int colorRgb() {
        return this.colorRgb;
    }

    public ParticleOptions particle() {
        return this.particle;
    }

    public Set<PetElement> strongAgainst() {
        return this.strongAgainst;
    }

    public Set<PetElement> weakAgainst() {
        return this.weakAgainst;
    }

    public MutableComponent displayName() {
        return Component.translatable("element.artilian." + this.serializedName)
                .withStyle(Style.EMPTY.withColor(this.colorRgb));
    }

    /** Single-target effectiveness multiplier against {@code defender}. */
    public float multiplierAgainst(PetElement defender) {
        if (this.strongAgainst.contains(defender)) {
            return SUPER_EFFECTIVE;
        }
        if (this.weakAgainst.contains(defender)) {
            return RESISTED;
        }
        return NEUTRAL;
    }

    /**
     * Combined effectiveness multiplier against a pet with a primary and optional secondary element.
     * Clamped cleanly into {@code [0.5625, 2.25]} so dual weaknesses feel rewarding without instant one-shots.
     */
    public float multiplierAgainst(PetElement defenderPrimary, Optional<PetElement> defenderSecondary) {
        float primaryMult = this.multiplierAgainst(defenderPrimary);
        if (defenderSecondary.isEmpty() || defenderSecondary.get() == defenderPrimary) {
            return primaryMult;
        }
        return primaryMult * this.multiplierAgainst(defenderSecondary.get());
    }

    public record ElementPair(PetElement primary, Optional<PetElement> secondary) {}

    /** Resolves an entity's elemental typing for Artilian pets, custom elemental mobs, and vanilla monsters. */
    public static Optional<ElementPair> elementsOf(LivingEntity entity) {
        if (entity instanceof ElementalMob elemental) {
            if (elemental.isAllTypes()) {
                return Optional.empty();
            }
            return Optional.of(new ElementPair(elemental.getPrimaryElement(), elemental.getSecondaryElement()));
        }
        if (entity instanceof Blaze || entity instanceof MagmaCube || entity instanceof Ghast
                || entity instanceof AbstractPiglin || entity instanceof ZombifiedPiglin) {
            return Optional.of(new ElementPair(FIRE, Optional.empty()));
        }
        if (entity instanceof Drowned || entity instanceof Guardian || entity instanceof ElderGuardian) {
            return Optional.of(new ElementPair(WATER, Optional.empty()));
        }
        if (entity instanceof Creeper || entity instanceof Bogged || entity instanceof Slime) {
            return Optional.of(new ElementPair(NATURE, Optional.empty()));
        }
        if (entity instanceof Phantom) {
            return Optional.of(new ElementPair(STARLIGHT, Optional.empty()));
        }
        if (entity instanceof Stray || entity instanceof SnowGolem) {
            return Optional.of(new ElementPair(ICE, Optional.empty()));
        }
        if (entity instanceof Breeze || entity instanceof Vex) {
            return Optional.of(new ElementPair(AIR, Optional.empty()));
        }
        if (entity instanceof Husk || entity instanceof Silverfish) {
            return Optional.of(new ElementPair(EARTH, Optional.empty()));
        }
        if (entity instanceof EnderMan || entity instanceof Endermite || entity instanceof Shulker || entity instanceof Warden) {
            return Optional.of(new ElementPair(SHADOW, Optional.empty()));
        }
        if (entity instanceof EnderDragon) {
            return Optional.of(new ElementPair(DRAGON, Optional.empty()));
        }
        return Optional.empty();
    }

    /** Returns this element's multiplier against {@code defender}, or 1.0x when untyped. */
    public float multiplierAgainst(LivingEntity defender) {
        return elementsOf(defender)
                .map(pair -> this.multiplierAgainst(pair.primary(), pair.secondary()))
                .orElse(NEUTRAL);
    }

    /** Applies this element's readable signature combat perk when the pet's ability cooldown is ready. */
    public void applySignatureEffect(LivingEntity attacker, LivingEntity target, ServerLevel level) {
        switch (this) {
            case FIRE -> target.igniteForSeconds(3.0F);
            case WATER -> {
                target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 50, 0));
                attacker.clearFire();
            }
            case NATURE -> {
                attacker.heal(2.0F);
                level.sendParticles(ParticleTypes.HEART, attacker.getX(), attacker.getY(0.85), attacker.getZ(),
                        3, 0.2, 0.2, 0.2, 0.02);
            }
            case ELECTRIC -> {
                target.addEffect(new MobEffectInstance(MobEffects.GLOWING, 60, 0));
                target.hurt(level.damageSources().magic(), 1.5F);
            }
            case ICE -> target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 60, 1));
            case AIR -> {
                target.setDeltaMovement(target.getDeltaMovement().add(0.0, 0.32, 0.0));
                target.hasImpulse = true;
                target.hurtMarked = true;
            }
            case EARTH -> target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 60, 0));
            case CRYSTAL -> {
                target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 40, 0));
                target.addEffect(new MobEffectInstance(MobEffects.GLOWING, 60, 0));
            }
            case STARLIGHT -> {
                target.addEffect(new MobEffectInstance(MobEffects.GLOWING, 80, 0));
                attacker.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 60, 0));
            }
            case SHADOW -> target.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 40, 0));
            case DRAGON -> {
                target.igniteForSeconds(2.0F);
                target.addEffect(new MobEffectInstance(MobEffects.GLOWING, 60, 0));
            }
        }
    }

    public static MutableComponent formatBadge(PetElement primary, Optional<PetElement> secondary) {
        MutableComponent badge = primary.displayName();
        if (secondary.isPresent() && secondary.get() != primary) {
            badge = badge.append(Component.literal(" / ").withStyle(Style.EMPTY.withColor(0xB4A9C8)))
                    .append(secondary.get().displayName());
        }
        return badge;
    }
}
