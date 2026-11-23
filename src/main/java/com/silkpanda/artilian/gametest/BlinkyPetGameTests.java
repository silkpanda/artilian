package com.silkpanda.artilian.gametest;

import com.silkpanda.artilian.ArtilianMod;
import com.silkpanda.artilian.dimension.EyeRealmDimension;
import com.silkpanda.artilian.enemy.PeeperEntity;
import com.silkpanda.artilian.pet.BlinkyEntity;
import com.silkpanda.artilian.pet.CapturedPetData;
import com.silkpanda.artilian.pet.PetElement;
import com.silkpanda.artilian.registry.ArtilianBlocks;
import com.silkpanda.artilian.registry.ArtilianDataComponents;
import com.silkpanda.artilian.registry.ArtilianEntities;
import com.silkpanda.artilian.registry.ArtilianItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/** Phase 4 Stage 4: Blinky capturable Eye Realm pet, spawns, capture crystal, and elemental synergy. */
@GameTestHolder(ArtilianMod.MODID)
@PrefixGameTestTemplate(false)
public class BlinkyPetGameTests {

    @GameTest(template = "tnt_arena")
    public static void blinkyRegisteredWithEggElementalTypesAndHoverImmunity(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        EntityType<BlinkyEntity> type = ArtilianEntities.BLINKY.get();
        helper.assertTrue(SpawnEggItem.byId(type) != null, "Blinky must have a registered spawn egg");

        BlinkyEntity blinky = type.create(level);
        helper.assertTrue(blinky != null, "Blinky must instantiate cleanly");
        helper.assertTrue(blinky.getMaxHealth() == 26.0F
                        && blinky.getPrimaryElement() == PetElement.STARLIGHT
                        && blinky.getSecondaryElement().equals(Optional.of(PetElement.SHADOW)),
                "Blinky must be 26 HP STARLIGHT / SHADOW");
        helper.assertFalse(blinky.causeFallDamage(20.0F, 1.0F, level.damageSources().fall()),
                "Hovering Blinky must take zero fall damage");

        helper.succeed();
    }

    @GameTest(template = "tnt_arena")
    public static void eyeRealmBiomeRegistersBlinkyCreatureSpawnAndRules(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        Biome eyeBiome = level.registryAccess()
                .registryOrThrow(Registries.BIOME)
                .get(EyeRealmDimension.EYE_REALM_BIOME);
        helper.assertTrue(eyeBiome != null, "EYE_REALM_BIOME must be registered");

        Set<EntityType<?>> creatures = eyeBiome.getMobSettings()
                .getMobs(MobCategory.CREATURE)
                .unwrap()
                .stream()
                .map(data -> data.type)
                .collect(Collectors.toSet());
        helper.assertTrue(creatures.contains(ArtilianEntities.BLINKY.get()),
                "EYE_REALM_BIOME must register Blinky as a CREATURE spawn");

        BlockPos groundRel = new BlockPos(4, 1, 4);
        BlockPos spawnPos = helper.absolutePos(groundRel.above());
        helper.setBlock(groundRel, ArtilianBlocks.EYE_TURF.get());
        helper.assertTrue(SpawnPlacements.checkSpawnRules(
                        ArtilianEntities.BLINKY.get(), level, MobSpawnType.NATURAL, spawnPos, level.random),
                "Blinky must be allowed to spawn on Eye Turf");

        helper.setBlock(groundRel, ArtilianBlocks.GAZE_STONE.get());
        helper.assertTrue(SpawnPlacements.checkSpawnRules(
                        ArtilianEntities.BLINKY.get(), level, MobSpawnType.NATURAL, spawnPos, level.random),
                "Blinky must be allowed to spawn on Gaze Stone");

        helper.setBlock(groundRel, Blocks.STONE);
        helper.assertFalse(SpawnPlacements.checkSpawnRules(
                        ArtilianEntities.BLINKY.get(), level, MobSpawnType.NATURAL, spawnPos, level.random),
                "Blinky must not spawn on plain Stone");

        helper.succeed();
    }

    @GameTest(template = "tnt_arena")
    public static void blinkyCanBeCapturedAndIsSuperEffectiveAgainstPeeper(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        ItemStack crystal = new ItemStack(ArtilianItems.CAPTURE_CRYSTAL.get());
        player.setItemInHand(InteractionHand.MAIN_HAND, crystal);

        BlinkyEntity blinky = ArtilianEntities.BLINKY.get().create(level);
        helper.assertTrue(blinky != null, "Blinky must instantiate");
        BlockPos pos = helper.absolutePos(new BlockPos(4, 2, 4));
        blinky.moveTo(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, 0.0F, 0.0F);
        blinky.addAffinity(100);
        level.addFreshEntity(blinky);

        player.interactOn(blinky, InteractionHand.MAIN_HAND);
        CapturedPetData stored = crystal.get(ArtilianDataComponents.CAPTURED_PET.get());
        helper.assertTrue(stored != null
                        && stored.entityType().equals(ArtilianEntities.BLINKY.getId())
                        && stored.primaryElement() == PetElement.STARLIGHT,
                "Capture Crystal must store Blinky identity and STARLIGHT primary element");

        PeeperEntity peeper = ArtilianEntities.PEEPER.get().create(level);
        helper.assertTrue(peeper != null, "Peeper must instantiate");
        float multiplier = blinky.attackElementAgainst(peeper).multiplierAgainst(peeper);
        helper.assertTrue(Math.abs(multiplier - PetElement.SUPER_EFFECTIVE) < 0.01F,
                "Blinky STARLIGHT attack must be 1.5x super-effective against SHADOW Peeper, got " + multiplier);

        blinky.getPrimaryElement().applySignatureEffect(blinky, peeper, level);
        helper.assertTrue(peeper.hasEffect(MobEffects.GLOWING),
                "Blinky STARLIGHT signature hit must apply Glowing to Peeper");

        helper.succeed();
    }
}
