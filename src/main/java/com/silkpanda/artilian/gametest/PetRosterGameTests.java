package com.silkpanda.artilian.gametest;

import com.silkpanda.artilian.ArtilianMod;
import com.silkpanda.artilian.pet.CaptureCrystalItem;
import com.silkpanda.artilian.pet.CapturedPetData;
import com.silkpanda.artilian.pet.CinderCragEntity;
import com.silkpanda.artilian.pet.PetElement;
import com.silkpanda.artilian.pet.ShadowWispEntity;
import com.silkpanda.artilian.registry.ArtilianBlocks;
import com.silkpanda.artilian.registry.ArtilianDataComponents;
import com.silkpanda.artilian.registry.ArtilianEntities;
import com.silkpanda.artilian.registry.ArtilianItems;
import com.silkpanda.artilian.worldgen.ArtilianBiomes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.Phantom;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.pathfinder.PathType;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import java.util.List;
import java.util.Optional;

/** Phase 2 St: Bob's Shadow Wisp & Cinder Crag roster, spawns, capture, fainting, and battle. */
@GameTestHolder(ArtilianMod.MODID)
@PrefixGameTestTemplate(false)
public class PetRosterGameTests {

    @GameTest(template = "tnt_arena")
    public static void shadowWispIsShadowTypeAndOnlySpawnsOnStarlightGround(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        ShadowWispEntity wisp = ArtilianEntities.SHADOW_WISP.get().create(level);
        helper.assertTrue(wisp != null && wisp.getPrimaryElement() == PetElement.SHADOW
                        && wisp.getSecondaryElement().isEmpty(),
                "Shadow Wisp must be pure SHADOW element");

        Registry<Biome> biomes = level.registryAccess().registryOrThrow(Registries.BIOME);
        Holder<Biome> starlight = biomes.getHolderOrThrow(ArtilianBiomes.STARLIGHT_HIGHLANDS);
        boolean inStarlightSpawns = starlight.value().getMobSettings().getMobs(MobCategory.CREATURE).unwrap()
                .stream().anyMatch(d -> d.type == ArtilianEntities.SHADOW_WISP.get());
        helper.assertTrue(inStarlightSpawns, "Starlight Highlands must include Shadow Wisp in CREATURE spawns");

        BlockPos spawnPos = helper.absolutePos(new BlockPos(7, 2, 7));
        level.setBlockAndUpdate(spawnPos, Blocks.AIR.defaultBlockState());
        level.setBlockAndUpdate(spawnPos.above(), Blocks.AIR.defaultBlockState());

        level.setBlockAndUpdate(spawnPos.below(), ArtilianBlocks.STARLIGHT_TURF.get().defaultBlockState());
        helper.assertTrue(SpawnPlacements.checkSpawnRules(
                        ArtilianEntities.SHADOW_WISP.get(), level, MobSpawnType.NATURAL, spawnPos, level.random),
                "Shadow Wisp must be allowed to spawn on Starlight Turf");

        level.setBlockAndUpdate(spawnPos.below(), ArtilianBlocks.STARLIGHT_SLATE.get().defaultBlockState());
        helper.assertTrue(SpawnPlacements.checkSpawnRules(
                        ArtilianEntities.SHADOW_WISP.get(), level, MobSpawnType.NATURAL, spawnPos, level.random),
                "Shadow Wisp must be allowed to spawn on Starlight Slate");

        level.setBlockAndUpdate(spawnPos.below(), Blocks.STONE.defaultBlockState());
        helper.assertFalse(SpawnPlacements.checkSpawnRules(
                        ArtilianEntities.SHADOW_WISP.get(), level, MobSpawnType.NATURAL, spawnPos, level.random),
                "Shadow Wisp must not spawn on plain Stone");

        helper.succeed();
    }

    @GameTest(template = "tnt_arena")
    public static void cinderCragIsFireEarthFireImmuneAndOnlySpawnsNearLava(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        CinderCragEntity crag = ArtilianEntities.CINDER_CRAG.get().create(level);
        helper.assertTrue(crag != null
                        && crag.getPrimaryElement() == PetElement.FIRE
                        && crag.getSecondaryElement().equals(Optional.of(PetElement.EARTH)),
                "Cinder Crag must be FIRE / EARTH dual-element");
        helper.assertTrue(crag.fireImmune(), "Cinder Crag must be fire and lava immune");
        helper.assertTrue(crag.getPathfindingMalus(PathType.LAVA) == 0.0F,
                "Cinder Crag should not fear lava pathfinding");

        Registry<Biome> biomes = level.registryAccess().registryOrThrow(Registries.BIOME);
        Holder<Biome> plains = biomes.getHolderOrThrow(Biomes.PLAINS);
        Holder<Biome> nether = biomes.getHolderOrThrow(Biomes.NETHER_WASTES);
        boolean inOverworld = plains.value().getMobSettings().getMobs(MobCategory.CREATURE).unwrap()
                .stream().anyMatch(d -> d.type == ArtilianEntities.CINDER_CRAG.get());
        boolean inNether = nether.value().getMobSettings().getMobs(MobCategory.CREATURE).unwrap()
                .stream().anyMatch(d -> d.type == ArtilianEntities.CINDER_CRAG.get());
        helper.assertTrue(inOverworld && inNether,
                "Cinder Crag biome modifiers must inject spawns across Overworld and Nether biomes");

        BlockPos spawnPos = helper.absolutePos(new BlockPos(7, 2, 7));
        level.setBlockAndUpdate(spawnPos.below(), Blocks.STONE.defaultBlockState());
        level.setBlockAndUpdate(spawnPos, Blocks.AIR.defaultBlockState());
        level.setBlockAndUpdate(spawnPos.above(), Blocks.AIR.defaultBlockState());
        helper.assertFalse(SpawnPlacements.checkSpawnRules(
                        ArtilianEntities.CINDER_CRAG.get(), level, MobSpawnType.NATURAL, spawnPos, level.random),
                "Cinder Crag must NOT spawn without nearby lava");

        BlockPos lavaPos = helper.absolutePos(new BlockPos(10, 1, 7));
        level.setBlockAndUpdate(lavaPos, Blocks.LAVA.defaultBlockState());
        helper.assertTrue(SpawnPlacements.checkSpawnRules(
                        ArtilianEntities.CINDER_CRAG.get(), level, MobSpawnType.NATURAL, spawnPos, level.random),
                "Cinder Crag must spawn on solid ground when lava is nearby");
        level.setBlockAndUpdate(lavaPos, Blocks.AIR.defaultBlockState());

        // Also confirm fire/lava damage is ignored
        level.addFreshEntity(crag);
        float before = crag.getHealth();
        crag.hurt(level.damageSources().lava(), 6.0F);
        helper.assertTrue(crag.getHealth() == before, "Cinder Crag must take 0 damage from lava");
        crag.discard();

        helper.succeed();
    }

    @GameTest(template = "tnt_arena")
    public static void bobPetsCaptureBefriendSummonAndFaintSafelyIntoCrystal(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        ServerPlayer bob = ChesterTestSupport.playerInTestArea(helper, "BobRoster");

        ShadowWispEntity wildWisp = ArtilianEntities.SHADOW_WISP.get().create(level);
        BlockPos wispPos = helper.absolutePos(new BlockPos(5, 1, 7));
        wildWisp.moveTo(wispPos.getX() + 0.5, wispPos.getY(), wispPos.getZ() + 0.5, 0.0F, 0.0F);
        level.addFreshEntity(wildWisp);

        ItemStack treats = new ItemStack(ArtilianItems.BIOME_TREAT.get(), 4);
        bob.setItemInHand(InteractionHand.MAIN_HAND, treats);
        bob.interactOn(wildWisp, InteractionHand.MAIN_HAND);
        bob.interactOn(wildWisp, InteractionHand.MAIN_HAND);
        helper.assertTrue(wildWisp.isBefriended(), "Two Biome Treats should befriend Shadow Wisp");

        ItemStack wispCrystal = new ItemStack(ArtilianItems.CAPTURE_CRYSTAL.get());
        bob.setItemInHand(InteractionHand.MAIN_HAND, wispCrystal);
        bob.interactOn(wildWisp, InteractionHand.MAIN_HAND);
        CapturedPetData wispData = wispCrystal.get(ArtilianDataComponents.CAPTURED_PET.get());
        helper.assertTrue(wispData != null && wispData.primaryElement() == PetElement.SHADOW
                        && wispData.entityType().equals(ArtilianEntities.SHADOW_WISP.getId()),
                "Capture Crystal should store Shadow Wisp identity and SHADOW element");

        CinderCragEntity wildCrag = ArtilianEntities.CINDER_CRAG.get().create(level);
        BlockPos cragPos = helper.absolutePos(new BlockPos(9, 1, 7));
        wildCrag.moveTo(cragPos.getX() + 0.5, cragPos.getY(), cragPos.getZ() + 0.5, 0.0F, 0.0F);
        wildCrag.addAffinity(100);
        level.addFreshEntity(wildCrag);

        ItemStack cragCrystal = new ItemStack(ArtilianItems.CAPTURE_CRYSTAL.get());
        bob.setItemInHand(InteractionHand.MAIN_HAND, cragCrystal);
        bob.interactOn(wildCrag, InteractionHand.MAIN_HAND);
        CapturedPetData cragData = cragCrystal.get(ArtilianDataComponents.CAPTURED_PET.get());
        helper.assertTrue(cragData != null && cragData.primaryElement() == PetElement.FIRE
                        && cragData.secondaryElement().equals(Optional.of(PetElement.EARTH)),
                "Capture Crystal should store Cinder Crag FIRE / EARTH elements");

        // Summon Cinder Crag and verify non-lethal fainting into crystal
        bob.setItemInHand(InteractionHand.MAIN_HAND, cragCrystal);
        cragCrystal.use(level, bob, InteractionHand.MAIN_HAND);
        List<? extends CinderCragEntity> crags = level.getEntities(
                ArtilianEntities.CINDER_CRAG.get(), e -> e.isAlive() && e.isOwner(bob));
        helper.assertTrue(crags.size() == 1, "Expected 1 summoned Cinder Crag");
        CinderCragEntity summonedCrag = crags.get(0);
        summonedCrag.hurt(level.damageSources().mobAttack(summonedCrag), 999.0F);

        helper.assertFalse(summonedCrag.isAlive(), "Fainted Cinder Crag should withdraw from the world");
        CapturedPetData faintedData = cragCrystal.get(ArtilianDataComponents.CAPTURED_PET.get());
        helper.assertTrue(faintedData != null && faintedData.fainted()
                        && Math.abs(CaptureCrystalItem.getVisualState(cragCrystal) - 0.25F) < 0.01F,
                "Fainted Cinder Crag must safely return to its crystal in fainted state");

        helper.succeed();
    }

    @GameTest(template = "tnt_arena")
    public static void bobPetsBattleWithShadowAndFireElementalMatchupsAndPerks(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        ServerPlayer bob = ChesterTestSupport.playerInTestArea(helper, "BobBattle");

        ShadowWispEntity wisp = ArtilianEntities.SHADOW_WISP.get().create(level);
        BlockPos wispPos = helper.absolutePos(new BlockPos(6, 1, 7));
        wisp.moveTo(wispPos.getX() + 0.5, wispPos.getY(), wispPos.getZ() + 0.5, 0.0F, 0.0F);
        wisp.setOwner(bob);
        wisp.addAffinity(100);
        level.addFreshEntity(wisp);

        Phantom phantom = EntityType.PHANTOM.create(level);
        phantom.moveTo(wispPos.getX() + 1.2, wispPos.getY(), wispPos.getZ() + 0.5, 0.0F, 0.0F);
        level.addFreshEntity(phantom);
        float phantomBefore = phantom.getHealth();
        wisp.doHurtTarget(phantom);
        float wispDamage = phantomBefore - phantom.getHealth();
        helper.assertTrue(Math.abs(wispDamage - 6.75F) < 0.05F,
                "Shadow Wisp (4.5 base) vs Starlight Phantom should deal 6.75 (1.5x), got " + wispDamage);
        helper.assertTrue(phantom.hasEffect(MobEffects.BLINDNESS),
                "Shadow Wisp signature hit should apply Blindness");
        phantom.discard();

        CinderCragEntity crag = ArtilianEntities.CINDER_CRAG.get().create(level);
        BlockPos cragPos = helper.absolutePos(new BlockPos(9, 1, 7));
        crag.moveTo(cragPos.getX() + 0.5, cragPos.getY(), cragPos.getZ() + 0.5, 0.0F, 0.0F);
        crag.setOwner(bob);
        crag.addAffinity(100);
        level.addFreshEntity(crag);

        Creeper creeper = EntityType.CREEPER.create(level);
        creeper.moveTo(cragPos.getX() + 1.2, cragPos.getY(), cragPos.getZ() + 0.5, 0.0F, 0.0F);
        level.addFreshEntity(creeper);
        float creeperBefore = creeper.getHealth();
        crag.doHurtTarget(creeper);
        float cragDamage = creeperBefore - creeper.getHealth();
        helper.assertTrue(Math.abs(cragDamage - 7.5F) < 0.05F,
                "Cinder Crag (5.0 base) vs Nature Creeper should deal 7.5 (1.5x), got " + cragDamage);
        helper.assertTrue(creeper.isOnFire(), "Cinder Crag signature hit should ignite target");

        helper.assertFalse(wisp.canAttack(crag), "Tamed pets must never attack allied tamed pets");

        creeper.discard();
        wisp.discard();
        crag.discard();
        helper.succeed();
    }
}
