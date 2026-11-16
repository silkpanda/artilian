package com.silkpanda.artilian.gametest;

import com.silkpanda.artilian.ArtilianMod;
import com.silkpanda.artilian.pet.CaptureCrystalItem;
import com.silkpanda.artilian.pet.CapturedPetData;
import com.silkpanda.artilian.pet.CinderCragEntity;
import com.silkpanda.artilian.pet.GalecornEntity;
import com.silkpanda.artilian.pet.PetElement;
import com.silkpanda.artilian.pet.ShadowWispEntity;
import com.silkpanda.artilian.pet.ZaphoxEntity;
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
import net.minecraft.world.entity.monster.Drowned;
import net.minecraft.world.entity.monster.Phantom;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SpawnEggItem;
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
        helper.assertTrue(wisp.getBbWidth() >= 1.3F && wisp.getBbHeight() >= 2.4F,
                "Shadow Wisp must be large (at least 1.3x2.4 blocks), got "
                        + wisp.getBbWidth() + "x" + wisp.getBbHeight());

        Registry<Biome> biomes = level.registryAccess().registryOrThrow(Registries.BIOME);
        Holder<Biome> starlight = biomes.getHolderOrThrow(ArtilianBiomes.STARLIGHT_HIGHLANDS);
        boolean inStarlightSpawns = starlight.value().getMobSettings().getMobs(MobCategory.CREATURE).unwrap()
                .stream().anyMatch(d -> d.type == ArtilianEntities.SHADOW_WISP.get());
        helper.assertTrue(inStarlightSpawns, "Starlight Highlands must include Shadow Wisp in CREATURE spawns");

        BlockPos spawnPos = helper.absolutePos(new BlockPos(7, 2, 7));
        level.setBlockAndUpdate(spawnPos, Blocks.AIR.defaultBlockState());
        level.setBlockAndUpdate(spawnPos.above(), Blocks.AIR.defaultBlockState());
        level.setBlockAndUpdate(spawnPos.above(2), Blocks.AIR.defaultBlockState());

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

    @GameTest(template = "tnt_arena")
    public static void zaphoxIsElectricAirImmuneToFallDamageAndSpawnsInHighlands(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        ZaphoxEntity zaphox = ArtilianEntities.ZAPHOX.get().create(level);
        helper.assertTrue(zaphox != null
                        && zaphox.getPrimaryElement() == PetElement.ELECTRIC
                        && zaphox.getSecondaryElement().equals(Optional.of(PetElement.AIR)),
                "Zaphox must be ELECTRIC / AIR dual-element");
        helper.assertTrue(zaphox.getBbWidth() >= 1.7F && zaphox.getBbHeight() >= 2.0F,
                "Zaphox must be elephant-sized (at least 1.7x2.0 blocks), got "
                        + zaphox.getBbWidth() + "x" + zaphox.getBbHeight());
        helper.assertFalse(zaphox.causeFallDamage(50.0F, 1.0F, level.damageSources().fall()),
                "Zaphox spreads its wings to fly and must take zero fall damage");

        Registry<Biome> biomes = level.registryAccess().registryOrThrow(Registries.BIOME);
        Holder<Biome> starlight = biomes.getHolderOrThrow(ArtilianBiomes.STARLIGHT_HIGHLANDS);
        boolean inStarlightSpawns = starlight.value().getMobSettings().getMobs(MobCategory.CREATURE).unwrap()
                .stream().anyMatch(d -> d.type == ArtilianEntities.ZAPHOX.get());
        helper.assertTrue(inStarlightSpawns, "Starlight Highlands must include Zaphox in CREATURE spawns");

        BlockPos spawnPos = helper.absolutePos(new BlockPos(7, 2, 7));
        level.setBlockAndUpdate(spawnPos, Blocks.AIR.defaultBlockState());
        level.setBlockAndUpdate(spawnPos.above(), Blocks.AIR.defaultBlockState());
        level.setBlockAndUpdate(spawnPos.above(2), Blocks.AIR.defaultBlockState());

        level.setBlockAndUpdate(spawnPos.below(), ArtilianBlocks.STARLIGHT_TURF.get().defaultBlockState());
        helper.assertTrue(SpawnPlacements.checkSpawnRules(
                        ArtilianEntities.ZAPHOX.get(), level, MobSpawnType.NATURAL, spawnPos, level.random),
                "Zaphox must be allowed to spawn on Starlight Turf");

        level.setBlockAndUpdate(spawnPos.below(), ArtilianBlocks.STARLIGHT_SLATE.get().defaultBlockState());
        helper.assertTrue(SpawnPlacements.checkSpawnRules(
                        ArtilianEntities.ZAPHOX.get(), level, MobSpawnType.NATURAL, spawnPos, level.random),
                "Zaphox must be allowed to spawn on Starlight Slate");

        level.setBlockAndUpdate(spawnPos.below(), Blocks.STONE.defaultBlockState());
        helper.assertFalse(SpawnPlacements.checkSpawnRules(
                        ArtilianEntities.ZAPHOX.get(), level, MobSpawnType.NATURAL, spawnPos, level.random),
                "Zaphox must not spawn on plain stone");

        helper.succeed();
    }

    @GameTest(template = "tnt_arena")
    public static void galecornIsAirNatureAndKnocksEnemiesBackWithWhirlwindTail(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        GalecornEntity galecorn = ArtilianEntities.GALECORN.get().create(level);
        helper.assertTrue(galecorn != null
                        && galecorn.getPrimaryElement() == PetElement.AIR
                        && galecorn.getSecondaryElement().equals(Optional.of(PetElement.NATURE)),
                "Galecorn must be AIR / NATURE dual-element");

        Registry<Biome> biomes = level.registryAccess().registryOrThrow(Registries.BIOME);
        Holder<Biome> ember = biomes.getHolderOrThrow(ArtilianBiomes.EMBERBLOOM_MEADOWS);
        Holder<Biome> woods = biomes.getHolderOrThrow(ArtilianBiomes.CRYSTAL_CANOPY_WOODS);
        boolean inEmber = ember.value().getMobSettings().getMobs(MobCategory.CREATURE).unwrap()
                .stream().anyMatch(d -> d.type == ArtilianEntities.GALECORN.get());
        boolean inWoods = woods.value().getMobSettings().getMobs(MobCategory.CREATURE).unwrap()
                .stream().anyMatch(d -> d.type == ArtilianEntities.GALECORN.get());
        helper.assertTrue(inEmber && inWoods,
                "Galecorn must spawn in Emberbloom Meadows and Crystal Canopy Woods");

        BlockPos galecornPos = helper.absolutePos(new BlockPos(6, 1, 7));
        galecorn.moveTo(galecornPos.getX() + 0.5, galecornPos.getY(), galecornPos.getZ() + 0.5, 0.0F, 0.0F);
        level.addFreshEntity(galecorn);

        Creeper creeper = EntityType.CREEPER.create(level);
        creeper.moveTo(galecornPos.getX() + 1.2, galecornPos.getY(), galecornPos.getZ() + 0.5, 0.0F, 0.0F);
        level.addFreshEntity(creeper);

        galecorn.doHurtTarget(creeper);
        helper.assertTrue(creeper.getDeltaMovement().x > 0.5,
                "Whirlwind tail must blow the enemy backward along X (> 0.5), got: " + creeper.getDeltaMovement().x);
        helper.assertTrue(creeper.getDeltaMovement().y > 0.3,
                "Whirlwind tail must lift the enemy upward into the air (> 0.3), got: " + creeper.getDeltaMovement().y);

        creeper.discard();
        galecorn.discard();
        helper.succeed();
    }

    @GameTest(template = "tnt_arena")
    public static void alicePetsCaptureBefriendSummonAndFaintSafelyIntoCrystal(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        ServerPlayer alice = ChesterTestSupport.playerInTestArea(helper, "AliceRoster");

        ZaphoxEntity wildZaphox = ArtilianEntities.ZAPHOX.get().create(level);
        BlockPos zaphoxPos = helper.absolutePos(new BlockPos(5, 1, 7));
        wildZaphox.moveTo(zaphoxPos.getX() + 0.5, zaphoxPos.getY(), zaphoxPos.getZ() + 0.5, 0.0F, 0.0F);
        level.addFreshEntity(wildZaphox);

        ItemStack treats = new ItemStack(ArtilianItems.BIOME_TREAT.get(), 4);
        alice.setItemInHand(InteractionHand.MAIN_HAND, treats);
        alice.interactOn(wildZaphox, InteractionHand.MAIN_HAND);
        alice.interactOn(wildZaphox, InteractionHand.MAIN_HAND);
        helper.assertTrue(wildZaphox.isBefriended(), "Two Biome Treats should befriend Zaphox");

        ItemStack zaphoxCrystal = new ItemStack(ArtilianItems.CAPTURE_CRYSTAL.get());
        alice.setItemInHand(InteractionHand.MAIN_HAND, zaphoxCrystal);
        alice.interactOn(wildZaphox, InteractionHand.MAIN_HAND);
        CapturedPetData zaphoxData = zaphoxCrystal.get(ArtilianDataComponents.CAPTURED_PET.get());
        helper.assertTrue(zaphoxData != null
                        && zaphoxData.primaryElement() == PetElement.ELECTRIC
                        && zaphoxData.secondaryElement().equals(Optional.of(PetElement.AIR))
                        && zaphoxData.entityType().equals(ArtilianEntities.ZAPHOX.getId()),
                "Capture Crystal should store Zaphox identity and ELECTRIC / AIR elements");

        GalecornEntity wildGalecorn = ArtilianEntities.GALECORN.get().create(level);
        BlockPos galecornPos = helper.absolutePos(new BlockPos(9, 1, 7));
        wildGalecorn.moveTo(galecornPos.getX() + 0.5, galecornPos.getY(), galecornPos.getZ() + 0.5, 0.0F, 0.0F);
        wildGalecorn.addAffinity(100);
        level.addFreshEntity(wildGalecorn);

        ItemStack galecornCrystal = new ItemStack(ArtilianItems.CAPTURE_CRYSTAL.get());
        alice.setItemInHand(InteractionHand.MAIN_HAND, galecornCrystal);
        alice.interactOn(wildGalecorn, InteractionHand.MAIN_HAND);
        CapturedPetData galecornData = galecornCrystal.get(ArtilianDataComponents.CAPTURED_PET.get());
        helper.assertTrue(galecornData != null
                        && galecornData.primaryElement() == PetElement.AIR
                        && galecornData.secondaryElement().equals(Optional.of(PetElement.NATURE))
                        && galecornData.entityType().equals(ArtilianEntities.GALECORN.getId()),
                "Capture Crystal should store Galecorn identity and AIR / NATURE elements");

        // Summon Galecorn and verify safe fainting back into crystal
        alice.setItemInHand(InteractionHand.MAIN_HAND, galecornCrystal);
        galecornCrystal.use(level, alice, InteractionHand.MAIN_HAND);
        List<? extends GalecornEntity> galecorns = level.getEntities(
                ArtilianEntities.GALECORN.get(), e -> e.isAlive() && e.isOwner(alice));
        helper.assertTrue(galecorns.size() == 1, "Expected 1 summoned Galecorn");
        GalecornEntity summonedGalecorn = galecorns.get(0);
        summonedGalecorn.hurt(level.damageSources().mobAttack(summonedGalecorn), 999.0F);

        helper.assertFalse(summonedGalecorn.isAlive(), "Fainted Galecorn should withdraw from the world");
        CapturedPetData faintedData = galecornCrystal.get(ArtilianDataComponents.CAPTURED_PET.get());
        helper.assertTrue(faintedData != null && faintedData.fainted()
                        && Math.abs(CaptureCrystalItem.getVisualState(galecornCrystal) - 0.25F) < 0.01F,
                "Fainted Galecorn must safely return to its crystal in fainted state");

        helper.succeed();
    }

    @GameTest(template = "tnt_arena")
    public static void alicePetsBattleWithElectricAndAirMatchupsAndSignaturePerks(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        ServerPlayer alice = ChesterTestSupport.playerInTestArea(helper, "AliceBattle");

        ZaphoxEntity zaphox = ArtilianEntities.ZAPHOX.get().create(level);
        BlockPos zaphoxPos = helper.absolutePos(new BlockPos(6, 1, 7));
        zaphox.moveTo(zaphoxPos.getX() + 0.5, zaphoxPos.getY(), zaphoxPos.getZ() + 0.5, 0.0F, 0.0F);
        zaphox.setOwner(alice);
        zaphox.addAffinity(100);
        level.addFreshEntity(zaphox);

        Drowned drowned = EntityType.DROWNED.create(level);
        drowned.moveTo(zaphoxPos.getX() + 1.2, zaphoxPos.getY(), zaphoxPos.getZ() + 0.5, 0.0F, 0.0F);
        level.addFreshEntity(drowned);
        float drownedBefore = drowned.getHealth();
        zaphox.doHurtTarget(drowned);
        float zaphoxDamage = drownedBefore - drowned.getHealth();
        // Base damage 5.0 * 1.5x (Electric vs Water Drowned) = 7.5 (7.38 after Drowned's 2 innate armor points)
        helper.assertTrue(zaphoxDamage >= 7.0F,
                "Zaphox (5.0 base) vs Water Drowned should deal super effective damage (> 7.0), got " + zaphoxDamage);
        helper.assertTrue(drowned.hasEffect(MobEffects.GLOWING),
                "Zaphox electric perk should make target glow");
        drowned.discard();

        GalecornEntity galecorn = ArtilianEntities.GALECORN.get().create(level);
        BlockPos galecornPos = helper.absolutePos(new BlockPos(9, 1, 7));
        galecorn.moveTo(galecornPos.getX() + 0.5, galecornPos.getY(), galecornPos.getZ() + 0.5, 0.0F, 0.0F);
        galecorn.setOwner(alice);
        galecorn.addAffinity(100);
        level.addFreshEntity(galecorn);

        Creeper creeper = EntityType.CREEPER.create(level);
        creeper.moveTo(galecornPos.getX() + 1.2, galecornPos.getY(), galecornPos.getZ() + 0.5, 0.0F, 0.0F);
        level.addFreshEntity(creeper);
        float creeperBefore = creeper.getHealth();
        galecorn.doHurtTarget(creeper);
        float galecornDamage = creeperBefore - creeper.getHealth();
        // Base damage 4.5 * 1.5x (Air vs Nature Creeper) = 6.75
        helper.assertTrue(Math.abs(galecornDamage - 6.75F) < 0.05F,
                "Galecorn (4.5 base) vs Nature Creeper should deal 6.75 (1.5x), got " + galecornDamage);

        helper.assertFalse(zaphox.canAttack(galecorn), "Tamed pets must never attack allied tamed pets");

        creeper.discard();
        zaphox.discard();
        galecorn.discard();
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void everyLivingEntityHasSpawnEgg(GameTestHelper helper) {
        for (var holder : ArtilianEntities.ENTITY_TYPES.getEntries()) {
            EntityType<?> type = holder.get();
            if (type == ArtilianEntities.PRIMED_TNT.get()) {
                continue;
            }
            helper.assertTrue(SpawnEggItem.byId(type) != null,
                    "Missing spawn egg for entity " + holder.getId());
        }
        helper.succeed();
    }
}
