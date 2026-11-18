package com.silkpanda.artilian.gametest;

import com.silkpanda.artilian.ArtilianMod;
import com.silkpanda.artilian.pet.ArtilianPetEntity;
import com.silkpanda.artilian.pet.CaptureCrystalItem;
import com.silkpanda.artilian.pet.CapturedPetData;
import com.silkpanda.artilian.pet.CinderCragEntity;
import com.silkpanda.artilian.pet.EmberDrakeEntity;
import com.silkpanda.artilian.pet.FlurrywingEntity;
import com.silkpanda.artilian.pet.GalecornEntity;
import com.silkpanda.artilian.pet.PeepkinEntity;
import com.silkpanda.artilian.pet.PrismlingEntity;
import com.silkpanda.artilian.pet.PetElement;
import com.silkpanda.artilian.pet.ShadowWispEntity;
import com.silkpanda.artilian.pet.SunbeamEntity;
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
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.Drowned;
import net.minecraft.world.entity.monster.Phantom;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LightBlock;
import net.minecraft.world.level.pathfinder.PathType;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/** Phase 2 St: Steve's Shadow Wisp & Cinder Crag roster, spawns, capture, fainting, and battle. */
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
    public static void stevePetsCaptureBefriendSummonAndFaintSafelyIntoCrystal(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        ServerPlayer steve = ChesterTestSupport.playerInTestArea(helper, "SteveRoster");

        ShadowWispEntity wildWisp = ArtilianEntities.SHADOW_WISP.get().create(level);
        BlockPos wispPos = helper.absolutePos(new BlockPos(5, 1, 7));
        wildWisp.moveTo(wispPos.getX() + 0.5, wispPos.getY(), wispPos.getZ() + 0.5, 0.0F, 0.0F);
        level.addFreshEntity(wildWisp);

        ItemStack treats = new ItemStack(ArtilianItems.BIOME_TREAT.get(), 4);
        steve.setItemInHand(InteractionHand.MAIN_HAND, treats);
        steve.interactOn(wildWisp, InteractionHand.MAIN_HAND);
        steve.interactOn(wildWisp, InteractionHand.MAIN_HAND);
        helper.assertTrue(wildWisp.isBefriended(), "Two Biome Treats should befriend Shadow Wisp");

        ItemStack wispCrystal = new ItemStack(ArtilianItems.CAPTURE_CRYSTAL.get());
        steve.setItemInHand(InteractionHand.MAIN_HAND, wispCrystal);
        steve.interactOn(wildWisp, InteractionHand.MAIN_HAND);
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
        steve.setItemInHand(InteractionHand.MAIN_HAND, cragCrystal);
        steve.interactOn(wildCrag, InteractionHand.MAIN_HAND);
        CapturedPetData cragData = cragCrystal.get(ArtilianDataComponents.CAPTURED_PET.get());
        helper.assertTrue(cragData != null && cragData.primaryElement() == PetElement.FIRE
                        && cragData.secondaryElement().equals(Optional.of(PetElement.EARTH)),
                "Capture Crystal should store Cinder Crag FIRE / EARTH elements");

        // Summon Cinder Crag and verify non-lethal fainting into crystal
        steve.setItemInHand(InteractionHand.MAIN_HAND, cragCrystal);
        cragCrystal.use(level, steve, InteractionHand.MAIN_HAND);
        List<? extends CinderCragEntity> crags = level.getEntities(
                ArtilianEntities.CINDER_CRAG.get(), e -> e.isAlive() && e.isOwner(steve));
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
    public static void stevePetsBattleWithShadowAndFireElementalMatchupsAndPerks(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        ServerPlayer steve = ChesterTestSupport.playerInTestArea(helper, "SteveBattle");

        ShadowWispEntity wisp = ArtilianEntities.SHADOW_WISP.get().create(level);
        BlockPos wispPos = helper.absolutePos(new BlockPos(6, 1, 7));
        wisp.moveTo(wispPos.getX() + 0.5, wispPos.getY(), wispPos.getZ() + 0.5, 0.0F, 0.0F);
        wisp.setOwner(steve);
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
        crag.setOwner(steve);
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
    public static void alexPetsCaptureBefriendSummonAndFaintSafelyIntoCrystal(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        ServerPlayer alex = ChesterTestSupport.playerInTestArea(helper, "AlexRoster");

        ZaphoxEntity wildZaphox = ArtilianEntities.ZAPHOX.get().create(level);
        BlockPos zaphoxPos = helper.absolutePos(new BlockPos(5, 1, 7));
        wildZaphox.moveTo(zaphoxPos.getX() + 0.5, zaphoxPos.getY(), zaphoxPos.getZ() + 0.5, 0.0F, 0.0F);
        level.addFreshEntity(wildZaphox);

        ItemStack treats = new ItemStack(ArtilianItems.BIOME_TREAT.get(), 4);
        alex.setItemInHand(InteractionHand.MAIN_HAND, treats);
        alex.interactOn(wildZaphox, InteractionHand.MAIN_HAND);
        alex.interactOn(wildZaphox, InteractionHand.MAIN_HAND);
        helper.assertTrue(wildZaphox.isBefriended(), "Two Biome Treats should befriend Zaphox");

        ItemStack zaphoxCrystal = new ItemStack(ArtilianItems.CAPTURE_CRYSTAL.get());
        alex.setItemInHand(InteractionHand.MAIN_HAND, zaphoxCrystal);
        alex.interactOn(wildZaphox, InteractionHand.MAIN_HAND);
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
        alex.setItemInHand(InteractionHand.MAIN_HAND, galecornCrystal);
        alex.interactOn(wildGalecorn, InteractionHand.MAIN_HAND);
        CapturedPetData galecornData = galecornCrystal.get(ArtilianDataComponents.CAPTURED_PET.get());
        helper.assertTrue(galecornData != null
                        && galecornData.primaryElement() == PetElement.AIR
                        && galecornData.secondaryElement().equals(Optional.of(PetElement.NATURE))
                        && galecornData.entityType().equals(ArtilianEntities.GALECORN.getId()),
                "Capture Crystal should store Galecorn identity and AIR / NATURE elements");

        // Summon Galecorn and verify safe fainting back into crystal
        alex.setItemInHand(InteractionHand.MAIN_HAND, galecornCrystal);
        galecornCrystal.use(level, alex, InteractionHand.MAIN_HAND);
        List<? extends GalecornEntity> galecorns = level.getEntities(
                ArtilianEntities.GALECORN.get(), e -> e.isAlive() && e.isOwner(alex));
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
    public static void alexPetsBattleWithElectricAndAirMatchupsAndSignaturePerks(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        ServerPlayer alex = ChesterTestSupport.playerInTestArea(helper, "AlexBattle");

        ZaphoxEntity zaphox = ArtilianEntities.ZAPHOX.get().create(level);
        BlockPos zaphoxPos = helper.absolutePos(new BlockPos(6, 1, 7));
        zaphox.moveTo(zaphoxPos.getX() + 0.5, zaphoxPos.getY(), zaphoxPos.getZ() + 0.5, 0.0F, 0.0F);
        zaphox.setOwner(alex);
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
        galecorn.setOwner(alex);
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

    @GameTest(template = "tnt_arena")
    public static void emberDrakeIsDragonFireImmuneToFireAndSpawnsInEmberbloomMeadows(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        EmberDrakeEntity drake = ArtilianEntities.EMBER_DRAKE.get().create(level);
        helper.assertTrue(drake != null
                        && drake.getPrimaryElement() == PetElement.DRAGON
                        && drake.getSecondaryElement().equals(Optional.of(PetElement.FIRE)),
                "Ember Drake must be DRAGON / FIRE dual-element");
        helper.assertTrue(drake.fireImmune(), "Ember Drake must be fire immune");
        helper.assertFalse(drake.causeFallDamage(30.0F, 1.0F, level.damageSources().fall()),
                "Winged Ember Drake hatchling must flutter safely with zero fall damage");

        Registry<Biome> biomes = level.registryAccess().registryOrThrow(Registries.BIOME);
        Holder<Biome> ember = biomes.getHolderOrThrow(ArtilianBiomes.EMBERBLOOM_MEADOWS);
        boolean inEmberSpawns = ember.value().getMobSettings().getMobs(MobCategory.CREATURE).unwrap()
                .stream().anyMatch(d -> d.type == ArtilianEntities.EMBER_DRAKE.get());
        helper.assertTrue(inEmberSpawns, "Emberbloom Meadows must include Ember Drake in CREATURE spawns");

        BlockPos spawnPos = helper.absolutePos(new BlockPos(7, 2, 7));
        level.setBlockAndUpdate(spawnPos, Blocks.AIR.defaultBlockState());
        level.setBlockAndUpdate(spawnPos.above(), Blocks.AIR.defaultBlockState());

        level.setBlockAndUpdate(spawnPos.below(), ArtilianBlocks.EMBER_TURF.get().defaultBlockState());
        helper.assertTrue(SpawnPlacements.checkSpawnRules(
                        ArtilianEntities.EMBER_DRAKE.get(), level, MobSpawnType.NATURAL, spawnPos, level.random),
                "Ember Drake must be allowed to spawn on Ember Turf");

        level.setBlockAndUpdate(spawnPos.below(), Blocks.STONE.defaultBlockState());
        helper.assertFalse(SpawnPlacements.checkSpawnRules(
                        ArtilianEntities.EMBER_DRAKE.get(), level, MobSpawnType.NATURAL, spawnPos, level.random),
                "Ember Drake must not spawn on plain Stone");

        level.addFreshEntity(drake);
        float before = drake.getHealth();
        drake.hurt(level.damageSources().lava(), 6.0F);
        helper.assertTrue(drake.getHealth() == before, "Ember Drake must take 0 damage from lava");
        drake.discard();

        helper.succeed();
    }

    @GameTest(template = "tnt_arena")
    public static void peepkinIsShadowStarlightFloatsSafelyAndSpawnsInHighlandsAndWoods(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        PeepkinEntity peepkin = ArtilianEntities.PEEPKIN.get().create(level);
        helper.assertTrue(peepkin != null
                        && peepkin.getPrimaryElement() == PetElement.SHADOW
                        && peepkin.getSecondaryElement().equals(Optional.of(PetElement.STARLIGHT)),
                "Peepkin must be SHADOW / STARLIGHT dual-element");
        helper.assertFalse(peepkin.causeFallDamage(40.0F, 1.0F, level.damageSources().fall()),
                "Floating Peepkin must take zero fall damage");

        Registry<Biome> biomes = level.registryAccess().registryOrThrow(Registries.BIOME);
        Holder<Biome> starlight = biomes.getHolderOrThrow(ArtilianBiomes.STARLIGHT_HIGHLANDS);
        Holder<Biome> woods = biomes.getHolderOrThrow(ArtilianBiomes.CRYSTAL_CANOPY_WOODS);
        boolean inStarlight = starlight.value().getMobSettings().getMobs(MobCategory.CREATURE).unwrap()
                .stream().anyMatch(d -> d.type == ArtilianEntities.PEEPKIN.get());
        boolean inWoods = woods.value().getMobSettings().getMobs(MobCategory.CREATURE).unwrap()
                .stream().anyMatch(d -> d.type == ArtilianEntities.PEEPKIN.get());
        helper.assertTrue(inStarlight && inWoods,
                "Peepkin must spawn in Starlight Highlands and Crystal Canopy Woods");

        BlockPos spawnPos = helper.absolutePos(new BlockPos(7, 2, 7));
        level.setBlockAndUpdate(spawnPos, Blocks.AIR.defaultBlockState());
        level.setBlockAndUpdate(spawnPos.above(), Blocks.AIR.defaultBlockState());

        level.setBlockAndUpdate(spawnPos.below(), ArtilianBlocks.STARLIGHT_SLATE.get().defaultBlockState());
        helper.assertTrue(SpawnPlacements.checkSpawnRules(
                        ArtilianEntities.PEEPKIN.get(), level, MobSpawnType.NATURAL, spawnPos, level.random),
                "Peepkin must be allowed to spawn on Starlight Slate");

        level.setBlockAndUpdate(spawnPos.below(), ArtilianBlocks.CRYSTAL_TURF.get().defaultBlockState());
        helper.assertTrue(SpawnPlacements.checkSpawnRules(
                        ArtilianEntities.PEEPKIN.get(), level, MobSpawnType.NATURAL, spawnPos, level.random),
                "Peepkin must be allowed to spawn on Crystal Turf");

        level.setBlockAndUpdate(spawnPos.below(), Blocks.STONE.defaultBlockState());
        helper.assertFalse(SpawnPlacements.checkSpawnRules(
                        ArtilianEntities.PEEPKIN.get(), level, MobSpawnType.NATURAL, spawnPos, level.random),
                "Peepkin must not spawn on plain Stone");

        helper.succeed();
    }

    @GameTest(template = "tnt_arena")
    public static void dragonAndEyePetsCaptureBefriendSummonAndFaintSafelyIntoCrystal(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        ServerPlayer player = ChesterTestSupport.playerInTestArea(helper, "DragonEyeRoster");

        EmberDrakeEntity wildDrake = ArtilianEntities.EMBER_DRAKE.get().create(level);
        BlockPos drakePos = helper.absolutePos(new BlockPos(5, 1, 7));
        wildDrake.moveTo(drakePos.getX() + 0.5, drakePos.getY(), drakePos.getZ() + 0.5, 0.0F, 0.0F);
        level.addFreshEntity(wildDrake);

        ItemStack treats = new ItemStack(ArtilianItems.BIOME_TREAT.get(), 4);
        player.setItemInHand(InteractionHand.MAIN_HAND, treats);
        player.interactOn(wildDrake, InteractionHand.MAIN_HAND);
        player.interactOn(wildDrake, InteractionHand.MAIN_HAND);
        helper.assertTrue(wildDrake.isBefriended(), "Two Biome Treats should befriend Ember Drake");

        ItemStack drakeCrystal = new ItemStack(ArtilianItems.CAPTURE_CRYSTAL.get());
        player.setItemInHand(InteractionHand.MAIN_HAND, drakeCrystal);
        player.interactOn(wildDrake, InteractionHand.MAIN_HAND);
        CapturedPetData drakeData = drakeCrystal.get(ArtilianDataComponents.CAPTURED_PET.get());
        helper.assertTrue(drakeData != null
                        && drakeData.primaryElement() == PetElement.DRAGON
                        && drakeData.secondaryElement().equals(Optional.of(PetElement.FIRE))
                        && drakeData.entityType().equals(ArtilianEntities.EMBER_DRAKE.getId()),
                "Capture Crystal should store Ember Drake identity and DRAGON / FIRE elements");

        PeepkinEntity wildPeepkin = ArtilianEntities.PEEPKIN.get().create(level);
        BlockPos peepPos = helper.absolutePos(new BlockPos(9, 1, 7));
        wildPeepkin.moveTo(peepPos.getX() + 0.5, peepPos.getY(), peepPos.getZ() + 0.5, 0.0F, 0.0F);
        wildPeepkin.addAffinity(100);
        level.addFreshEntity(wildPeepkin);

        ItemStack peepCrystal = new ItemStack(ArtilianItems.CAPTURE_CRYSTAL.get());
        player.setItemInHand(InteractionHand.MAIN_HAND, peepCrystal);
        player.interactOn(wildPeepkin, InteractionHand.MAIN_HAND);
        CapturedPetData peepData = peepCrystal.get(ArtilianDataComponents.CAPTURED_PET.get());
        helper.assertTrue(peepData != null
                        && peepData.primaryElement() == PetElement.SHADOW
                        && peepData.secondaryElement().equals(Optional.of(PetElement.STARLIGHT))
                        && peepData.entityType().equals(ArtilianEntities.PEEPKIN.getId()),
                "Capture Crystal should store Peepkin identity and SHADOW / STARLIGHT elements");

        // Summon Ember Drake and verify safe non-lethal fainting back into crystal
        player.setItemInHand(InteractionHand.MAIN_HAND, drakeCrystal);
        drakeCrystal.use(level, player, InteractionHand.MAIN_HAND);
        List<? extends EmberDrakeEntity> drakes = level.getEntities(
                ArtilianEntities.EMBER_DRAKE.get(), e -> e.isAlive() && e.isOwner(player));
        helper.assertTrue(drakes.size() == 1, "Expected 1 summoned Ember Drake");
        EmberDrakeEntity summonedDrake = drakes.get(0);
        summonedDrake.hurt(level.damageSources().mobAttack(summonedDrake), 999.0F);

        helper.assertFalse(summonedDrake.isAlive(), "Fainted Ember Drake should withdraw from the world");
        CapturedPetData faintedData = drakeCrystal.get(ArtilianDataComponents.CAPTURED_PET.get());
        helper.assertTrue(faintedData != null && faintedData.fainted()
                        && Math.abs(CaptureCrystalItem.getVisualState(drakeCrystal) - 0.25F) < 0.01F,
                "Fainted Ember Drake must safely return to its crystal in fainted state");

        helper.succeed();
    }

    @GameTest(template = "tnt_arena")
    public static void dragonAndEyePetsBattleWithDragonAndShadowElementalMatchupsAndPerks(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        ServerPlayer player = ChesterTestSupport.playerInTestArea(helper, "DragonEyeBattle");

        EmberDrakeEntity drake = ArtilianEntities.EMBER_DRAKE.get().create(level);
        BlockPos drakePos = helper.absolutePos(new BlockPos(6, 1, 7));
        drake.moveTo(drakePos.getX() + 0.5, drakePos.getY(), drakePos.getZ() + 0.5, 0.0F, 0.0F);
        drake.setOwner(player);
        drake.addAffinity(100);
        level.addFreshEntity(drake);

        Creeper creeper = EntityType.CREEPER.create(level);
        creeper.moveTo(drakePos.getX() + 1.2, drakePos.getY(), drakePos.getZ() + 0.5, 0.0F, 0.0F);
        level.addFreshEntity(creeper);
        float creeperBefore = creeper.getHealth();
        drake.doHurtTarget(creeper);
        float drakeDamage = creeperBefore - creeper.getHealth();
        helper.assertTrue(Math.abs(drakeDamage - 7.5F) < 0.05F,
                "Ember Drake (5.0 base, DRAGON) vs Nature Creeper should deal 7.5 (1.5x), got " + drakeDamage);
        helper.assertTrue(creeper.isOnFire() && creeper.hasEffect(MobEffects.GLOWING),
                "Ember Drake DRAGON signature hit should ignite and apply Glowing");
        creeper.discard();

        PeepkinEntity peepkin = ArtilianEntities.PEEPKIN.get().create(level);
        BlockPos peepPos = helper.absolutePos(new BlockPos(9, 1, 7));
        peepkin.moveTo(peepPos.getX() + 0.5, peepPos.getY(), peepPos.getZ() + 0.5, 0.0F, 0.0F);
        peepkin.setOwner(player);
        peepkin.addAffinity(100);
        level.addFreshEntity(peepkin);

        Phantom phantom = EntityType.PHANTOM.create(level);
        phantom.moveTo(peepPos.getX() + 1.2, peepPos.getY(), peepPos.getZ() + 0.5, 0.0F, 0.0F);
        level.addFreshEntity(phantom);
        float phantomBefore = phantom.getHealth();
        peepkin.doHurtTarget(phantom);
        float peepDamage = phantomBefore - phantom.getHealth();
        helper.assertTrue(Math.abs(peepDamage - 6.75F) < 0.05F,
                "Peepkin (4.5 base, SHADOW) vs Starlight Phantom should deal 6.75 (1.5x), got " + peepDamage);
        helper.assertTrue(phantom.hasEffect(MobEffects.BLINDNESS),
                "Peepkin SHADOW signature hit should apply Blindness");

        helper.assertFalse(drake.canAttack(peepkin), "Tamed pets must never attack allied tamed pets");

        phantom.discard();
        drake.discard();
        peepkin.discard();
        helper.succeed();
    }

    @GameTest(template = "tnt_arena")
    public static void prismlingIsAllTypesLegendarySizedAndSpawnsInAllThreeBiomes(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        PrismlingEntity prismling = ArtilianEntities.PRISMLING.get().create(level);
        helper.assertTrue(prismling != null && prismling.isAllTypes(), "Prismling must be an all-types pet");

        EntityType<?> prismType = ArtilianEntities.PRISMLING.get();
        EntityType<?> peepType = ArtilianEntities.PEEPKIN.get();
        helper.assertTrue(prismType.getHeight() >= peepType.getHeight() * 2.0F
                        && prismType.getWidth() >= peepType.getWidth() * 2.0F,
                "Legendary Prismling must be at least twice Peepkin's size, got "
                        + prismType.getWidth() + "x" + prismType.getHeight());

        Registry<Biome> biomes = level.registryAccess().registryOrThrow(Registries.BIOME);
        for (var key : List.of(ArtilianBiomes.EMBERBLOOM_MEADOWS, ArtilianBiomes.STARLIGHT_HIGHLANDS,
                ArtilianBiomes.CRYSTAL_CANOPY_WOODS)) {
            boolean rareSpawn = biomes.getHolderOrThrow(key).value().getMobSettings()
                    .getMobs(MobCategory.CREATURE).unwrap().stream()
                    .anyMatch(d -> d.type == prismType && d.getWeight().asInt() == 1 && d.maxCount == 1);
            helper.assertTrue(rareSpawn, "Prismling must be a rare (weight 1, single) spawn in " + key.location());
        }

        BlockPos spawnPos = helper.absolutePos(new BlockPos(7, 2, 7));
        level.setBlockAndUpdate(spawnPos, Blocks.AIR.defaultBlockState());
        level.setBlockAndUpdate(spawnPos.above(), Blocks.AIR.defaultBlockState());
        for (var ground : List.of(ArtilianBlocks.EMBER_TURF.get(), ArtilianBlocks.STARLIGHT_TURF.get(),
                ArtilianBlocks.CRYSTAL_TURF.get(), ArtilianBlocks.STARLIGHT_SLATE.get())) {
            level.setBlockAndUpdate(spawnPos.below(), ground.defaultBlockState());
            helper.assertTrue(SpawnPlacements.checkSpawnRules(
                            ArtilianEntities.PRISMLING.get(), level, MobSpawnType.NATURAL, spawnPos, level.random),
                    "Prismling must be allowed to spawn on " + ground.getDescriptionId());
        }
        level.setBlockAndUpdate(spawnPos.below(), Blocks.STONE.defaultBlockState());
        helper.assertFalse(SpawnPlacements.checkSpawnRules(
                        ArtilianEntities.PRISMLING.get(), level, MobSpawnType.NATURAL, spawnPos, level.random),
                "Prismling must not spawn on plain Stone");

        helper.succeed();
    }

    @GameTest(template = "tnt_arena")
    public static void prismlingAttacksWithTheBestElementAgainstEveryTarget(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        ServerPlayer player = ChesterTestSupport.playerInTestArea(helper, "PrismlingBattle");
        BlockPos pos = helper.absolutePos(new BlockPos(6, 1, 7));

        PrismlingEntity prismling = ArtilianEntities.PRISMLING.get().create(level);
        prismling.moveTo(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, 0.0F, 0.0F);
        prismling.setOwner(player);
        prismling.addAffinity(100);
        level.addFreshEntity(prismling);

        List<LivingEntity> targets = List.of(
                EntityType.CREEPER.create(level),
                EntityType.DROWNED.create(level),
                EntityType.PHANTOM.create(level),
                ArtilianEntities.EMBER_DRAKE.get().create(level),
                ArtilianEntities.PEEPKIN.get().create(level),
                ArtilianEntities.ZAPHOX.get().create(level));
        for (LivingEntity target : targets) {
            float best = PetElement.NEUTRAL;
            for (PetElement element : PetElement.values()) {
                best = Math.max(best, element.multiplierAgainst(target));
            }
            // Ties are broken at random, so pick many times: every pick must be a best-damage element.
            for (int i = 0; i < 25; i++) {
                PetElement chosen = prismling.attackElementAgainst(target);
                helper.assertTrue(chosen.multiplierAgainst(target) == best,
                        "Prismling picked " + chosen + " (" + chosen.multiplierAgainst(target) + "x) against "
                                + target.getType().getDescriptionId() + " but the best element does " + best + "x");
            }
        }

        Creeper creeper = EntityType.CREEPER.create(level);
        creeper.moveTo(pos.getX() + 1.5, pos.getY(), pos.getZ() + 0.5, 0.0F, 0.0F);
        level.addFreshEntity(creeper);
        float before = creeper.getHealth();
        prismling.doHurtTarget(creeper);
        float dealt = before - creeper.getHealth();
        helper.assertTrue(dealt >= 7.45F,
                "Prismling (5.0 base) must hit a Nature Creeper super-effectively for at least 7.5, got " + dealt);

        creeper.discard();
        prismling.discard();
        helper.succeed();
    }

    @GameTest(template = "tnt_arena")
    public static void prismlingTakesNeutralDamageFromEveryElement(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        PrismlingEntity prismling = ArtilianEntities.PRISMLING.get().create(level);
        for (PetElement attacker : PetElement.values()) {
            helper.assertTrue(attacker.multiplierAgainst(prismling) == PetElement.NEUTRAL,
                    attacker + " must hit all-types Prismling for neutral 1.0x, got "
                            + attacker.multiplierAgainst(prismling) + "x");
        }
        helper.succeed();
    }

    @GameTest(template = "tnt_arena")
    public static void prismlingCapturesBefriendsSummonsAndFaintsSafelyIntoCrystal(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        ServerPlayer player = ChesterTestSupport.playerInTestArea(helper, "PrismlingRoster");

        PrismlingEntity wild = ArtilianEntities.PRISMLING.get().create(level);
        BlockPos pos = helper.absolutePos(new BlockPos(7, 1, 7));
        wild.moveTo(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, 0.0F, 0.0F);
        level.addFreshEntity(wild);

        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(ArtilianItems.BIOME_TREAT.get(), 4));
        player.interactOn(wild, InteractionHand.MAIN_HAND);
        player.interactOn(wild, InteractionHand.MAIN_HAND);
        helper.assertTrue(wild.isBefriended(), "Two Biome Treats should befriend Prismling");

        ItemStack crystal = new ItemStack(ArtilianItems.CAPTURE_CRYSTAL.get());
        player.setItemInHand(InteractionHand.MAIN_HAND, crystal);
        player.interactOn(wild, InteractionHand.MAIN_HAND);
        CapturedPetData data = crystal.get(ArtilianDataComponents.CAPTURED_PET.get());
        helper.assertTrue(data != null && data.entityType().equals(ArtilianEntities.PRISMLING.getId()),
                "Capture Crystal should store Prismling");

        List<Component> tooltip = new ArrayList<>();
        crystal.getItem().appendHoverText(crystal, Item.TooltipContext.EMPTY, tooltip, TooltipFlag.NORMAL);
        boolean allTypesBadge = tooltip.stream()
                .filter(line -> line.getContents() instanceof TranslatableContents tc
                        && tc.getKey().equals("tooltip.artilian.pet.elements"))
                .map(line -> ((TranslatableContents) line.getContents()).getArgs()[0])
                .anyMatch(arg -> arg instanceof Component badge
                        && badge.getContents() instanceof TranslatableContents badgeKey
                        && badgeKey.getKey().equals("element.artilian.all_types"));
        helper.assertTrue(allTypesBadge, "Prismling's crystal must show the All Types badge");

        crystal.use(level, player, InteractionHand.MAIN_HAND);
        List<? extends PrismlingEntity> summoned = level.getEntities(
                ArtilianEntities.PRISMLING.get(), e -> e.isAlive() && e.isOwner(player));
        helper.assertTrue(summoned.size() == 1, "Expected 1 summoned Prismling");
        PrismlingEntity pet = summoned.get(0);
        pet.hurt(level.damageSources().mobAttack(pet), 999.0F);

        helper.assertFalse(pet.isAlive(), "Fainted Prismling should withdraw from the world");
        CapturedPetData fainted = crystal.get(ArtilianDataComponents.CAPTURED_PET.get());
        helper.assertTrue(fainted != null && fainted.fainted()
                        && Math.abs(CaptureCrystalItem.getVisualState(crystal) - 0.25F) < 0.01F,
                "Fainted Prismling must safely return to its crystal in fainted state");

        helper.succeed();
    }

    @GameTest(template = "tnt_arena")
    public static void flurrywingIsIceAirTallAndSpawnsInStarlightHighlands(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        FlurrywingEntity flurrywing = ArtilianEntities.FLURRYWING.get().create(level);
        helper.assertTrue(flurrywing != null
                        && flurrywing.getPrimaryElement() == PetElement.ICE
                        && flurrywing.getSecondaryElement().equals(Optional.of(PetElement.AIR)),
                "Flurrywing must be ICE / AIR dual-element");

        EntityType<?> flurryType = ArtilianEntities.FLURRYWING.get();
        helper.assertTrue(flurryType.getHeight() >= 2.5F && flurryType.getHeight() <= 3.0F,
                "Flurrywing must stand 2.5 to 3 blocks tall, got " + flurryType.getHeight());

        Registry<Biome> biomes = level.registryAccess().registryOrThrow(Registries.BIOME);
        boolean inHighlands = biomes.getHolderOrThrow(ArtilianBiomes.STARLIGHT_HIGHLANDS).value().getMobSettings()
                .getMobs(MobCategory.CREATURE).unwrap().stream()
                .anyMatch(d -> d.type == flurryType && d.getWeight().asInt() == 4 && d.minCount == 1 && d.maxCount == 2);
        helper.assertTrue(inHighlands, "Flurrywing must spawn in Starlight Highlands (weight 4, group 1-2)");
        for (var key : List.of(ArtilianBiomes.EMBERBLOOM_MEADOWS, ArtilianBiomes.CRYSTAL_CANOPY_WOODS)) {
            boolean elsewhere = biomes.getHolderOrThrow(key).value().getMobSettings()
                    .getMobs(MobCategory.CREATURE).unwrap().stream().anyMatch(d -> d.type == flurryType);
            helper.assertFalse(elsewhere, "Flurrywing must only spawn in Starlight Highlands, found in " + key.location());
        }

        BlockPos spawnPos = helper.absolutePos(new BlockPos(7, 2, 7));
        level.setBlockAndUpdate(spawnPos, Blocks.AIR.defaultBlockState());
        level.setBlockAndUpdate(spawnPos.above(), Blocks.AIR.defaultBlockState());
        for (var ground : List.of(ArtilianBlocks.STARLIGHT_SLATE.get(), ArtilianBlocks.STARLIGHT_TURF.get())) {
            level.setBlockAndUpdate(spawnPos.below(), ground.defaultBlockState());
            helper.assertTrue(SpawnPlacements.checkSpawnRules(
                            ArtilianEntities.FLURRYWING.get(), level, MobSpawnType.NATURAL, spawnPos, level.random),
                    "Flurrywing must be allowed to spawn on " + ground.getDescriptionId());
        }
        level.setBlockAndUpdate(spawnPos.below(), Blocks.STONE.defaultBlockState());
        helper.assertFalse(SpawnPlacements.checkSpawnRules(
                        ArtilianEntities.FLURRYWING.get(), level, MobSpawnType.NATURAL, spawnPos, level.random),
                "Flurrywing must not spawn on plain Stone");

        helper.succeed();
    }

    @GameTest(template = "tnt_arena")
    public static void flurrywingHoversAndTakesNoFallDamage(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        FlurrywingEntity flurrywing = ArtilianEntities.FLURRYWING.get().create(level);
        BlockPos pos = helper.absolutePos(new BlockPos(7, 1, 7));
        flurrywing.moveTo(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, 0.0F, 0.0F);
        level.addFreshEntity(flurrywing);

        float before = flurrywing.getHealth();
        // A 10-block drop: 7 damage for anything that can be hurt by falling.
        boolean hurt = flurrywing.causeFallDamage(10.0F, 1.0F, level.damageSources().fall());
        helper.assertFalse(hurt, "Hovering Flurrywing must report no fall damage");
        helper.assertTrue(flurrywing.getHealth() == before,
                "Flurrywing dropped from 10 blocks must land at full health, went " + before + " -> " + flurrywing.getHealth());

        flurrywing.discard();
        helper.succeed();
    }

    @GameTest(template = "tnt_arena")
    public static void flurrywingCapturesBefriendsSummonsAndFaintsSafelyIntoCrystal(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        ServerPlayer player = ChesterTestSupport.playerInTestArea(helper, "FlurrywingRoster");

        FlurrywingEntity wild = ArtilianEntities.FLURRYWING.get().create(level);
        BlockPos pos = helper.absolutePos(new BlockPos(7, 1, 7));
        wild.moveTo(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, 0.0F, 0.0F);
        level.addFreshEntity(wild);

        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(ArtilianItems.BIOME_TREAT.get(), 4));
        player.interactOn(wild, InteractionHand.MAIN_HAND);
        player.interactOn(wild, InteractionHand.MAIN_HAND);
        helper.assertTrue(wild.isBefriended(), "Two Biome Treats should befriend Flurrywing");

        ItemStack crystal = new ItemStack(ArtilianItems.CAPTURE_CRYSTAL.get());
        player.setItemInHand(InteractionHand.MAIN_HAND, crystal);
        player.interactOn(wild, InteractionHand.MAIN_HAND);
        CapturedPetData data = crystal.get(ArtilianDataComponents.CAPTURED_PET.get());
        helper.assertTrue(data != null
                        && data.primaryElement() == PetElement.ICE
                        && data.secondaryElement().equals(Optional.of(PetElement.AIR))
                        && data.entityType().equals(ArtilianEntities.FLURRYWING.getId()),
                "Capture Crystal should store Flurrywing identity and ICE / AIR elements");

        crystal.use(level, player, InteractionHand.MAIN_HAND);
        List<? extends FlurrywingEntity> summoned = level.getEntities(
                ArtilianEntities.FLURRYWING.get(), e -> e.isAlive() && e.isOwner(player));
        helper.assertTrue(summoned.size() == 1, "Expected 1 summoned Flurrywing");
        FlurrywingEntity pet = summoned.get(0);
        pet.hurt(level.damageSources().mobAttack(pet), 999.0F);

        helper.assertFalse(pet.isAlive(), "Fainted Flurrywing should withdraw from the world");
        CapturedPetData fainted = crystal.get(ArtilianDataComponents.CAPTURED_PET.get());
        helper.assertTrue(fainted != null && fainted.fainted()
                        && Math.abs(CaptureCrystalItem.getVisualState(crystal) - 0.25F) < 0.01F,
                "Fainted Flurrywing must safely return to its crystal in fainted state");

        helper.succeed();
    }

    @GameTest(template = "tnt_arena")
    public static void flurrywingBattlesWithIceMatchups(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        ServerPlayer player = ChesterTestSupport.playerInTestArea(helper, "FlurrywingBattle");
        BlockPos pos = helper.absolutePos(new BlockPos(6, 1, 7));

        FlurrywingEntity flurrywing = ArtilianEntities.FLURRYWING.get().create(level);
        flurrywing.moveTo(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, 0.0F, 0.0F);
        flurrywing.setOwner(player);
        flurrywing.addAffinity(100);
        level.addFreshEntity(flurrywing);

        Creeper creeper = EntityType.CREEPER.create(level);
        creeper.moveTo(pos.getX() + 1.5, pos.getY(), pos.getZ() + 0.5, 0.0F, 0.0F);
        level.addFreshEntity(creeper);
        float before = creeper.getHealth();
        flurrywing.doHurtTarget(creeper);
        float dealt = before - creeper.getHealth();
        helper.assertTrue(Math.abs(dealt - 6.75F) < 0.05F,
                "Flurrywing (4.5 base, ICE) vs Nature Creeper should deal 6.75 (1.5x), got " + dealt);
        helper.assertTrue(creeper.hasEffect(MobEffects.MOVEMENT_SLOWDOWN),
                "Flurrywing ICE signature hit should apply Slowness");

        helper.assertTrue(PetElement.FIRE.multiplierAgainst(flurrywing) == 1.5F,
                "Fire must hit Ice-type Flurrywing for 1.5x, got " + PetElement.FIRE.multiplierAgainst(flurrywing));

        creeper.discard();
        flurrywing.discard();
        helper.succeed();
    }

    @GameTest(template = "tnt_arena")
    public static void sunsteelElementMatchups(GameTestHelper helper) {
        for (PetElement strong : List.of(PetElement.SHADOW, PetElement.ICE, PetElement.DRAGON)) {
            helper.assertTrue(Math.abs(PetElement.SUNSTEEL.multiplierAgainst(strong) - 1.5F) < 0.001F,
                    "Sunsteel must be 1.5x strong vs " + strong);
        }
        for (PetElement resisted : List.of(PetElement.FIRE, PetElement.EARTH)) {
            helper.assertTrue(Math.abs(PetElement.SUNSTEEL.multiplierAgainst(resisted) - 0.75F) < 0.001F,
                    "Sunsteel must be 0.75x resisted vs " + resisted);
        }
        helper.assertTrue(PetElement.SUNSTEEL.multiplierAgainst(PetElement.WATER) == 1.0F,
                "Sunsteel must be neutral vs Water");
        helper.assertTrue(PetElement.WATER.multiplierAgainst(PetElement.SUNSTEEL) == 1.5F,
                "Water must be 1.5x strong vs Sunsteel (rust)");
        helper.assertTrue(PetElement.ELECTRIC.multiplierAgainst(PetElement.SUNSTEEL) == 1.5F,
                "Electric must be 1.5x strong vs Sunsteel (conducts)");
        helper.assertTrue(PetElement.SHADOW.multiplierAgainst(PetElement.SUNSTEEL) == 0.75F,
                "Shadow must be 0.75x resisted vs Sunsteel");
        helper.assertTrue(PetElement.ICE.multiplierAgainst(PetElement.SUNSTEEL) == 0.75F,
                "Ice must be 0.75x resisted vs Sunsteel");
        helper.assertTrue(PetElement.FIRE.multiplierAgainst(PetElement.SUNSTEEL) == 1.0F,
                "Fire must be neutral vs Sunsteel");
        helper.succeed();
    }

    @GameTest(template = "tnt_arena")
    public static void sunbeamIsSunsteelLegendaryGhastSizedAndSpawnsInEmberbloom(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        SunbeamEntity sunbeam = ArtilianEntities.SUNBEAM.get().create(level);
        helper.assertTrue(sunbeam != null
                        && sunbeam.getPrimaryElement() == PetElement.SUNSTEEL
                        && sunbeam.getSecondaryElement().isEmpty()
                        && !sunbeam.isAllTypes(),
                "Sunbeam must be a pure Sunsteel legendary with no secondary and no all-types rule");
        helper.assertTrue(sunbeam.getAttributeValue(Attributes.MAX_HEALTH) == 30.0,
                "Legendary Sunbeam must have 30 max health, got " + sunbeam.getAttributeValue(Attributes.MAX_HEALTH));

        EntityType<?> sunbeamType = ArtilianEntities.SUNBEAM.get();
        helper.assertTrue(sunbeamType.getHeight() >= 4.0F && sunbeamType.getWidth() >= 3.0F,
                "Sunbeam must be ghast-sized (at least 3.0 x 4.0), got "
                        + sunbeamType.getWidth() + "x" + sunbeamType.getHeight());

        Registry<Biome> biomes = level.registryAccess().registryOrThrow(Registries.BIOME);
        boolean inEmberbloom = biomes.getHolderOrThrow(ArtilianBiomes.EMBERBLOOM_MEADOWS).value().getMobSettings()
                .getMobs(MobCategory.CREATURE).unwrap().stream()
                .anyMatch(d -> d.type == sunbeamType && d.getWeight().asInt() == 1 && d.minCount == 1 && d.maxCount == 1);
        helper.assertTrue(inEmberbloom, "Emberbloom Meadows must include Sunbeam at weight 1, group 1");
        for (var key : List.of(ArtilianBiomes.STARLIGHT_HIGHLANDS, ArtilianBiomes.CRYSTAL_CANOPY_WOODS)) {
            boolean elsewhere = biomes.getHolderOrThrow(key).value().getMobSettings()
                    .getMobs(MobCategory.CREATURE).unwrap().stream().anyMatch(d -> d.type == sunbeamType);
            helper.assertFalse(elsewhere, "Sunbeam must only spawn in Emberbloom Meadows, found in " + key.location());
        }

        helper.succeed();
    }

    @GameTest(template = "tnt_arena")
    public static void sunbeamHoversAndTakesNoFallDamage(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        SunbeamEntity sunbeam = ArtilianEntities.SUNBEAM.get().create(level);
        BlockPos pos = helper.absolutePos(new BlockPos(7, 1, 7));
        sunbeam.moveTo(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, 0.0F, 0.0F);
        level.addFreshEntity(sunbeam);

        float before = sunbeam.getHealth();
        boolean hurt = sunbeam.causeFallDamage(10.0F, 1.0F, level.damageSources().fall());
        helper.assertFalse(hurt, "Hovering Sunbeam must report no fall damage");
        helper.assertTrue(sunbeam.getHealth() == before,
                "Sunbeam dropped from 10 blocks must land at full health, went " + before + " -> " + sunbeam.getHealth());

        sunbeam.discard();
        helper.succeed();
    }

    @GameTest(template = "tnt_arena")
    public static void sunbeamAuraHealsOwnerAndOwnersPetsOnly(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        ServerPlayer owner = ChesterTestSupport.playerInTestArea(helper, "SunbeamOwner");
        ServerPlayer stranger = ChesterTestSupport.playerInTestArea(helper, "SunbeamStranger");

        SunbeamEntity sunbeam = ArtilianEntities.SUNBEAM.get().create(level);
        BlockPos sunbeamPos = helper.absolutePos(new BlockPos(2, 1, 2));
        sunbeam.moveTo(sunbeamPos.getX() + 0.5, sunbeamPos.getY(), sunbeamPos.getZ() + 0.5, 0.0F, 0.0F);
        sunbeam.setOwner(owner);
        level.addFreshEntity(sunbeam);

        FlurrywingEntity ownPet = ArtilianEntities.FLURRYWING.get().create(level);
        BlockPos ownPos = helper.absolutePos(new BlockPos(3, 1, 3));
        ownPet.moveTo(ownPos.getX() + 0.5, ownPos.getY(), ownPos.getZ() + 0.5, 0.0F, 0.0F);
        ownPet.setOwner(owner);
        level.addFreshEntity(ownPet);

        FlurrywingEntity otherPet = ArtilianEntities.FLURRYWING.get().create(level);
        BlockPos otherPos = helper.absolutePos(new BlockPos(2, 1, 4));
        otherPet.moveTo(otherPos.getX() + 0.5, otherPos.getY(), otherPos.getZ() + 0.5, 0.0F, 0.0F);
        otherPet.setOwner(stranger);
        level.addFreshEntity(otherPet);

        float halfHealth = ownPet.getMaxHealth() / 2.0F;
        ownPet.setHealth(halfHealth);
        otherPet.setHealth(halfHealth);

        sunbeam.applySunAura();

        helper.assertTrue(owner.hasEffect(MobEffects.REGENERATION),
                "Sunbeam's aura must give its owner Regeneration");
        helper.assertTrue(ownPet.getHealth() > halfHealth,
                "Sunbeam's aura must heal its owner's pet, stayed at " + ownPet.getHealth());
        helper.assertTrue(otherPet.getHealth() == halfHealth,
                "Sunbeam's aura must never heal another player's pet, went to " + otherPet.getHealth());
        helper.assertFalse(stranger.hasEffect(MobEffects.REGENERATION),
                "Sunbeam's aura must never regenerate another player");

        sunbeam.setSitting(true);
        owner.removeEffect(MobEffects.REGENERATION);
        ownPet.setHealth(halfHealth);
        otherPet.setHealth(halfHealth);

        sunbeam.applySunAura();

        helper.assertFalse(owner.hasEffect(MobEffects.REGENERATION),
                "A sitting Sunbeam's aura must not regenerate its owner");
        helper.assertTrue(ownPet.getHealth() == halfHealth,
                "A sitting Sunbeam's aura must not heal any pet");
        helper.assertTrue(otherPet.getHealth() == halfHealth,
                "A sitting Sunbeam's aura must not heal another player's pet");

        sunbeam.discard();
        ownPet.discard();
        otherPet.discard();
        helper.succeed();
    }

    @GameTest(template = "tnt_arena")
    public static void sunbeamLightOnlyReplacesAirAndIsRemovedWithIt(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        SunbeamEntity sunbeam = ArtilianEntities.SUNBEAM.get().create(level);
        BlockPos start = helper.absolutePos(new BlockPos(5, 3, 5));
        sunbeam.moveTo(start.getX() + 0.5, start.getY(), start.getZ() + 0.5, 0.0F, 0.0F);
        level.addFreshEntity(sunbeam);

        for (int i = 0; i < 10 && sunbeam.getPlacedLight() == null; i++) {
            clearChestAir(level, sunbeam);
            sunbeam.tick();
        }
        BlockPos firstLight = sunbeam.getPlacedLight();
        helper.assertTrue(firstLight != null && level.getBlockState(firstLight).is(Blocks.LIGHT),
                "EXPLOITED P2-05: Sunbeam must place a light block into air at its chest");
        helper.assertTrue(level.getBlockState(firstLight).getValue(LightBlock.LEVEL) == SunbeamEntity.LIGHT_LEVEL,
                "EXPLOITED P2-05: the placed light block must be level 15");

        // A real block at the chest target must never be replaced by the aura.
        BlockPos stoneSpot = helper.absolutePos(new BlockPos(7, 3, 5));
        sunbeam.moveTo(stoneSpot.getX() + 0.5, stoneSpot.getY(), stoneSpot.getZ() + 0.5, 0.0F, 0.0F);
        BlockPos chest = BlockPos.containing(sunbeam.getX(), sunbeam.getY(0.6), sunbeam.getZ());
        level.setBlockAndUpdate(chest, Blocks.STONE.defaultBlockState());
        sunbeam.tick();
        helper.assertTrue(level.getBlockState(chest).is(Blocks.STONE),
                "EXPLOITED P2-05: sun aura replaced a real block with light at " + chest);

        // Back in open air, then removal must clean up exactly the light it placed.
        BlockPos airSpot = helper.absolutePos(new BlockPos(3, 3, 5));
        sunbeam.moveTo(airSpot.getX() + 0.5, airSpot.getY(), airSpot.getZ() + 0.5, 0.0F, 0.0F);
        for (int i = 0; i < 10 && sunbeam.getPlacedLight() == null; i++) {
            clearChestAir(level, sunbeam);
            sunbeam.tick();
        }
        BlockPos secondLight = sunbeam.getPlacedLight();
        helper.assertTrue(secondLight != null && level.getBlockState(secondLight).is(Blocks.LIGHT),
                "EXPLOITED P2-05: Sunbeam must re-place its light after returning to air");

        // A save while lit must remember the light, so a reloaded Sunbeam can still clear it.
        net.minecraft.nbt.CompoundTag saved = new net.minecraft.nbt.CompoundTag();
        sunbeam.addAdditionalSaveData(saved);
        SunbeamEntity reloaded = ArtilianEntities.SUNBEAM.get().create(level);
        reloaded.readAdditionalSaveData(saved);
        helper.assertTrue(secondLight.equals(reloaded.getPlacedLight()),
                "EXPLOITED P2-05: a saved Sunbeam forgot its light at " + secondLight + " and would orphan it on reload");

        sunbeam.discard();
        helper.assertTrue(level.getBlockState(secondLight).is(Blocks.AIR),
                "EXPLOITED P2-05: light block left behind after Sunbeam was removed at " + secondLight);

        helper.succeed();
    }

    @GameTest(template = "tnt_arena")
    public static void sunbeamCapturesBefriendsSummonsAndFaintsSafelyIntoCrystal(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        ServerPlayer player = ChesterTestSupport.playerInTestArea(helper, "SunbeamRoster");

        SunbeamEntity wild = ArtilianEntities.SUNBEAM.get().create(level);
        BlockPos pos = helper.absolutePos(new BlockPos(7, 1, 7));
        wild.moveTo(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, 0.0F, 0.0F);
        level.addFreshEntity(wild);

        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(ArtilianItems.BIOME_TREAT.get(), 4));
        player.interactOn(wild, InteractionHand.MAIN_HAND);
        player.interactOn(wild, InteractionHand.MAIN_HAND);
        helper.assertTrue(wild.isBefriended(), "Two Biome Treats should befriend Sunbeam");

        ItemStack crystal = new ItemStack(ArtilianItems.CAPTURE_CRYSTAL.get());
        player.setItemInHand(InteractionHand.MAIN_HAND, crystal);
        player.interactOn(wild, InteractionHand.MAIN_HAND);
        CapturedPetData data = crystal.get(ArtilianDataComponents.CAPTURED_PET.get());
        helper.assertTrue(data != null
                        && data.primaryElement() == PetElement.SUNSTEEL
                        && data.secondaryElement().isEmpty()
                        && data.entityType().equals(ArtilianEntities.SUNBEAM.getId()),
                "Capture Crystal should store Sunbeam identity and pure Sunsteel typing");

        crystal.use(level, player, InteractionHand.MAIN_HAND);
        List<? extends SunbeamEntity> summoned = level.getEntities(
                ArtilianEntities.SUNBEAM.get(), e -> e.isAlive() && e.isOwner(player));
        helper.assertTrue(summoned.size() == 1, "Expected 1 summoned Sunbeam");
        SunbeamEntity pet = summoned.get(0);
        pet.hurt(level.damageSources().mobAttack(pet), 999.0F);

        helper.assertFalse(pet.isAlive(), "Fainted Sunbeam should withdraw from the world");
        CapturedPetData fainted = crystal.get(ArtilianDataComponents.CAPTURED_PET.get());
        helper.assertTrue(fainted != null && fainted.fainted()
                        && Math.abs(CaptureCrystalItem.getVisualState(crystal) - 0.25F) < 0.01F,
                "Fainted Sunbeam must safely return to its crystal in fainted state");

        helper.succeed();
    }

    /** Keeps a three-block window of air around the chest target while a Sunbeam test tick settles. */
    private static void clearChestAir(ServerLevel level, SunbeamEntity sunbeam) {
        BlockPos chest = BlockPos.containing(sunbeam.getX(), sunbeam.getY(0.6), sunbeam.getZ());
        for (int dy = -1; dy <= 1; dy++) {
            level.setBlockAndUpdate(chest.above(dy), Blocks.AIR.defaultBlockState());
        }
    }
}
