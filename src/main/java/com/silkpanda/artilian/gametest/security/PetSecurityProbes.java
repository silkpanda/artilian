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
        ServerPlayer alice = ChesterTestSupport.playerInTestArea(helper, "AliceOwner");
        ServerPlayer bob = ChesterTestSupport.playerInTestArea(helper, "BobIntruder");

        CrystalFoxEntity wildFox = spawnFox(helper, new BlockPos(7, 1, 7));
        wildFox.addAffinity(100);

        ItemStack alicesCrystal = new ItemStack(ArtilianItems.CAPTURE_CRYSTAL.get());
        alice.setItemInHand(InteractionHand.MAIN_HAND, alicesCrystal);
        alice.interactOn(wildFox, InteractionHand.MAIN_HAND);
        alicesCrystal.use(level, alice, InteractionHand.MAIN_HAND);

        CrystalFoxEntity alicesFox = foxesOf(helper, alice).get(0);
        alicesFox.setHealth(14.0F);

        // 1. Bob tries to capture Alice's summoned fox with an empty crystal.
        ItemStack bobsEmpty = new ItemStack(ArtilianItems.CAPTURE_CRYSTAL.get());
        bob.setItemInHand(InteractionHand.MAIN_HAND, bobsEmpty);
        bob.interactOn(alicesFox, InteractionHand.MAIN_HAND);
        helper.assertTrue(alicesFox.isAlive() && !bobsEmpty.has(ArtilianDataComponents.CAPTURED_PET.get()),
                "EXPLOITED P2-01: a non-owner captured someone else's summoned pet");

        // 2. Bob tries to shift-right-click to sit Alice's fox.
        bob.setShiftKeyDown(true);
        bob.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
        bob.interactOn(alicesFox, InteractionHand.MAIN_HAND);
        helper.assertFalse(alicesFox.isSitting() || alicesFox.toggleSitting(bob),
                "EXPLOITED P2-01: a non-owner commanded someone else's pet to sit");

        // 3. Bob tries to feed a Biome Treat to Alice's injured fox.
        ItemStack bobsTreat = new ItemStack(ArtilianItems.BIOME_TREAT.get(), 1);
        bob.setShiftKeyDown(false);
        bob.setItemInHand(InteractionHand.MAIN_HAND, bobsTreat);
        bob.interactOn(alicesFox, InteractionHand.MAIN_HAND);
        helper.assertTrue(Math.abs(alicesFox.getHealth() - 14.0F) < 0.01F,
                "EXPLOITED P2-01: a non-owner interacted/healed someone else's pet");

        // 4. Bob holds Alice's filled Capture Crystal and tries to recall Alice's fox.
        bob.setItemInHand(InteractionHand.MAIN_HAND, alicesCrystal);
        alicesCrystal.use(level, bob, InteractionHand.MAIN_HAND);
        helper.assertTrue(alicesFox.isAlive() && alicesCrystal.get(ArtilianDataComponents.CAPTURED_PET.get()).summoned(),
                "EXPLOITED P2-01: a non-owner recalled or stole from another player's Capture Crystal");

        alicesFox.discard();
        helper.succeed();
    }

    /** P2-02: fatal combat/environmental damage must faint the pet safely back into its crystal, never kill it. */
    @GameTest(template = "tnt_arena", timeoutTicks = 140)
    public static void p2_02_nonLethalFaintingSavesPetIntoCrystalInsteadOfDying(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        ServerPlayer alice = ChesterTestSupport.playerInTestArea(helper, "AliceFaint");

        CrystalFoxEntity wildFox = spawnFox(helper, new BlockPos(7, 1, 7));
        wildFox.addAffinity(100);
        wildFox.setCustomName(Component.literal("Prism"));

        ItemStack crystal = new ItemStack(ArtilianItems.CAPTURE_CRYSTAL.get());
        alice.setItemInHand(InteractionHand.MAIN_HAND, crystal);
        alice.interactOn(wildFox, InteractionHand.MAIN_HAND);
        crystal.use(level, alice, InteractionHand.MAIN_HAND);
        alice.getInventory().setItem(1, crystal);

        CrystalFoxEntity fox = foxesOf(helper, alice).get(0);
        UUID petId = fox.getPetId().orElseThrow();

        // Hit with massive lethal mob damage (500 HP).
        Zombie attacker = EntityType.ZOMBIE.create(level);
        BlockPos attackerPos = helper.absolutePos(new BlockPos(8, 1, 7));
        attacker.moveTo(attackerPos.getX() + 0.5, attackerPos.getY(), attackerPos.getZ() + 0.5, 0.0F, 0.0F);
        level.addFreshEntity(attacker);
        fox.hurt(level.damageSources().mobAttack(attacker), 500.0F);
        attacker.discard();

        ItemStack heldCrystal = alice.getInventory().getItem(1);
        CapturedPetData afterFaint = heldCrystal.get(ArtilianDataComponents.CAPTURED_PET.get());
        helper.assertTrue(afterFaint != null && afterFaint.fainted() && afterFaint.petId().equals(petId),
                "EXPLOITED P2-02: lethal damage killed the pet instead of fainting it safely into its Capture Crystal");
        helper.assertTrue(afterFaint.customName().equals(Optional.of("Prism")),
                "EXPLOITED P2-02: fainted crystal lost pet custom name");

        // Attempting to summon immediately before the 100-tick rest ends must fail.
        alice.setItemInHand(InteractionHand.MAIN_HAND, heldCrystal);
        heldCrystal.use(level, alice, InteractionHand.MAIN_HAND);
        helper.assertTrue(foxesOf(helper, alice).isEmpty(),
                "Fainted pet should rest before re-summoning without a Biome Treat");

        // Player death preserves PetRosterData via copyOnDeath().
        ServerPlayer respawned = new FakePlayer(level, new GameProfile(alice.getUUID(), "AliceFaint"));
        respawned.restoreFrom(alice, false);
        helper.assertTrue(PetRosterData.get(respawned).getRecord(petId).map(PetRosterData.PetRecord::fainted).orElse(false),
                "EXPLOITED P2-02: PetRosterData did not survive owner death");

        // After the 100-tick rest expires, the pet summons healthy again.
        helper.runAfterDelay(105, () -> {
            heldCrystal.use(level, alice, InteractionHand.MAIN_HAND);
            List<? extends CrystalFoxEntity> recovered = foxesOf(helper, alice);
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
        ServerPlayer alice = ChesterTestSupport.playerInTestArea(helper, "AliceDup");

        CrystalFoxEntity wildFox = spawnFox(helper, new BlockPos(7, 1, 7));
        wildFox.addAffinity(100);

        ItemStack crystalA = new ItemStack(ArtilianItems.CAPTURE_CRYSTAL.get());
        alice.setItemInHand(InteractionHand.MAIN_HAND, crystalA);
        alice.interactOn(wildFox, InteractionHand.MAIN_HAND);

        ItemStack crystalB = crystalA.copy();
        UUID petId = crystalA.get(ArtilianDataComponents.CAPTURED_PET.get()).petId();

        // Summon with crystalA, then try to summon a duplicate with crystalB.
        crystalA.use(level, alice, InteractionHand.MAIN_HAND);
        helper.assertTrue(foxesOf(helper, alice).size() == 1, "Expected 1 active fox after crystalA summon");

        crystalB.use(level, alice, InteractionHand.MAIN_HAND);
        List<? extends CrystalFoxEntity> afterCloneUse = level.getEntities(ArtilianEntities.CRYSTAL_FOX.get(),
                fox -> fox.isAlive() && fox.getPetId().equals(Optional.of(petId)));
        helper.assertTrue(afterCloneUse.size() <= 1,
                "EXPLOITED P2-03: cloned Capture Crystal spawned " + afterCloneUse.size() + " pets for one petId");

        // Re-summon and faint while crystalA is NOT in inventory; crystalA must still sync to fainted when used.
        if (afterCloneUse.isEmpty()) {
            crystalA.use(level, alice, InteractionHand.MAIN_HAND);
        }
        CrystalFoxEntity active = foxesOf(helper, alice).get(0);
        alice.getInventory().clearContent();
        active.hurt(level.damageSources().generic(), 200.0F);
        helper.assertFalse(active.isAlive(), "Fainted fox should leave world");

        // Alice pulls crystalA back out of storage and tries to use it while the pet is resting.
        alice.setItemInHand(InteractionHand.MAIN_HAND, crystalA);
        crystalA.use(level, alice, InteractionHand.MAIN_HAND);
        helper.assertTrue(foxesOf(helper, alice).isEmpty()
                        && crystalA.get(ArtilianDataComponents.CAPTURED_PET.get()).fainted(),
                "EXPLOITED P2-03: out-of-inventory crystal bypassed faint rest and spawned a duplicate pet");

        helper.succeed();
    }

    /** P2-04: tamed pets must never attack Players, Chester, villagers, friendly golems, or allied/tamed pets. */
    @GameTest(template = "tnt_arena", timeoutTicks = 60)
    public static void p2_04_tamedPetNeverAttacksPlayersChesterVillagersOrAlliedPets(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        ServerPlayer alice = ChesterTestSupport.playerInTestArea(helper, "AliceSafe");
        ServerPlayer bob = ChesterTestSupport.playerInTestArea(helper, "BobAlly");

        CrystalFoxEntity wildFox = spawnFox(helper, new BlockPos(7, 0, 7));
        wildFox.addAffinity(100);
        ItemStack crystal = new ItemStack(ArtilianItems.CAPTURE_CRYSTAL.get());
        alice.setItemInHand(InteractionHand.MAIN_HAND, crystal);
        alice.interactOn(wildFox, InteractionHand.MAIN_HAND);
        crystal.use(level, alice, InteractionHand.MAIN_HAND);

        CrystalFoxEntity fox = foxesOf(helper, alice).get(0);
        BlockPos around = helper.absolutePos(new BlockPos(8, 0, 7));
        fox.moveTo(around.getX() - 0.5, around.getY(), around.getZ() + 0.5, 0.0F, 0.0F);
        bob.moveTo(around.getX() + 0.5, around.getY(), around.getZ() + 0.5, 0.0F, 0.0F);

        ChesterEntity chester = ArtilianEntities.CHESTER.get().create(level);
        chester.setOwner(bob);
        chester.moveTo(around.getX() + 0.5, around.getY(), around.getZ() + 1.5, 0.0F, 0.0F);
        level.addFreshEntity(chester);

        Villager villager = EntityType.VILLAGER.create(level);
        villager.moveTo(around.getX() + 1.5, around.getY(), around.getZ() + 0.5, 0.0F, 0.0F);
        level.addFreshEntity(villager);

        IronGolem golem = EntityType.IRON_GOLEM.create(level);
        golem.moveTo(around.getX() + 0.5, around.getY(), around.getZ() - 0.5, 0.0F, 0.0F);
        level.addFreshEntity(golem);

        Wolf tamedWolf = EntityType.WOLF.create(level);
        tamedWolf.tame(bob);
        tamedWolf.moveTo(around.getX() + 1.5, around.getY(), around.getZ() - 0.5, 0.0F, 0.0F);
        level.addFreshEntity(tamedWolf);

        CrystalFoxEntity befriendedFox = spawnFox(helper, new BlockPos(9, 0, 8));
        befriendedFox.addAffinity(100);

        ArmorStand armorStand = EntityType.ARMOR_STAND.create(level);
        armorStand.moveTo(around.getX() + 1.5, around.getY(), around.getZ() + 1.5, 0.0F, 0.0F);
        level.addFreshEntity(armorStand);

        List<LivingEntity> protectedEntities = List.of(
                bob, chester, villager, golem, tamedWolf, befriendedFox, armorStand);

        // Even under owner-assist / owner-hurt provocation or direct doHurtTarget, all friendly entities stay safe.
        for (LivingEntity protectedEntity : protectedEntities) {
            helper.assertFalse(PetService.isValidBattleTarget(alice, protectedEntity),
                    "EXPLOITED P2-04: pet target filter allowed " + protectedEntity.getType().getDescriptionId());
            helper.assertFalse(fox.canAttack(protectedEntity),
                    "EXPLOITED P2-04: pet canAttack returned true for " + protectedEntity.getType().getDescriptionId());
            alice.setLastHurtByMob(protectedEntity);
            alice.setLastHurtMob(protectedEntity);
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
