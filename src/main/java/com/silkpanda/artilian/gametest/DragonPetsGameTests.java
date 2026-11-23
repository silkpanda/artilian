package com.silkpanda.artilian.gametest;

import com.silkpanda.artilian.ArtilianMod;
import com.silkpanda.artilian.dragon.DragonEggBlock;
import com.silkpanda.artilian.pet.AmberDragonlingEntity;
import com.silkpanda.artilian.pet.ArtilianPetEntity;
import com.silkpanda.artilian.pet.CanopyDragonlingEntity;
import com.silkpanda.artilian.pet.CaptureCrystalItem;
import com.silkpanda.artilian.pet.CapturedPetData;
import com.silkpanda.artilian.pet.PetDragonEntity;
import com.silkpanda.artilian.pet.PetElement;
import com.silkpanda.artilian.pet.StormDragonlingEntity;
import com.silkpanda.artilian.registry.ArtilianBlocks;
import com.silkpanda.artilian.registry.ArtilianDataComponents;
import com.silkpanda.artilian.registry.ArtilianEntities;
import com.silkpanda.artilian.registry.ArtilianItems;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.core.Direction;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import java.util.List;
import java.util.Optional;

/** Phase 5 Stage 6: Hatchable Pet Dragons & Dragon Eggs. */
@GameTestHolder(ArtilianMod.MODID)
@PrefixGameTestTemplate(false)
public class DragonPetsGameTests {

    @GameTest(template = "tnt_arena")
    public static void petDragonsRegisteredWithEggsElementsAndFallImmunity(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();

        CanopyDragonlingEntity canopy = ArtilianEntities.CANOPY_DRAGONLING.get().create(level);
        AmberDragonlingEntity amber = ArtilianEntities.AMBER_DRAGONLING.get().create(level);
        StormDragonlingEntity storm = ArtilianEntities.STORM_DRAGONLING.get().create(level);
        helper.assertTrue(canopy != null && amber != null && storm != null,
                "All three hatchable dragonlings must instantiate cleanly");

        helper.assertTrue(SpawnEggItem.byId(ArtilianEntities.CANOPY_DRAGONLING.get()) == ArtilianItems.CANOPY_DRAGONLING_SPAWN_EGG.get(),
                "Canopy Dragonling must have its spawn egg");
        helper.assertTrue(SpawnEggItem.byId(ArtilianEntities.AMBER_DRAGONLING.get()) == ArtilianItems.AMBER_DRAGONLING_SPAWN_EGG.get(),
                "Amber Dragonling must have its spawn egg");
        helper.assertTrue(SpawnEggItem.byId(ArtilianEntities.STORM_DRAGONLING.get()) == ArtilianItems.STORM_DRAGONLING_SPAWN_EGG.get(),
                "Storm Dragonling must have its spawn egg");

        helper.assertTrue(canopy.getPrimaryElement() == PetElement.DRAGON
                        && canopy.getSecondaryElement().equals(Optional.of(PetElement.NATURE)),
                "Canopy Dragonling must be DRAGON / NATURE");
        helper.assertTrue(amber.getPrimaryElement() == PetElement.DRAGON
                        && amber.getSecondaryElement().equals(Optional.of(PetElement.FIRE))
                        && amber.fireImmune(),
                "Amber Dragonling must be DRAGON / FIRE and fireImmune");
        helper.assertTrue(storm.getPrimaryElement() == PetElement.DRAGON
                        && storm.getSecondaryElement().equals(Optional.of(PetElement.ELECTRIC)),
                "Storm Dragonling must be DRAGON / ELECTRIC");

        helper.assertFalse(canopy.causeFallDamage(10.0F, 1.0F, level.damageSources().fall()),
                "Canopy Dragonling must not take fall damage");
        helper.assertFalse(amber.causeFallDamage(10.0F, 1.0F, level.damageSources().fall()),
                "Amber Dragonling must not take fall damage");
        helper.assertFalse(storm.causeFallDamage(10.0F, 1.0F, level.damageSources().fall()),
                "Storm Dragonling must not take fall damage");

        helper.succeed();
    }

    @GameTest(template = "tnt_arena")
    public static void wildDragonLootTablesCanDropMatchingDragonEggs(GameTestHelper helper) {
        assertLootContains(helper, ArtilianEntities.CANOPY_WYVERN.get(), ArtilianItems.CANOPY_WYVERN_EGG_ITEM.get());
        assertLootContains(helper, ArtilianEntities.AMBER_WYVERN.get(), ArtilianItems.AMBER_WYVERN_EGG_ITEM.get());
        assertLootContains(helper, ArtilianEntities.STORM_GLIDEWING.get(), ArtilianItems.STORM_GLIDEWING_EGG_ITEM.get());
        helper.succeed();
    }

    private static void assertLootContains(GameTestHelper helper, EntityType<?> entityType, Item expectedEgg) {
        ServerLevel level = helper.getLevel();
        var entity = entityType.create(level);
        helper.assertTrue(entity != null, "Entity must instantiate for loot check: " + entityType);
        entity.setPos(helper.absoluteVec(new Vec3(3.5, 2.0, 3.5)));

        LootTable lootTable = level.getServer().reloadableRegistries().getLootTable(entityType.getDefaultLootTable());
        LootParams params = new LootParams.Builder(level)
                .withParameter(LootContextParams.THIS_ENTITY, entity)
                .withParameter(LootContextParams.ORIGIN, entity.position())
                .withParameter(LootContextParams.DAMAGE_SOURCE, level.damageSources().generic())
                .create(LootContextParamSets.ENTITY);

        boolean found = false;
        for (int i = 0; i < 40 && !found; i++) {
            found = lootTable.getRandomItems(params).stream().anyMatch(stack -> stack.is(expectedEgg));
        }
        helper.assertTrue(found, entityType + " loot table must be able to drop " + expectedEgg);
    }

    @GameTest(template = "tnt_arena")
    public static void dragonEggIncubatesHatchesBefriendedDragonlingAndCapturesInCrystal(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos basePos = new BlockPos(3, 1, 3);
        BlockPos eggPos = basePos.above();

        helper.setBlock(basePos, ArtilianBlocks.AMBER_MOSS.get());
        helper.setBlock(eggPos, ArtilianBlocks.CANOPY_WYVERN_EGG.get());

        BlockPos absEggPos = helper.absolutePos(eggPos);
        BlockState state = level.getBlockState(absEggPos);
        DragonEggBlock eggBlock = (DragonEggBlock) state.getBlock();
        helper.assertTrue(
                eggBlock.getHatchIntervalTicks(level, absEggPos) == DragonEggBlock.WARM_HATCH_INTERVAL_TICKS,
                "Egg on Amber Moss must incubate at warm speed");

        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        ItemStack treats = new ItemStack(ArtilianItems.BIOME_TREAT.get(), 3);
        player.setItemInHand(InteractionHand.MAIN_HAND, treats);

        BlockHitResult hit = new BlockHitResult(Vec3.atCenterOf(absEggPos), Direction.UP, absEggPos, false);
        // Stage 0 -> 1
        level.getBlockState(absEggPos).useItemOn(treats, level, player, InteractionHand.MAIN_HAND, hit);
        helper.assertTrue(level.getBlockState(absEggPos).getValue(DragonEggBlock.HATCH) == 1,
                "First Biome Treat must advance hatch stage to 1");
        // Stage 1 -> 2
        level.getBlockState(absEggPos).useItemOn(treats, level, player, InteractionHand.MAIN_HAND, hit);
        helper.assertTrue(level.getBlockState(absEggPos).getValue(DragonEggBlock.HATCH) == 2,
                "Second Biome Treat must advance hatch stage to 2");
        // Stage 2 -> hatch
        level.getBlockState(absEggPos).useItemOn(treats, level, player, InteractionHand.MAIN_HAND, hit);
        helper.assertTrue(treats.isEmpty(), "Three Biome Treats must be consumed in survival");
        helper.assertTrue(level.getBlockState(absEggPos).is(Blocks.AIR),
                "Hatched egg block must be removed");

        List<CanopyDragonlingEntity> hatched = helper.getEntities(ArtilianEntities.CANOPY_DRAGONLING.get());
        helper.assertTrue(hatched.size() == 1, "Hatching must spawn exactly 1 Canopy Dragonling");
        CanopyDragonlingEntity dragonling = hatched.getFirst();
        helper.assertTrue(dragonling.isBefriended() && dragonling.getAffinity() == ArtilianPetEntity.MAX_AFFINITY,
                "Hatched dragonling must start befriended at max affinity");
        helper.assertTrue(dragonling.isPersistenceRequired(),
                "Hatched dragonling must have persistence required");

        // Capture into an empty Capture Crystal
        ItemStack crystal = new ItemStack(ArtilianItems.CAPTURE_CRYSTAL.get());
        player.setItemInHand(InteractionHand.MAIN_HAND, crystal);
        crystal.interactLivingEntity(player, dragonling, InteractionHand.MAIN_HAND);
        ItemStack held = player.getMainHandItem();
        CapturedPetData data = held.get(ArtilianDataComponents.CAPTURED_PET.get());
        helper.assertTrue(data != null
                        && data.entityType().equals(ArtilianEntities.CANOPY_DRAGONLING.getId())
                        && data.isOwnedBy(player.getUUID()),
                "Empty Capture Crystal must capture hatched dragonling cleanly");

        helper.succeed();
    }

    @GameTest(template = "tnt_arena")
    public static void amberAndStormEggsHatchMatchingDragonlings(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();

        BlockPos amberPos = helper.absolutePos(new BlockPos(2, 2, 2));
        level.setBlock(amberPos, ArtilianBlocks.AMBER_WYVERN_EGG.get().defaultBlockState(), 3);
        PetDragonEntity amber = ((DragonEggBlock) ArtilianBlocks.AMBER_WYVERN_EGG.get()).hatchEgg(level, amberPos, null);
        helper.assertTrue(amber instanceof AmberDragonlingEntity,
                "Amber Wyvern Egg must hatch Amber Dragonling");

        BlockPos stormPos = helper.absolutePos(new BlockPos(4, 2, 4));
        level.setBlock(stormPos, ArtilianBlocks.STORM_GLIDEWING_EGG.get().defaultBlockState(), 3);
        PetDragonEntity storm = ((DragonEggBlock) ArtilianBlocks.STORM_GLIDEWING_EGG.get()).hatchEgg(level, stormPos, null);
        helper.assertTrue(storm instanceof StormDragonlingEntity,
                "Storm Glidewing Egg must hatch Storm Dragonling");

        helper.succeed();
    }
}
