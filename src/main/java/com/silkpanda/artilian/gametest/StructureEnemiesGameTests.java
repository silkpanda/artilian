package com.silkpanda.artilian.gametest;

import com.silkpanda.artilian.ArtilianMod;
import com.silkpanda.artilian.enemy.AstralStalkerEntity;
import com.silkpanda.artilian.enemy.CinderRaiderEntity;
import com.silkpanda.artilian.enemy.PrismSentinelEntity;
import com.silkpanda.artilian.enemy.StructureEnemyEntity;
import com.silkpanda.artilian.pet.CrystalFoxEntity;
import com.silkpanda.artilian.pet.PetElement;
import com.silkpanda.artilian.pet.PetService;
import com.silkpanda.artilian.registry.ArtilianDataComponents;
import com.silkpanda.artilian.registry.ArtilianEntities;
import com.silkpanda.artilian.registry.ArtilianItems;
import com.silkpanda.artilian.registry.ArtilianTags;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.level.storage.loot.LootTable;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import java.util.List;
import java.util.Optional;

/** Phase 2 Stage 6 Stage 1: Custom structure enemies, tags, loot, eggs, and pet battle targeting. */
@GameTestHolder(ArtilianMod.MODID)
@PrefixGameTestTemplate(false)
public class StructureEnemiesGameTests {

    @GameTest(template = "tnt_arena")
    public static void structureEnemiesRegisteredTaggedHaveEggsAndElementalTypes(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();

        List<EntityType<? extends StructureEnemyEntity>> types = List.of(
                ArtilianEntities.PRISM_SENTINEL.get(),
                ArtilianEntities.CINDER_RAIDER.get(),
                ArtilianEntities.ASTRAL_STALKER.get()
        );
        for (EntityType<? extends StructureEnemyEntity> type : types) {
            helper.assertTrue(type.is(ArtilianTags.EntityTypes.STRUCTURE_ENEMIES),
                    type + " must be tagged artilian:structure_enemies");
            helper.assertTrue(SpawnEggItem.byId(type) != null,
                    type + " must have a registered spawn egg");
            var lootTable = level.getServer().reloadableRegistries().getLootTable(type.getDefaultLootTable());
            helper.assertTrue(lootTable != LootTable.EMPTY,
                    type + " must have a non-empty entity loot table");
        }

        PrismSentinelEntity sentinel = ArtilianEntities.PRISM_SENTINEL.get().create(level);
        CinderRaiderEntity raider = ArtilianEntities.CINDER_RAIDER.get().create(level);
        AstralStalkerEntity stalker = ArtilianEntities.ASTRAL_STALKER.get().create(level);
        helper.assertTrue(sentinel != null && raider != null && stalker != null,
                "All three structure enemies must instantiate cleanly");

        helper.assertTrue(sentinel.getPrimaryElement() == PetElement.CRYSTAL
                        && sentinel.getSecondaryElement().equals(Optional.of(PetElement.EARTH)),
                "Prism Sentinel must be CRYSTAL / EARTH");
        helper.assertTrue(raider.getPrimaryElement() == PetElement.FIRE
                        && raider.getSecondaryElement().equals(Optional.of(PetElement.EARTH))
                        && raider.fireImmune(),
                "Cinder Raider must be fire-immune FIRE / EARTH");
        helper.assertTrue(stalker.getPrimaryElement() == PetElement.STARLIGHT
                        && stalker.getSecondaryElement().equals(Optional.of(PetElement.SHADOW)),
                "Astral Stalker must be STARLIGHT / SHADOW");

        helper.assertTrue(PetElement.WATER.multiplierAgainst(raider) > PetElement.NEUTRAL,
                "Water pets must deal super-effective damage against Fire/Earth Cinder Raider");
        helper.assertTrue(PetElement.CRYSTAL.multiplierAgainst(stalker) > PetElement.NEUTRAL,
                "Crystal pets must deal super-effective damage against Starlight/Shadow Astral Stalker");

        helper.succeed();
    }

    @GameTest(template = "tnt_arena", timeoutTicks = 100)
    public static void tamedPetsEngageCustomStructureEnemiesAutonomously(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        ServerPlayer alex = ChesterTestSupport.playerInTestArea(helper, "AlexEnemy");

        PrismSentinelEntity sentinel = ArtilianEntities.PRISM_SENTINEL.get().create(level);
        CinderRaiderEntity raider = ArtilianEntities.CINDER_RAIDER.get().create(level);
        AstralStalkerEntity stalker = ArtilianEntities.ASTRAL_STALKER.get().create(level);
        helper.assertTrue(sentinel != null && raider != null && stalker != null,
                "All three enemies must exist for target validation");

        helper.assertTrue(PetService.isValidAutonomousBattleTarget(alex, sentinel)
                        && PetService.isValidAutonomousBattleTarget(alex, raider)
                        && PetService.isValidAutonomousBattleTarget(alex, stalker),
                "All three custom structure enemies must be valid autonomous battle targets");

        CrystalFoxEntity fox = summonTamedFox(helper, alex, new BlockPos(7, 0, 7));
        BlockPos enemyPos = helper.absolutePos(new BlockPos(9, 0, 7));
        sentinel.moveTo(enemyPos.getX() + 0.5, enemyPos.getY(), enemyPos.getZ() + 0.5, 0.0F, 0.0F);
        sentinel.setNoAi(true);
        level.addFreshEntity(sentinel);

        helper.succeedWhen(() -> {
            helper.assertTrue(sentinel.getHealth() < sentinel.getMaxHealth(),
                    "Expected tamed Crystal Fox to attack Prism Sentinel; target=" + fox.getTarget());
            fox.setSitting(true);
            helper.assertTrue(fox.getTarget() == null,
                    "Sitting pet must drop combat target immediately");
            sentinel.discard();
            fox.discard();
        });
    }

    @GameTest(template = "tnt_arena")
    public static void captureCrystalCannotCaptureStructureEnemies(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        ServerPlayer steve = ChesterTestSupport.playerInTestArea(helper, "SteveNoCap");

        CinderRaiderEntity raider = ArtilianEntities.CINDER_RAIDER.get().create(level);
        BlockPos pos = helper.absolutePos(new BlockPos(7, 0, 7));
        raider.moveTo(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, 0.0F, 0.0F);
        raider.setNoAi(true);
        level.addFreshEntity(raider);

        ItemStack emptyCrystal = new ItemStack(ArtilianItems.CAPTURE_CRYSTAL.get());
        steve.setItemInHand(InteractionHand.MAIN_HAND, emptyCrystal);
        steve.interactOn(raider, InteractionHand.MAIN_HAND);

        helper.assertTrue(raider.isAlive() && !emptyCrystal.has(ArtilianDataComponents.CAPTURED_PET.get()),
                "Structure enemies must never be capturable inside a Capture Crystal");

        raider.discard();
        helper.succeed();
    }

    private static CrystalFoxEntity summonTamedFox(GameTestHelper helper, ServerPlayer owner, BlockPos relPos) {
        CrystalFoxEntity wild = ArtilianEntities.CRYSTAL_FOX.get().create(helper.getLevel());
        BlockPos abs = helper.absolutePos(relPos);
        wild.moveTo(abs.getX() + 0.5, abs.getY(), abs.getZ() + 0.5, 0.0F, 0.0F);
        wild.addAffinity(100);
        helper.getLevel().addFreshEntity(wild);

        ItemStack crystal = new ItemStack(ArtilianItems.CAPTURE_CRYSTAL.get());
        owner.setItemInHand(InteractionHand.MAIN_HAND, crystal);
        owner.interactOn(wild, InteractionHand.MAIN_HAND);
        crystal.use(helper.getLevel(), owner, InteractionHand.MAIN_HAND);
        List<? extends CrystalFoxEntity> foxes = helper.getLevel().getEntities(ArtilianEntities.CRYSTAL_FOX.get(),
                fox -> fox.isAlive() && fox.isOwner(owner));
        CrystalFoxEntity summoned = foxes.get(0);
        summoned.moveTo(abs.getX() + 0.5, abs.getY(), abs.getZ() + 0.5, 0.0F, 0.0F);
        return summoned;
    }
}
