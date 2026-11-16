package com.silkpanda.artilian.gametest.security;

import com.mojang.authlib.GameProfile;
import com.silkpanda.artilian.ArtilianMod;
import com.silkpanda.artilian.chester.ChesterEntity;
import com.silkpanda.artilian.gametest.ChesterTestSupport;
import com.silkpanda.artilian.pet.CapturedPetData;
import com.silkpanda.artilian.pet.CrystalFoxEntity;
import com.silkpanda.artilian.pet.PetRosterData;
import com.silkpanda.artilian.pet.PetService;
import com.silkpanda.artilian.registry.ArtilianDataComponents;
import com.silkpanda.artilian.registry.ArtilianEntities;
import com.silkpanda.artilian.registry.ArtilianItems;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.animal.Wolf;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.util.FakePlayer;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Ownership, non-lethal fainting, and anti-duplication probes for Artilian pets,
 * indexed in security/pets/README.md. Each probe was red-proven (EXPLOITED) with its
 * control disabled and HELD with it enabled.
 */
@GameTestHolder(ArtilianMod.MODID)
@PrefixGameTestTemplate(false)
public class PetSecurityProbes {

    /** P2-01: a non-owner tries to capture, sit, heal, or recall another player's pet/crystal. */
    @GameTest(template = "tnt_arena")
    public static void p2_01_nonOwnerCannotCaptureStealOrRecallOthersPet(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        ServerPlayer alex = ChesterTestSupport.playerInTestArea(helper, "AlexOwner");
        ServerPlayer steve = ChesterTestSupport.playerInTestArea(helper, "SteveIntruder");

        CrystalFoxEntity wildFox = spawnFox(helper, new BlockPos(7, 1, 7));
        wildFox.addAffinity(100);

        ItemStack alexsCrystal = new ItemStack(ArtilianItems.CAPTURE_CRYSTAL.get());
        alex.setItemInHand(InteractionHand.MAIN_HAND, alexsCrystal);
        alex.interactOn(wildFox, InteractionHand.MAIN_HAND);
        alexsCrystal.use(level, alex, InteractionHand.MAIN_HAND);

        CrystalFoxEntity alexsFox = foxesOf(helper, alex).get(0);
        alexsFox.setHealth(14.0F);

        // 1. Steve tries to capture Alex's summoned fox with an empty crystal.
        ItemStack stevesEmpty = new ItemStack(ArtilianItems.CAPTURE_CRYSTAL.get());
        steve.setItemInHand(InteractionHand.MAIN_HAND, stevesEmpty);
        steve.interactOn(alexsFox, InteractionHand.MAIN_HAND);
        helper.assertTrue(alexsFox.isAlive() && !stevesEmpty.has(ArtilianDataComponents.CAPTURED_PET.get()),
                "EXPLOITED P2-01: a non-owner captured someone else's summoned pet");

        // 2. Steve tries to shift-right-click to sit Alex's fox.
        steve.setShiftKeyDown(true);
        steve.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
        steve.interactOn(alexsFox, InteractionHand.MAIN_HAND);
        helper.assertFalse(alexsFox.isSitting() || alexsFox.toggleSitting(steve),
                "EXPLOITED P2-01: a non-owner commanded someone else's pet to sit");

        // 3. Steve tries to feed a Biome Treat to Alex's injured fox.
        ItemStack stevesTreat = new ItemStack(ArtilianItems.BIOME_TREAT.get(), 1);
        steve.setShiftKeyDown(false);
        steve.setItemInHand(InteractionHand.MAIN_HAND, stevesTreat);
        steve.interactOn(alexsFox, InteractionHand.MAIN_HAND);
        helper.assertTrue(Math.abs(alexsFox.getHealth() - 14.0F) < 0.01F,
                "EXPLOITED P2-01: a non-owner interacted/healed someone else's pet");

        // 4. Steve holds Alex's filled Capture Crystal and tries to recall Alex's fox.
        steve.setItemInHand(InteractionHand.MAIN_HAND, alexsCrystal);
        alexsCrystal.use(level, steve, InteractionHand.MAIN_HAND);
        helper.assertTrue(alexsFox.isAlive() && alexsCrystal.get(ArtilianDataComponents.CAPTURED_PET.get()).summoned(),
                "EXPLOITED P2-01: a non-owner recalled or stole from another player's Capture Crystal");

        alexsFox.discard();
        helper.succeed();
    }

    /** P2-02: fatal combat/environmental damage must faint the pet safely back into its crystal, never kill it. */
    @GameTest(template = "tnt_arena", timeoutTicks = 140)
    public static void p2_02_nonLethalFaintingSavesPetIntoCrystalInsteadOfDying(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        ServerPlayer alex = ChesterTestSupport.playerInTestArea(helper, "AlexFaint");

        CrystalFoxEntity wildFox = spawnFox(helper, new BlockPos(7, 1, 7));
        wildFox.addAffinity(100);
        wildFox.setCustomName(Component.literal("Prism"));

        ItemStack crystal = new ItemStack(ArtilianItems.CAPTURE_CRYSTAL.get());
        alex.setItemInHand(InteractionHand.MAIN_HAND, crystal);
        alex.interactOn(wildFox, InteractionHand.MAIN_HAND);
        crystal.use(level, alex, InteractionHand.MAIN_HAND);
        alex.getInventory().setItem(1, crystal);

        CrystalFoxEntity fox = foxesOf(helper, alex).get(0);
        UUID petId = fox.getPetId().orElseThrow();

        // Hit with massive lethal mob damage (500 HP).
        Zombie attacker = EntityType.ZOMBIE.create(level);
        BlockPos attackerPos = helper.absolutePos(new BlockPos(8, 1, 7));
        attacker.moveTo(attackerPos.getX() + 0.5, attackerPos.getY(), attackerPos.getZ() + 0.5, 0.0F, 0.0F);
        level.addFreshEntity(attacker);
        fox.hurt(level.damageSources().mobAttack(attacker), 500.0F);
        attacker.discard();

        ItemStack heldCrystal = alex.getInventory().getItem(1);
        CapturedPetData afterFaint = heldCrystal.get(ArtilianDataComponents.CAPTURED_PET.get());
        helper.assertTrue(afterFaint != null && afterFaint.fainted() && afterFaint.petId().equals(petId),
                "EXPLOITED P2-02: lethal damage killed the pet instead of fainting it safely into its Capture Crystal");
        helper.assertTrue(afterFaint.customName().equals(Optional.of("Prism")),
                "EXPLOITED P2-02: fainted crystal lost pet custom name");

        // Attempting to summon immediately before the 100-tick rest ends must fail.
        alex.setItemInHand(InteractionHand.MAIN_HAND, heldCrystal);
        heldCrystal.use(level, alex, InteractionHand.MAIN_HAND);
        helper.assertTrue(foxesOf(helper, alex).isEmpty(),
                "Fainted pet should rest before re-summoning without a Biome Treat");

        // Player death preserves PetRosterData via copyOnDeath().
        ServerPlayer respawned = new FakePlayer(level, new GameProfile(alex.getUUID(), "AlexFaint"));
        respawned.restoreFrom(alex, false);
        helper.assertTrue(PetRosterData.get(respawned).getRecord(petId).map(PetRosterData.PetRecord::fainted).orElse(false),
                "EXPLOITED P2-02: PetRosterData did not survive owner death");

        // After the 100-tick rest expires, the pet summons healthy again.
        helper.runAfterDelay(105, () -> {
            heldCrystal.use(level, alex, InteractionHand.MAIN_HAND);
            List<? extends CrystalFoxEntity> recovered = foxesOf(helper, alex);
            helper.assertTrue(recovered.size() == 1,
                    "Expected fainted Crystal Fox to summon after rest period, found " + recovered.size());
            CrystalFoxEntity revived = recovered.get(0);
            helper.assertTrue(revived.getPetId().equals(Optional.of(petId))
                            && Math.abs(revived.getHealth() - revived.getMaxHealth()) < 0.01F
                            && "Prism".equals(revived.getCustomName() != null ? revived.getCustomName().getString() : null),
                    "Revived Crystal Fox must keep petId, full health, and custom name 'Prism'");
            revived.discard();
            helper.succeed();
        });
    }

    /** P2-03: a copied crystal or crystal stored away during fainting cannot duplicate an active pet. */
    @GameTest(template = "tnt_arena")
    public static void p2_03_clonedCrystalCannotDuplicateActivePet(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        ServerPlayer alex = ChesterTestSupport.playerInTestArea(helper, "AlexDup");

        CrystalFoxEntity wildFox = spawnFox(helper, new BlockPos(7, 1, 7));
        wildFox.addAffinity(100);

        ItemStack crystalA = new ItemStack(ArtilianItems.CAPTURE_CRYSTAL.get());
        alex.setItemInHand(InteractionHand.MAIN_HAND, crystalA);
        alex.interactOn(wildFox, InteractionHand.MAIN_HAND);

        ItemStack crystalB = crystalA.copy();
        UUID petId = crystalA.get(ArtilianDataComponents.CAPTURED_PET.get()).petId();

        // Summon with crystalA, then try to summon a duplicate with crystalB.
        crystalA.use(level, alex, InteractionHand.MAIN_HAND);
        helper.assertTrue(foxesOf(helper, alex).size() == 1, "Expected 1 active fox after crystalA summon");

        crystalB.use(level, alex, InteractionHand.MAIN_HAND);
        List<? extends CrystalFoxEntity> afterCloneUse = level.getEntities(ArtilianEntities.CRYSTAL_FOX.get(),
                fox -> fox.isAlive() && fox.getPetId().equals(Optional.of(petId)));
        helper.assertTrue(afterCloneUse.size() <= 1,
                "EXPLOITED P2-03: cloned Capture Crystal spawned " + afterCloneUse.size() + " pets for one petId");

        // Re-summon and faint while crystalA is NOT in inventory; crystalA must still sync to fainted when used.
        if (afterCloneUse.isEmpty()) {
            crystalA.use(level, alex, InteractionHand.MAIN_HAND);
        }
        CrystalFoxEntity active = foxesOf(helper, alex).get(0);
        alex.getInventory().clearContent();
        active.hurt(level.damageSources().generic(), 200.0F);
        helper.assertFalse(active.isAlive(), "Fainted fox should leave world");

        // Alex pulls crystalA back out of storage and tries to use it while the pet is resting.
        alex.setItemInHand(InteractionHand.MAIN_HAND, crystalA);
        crystalA.use(level, alex, InteractionHand.MAIN_HAND);
        helper.assertTrue(foxesOf(helper, alex).isEmpty()
                        && crystalA.get(ArtilianDataComponents.CAPTURED_PET.get()).fainted(),
                "EXPLOITED P2-03: out-of-inventory crystal bypassed faint rest and spawned a duplicate pet");

        helper.succeed();
    }

    /** P2-04: tamed pets must never attack Players, Chester, villagers, friendly golems, or allied/tamed pets. */
    @GameTest(template = "tnt_arena", timeoutTicks = 60)
    public static void p2_04_tamedPetNeverAttacksPlayersChesterVillagersOrAlliedPets(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        ServerPlayer alex = ChesterTestSupport.playerInTestArea(helper, "AlexSafe");
        ServerPlayer steve = ChesterTestSupport.playerInTestArea(helper, "SteveAlly");

        CrystalFoxEntity wildFox = spawnFox(helper, new BlockPos(7, 0, 7));
        wildFox.addAffinity(100);
        ItemStack crystal = new ItemStack(ArtilianItems.CAPTURE_CRYSTAL.get());
        alex.setItemInHand(InteractionHand.MAIN_HAND, crystal);
        alex.interactOn(wildFox, InteractionHand.MAIN_HAND);
        crystal.use(level, alex, InteractionHand.MAIN_HAND);

        CrystalFoxEntity fox = foxesOf(helper, alex).get(0);
        BlockPos around = helper.absolutePos(new BlockPos(8, 0, 7));
        fox.moveTo(around.getX() - 0.5, around.getY(), around.getZ() + 0.5, 0.0F, 0.0F);
        steve.moveTo(around.getX() + 0.5, around.getY(), around.getZ() + 0.5, 0.0F, 0.0F);

        ChesterEntity chester = ArtilianEntities.CHESTER.get().create(level);
        chester.setOwner(steve);
        chester.moveTo(around.getX() + 0.5, around.getY(), around.getZ() + 1.5, 0.0F, 0.0F);
        level.addFreshEntity(chester);

        Villager villager = EntityType.VILLAGER.create(level);
        villager.moveTo(around.getX() + 1.5, around.getY(), around.getZ() + 0.5, 0.0F, 0.0F);
        level.addFreshEntity(villager);

        IronGolem golem = EntityType.IRON_GOLEM.create(level);
        golem.moveTo(around.getX() + 0.5, around.getY(), around.getZ() - 0.5, 0.0F, 0.0F);
        level.addFreshEntity(golem);

        Wolf tamedWolf = EntityType.WOLF.create(level);
        tamedWolf.tame(steve);
        tamedWolf.moveTo(around.getX() + 1.5, around.getY(), around.getZ() - 0.5, 0.0F, 0.0F);
        level.addFreshEntity(tamedWolf);

        CrystalFoxEntity befriendedFox = spawnFox(helper, new BlockPos(9, 0, 8));
        befriendedFox.addAffinity(100);

        ArmorStand armorStand = EntityType.ARMOR_STAND.create(level);
        armorStand.moveTo(around.getX() + 1.5, around.getY(), around.getZ() + 1.5, 0.0F, 0.0F);
        level.addFreshEntity(armorStand);

        List<LivingEntity> protectedEntities = List.of(
                steve, chester, villager, golem, tamedWolf, befriendedFox, armorStand);

        // Even under owner-assist / owner-hurt provocation or direct doHurtTarget, all friendly entities stay safe.
        for (LivingEntity protectedEntity : protectedEntities) {
            helper.assertFalse(PetService.isValidBattleTarget(alex, protectedEntity),
                    "EXPLOITED P2-04: pet target filter allowed " + protectedEntity.getType().getDescriptionId());
            helper.assertFalse(fox.canAttack(protectedEntity),
                    "EXPLOITED P2-04: pet canAttack returned true for " + protectedEntity.getType().getDescriptionId());
            alex.setLastHurtByMob(protectedEntity);
            alex.setLastHurtMob(protectedEntity);
            helper.assertFalse(fox.doHurtTarget(protectedEntity),
                    "EXPLOITED P2-04: pet dealt battle damage to " + protectedEntity.getType().getDescriptionId());
        }

        // Also ensure autonomous goal scans over 15 ticks never acquire any of them.
        helper.runAfterDelay(15, () -> {
            helper.assertTrue(fox.getTarget() == null,
                    "EXPLOITED P2-04: pet acquired unexpected target " + fox.getTarget());
            for (LivingEntity protectedEntity : protectedEntities) {
                helper.assertTrue(protectedEntity.isAlive()
                                && protectedEntity.getHealth() >= protectedEntity.getMaxHealth() - 0.01F,
                        "EXPLOITED P2-04: protected entity took damage: "
                                + protectedEntity.getType().getDescriptionId());
            }
            chester.discard();
            villager.discard();
            golem.discard();
            tamedWolf.discard();
            befriendedFox.discard();
            armorStand.discard();
            fox.discard();
            helper.succeed();
        });
    }

    private static CrystalFoxEntity spawnFox(GameTestHelper helper, BlockPos rel) {
        CrystalFoxEntity fox = ArtilianEntities.CRYSTAL_FOX.get().create(helper.getLevel());
        BlockPos abs = helper.absolutePos(rel);
        fox.moveTo(abs.getX() + 0.5, abs.getY(), abs.getZ() + 0.5, 0.0F, 0.0F);
        helper.getLevel().addFreshEntity(fox);
        return fox;
    }

    private static List<? extends CrystalFoxEntity> foxesOf(GameTestHelper helper, ServerPlayer owner) {
        return helper.getLevel().getEntities(ArtilianEntities.CRYSTAL_FOX.get(),
                fox -> fox.isAlive() && fox.isOwner(owner));
    }
}
