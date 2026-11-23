package com.silkpanda.artilian.gametest;

import com.silkpanda.artilian.ArtilianMod;
import com.silkpanda.artilian.boss.BossAltarBlockEntity;
import com.silkpanda.artilian.boss.GreatAllSeerEntity;
import com.silkpanda.artilian.boss.TelegraphedAttack;
import com.silkpanda.artilian.chester.ChesterEntity;
import com.silkpanda.artilian.pet.PetElement;
import com.silkpanda.artilian.pet.PetService;
import com.silkpanda.artilian.registry.ArtilianBlocks;
import com.silkpanda.artilian.registry.ArtilianDataComponents;
import com.silkpanda.artilian.registry.ArtilianEntities;
import com.silkpanda.artilian.registry.ArtilianItems;
import com.silkpanda.artilian.registry.ArtilianTags;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.BossEvent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import java.util.List;
import java.util.Optional;

/** Phase 4 Stage 6: The Great All-Seer boss, telegraphed attacks, Overdrive, loot, and Boss Altar rematch. */
@GameTestHolder(ArtilianMod.MODID)
@PrefixGameTestTemplate(false)
public class GreatAllSeerGameTests {

    private static List<GreatAllSeerEntity> seersNear(GameTestHelper helper, BlockPos absAltar) {
        return helper.getLevel().getEntitiesOfClass(GreatAllSeerEntity.class, new AABB(absAltar).inflate(6.0));
    }

    private static void cleanUpAltar(GameTestHelper helper, BlockPos absAltar, ServerPlayer player) {
        helper.getLevel().removeBlock(absAltar, false);
        seersNear(helper, absAltar).forEach(GreatAllSeerEntity::discard);
        CustomTntGameTests.removeTestPlayer(helper.getLevel(), player);
    }

    private static void checkOrCleanUp(GameTestHelper helper, BlockPos absAltar, ServerPlayer player, Runnable check) {
        try {
            check.run();
        } catch (RuntimeException | AssertionError failure) {
            cleanUpAltar(helper, absAltar, player);
            throw failure;
        }
    }

    private static void cleanUpEntities(ServerLevel level, GreatAllSeerEntity boss, ServerPlayer... players) {
        boss.discard();
        for (ServerPlayer player : players) {
            CustomTntGameTests.removeTestPlayer(level, player);
        }
    }

    private static int countItem(List<ItemStack> stacks, Item item) {
        int total = 0;
        for (ItemStack stack : stacks) {
            if (stack.is(item)) {
                total += stack.getCount();
            }
        }
        return total;
    }

    @GameTest(template = "structure_arena")
    public static void greatAllSeerAttributesElementsTagEggHoverAndLootTable(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        GreatAllSeerEntity boss = helper.spawn(ArtilianEntities.GREAT_ALL_SEER.get(), 9, 1, 9);
        boss.setNoAi(true);
        ServerPlayer player = CustomTntGameTests.spawnSurvivalPlayer(helper, "seer-tester", new Vec3(14.5, 1.0, 9.5));

        try {
            helper.assertTrue(boss.getPrimaryElement() == PetElement.SHADOW
                            && boss.getSecondaryElement().equals(Optional.of(PetElement.STARLIGHT)),
                    "Great All-Seer must be SHADOW / STARLIGHT");
            helper.assertTrue(boss.getMaxHealth() == 240.0F,
                    "Great All-Seer max health must be 240.0, got " + boss.getMaxHealth());
            helper.assertTrue(ArtilianEntities.GREAT_ALL_SEER.get().is(ArtilianTags.EntityTypes.BOSSES),
                    "GREAT_ALL_SEER must be in artilian:bosses");
            helper.assertTrue(SpawnEggItem.byId(ArtilianEntities.GREAT_ALL_SEER.get()) != null,
                    "GREAT_ALL_SEER must have a spawn egg");
            helper.assertTrue(PetService.isValidBattleTarget(player, boss),
                    "Tamed pets must be allowed to target The Great All-Seer");
            helper.assertTrue(!boss.causeFallDamage(12.0F, 1.0F, level.damageSources().fall()),
                    "Hovering Great All-Seer must be immune to fall damage");

            ItemStack emptyCrystal = new ItemStack(ArtilianItems.CAPTURE_CRYSTAL.get());
            player.setItemInHand(InteractionHand.MAIN_HAND, emptyCrystal);
            player.interactOn(boss, InteractionHand.MAIN_HAND);
            helper.assertTrue(boss.isAlive() && !emptyCrystal.has(ArtilianDataComponents.CAPTURED_PET.get()),
                    "Capture Crystal must never capture a boss");

            LootTable table = level.getServer().reloadableRegistries().getLootTable(boss.getLootTable());
            LootParams params = new LootParams.Builder(level)
                    .withParameter(LootContextParams.THIS_ENTITY, boss)
                    .withParameter(LootContextParams.ORIGIN, boss.position())
                    .withParameter(LootContextParams.DAMAGE_SOURCE, level.damageSources().playerAttack(player))
                    .withParameter(LootContextParams.ATTACKING_ENTITY, player)
                    .create(LootContextParamSets.ENTITY);
            List<ItemStack> drops = table.getRandomItems(params);

            int lenses = countItem(drops, ArtilianItems.ALL_SEERS_LENS.get());
            int starlight = countItem(drops, ArtilianItems.STARLIGHT_CRYSTAL.get());
            int amethyst = countItem(drops, Items.AMETHYST_SHARD);
            int pearls = countItem(drops, Items.ENDER_PEARL);
            int treats = countItem(drops, ArtilianItems.BIOME_TREAT.get());
            helper.assertTrue(lenses >= 2 && lenses <= 3, "Expected 2-3 All-Seer's Lenses, got " + lenses);
            helper.assertTrue(starlight >= 4 && starlight <= 6, "Expected 4-6 Starlight Crystals, got " + starlight);
            helper.assertTrue(amethyst >= 4 && amethyst <= 6, "Expected 4-6 Amethyst Shards, got " + amethyst);
            helper.assertTrue(pearls >= 1 && pearls <= 2, "Expected 1-2 Ender Pearls, got " + pearls);
            helper.assertTrue(treats >= 2 && treats <= 4, "Expected 2-4 Biome Treats, got " + treats);
            helper.succeed();
        } finally {
            cleanUpEntities(level, boss, player);
        }
    }

    @GameTest(template = "structure_arena", timeoutTicks = 120)
    public static void greatAllSeerTelegraphsGazingBeamTwilightPulseAndNova(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        GreatAllSeerEntity boss = helper.spawn(ArtilianEntities.GREAT_ALL_SEER.get(), 9, 1, 9);
        boss.setNoAi(true);
        ServerPlayer target = CustomTntGameTests.spawnSurvivalPlayer(helper, "seer-target", new Vec3(12.5, 1.0, 9.5));

        helper.runAfterDelay(61, () -> {
            try {
                for (TelegraphedAttack attack : List.of(
                        GreatAllSeerEntity.GAZING_BEAM,
                        GreatAllSeerEntity.TWILIGHT_PULSE,
                        GreatAllSeerEntity.ALL_SEEING_NOVA)) {
                    helper.assertTrue(attack.windupTicks() >= 30,
                            attack + " windup must be >= 30 ticks for kid readability");
                }

                helper.assertTrue(!boss.isAllSeeing(), "Great All-Seer must start out of Overdrive");
                helper.assertTrue(boss.getBossEvent().getColor() == BossEvent.BossBarColor.PINK,
                        "Phase 1 boss bar must be PINK");

                // 1. Twilight Pulse
                float hpBefore = target.getHealth();
                helper.assertTrue(boss.startTelegraph(GreatAllSeerEntity.TWILIGHT_PULSE), "Twilight Pulse must start");
                for (int t = 0; t < GreatAllSeerEntity.TWILIGHT_PULSE.windupTicks(); t++) {
                    boss.tickTelegraph();
                }
                helper.assertTrue(target.getHealth() < hpBefore, "Twilight Pulse must damage in-ring player");
                helper.assertTrue(target.hasEffect(MobEffects.MOVEMENT_SLOWDOWN) && target.hasEffect(MobEffects.WEAKNESS),
                        "Twilight Pulse must apply Slowness and Weakness");

                // 2. Gazing Beam locked onto target position
                float hpBeforeBeam = target.getHealth();
                Vec3 beamCenter = boss.telegraphCenterFor(GreatAllSeerEntity.GAZING_BEAM, target);
                helper.assertTrue(boss.startTelegraph(GreatAllSeerEntity.GAZING_BEAM, beamCenter), "Gazing Beam must start");
                for (int t = 0; t < GreatAllSeerEntity.GAZING_BEAM.windupTicks(); t++) {
                    boss.tickTelegraph();
                }
                helper.assertTrue(target.getHealth() < hpBeforeBeam, "Gazing Beam must damage in-ring player");
                helper.assertTrue(target.hasEffect(MobEffects.GLOWING), "Gazing Beam must apply Glowing");

                // 3. Half-health All-Seeing Overdrive & All-Seeing Nova
                boss.setHealth(boss.getMaxHealth() * 0.45F);
                boss.checkAllSeeing();
                helper.assertTrue(boss.isAllSeeing(), "Great All-Seer must enter Overdrive at <= 50% HP");
                helper.assertTrue(boss.getBossEvent().getColor() == BossEvent.BossBarColor.PURPLE,
                        "Overdrive boss bar must turn PURPLE");

                CompoundTag saved = new CompoundTag();
                boss.addAdditionalSaveData(saved);
                helper.assertTrue(saved.getBoolean("AllSeeing"), "AllSeeing flag must serialize to NBT");

                float hpBeforeNova = target.getHealth();
                helper.assertTrue(boss.startTelegraph(GreatAllSeerEntity.ALL_SEEING_NOVA), "All-Seeing Nova must start");
                for (int t = 0; t < GreatAllSeerEntity.ALL_SEEING_NOVA.windupTicks(); t++) {
                    boss.tickTelegraph();
                }
                helper.assertTrue(target.getHealth() < hpBeforeNova, "All-Seeing Nova must damage in-ring player");
                helper.succeed();
            } finally {
                cleanUpEntities(level, boss, target);
            }
        });
    }

    @GameTest(template = "structure_arena")
    public static void greatAllSeerTelegraphsDoNotBreakBlocksAndSpareChester(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        GreatAllSeerEntity boss = helper.spawn(ArtilianEntities.GREAT_ALL_SEER.get(), 9, 1, 9);
        boss.setNoAi(true);
        ChesterEntity chester = helper.spawn(ArtilianEntities.CHESTER.get(), 11, 1, 9);
        chester.setNoAi(true);

        BlockPos glassPos = new BlockPos(10, 1, 9);
        helper.setBlock(glassPos, Blocks.GLASS);

        try {
            float chesterHp = chester.getHealth();
            for (TelegraphedAttack attack : List.of(
                    GreatAllSeerEntity.TWILIGHT_PULSE,
                    GreatAllSeerEntity.GAZING_BEAM,
                    GreatAllSeerEntity.ALL_SEEING_NOVA)) {
                Vec3 center = attack == GreatAllSeerEntity.GAZING_BEAM ? chester.position() : boss.position();
                boss.startTelegraph(attack, center);
                for (int t = 0; t < attack.windupTicks(); t++) {
                    boss.tickTelegraph();
                }
            }
            helper.assertBlockPresent(Blocks.GLASS, glassPos);
            helper.assertTrue(chester.getHealth() == chesterHp && chester.isAlive(),
                    "Great All-Seer telegraphs must never harm Chester");
            helper.succeed();
        } finally {
            boss.discard();
            chester.discard();
        }
    }

    @GameTest(template = "structure_arena", timeoutTicks = 200)
    public static void bossAltarSummonsGreatAllSeerWithOcularKeyAndAllowsRematch(GameTestHelper helper) {
        BlockPos relAltar = new BlockPos(9, 1, 9);
        BlockPos absAltar = helper.absolutePos(relAltar);
        helper.setBlock(relAltar, ArtilianBlocks.BOSS_ALTAR.get());

        ServerPlayer player = CustomTntGameTests.spawnSurvivalPlayer(helper, "seer-summoner", new Vec3(7.5, 1.0, 9.5));
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(ArtilianItems.OCULAR_KEY.get(), 2));
        BossFrameworkGameTests.useAltar(helper, player, absAltar);

        checkOrCleanUp(helper, absAltar, player, () -> {
            BossAltarBlockEntity altar = helper.getBlockEntity(relAltar);
            helper.assertTrue(altar.isBusy(helper.getLevel()), "Altar must become busy after Ocular Key summon");
            helper.assertTrue(player.getMainHandItem().getCount() == 1, "Ocular Key must consume 1 item");
        });

        helper.runAfterDelay(BossAltarBlockEntity.COUNTDOWN_TICKS + 2, () -> checkOrCleanUp(helper, absAltar, player, () -> {
            List<GreatAllSeerEntity> first = seersNear(helper, absAltar);
            helper.assertTrue(first.size() == 1, "Expected 1 Great All-Seer after countdown, found " + first.size());

            // Busy lock refuses second summon
            BossFrameworkGameTests.useAltar(helper, player, absAltar);
            helper.assertTrue(player.getMainHandItem().getCount() == 1,
                    "Busy altar must not consume second Ocular Key");

            // Defeat boss -> altar unlocks for rematch
            first.get(0).discard();
            BossAltarBlockEntity altar = helper.getBlockEntity(relAltar);
            helper.assertTrue(!altar.isBusy(helper.getLevel()), "Altar must unlock once Great All-Seer is defeated");

            BossFrameworkGameTests.useAltar(helper, player, absAltar);
            helper.assertTrue(altar.isBusy(helper.getLevel()), "Altar must allow rematch summon after defeat");

            cleanUpAltar(helper, absAltar, player);
            helper.succeed();
        }));
    }
}
