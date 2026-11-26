package com.silkpanda.artilian.gametest;

import com.silkpanda.artilian.ArtilianMod;
import com.silkpanda.artilian.chester.ChesterEntity;
import com.silkpanda.artilian.gear.TwinStarAbilities;
import com.silkpanda.artilian.mana.ManaData;
import com.silkpanda.artilian.pet.CrystalFoxEntity;
import com.silkpanda.artilian.registry.ArtilianEntities;
import com.silkpanda.artilian.registry.ArtilianItems;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.FarmBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import net.neoforged.neoforge.network.registration.NetworkRegistry;

import java.util.List;

/** Phase 6 Stage 1E: Twin-Star weapons & tools active/passive ability GameTests. */
@GameTestHolder(ArtilianMod.MODID)
@PrefixGameTestTemplate(false)
public class TwinStarAbilitiesGameTests {

    private static ServerPlayer spawnTestPlayer(GameTestHelper helper, String name, Vec3 relPos) {
        ServerPlayer player = CustomTntGameTests.spawnSurvivalPlayer(helper, name, relPos);
        NetworkRegistry.configureMockConnection(player.connection.getConnection());
        return player;
    }

    @GameTest(template = "tnt_arena", timeoutTicks = 60)
    public static void eclipseTwinSlashHitsHostilesSparesKidSafeStopsAtWallsAndHealsAllies(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        ServerPlayer player = spawnTestPlayer(helper, "AlexTwinBlade", new Vec3(7.5, 2.0, 2.5));
        player.setHealth(player.getMaxHealth() - 4.0F);
        float playerHpBefore = player.getHealth();

        // Wall at z=7 blocks the arc from hitting the behind-wall zombie at z=9
        helper.setBlock(new BlockPos(7, 2, 7), Blocks.STONE);
        helper.setBlock(new BlockPos(7, 3, 7), Blocks.STONE);

        Zombie frontZombie = helper.spawnWithNoFreeWill(EntityType.ZOMBIE, new BlockPos(7, 2, 5));
        Zombie behindWallZombie = helper.spawnWithNoFreeWill(EntityType.ZOMBIE, new BlockPos(7, 2, 9));
        Villager villager = helper.spawnWithNoFreeWill(EntityType.VILLAGER, new BlockPos(7, 2, 4));

        ChesterEntity chester = new ChesterEntity(ArtilianEntities.CHESTER.get(), level);
        Vec3 chesterPos = helper.absoluteVec(new Vec3(6.5, 2.0, 3.5));
        chester.setPos(chesterPos.x, chesterPos.y, chesterPos.z);
        chester.setOwner(player);
        level.addFreshEntity(chester);

        CrystalFoxEntity allyPet = helper.spawnWithNoFreeWill(ArtilianEntities.CRYSTAL_FOX.get(), new BlockPos(8, 2, 3));
        allyPet.setOwner(player);
        allyPet.setHealth(allyPet.getMaxHealth() - 3.0F);
        float petHpBefore = allyPet.getHealth();

        player.lookAt(EntityAnchorArgument.Anchor.EYES, frontZombie.getEyePosition());
        float frontHpBefore = frontZombie.getHealth();
        float behindHpBefore = behindWallZombie.getHealth();
        float villagerHpBefore = villager.getHealth();
        float chesterHpBefore = chester.getHealth();

        boolean cast = TwinStarAbilities.castEclipseTwinSlash(level, player, ArtilianItems.TWIN_STAR_BLADE.get());
        helper.assertTrue(cast, "Eclipse Twin-Slash should succeed with full Mana");
        helper.assertTrue(frontZombie.getHealth() < frontHpBefore, "Eclipse Twin-Slash should damage Zombie in path");
        helper.assertTrue(frontZombie.getRemainingFireTicks() > 0, "Eclipse Twin-Slash should ignite Zombie");
        MobEffectInstance weakness = frontZombie.getEffect(MobEffects.WEAKNESS);
        helper.assertTrue(weakness != null && weakness.getAmplifier() >= 1,
                "Eclipse Twin-Slash should apply Weakness II");
        helper.assertTrue(frontZombie.hasEffect(MobEffects.GLOWING),
                "Eclipse Twin-Slash should apply Glowing");

        helper.assertTrue(behindWallZombie.getHealth() == behindHpBefore,
                "Eclipse Twin-Slash must stop at solid walls");
        helper.assertTrue(villager.getHealth() == villagerHpBefore && !villager.hasEffect(MobEffects.WEAKNESS),
                "Eclipse Twin-Slash must spare Villager");
        helper.assertTrue(chester.getHealth() == chesterHpBefore && !chester.hasEffect(MobEffects.WEAKNESS),
                "Eclipse Twin-Slash must spare Chester");

        helper.assertTrue(player.getHealth() > playerHpBefore, "Eclipse Twin-Slash should heal caster");
        helper.assertTrue(allyPet.getHealth() > petHpBefore, "Eclipse Twin-Slash should heal nearby owned pet");
        MobEffectInstance speed = player.getEffect(MobEffects.MOVEMENT_SPEED);
        helper.assertTrue(speed != null && speed.getAmplifier() >= 1, "Eclipse Twin-Slash should grant Speed II");
        helper.assertTrue(player.hasEffect(MobEffects.DAMAGE_BOOST), "Eclipse Twin-Slash should grant Strength I");
        helper.assertTrue(ManaData.get(player).getMana() == 100 - TwinStarAbilities.ECLIPSE_TWIN_SLASH_MANA,
                "Eclipse Twin-Slash should consume 35 Mana");

        for (int dz = 2; dz <= 6; dz++) {
            helper.assertFalse(helper.getBlockState(new BlockPos(7, 2, dz)).is(Blocks.FIRE),
                    "Eclipse Twin-Slash must never place fire blocks on terrain");
        }
        helper.succeed();
    }

    @GameTest(template = "tnt_arena", timeoutTicks = 60)
    public static void twinStarSanctuaryCleansesBuffsAlliesAndSlowsHostilesSafely(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        ServerPlayer player = spawnTestPlayer(helper, "SteveTwinStaff", new Vec3(7.5, 2.0, 7.5));
        player.addEffect(new MobEffectInstance(MobEffects.POISON, 100, 0));
        player.igniteForSeconds(5);

        CrystalFoxEntity allyPet = helper.spawnWithNoFreeWill(ArtilianEntities.CRYSTAL_FOX.get(), new BlockPos(9, 2, 7));
        allyPet.setOwner(player);
        allyPet.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 100, 0));

        Zombie zombie = helper.spawnWithNoFreeWill(EntityType.ZOMBIE, new BlockPos(10, 2, 7));
        double zombieDistBefore = zombie.distanceTo(player);
        float zombieHpBefore = zombie.getHealth();

        boolean cast = TwinStarAbilities.castTwinStarSanctuary(level, player, ArtilianItems.TWIN_STAR_STAFF.get());
        helper.assertTrue(cast, "Twin-Star Sanctuary should succeed with full Mana");
        helper.assertFalse(player.hasEffect(MobEffects.POISON), "Twin-Star Sanctuary should cleanse harmful effects on caster");
        helper.assertTrue(player.getRemainingFireTicks() <= 0, "Twin-Star Sanctuary should extinguish fire on caster");
        MobEffectInstance absorption = player.getEffect(MobEffects.ABSORPTION);
        helper.assertTrue(absorption != null && absorption.getAmplifier() >= 1,
                "Twin-Star Sanctuary should grant Absorption II");
        helper.assertTrue(player.hasEffect(MobEffects.DAMAGE_RESISTANCE),
                "Twin-Star Sanctuary should grant Resistance I");
        helper.assertTrue(player.hasEffect(MobEffects.REGENERATION),
                "Twin-Star Sanctuary should grant Regeneration II");

        helper.assertFalse(allyPet.hasEffect(MobEffects.WEAKNESS),
                "Twin-Star Sanctuary should cleanse harmful effects on owned pet");
        helper.assertTrue(allyPet.hasEffect(MobEffects.ABSORPTION),
                "Twin-Star Sanctuary should shield owned pet");

        MobEffectInstance slow = zombie.getEffect(MobEffects.MOVEMENT_SLOWDOWN);
        helper.assertTrue(slow != null && slow.getAmplifier() >= 1,
                "Twin-Star Sanctuary should apply Slowness II to hostiles");
        helper.assertTrue(zombie.getHealth() == zombieHpBefore,
                "Twin-Star Sanctuary should push/slow without dealing damage");
        helper.assertTrue(zombie.getDeltaMovement().lengthSqr() > 0.01 || zombie.distanceTo(player) >= zombieDistBefore,
                "Twin-Star Sanctuary should push hostiles back");
        helper.assertTrue(ManaData.get(player).getMana() == 100 - TwinStarAbilities.TWIN_STAR_SANCTUARY_MANA,
                "Twin-Star Sanctuary should consume 40 Mana");
        helper.succeed();
    }

    @GameTest(template = "tnt_arena", timeoutTicks = 40)
    public static void astralSightAutoSmeltsOresGrantsHasteIIIAndRespectsSilkTouch(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        ServerPlayer player = spawnTestPlayer(helper, "AlexTwinPick", new Vec3(7.5, 2.0, 3.5));
        ItemStack pick = new ItemStack(ArtilianItems.TWIN_STAR_PICKAXE.get());

        BlockPos orePos = helper.absolutePos(new BlockPos(7, 2, 11));
        Vec3 dropStart = helper.absoluteVec(new Vec3(7.5, 2.2, 11.0));
        ItemEntity rawDrop = new ItemEntity(level, dropStart.x, dropStart.y, dropStart.z, new ItemStack(Items.RAW_IRON));
        rawDrop.setDeltaMovement(Vec3.ZERO);
        level.addFreshEntity(rawDrop);

        double distBefore = rawDrop.position().distanceTo(player.position());
        pick.getItem().mineBlock(pick, level, Blocks.IRON_ORE.defaultBlockState(), orePos, player);

        MobEffectInstance haste = player.getEffect(MobEffects.DIG_SPEED);
        helper.assertTrue(haste != null && haste.getAmplifier() >= 2,
                "Astral Sight should grant Haste III on ore break");
        helper.assertTrue(rawDrop.getItem().is(Items.IRON_INGOT),
                "Astral Sight should auto-smelt RAW_IRON into IRON_INGOT");
        helper.assertTrue(rawDrop.position().distanceTo(player.position()) < distBefore,
                "Astral Sight should pull drops within 10 blocks toward player");

        ItemStack silkPick = new ItemStack(ArtilianItems.TWIN_STAR_PICKAXE.get());
        Holder<Enchantment> silkTouch = level.registryAccess()
                .registryOrThrow(Registries.ENCHANTMENT)
                .getHolderOrThrow(Enchantments.SILK_TOUCH);
        silkPick.enchant(silkTouch, 1);

        ItemEntity silkRawDrop = new ItemEntity(level, dropStart.x, dropStart.y, dropStart.z, new ItemStack(Items.RAW_IRON));
        silkRawDrop.setDeltaMovement(Vec3.ZERO);
        level.addFreshEntity(silkRawDrop);
        silkPick.getItem().mineBlock(silkPick, level, Blocks.IRON_ORE.defaultBlockState(), orePos, player);
        helper.assertTrue(silkRawDrop.getItem().is(Items.RAW_IRON),
                "Astral Sight must NOT auto-smelt when Silk Touch is present");
        helper.succeed();
    }

    @GameTest(template = "tnt_arena", timeoutTicks = 40)
    public static void twinStarQuarryCarvesThreeByThreeByTwoSafelySkippingBlockEntitiesAndObsidian(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        ServerPlayer player = spawnTestPlayer(helper, "SteveTwinQuarry", new Vec3(7.5, 2.0, 4.5));
        ItemStack pick = new ItemStack(ArtilianItems.TWIN_STAR_PICKAXE.get());

        // Build 3x3x2 wall at z=7..8, skipping chest at (6,2,7) and obsidian at (8,3,7)
        for (int d = 0; d <= 1; d++) {
            for (int dx = -1; dx <= 1; dx++) {
                for (int dy = 0; dy <= 2; dy++) {
                    helper.setBlock(new BlockPos(7 + dx, 2 + dy, 7 + d), Blocks.STONE);
                }
            }
        }
        helper.setBlock(new BlockPos(6, 2, 7), Blocks.CHEST);
        helper.setBlock(new BlockPos(8, 3, 7), Blocks.OBSIDIAN);
        helper.setBlock(new BlockPos(7, 3, 8), Blocks.IRON_ORE);

        BlockPos originAbs = helper.absolutePos(new BlockPos(7, 3, 7));
        boolean cast = TwinStarAbilities.castTwinStarQuarry(
                level, player, pick, ArtilianItems.TWIN_STAR_PICKAXE.get(), originAbs, Direction.NORTH);

        helper.assertTrue(cast, "Twin-Star Quarry should carve 3x3x2 pickaxe tunnel");
        helper.assertTrue(helper.getBlockState(new BlockPos(7, 3, 7)).isAir(), "Center front block should be carved");
        helper.assertTrue(helper.getBlockState(new BlockPos(7, 2, 8)).isAir(), "Back layer block should be carved");
        helper.assertTrue(helper.getBlockState(new BlockPos(7, 3, 8)).isAir(), "Back layer ore should be carved");
        helper.assertTrue(helper.getBlockState(new BlockPos(6, 2, 7)).is(Blocks.CHEST),
                "Twin-Star Quarry must never break BlockEntity chests");
        helper.assertTrue(helper.getBlockState(new BlockPos(8, 3, 7)).is(Blocks.OBSIDIAN),
                "Twin-Star Quarry must skip obsidian");

        List<ItemEntity> ingots = level.getEntitiesOfClass(
                ItemEntity.class, new AABB(originAbs).inflate(6.0), e -> e.getItem().is(Items.IRON_INGOT));
        helper.assertFalse(ingots.isEmpty(), "Twin-Star Quarry should auto-smelt mined iron ore");
        helper.assertTrue(ManaData.get(player).getMana() == 100 - TwinStarAbilities.TWIN_STAR_QUARRY_MANA,
                "Twin-Star Quarry should consume 30 Mana");
        helper.succeed();
    }

    @GameTest(template = "tnt_arena", timeoutTicks = 40)
    public static void sovereignGazeDamagesHostilesSparesKidSafeAndBuffsAllies(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        ServerPlayer player = spawnTestPlayer(helper, "AlexTwinAxe", new Vec3(7.5, 2.0, 7.5));
        Zombie zombie = helper.spawnWithNoFreeWill(EntityType.ZOMBIE, new BlockPos(11, 2, 7));
        Villager villager = helper.spawnWithNoFreeWill(EntityType.VILLAGER, new BlockPos(5, 2, 7));

        ChesterEntity chester = new ChesterEntity(ArtilianEntities.CHESTER.get(), level);
        Vec3 chesterPos = helper.absoluteVec(new Vec3(8.5, 2.0, 7.5));
        chester.setPos(chesterPos.x, chesterPos.y, chesterPos.z);
        chester.setOwner(player);
        level.addFreshEntity(chester);

        float zombieHpBefore = zombie.getHealth();
        float villagerHpBefore = villager.getHealth();
        float chesterHpBefore = chester.getHealth();

        boolean cast = TwinStarAbilities.castSovereignGaze(level, player, ArtilianItems.TWIN_STAR_AXE.get());
        helper.assertTrue(cast, "Sovereign Gaze should succeed with full Mana");
        helper.assertTrue(zombie.getHealth() < zombieHpBefore, "Sovereign Gaze should damage Zombie");
        helper.assertTrue(zombie.hasEffect(MobEffects.GLOWING), "Sovereign Gaze should apply Glowing");
        MobEffectInstance slow = zombie.getEffect(MobEffects.MOVEMENT_SLOWDOWN);
        helper.assertTrue(slow != null && slow.getAmplifier() >= 1,
                "Sovereign Gaze should apply Slowness II");
        helper.assertTrue(villager.getHealth() == villagerHpBefore && !villager.hasEffect(MobEffects.MOVEMENT_SLOWDOWN),
                "Sovereign Gaze must spare Villager");
        helper.assertTrue(chester.getHealth() == chesterHpBefore, "Sovereign Gaze must spare Chester");
        helper.assertTrue(chester.hasEffect(MobEffects.ABSORPTION),
                "Sovereign Gaze should grant Chester Absorption I");
        helper.assertTrue(player.hasEffect(MobEffects.ABSORPTION) && player.hasEffect(MobEffects.MOVEMENT_SPEED),
                "Sovereign Gaze should grant caster Absorption I + Speed I");
        helper.assertTrue(ManaData.get(player).getMana() == 100 - TwinStarAbilities.SOVEREIGN_GAZE_MANA,
                "Sovereign Gaze should consume 35 Mana");
        helper.succeed();
    }

    @GameTest(template = "tnt_arena", timeoutTicks = 40)
    public static void worldtreeHarvestFellsConnectedMatchingLogsOnly(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        ServerPlayer player = spawnTestPlayer(helper, "SteveWorldtree", new Vec3(5.5, 2.0, 5.5));
        ItemStack axe = new ItemStack(ArtilianItems.TWIN_STAR_AXE.get());

        for (int dy = 0; dy < 5; dy++) {
            helper.setBlock(new BlockPos(7, 2 + dy, 7), Blocks.OAK_LOG);
        }
        helper.setBlock(new BlockPos(8, 5, 7), Blocks.OAK_LOG);
        helper.setBlock(new BlockPos(8, 2, 7), Blocks.BIRCH_LOG);
        helper.setBlock(new BlockPos(6, 2, 7), Blocks.OAK_LEAVES);

        boolean cast = TwinStarAbilities.castWorldtreeHarvest(
                level, player, axe, ArtilianItems.TWIN_STAR_AXE.get(), helper.absolutePos(new BlockPos(7, 2, 7)));
        helper.assertTrue(cast, "Worldtree Harvest should fell connected oak logs");
        for (int dy = 0; dy < 5; dy++) {
            helper.assertTrue(helper.getBlockState(new BlockPos(7, 2 + dy, 7)).isAir(),
                    "Oak trunk log should be felled");
        }
        helper.assertTrue(helper.getBlockState(new BlockPos(8, 5, 7)).isAir(), "Oak branch log should be felled");
        helper.assertTrue(helper.getBlockState(new BlockPos(8, 2, 7)).is(Blocks.BIRCH_LOG),
                "Worldtree Harvest must leave non-matching log types untouched");
        helper.assertTrue(helper.getBlockState(new BlockPos(6, 2, 7)).is(Blocks.OAK_LEAVES),
                "Worldtree Harvest must leave leaves untouched");
        helper.assertTrue(ManaData.get(player).getMana() == 100 - TwinStarAbilities.WORLDTREE_HARVEST_MANA,
                "Worldtree Harvest should consume 25 Mana");
        helper.succeed();
    }

    @GameTest(template = "tnt_arena", timeoutTicks = 40)
    public static void eclipseExcavateClearsFiveByFiveShovelBlocksSafely(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        ServerPlayer player = spawnTestPlayer(helper, "AlexTwinShovel", new Vec3(4.5, 2.0, 4.5));
        ItemStack shovel = new ItemStack(ArtilianItems.TWIN_STAR_SHOVEL.get());

        for (int dx = -2; dx <= 2; dx++) {
            for (int dz = -2; dz <= 2; dz++) {
                helper.setBlock(new BlockPos(7 + dx, 2, 7 + dz), Blocks.SAND);
            }
        }
        helper.setBlock(new BlockPos(9, 2, 9), Blocks.STONE);

        boolean cast = TwinStarAbilities.castEclipseExcavate(
                level, player, shovel, ArtilianItems.TWIN_STAR_SHOVEL.get(),
                helper.absolutePos(new BlockPos(7, 2, 7)), Direction.UP);
        helper.assertTrue(cast, "Eclipse Excavate should clear matching 5x5 shovel face");
        helper.assertTrue(helper.getBlockState(new BlockPos(7, 2, 7)).isAir(), "Center sand should be cleared");
        helper.assertTrue(helper.getBlockState(new BlockPos(5, 2, 5)).isAir(), "Corner sand in 5x5 should be cleared");
        helper.assertTrue(helper.getBlockState(new BlockPos(9, 2, 9)).is(Blocks.STONE),
                "Eclipse Excavate must not break non-shovel blocks");
        helper.assertTrue(player.hasEffect(MobEffects.MOVEMENT_SPEED) && player.hasEffect(MobEffects.DIG_SPEED),
                "Eclipse Excavate should grant Speed II + Haste II");
        helper.assertTrue(ManaData.get(player).getMana() == 100 - TwinStarAbilities.ECLIPSE_EXCAVATE_MANA,
                "Eclipse Excavate should consume 25 Mana");
        helper.succeed();
    }

    @GameTest(template = "tnt_arena", timeoutTicks = 40)
    public static void twinRealmGenesisGrowsHydratesElevenByElevenAndReplantsNineByNine(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        ServerPlayer player = spawnTestPlayer(helper, "SteveTwinHoe", new Vec3(7.5, 2.0, 2.0));
        ItemStack hoe = new ItemStack(ArtilianItems.TWIN_STAR_HOE.get());

        // 11x11 area centered at (7, 2, 7): corners at (2..12, 2..12)
        BlockPos edgeDryFarm = new BlockPos(2, 1, 2);
        BlockPos edgeYoungCrop = new BlockPos(2, 2, 2);
        BlockPos matureCropPos = new BlockPos(11, 2, 11); // within 9x9 (dx=4, dz=4)

        for (int dx = -5; dx <= 5; dx++) {
            for (int dz = -5; dz <= 5; dz++) {
                helper.setBlock(new BlockPos(7 + dx, 1, 7 + dz), Blocks.FARMLAND);
            }
        }
        helper.setBlock(edgeYoungCrop, Blocks.WHEAT.defaultBlockState());
        helper.setBlock(matureCropPos, Blocks.WHEAT.defaultBlockState().setValue(CropBlock.AGE, CropBlock.MAX_AGE));

        boolean cast = TwinStarAbilities.castTwinRealmGenesis(
                level, player, hoe, ArtilianItems.TWIN_STAR_HOE.get(), helper.absolutePos(new BlockPos(7, 1, 7)));
        helper.assertTrue(cast, "Twin-Realm Genesis should succeed on farmland");
        helper.assertTrue(helper.getBlockState(edgeDryFarm).getValue(FarmBlock.MOISTURE) == FarmBlock.MAX_MOISTURE,
                "Twin-Realm Genesis should hydrate farmland at 11x11 corner");
        helper.assertTrue(helper.getBlockState(edgeYoungCrop).getValue(CropBlock.AGE) > 0,
                "Twin-Realm Genesis should grow young crop at 11x11 corner");

        BlockState replanted = helper.getBlockState(matureCropPos);
        helper.assertTrue(replanted.is(Blocks.WHEAT) && replanted.getValue(CropBlock.AGE) == 0,
                "Twin-Realm Genesis should auto-harvest and replant mature crop in 9x9");

        List<ItemEntity> wheatDrops = level.getEntitiesOfClass(
                ItemEntity.class,
                new AABB(helper.absolutePos(new BlockPos(7, 2, 7))).inflate(12.0),
                e -> e.getItem().is(Items.WHEAT));
        helper.assertFalse(wheatDrops.isEmpty(), "Twin-Realm Genesis should drop harvested wheat");
        MobEffectInstance regen = player.getEffect(MobEffects.REGENERATION);
        helper.assertTrue(regen != null && regen.getAmplifier() >= 1,
                "Twin-Realm Genesis should grant caster Regeneration II");
        helper.assertTrue(ManaData.get(player).getMana() == 100 - TwinStarAbilities.TWIN_REALM_GENESIS_MANA,
                "Twin-Realm Genesis should consume 30 Mana");
        helper.succeed();
    }
}
