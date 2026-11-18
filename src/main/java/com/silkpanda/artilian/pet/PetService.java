package com.silkpanda.artilian.pet;

import com.silkpanda.artilian.chester.ChesterEntity;
import com.silkpanda.artilian.registry.ArtilianDataComponents;
import com.silkpanda.artilian.registry.ArtilianItems;
import com.silkpanda.artilian.registry.ArtilianTags;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.AbstractGolem;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.UUID;

/**
 * Authoritative server-side rules for pet befriending, capture, summoning, recalling,
 * single-instance anti-duplication, and kid-safe non-lethal fainting.
 */
public final class PetService {
    public static final int TREAT_AFFINITY_GAIN = 50;
    public static final float TREAT_HEAL_AMOUNT = 6.0F;
    public static final float WEAKENED_CAPTURE_RATIO = 0.60F;
    public static final float WILD_CAPTURE_CHANCE = 0.60F;
    public static final int FAINT_RECOVERY_TICKS = 100;

    private PetService() {}

    /** Kid-safe control: tamed pets always faint safely back into their crystal instead of dying. */
    public static boolean isNonLethalFaintingEnabled() {
        return true;
    }

    /**
     * Kid-safe battle target rule: tamed pets never target Players, Chester, villagers, friendly
     * golems, armor stands, baby animals, or any tamed/befriended pets, and only engage hostile
     * enemies or {@code artilian:structure_enemies}.
     */
    public static boolean isValidBattleTarget(@Nullable Player owner, LivingEntity candidate) {
        if (!candidate.isAlive() || candidate.isSpectator() || candidate == owner) {
            return false;
        }
        if (candidate instanceof Player
                || candidate instanceof ChesterEntity
                || candidate instanceof ArmorStand
                || candidate instanceof AbstractVillager) {
            return false;
        }
        if (candidate instanceof AbstractGolem && !(candidate instanceof Enemy)) {
            return false;
        }
        if (candidate instanceof ArtilianPetEntity pet && (pet.isTamed() || pet.isBefriended())) {
            return false;
        }
        if (candidate instanceof OwnableEntity ownable && ownable.getOwnerUUID() != null) {
            return false;
        }
        if (candidate instanceof TamableAnimal tamable && tamable.isTame()) {
            return false;
        }
        if (candidate instanceof AbstractHorse horse && horse.isTamed()) {
            return false;
        }
        if (candidate instanceof Animal animal && animal.isBaby()) {
            return false;
        }
        return candidate instanceof Enemy || candidate.getType().is(ArtilianTags.EntityTypes.STRUCTURE_ENEMIES)
                || candidate.getType().is(ArtilianTags.EntityTypes.BOSSES);
    }

    /**
     * Autonomous scan filter: skips Creepers so pets never accidentally trigger a Creeper blast
     * next to the kids' base unless the player attacks it first.
     */
    public static boolean isValidAutonomousBattleTarget(@Nullable Player owner, LivingEntity candidate) {
        return !(candidate instanceof Creeper) && isValidBattleTarget(owner, candidate);
    }

    /** Executes an elemental melee hit with type effectiveness, particles, and signature perk. */
    public static boolean performElementalAttack(ArtilianPetEntity pet, LivingEntity target, ServerLevel level) {
        if (!isValidBattleTarget(pet.getOwnerInLevel(), target)) {
            return false;
        }
        PetElement attackElement = pet.attackElementAgainst(target);
        float baseDamage = (float) pet.getAttributeValue(Attributes.ATTACK_DAMAGE);
        float multiplier = attackElement.multiplierAgainst(target);
        float finalDamage = Math.max(1.0F, baseDamage * multiplier);

        boolean hit = target.hurt(level.damageSources().mobAttack(pet), finalDamage);
        if (!hit) {
            return false;
        }

        pet.triggerAttackAnim();
        level.sendParticles(attackElement.particle(), target.getX(), target.getY(0.6), target.getZ(),
                10, 0.25, 0.25, 0.25, 0.04);

        if (multiplier > PetElement.NEUTRAL) {
            level.sendParticles(ParticleTypes.CRIT, target.getX(), target.getY(0.75), target.getZ(),
                    8, 0.25, 0.25, 0.25, 0.08);
            level.playSound(null, target.getX(), target.getY(), target.getZ(),
                    SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.NEUTRAL, 0.85F, 1.55F);
        } else if (multiplier < PetElement.NEUTRAL) {
            level.playSound(null, target.getX(), target.getY(), target.getZ(),
                    SoundEvents.AMETHYST_BLOCK_HIT, SoundSource.NEUTRAL, 0.7F, 0.75F);
        } else {
            level.playSound(null, target.getX(), target.getY(), target.getZ(),
                    pet.getHappySound(), SoundSource.NEUTRAL, 0.75F, 1.35F);
        }

        if (pet.tryConsumeSignatureCooldown()) {
            attackElement.applySignatureEffect(pet, target, level);
        }
        return true;
    }

    public static InteractionResult feedBiomeTreat(Player player, ArtilianPetEntity pet, ItemStack treatStack) {
        if (!(pet.level() instanceof ServerLevel serverLevel)) {
            return InteractionResult.SUCCESS;
        }
        if (pet.isTamed()) {
            if (!pet.isOwner(player)) {
                notifyNotOwner(player, pet);
                return InteractionResult.CONSUME;
            }
            if (!player.getAbilities().instabuild) {
                treatStack.shrink(1);
            }
            pet.heal(TREAT_HEAL_AMOUNT);
            pet.clearFire();
            syncPetSnapshotToOwner(player, pet);
            serverLevel.sendParticles(ParticleTypes.HEART, pet.getX(), pet.getY(0.85), pet.getZ(),
                    6, 0.25, 0.25, 0.25, 0.03);
            serverLevel.sendParticles(pet.getPrimaryElement().particle(), pet.getX(), pet.getY(0.5), pet.getZ(),
                    6, 0.25, 0.25, 0.25, 0.03);
            serverLevel.playSound(null, pet.getX(), pet.getY(), pet.getZ(),
                    SoundEvents.GENERIC_EAT, SoundSource.NEUTRAL, 0.8F, 1.15F);
            serverLevel.playSound(null, pet.getX(), pet.getY(), pet.getZ(),
                    pet.getHappySound(), SoundSource.NEUTRAL, 0.7F, 1.25F);
            player.displayClientMessage(Component.translatable("message.artilian.pet.treat_healed",
                    pet.getDisplayName()).withStyle(ChatFormatting.GREEN), true);
            return InteractionResult.CONSUME;
        }

        if (!player.getAbilities().instabuild) {
            treatStack.shrink(1);
        }
        pet.addAffinity(TREAT_AFFINITY_GAIN);
        pet.heal(4.0F);
        serverLevel.sendParticles(ParticleTypes.HEART, pet.getX(), pet.getY(0.85), pet.getZ(),
                pet.isBefriended() ? 10 : 5, 0.3, 0.3, 0.3, 0.03);
        serverLevel.playSound(null, pet.getX(), pet.getY(), pet.getZ(),
                SoundEvents.GENERIC_EAT, SoundSource.NEUTRAL, 0.8F, 1.1F);
        serverLevel.playSound(null, pet.getX(), pet.getY(), pet.getZ(),
                SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.NEUTRAL, 0.8F, 1.3F);

        if (pet.isBefriended()) {
            serverLevel.sendParticles(pet.getPrimaryElement().particle(), pet.getX(), pet.getY(0.6), pet.getZ(),
                    12, 0.35, 0.35, 0.35, 0.04);
            player.displayClientMessage(Component.translatable("message.artilian.pet.befriended",
                    pet.getDisplayName()).withStyle(ChatFormatting.AQUA), true);
        } else {
            player.displayClientMessage(Component.translatable("message.artilian.pet.affinity",
                    pet.getDisplayName(), pet.getAffinity()).withStyle(ChatFormatting.AQUA), true);
        }
        return InteractionResult.CONSUME;
    }

    public static InteractionResult tryCapturePet(Player player, ArtilianPetEntity pet, ItemStack crystalStack) {
        if (!(pet.level() instanceof ServerLevel serverLevel)) {
            return InteractionResult.SUCCESS;
        }
        if (crystalStack.has(ArtilianDataComponents.CAPTURED_PET.get())) {
            player.displayClientMessage(Component.translatable("message.artilian.crystal.already_full")
                    .withStyle(ChatFormatting.GOLD), true);
            serverLevel.playSound(null, pet.getX(), pet.getY(), pet.getZ(),
                    SoundEvents.NOTE_BLOCK_BASS.value(), SoundSource.PLAYERS, 0.7F, 0.7F);
            return InteractionResult.CONSUME;
        }
        if (pet.isTamed()) {
            if (!pet.isOwner(player)) {
                notifyNotOwner(player, pet);
            } else {
                player.displayClientMessage(Component.translatable("message.artilian.crystal.use_bound_crystal",
                        pet.getDisplayName()).withStyle(ChatFormatting.AQUA), true);
                serverLevel.playSound(null, pet.getX(), pet.getY(), pet.getZ(),
                        SoundEvents.AMETHYST_BLOCK_HIT, SoundSource.PLAYERS, 0.7F, 1.0F);
            }
            return InteractionResult.CONSUME;
        }

        boolean guaranteed = pet.isBefriended()
                || pet.getHealth() <= pet.getMaxHealth() * WEAKENED_CAPTURE_RATIO;
        boolean captured = guaranteed || pet.getRandom().nextFloat() < WILD_CAPTURE_CHANCE;
        if (!captured) {
            serverLevel.sendParticles(ParticleTypes.POOF, pet.getX(), pet.getY(0.6), pet.getZ(),
                    8, 0.25, 0.25, 0.25, 0.03);
            serverLevel.playSound(null, pet.getX(), pet.getY(), pet.getZ(),
                    SoundEvents.AMETHYST_BLOCK_HIT, SoundSource.PLAYERS, 0.8F, 0.85F);
            player.displayClientMessage(Component.translatable("message.artilian.crystal.almost",
                    pet.getDisplayName()).withStyle(ChatFormatting.YELLOW), true);
            return InteractionResult.CONSUME;
        }

        UUID petId = UUID.randomUUID();
        ResourceLocation entityTypeId = BuiltInRegistries.ENTITY_TYPE.getKey(pet.getType());
        Optional<String> customName = pet.hasCustomName()
                ? Optional.of(pet.getCustomName().getString())
                : Optional.empty();
        CapturedPetData capturedData = new CapturedPetData(
                petId,
                entityTypeId,
                pet.getPrimaryElement(),
                pet.getSecondaryElement(),
                player.getUUID(),
                player.getGameProfile().getName(),
                customName,
                pet.getHealth(),
                pet.getMaxHealth(),
                false,
                0L,
                false,
                Optional.empty()
        );

        PetRosterData.get(player).putRecord(petId, new PetRosterData.PetRecord(
                null, false, 0L, pet.getHealth(), pet.getMaxHealth(), customName.orElse(null)));

        giveFilledCrystal(player, crystalStack, capturedData);

        serverLevel.sendParticles(pet.getPrimaryElement().particle(), pet.getX(), pet.getY(0.6), pet.getZ(),
                18, 0.35, 0.4, 0.35, 0.05);
        pet.getSecondaryElement().ifPresent(sec -> serverLevel.sendParticles(
                sec.particle(), pet.getX(), pet.getY(0.6), pet.getZ(), 10, 0.3, 0.3, 0.3, 0.04));
        serverLevel.playSound(null, pet.getX(), pet.getY(), pet.getZ(),
                SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 1.0F, 1.35F);
        serverLevel.playSound(null, pet.getX(), pet.getY(), pet.getZ(),
                SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.PLAYERS, 0.8F, 1.25F);
        player.displayClientMessage(Component.translatable("message.artilian.crystal.captured",
                pet.getDisplayName()).withStyle(ChatFormatting.AQUA), true);

        pet.discard();
        return InteractionResult.CONSUME;
    }

    public static InteractionResultHolder<ItemStack> useCaptureCrystal(
            ServerLevel level, Player player, InteractionHand hand, ItemStack crystalStack) {
        CapturedPetData rawData = crystalStack.get(ArtilianDataComponents.CAPTURED_PET.get());
        if (rawData == null) {
            player.displayClientMessage(Component.translatable("message.artilian.crystal.empty_hint")
                    .withStyle(ChatFormatting.GRAY), true);
            return InteractionResultHolder.pass(crystalStack);
        }

        if (!rawData.isOwnedBy(player.getUUID())) {
            player.displayClientMessage(Component.translatable("message.artilian.crystal.not_yours",
                    rawData.ownerName()).withStyle(ChatFormatting.RED), true);
            level.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.CHEST_LOCKED, SoundSource.PLAYERS, 0.6F, 1.0F);
            return InteractionResultHolder.fail(crystalStack);
        }

        PetRosterData roster = PetRosterData.get(player);
        CapturedPetData data = reconcileWithRoster(roster, crystalStack, rawData);

        // If currently summoned and alive anywhere on the server, recall it cleanly.
        if (data.activeEntityUuid().isPresent()) {
            ArtilianPetEntity loaded = findLoadedPet(level, data.activeEntityUuid().get());
            if (loaded != null && loaded.isAlive()) {
                recallPet(level, player, crystalStack, data, loaded);
                return InteractionResultHolder.consume(crystalStack);
            }
        }

        // If fainted, allow Biome Treat wake-up from the other hand or wait out the brief rest timer.
        if (data.fainted()) {
            InteractionHand otherHand = hand == InteractionHand.MAIN_HAND ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND;
            ItemStack otherStack = player.getItemInHand(otherHand);
            if (otherStack.is(ArtilianItems.BIOME_TREAT.get())) {
                if (!player.getAbilities().instabuild) {
                    otherStack.shrink(1);
                }
                CapturedPetData woken = data.asRecalled(data.maxHealth(), data.customName());
                crystalStack.set(ArtilianDataComponents.CAPTURED_PET.get(), woken);
                roster.putRecord(woken.petId(), new PetRosterData.PetRecord(
                        null, false, 0L, woken.maxHealth(), woken.maxHealth(), woken.customName().orElse(null)));
                summonPet(level, player, crystalStack, woken);
                return InteractionResultHolder.consume(crystalStack);
            }

            long remainingTicks = data.readyAtTick() - level.getGameTime();
            if (remainingTicks > 0) {
                long secondsLeft = Math.max(1L, (remainingTicks + 19L) / 20L);
                player.displayClientMessage(Component.translatable("message.artilian.crystal.resting",
                        displayNameFor(data), secondsLeft).withStyle(ChatFormatting.GOLD), true);
                level.playSound(null, player.getX(), player.getY(), player.getZ(),
                        SoundEvents.NOTE_BLOCK_CHIME.value(), SoundSource.PLAYERS, 0.6F, 0.75F);
                return InteractionResultHolder.fail(crystalStack);
            }
            data = data.asRecalled(data.maxHealth(), data.customName());
            crystalStack.set(ArtilianDataComponents.CAPTURED_PET.get(), data);
        }

        if (summonPet(level, player, crystalStack, data)) {
            return InteractionResultHolder.consume(crystalStack);
        }
        return InteractionResultHolder.fail(crystalStack);
    }

    /** Safely faints a tamed pet back into its Capture Crystal instead of letting it die. */
    public static void faintPet(ArtilianPetEntity pet) {
        if (!(pet.level() instanceof ServerLevel level) || !pet.isTamed() || pet.getPetId().isEmpty()) {
            return;
        }
        UUID petId = pet.getPetId().get();
        long readyAtTick = level.getGameTime() + FAINT_RECOVERY_TICKS;
        Optional<String> customName = pet.hasCustomName()
                ? Optional.of(pet.getCustomName().getString())
                : Optional.empty();

        level.sendParticles(ParticleTypes.POOF, pet.getX(), pet.getY(0.5), pet.getZ(),
                12, 0.3, 0.3, 0.3, 0.04);
        level.sendParticles(pet.getPrimaryElement().particle(), pet.getX(), pet.getY(0.6), pet.getZ(),
                12, 0.35, 0.35, 0.35, 0.04);
        level.playSound(null, pet.getX(), pet.getY(), pet.getZ(),
                SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.NEUTRAL, 0.9F, 0.8F);

        Player owner = findOwnerPlayer(level, pet);
        if (owner != null) {
            PetRosterData.get(owner).putRecord(petId, new PetRosterData.PetRecord(
                    null, true, readyAtTick, pet.getMaxHealth(), pet.getMaxHealth(), customName.orElse(null)));
            updateOwnersCrystals(owner, petId, existing -> existing.asFainted(readyAtTick, customName));
            owner.displayClientMessage(Component.translatable("message.artilian.pet.fainted",
                    pet.getDisplayName()).withStyle(ChatFormatting.GOLD), false);
        }
        pet.discard();
    }

    public static void notifyNotOwner(Player player, ArtilianPetEntity pet) {
        String owner = pet.getOwnerName().isEmpty() ? "another player" : pet.getOwnerName();
        player.displayClientMessage(Component.translatable("message.artilian.pet.not_yours",
                pet.getDisplayName(), owner).withStyle(ChatFormatting.RED), true);
        pet.level().playSound(null, pet.getX(), pet.getY(), pet.getZ(),
                SoundEvents.CHEST_LOCKED, SoundSource.PLAYERS, 0.6F, 1.0F);
    }

    private static boolean summonPet(
            ServerLevel level, Player player, ItemStack crystalStack, CapturedPetData data) {
        EntityType<?> type = BuiltInRegistries.ENTITY_TYPE.getOptional(data.entityType()).orElse(null);
        if (type == null) {
            return false;
        }
        Entity created = type.create(level);
        if (!(created instanceof ArtilianPetEntity pet)) {
            return false;
        }

        Vec3 spawnPos = findSafeSpawnPos(level, player);
        pet.moveTo(spawnPos.x, spawnPos.y, spawnPos.z, player.getYRot(), 0.0F);
        pet.setOwner(player);
        pet.setPetId(data.petId());
        pet.addAffinity(ArtilianPetEntity.MAX_AFFINITY);
        data.customName().ifPresent(name -> pet.setCustomName(Component.literal(name)));
        float restoreHealth = data.health() > 0.0F ? Math.min(data.health(), pet.getMaxHealth()) : pet.getMaxHealth();
        pet.setHealth(restoreHealth);

        if (!level.addFreshEntity(pet)) {
            return false;
        }

        CapturedPetData summonedData = data.asSummoned(pet.getUUID(), pet.getHealth(), data.customName());
        crystalStack.set(ArtilianDataComponents.CAPTURED_PET.get(), summonedData);
        PetRosterData.get(player).putRecord(data.petId(), new PetRosterData.PetRecord(
                pet.getUUID(), false, 0L, pet.getHealth(), pet.getMaxHealth(), data.customName().orElse(null)));

        level.sendParticles(pet.getPrimaryElement().particle(), pet.getX(), pet.getY(0.6), pet.getZ(),
                14, 0.35, 0.35, 0.35, 0.04);
        level.playSound(null, pet.getX(), pet.getY(), pet.getZ(),
                SoundEvents.AMETHYST_BLOCK_RESONATE, SoundSource.PLAYERS, 0.9F, 1.3F);
        player.displayClientMessage(Component.translatable("message.artilian.crystal.summoned",
                pet.getDisplayName()).withStyle(ChatFormatting.AQUA), true);
        return true;
    }

    private static void recallPet(
            ServerLevel level, Player player, ItemStack crystalStack, CapturedPetData data, ArtilianPetEntity pet) {
        Optional<String> updatedName = pet.hasCustomName()
                ? Optional.of(pet.getCustomName().getString())
                : Optional.empty();
        float currentHealth = Math.max(1.0F, pet.getHealth());
        CapturedPetData recalled = data.asRecalled(currentHealth, updatedName);

        crystalStack.set(ArtilianDataComponents.CAPTURED_PET.get(), recalled);
        PetRosterData.get(player).putRecord(data.petId(), new PetRosterData.PetRecord(
                null, false, 0L, currentHealth, pet.getMaxHealth(), updatedName.orElse(null)));

        level.sendParticles(pet.getPrimaryElement().particle(), pet.getX(), pet.getY(0.6), pet.getZ(),
                12, 0.3, 0.3, 0.3, 0.04);
        level.playSound(null, pet.getX(), pet.getY(), pet.getZ(),
                SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 0.9F, 1.1F);
        player.displayClientMessage(Component.translatable("message.artilian.crystal.recalled",
                pet.getDisplayName()).withStyle(ChatFormatting.AQUA), true);
        pet.discard();
    }

    private static CapturedPetData reconcileWithRoster(
            PetRosterData roster, ItemStack crystalStack, CapturedPetData data) {
        Optional<PetRosterData.PetRecord> existing = roster.getRecord(data.petId());
        if (existing.isEmpty()) {
            roster.putRecord(data.petId(), new PetRosterData.PetRecord(
                    data.activeEntityUuid().orElse(null),
                    data.fainted(),
                    data.readyAtTick(),
                    data.health(),
                    data.maxHealth(),
                    data.customName().orElse(null)
            ));
            return data;
        }
        PetRosterData.PetRecord record = existing.get();
        CapturedPetData synced = new CapturedPetData(
                data.petId(),
                data.entityType(),
                data.primaryElement(),
                data.secondaryElement(),
                data.ownerUuid(),
                data.ownerName(),
                Optional.ofNullable(record.customName()),
                record.health(),
                record.maxHealth(),
                record.fainted(),
                record.readyAtTick(),
                record.isSummoned(),
                Optional.ofNullable(record.activeEntityUuid())
        );
        if (!synced.equals(data)) {
            crystalStack.set(ArtilianDataComponents.CAPTURED_PET.get(), synced);
        }
        return synced;
    }

    private static void syncPetSnapshotToOwner(Player owner, ArtilianPetEntity pet) {
        if (pet.getPetId().isEmpty()) {
            return;
        }
        UUID petId = pet.getPetId().get();
        Optional<String> updatedName = pet.hasCustomName()
                ? Optional.of(pet.getCustomName().getString())
                : Optional.empty();
        PetRosterData.get(owner).putRecord(petId, new PetRosterData.PetRecord(
                pet.getUUID(), false, 0L, pet.getHealth(), pet.getMaxHealth(), updatedName.orElse(null)));
        updateOwnersCrystals(owner, petId, existing -> existing.asSummoned(pet.getUUID(), pet.getHealth(), updatedName));
    }

    private static void updateOwnersCrystals(
            Player owner, UUID petId, java.util.function.UnaryOperator<CapturedPetData> updater) {
        Inventory inventory = owner.getInventory();
        for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
            ItemStack stack = inventory.getItem(slot);
            if (!stack.is(ArtilianItems.CAPTURE_CRYSTAL.get())) {
                continue;
            }
            CapturedPetData data = stack.get(ArtilianDataComponents.CAPTURED_PET.get());
            if (data != null && data.petId().equals(petId)) {
                stack.set(ArtilianDataComponents.CAPTURED_PET.get(), updater.apply(data));
            }
        }
    }

    private static void giveFilledCrystal(Player player, ItemStack handCrystal, CapturedPetData capturedData) {
        if (handCrystal.getCount() == 1) {
            handCrystal.set(ArtilianDataComponents.CAPTURED_PET.get(), capturedData);
            return;
        }
        handCrystal.shrink(1);
        ItemStack filled = new ItemStack(ArtilianItems.CAPTURE_CRYSTAL.get());
        filled.set(ArtilianDataComponents.CAPTURED_PET.get(), capturedData);
        if (!player.getInventory().add(filled)) {
            player.drop(filled, false);
        }
    }

    @Nullable
    private static ArtilianPetEntity findLoadedPet(ServerLevel fallbackLevel, UUID entityUuid) {
        MinecraftServer server = fallbackLevel.getServer();
        if (server != null) {
            for (ServerLevel lvl : server.getAllLevels()) {
                Entity found = lvl.getEntity(entityUuid);
                if (found instanceof ArtilianPetEntity pet) {
                    return pet;
                }
            }
        } else {
            Entity found = fallbackLevel.getEntity(entityUuid);
            if (found instanceof ArtilianPetEntity pet) {
                return pet;
            }
        }
        return null;
    }

    @Nullable
    private static Player findOwnerPlayer(ServerLevel level, ArtilianPetEntity pet) {
        Optional<UUID> ownerId = pet.getOwnerId();
        if (ownerId.isEmpty()) {
            return null;
        }
        MinecraftServer server = level.getServer();
        if (server != null) {
            ServerPlayer online = server.getPlayerList().getPlayer(ownerId.get());
            if (online != null) {
                return online;
            }
        }
        Player inLevel = level.getPlayerByUUID(ownerId.get());
        if (inLevel != null) {
            return inLevel;
        }
        return pet.getCachedOwner();
    }

    private static Vec3 findSafeSpawnPos(Level level, Player player) {
        Vec3 forward = player.getLookAngle().multiply(1.0, 0.0, 1.0);
        if (forward.lengthSqr() > 1.0E-4) {
            forward = forward.normalize().scale(1.8);
        } else {
            forward = new Vec3(1.5, 0.0, 0.0);
        }
        BlockPos candidate = BlockPos.containing(player.getX() + forward.x, player.getY(), player.getZ() + forward.z);
        if (level.getBlockState(candidate.below()).isSolid() && level.isEmptyBlock(candidate)) {
            return new Vec3(candidate.getX() + 0.5, candidate.getY(), candidate.getZ() + 0.5);
        }
        return player.position().add(0.0, 0.1, 0.0);
    }

    private static Component displayNameFor(CapturedPetData data) {
        if (data.customName().isPresent() && !data.customName().get().isBlank()) {
            return Component.literal(data.customName().get());
        }
        EntityType<?> type = BuiltInRegistries.ENTITY_TYPE.get(data.entityType());
        return type.getDescription();
    }
}
