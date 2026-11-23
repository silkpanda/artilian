package com.silkpanda.artilian.gametest;

import com.silkpanda.artilian.ArtilianMod;
import com.silkpanda.artilian.boss.BossAltarBlockEntity;
import com.silkpanda.artilian.boss.EmbercrownSovereignEntity;
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

/** Phase 5 Stage 8: Embercrown Sovereign boss, telegraphed attacks, Primal Fury, loot, and Boss Altar rematch. */
@GameTestHolder(ArtilianMod.MODID)
@PrefixGameTestTemplate(false)
public class EmbercrownSovereignGameTests {

    private static List<EmbercrownSovereignEntity> sovereignsNear(GameTestHelper helper, BlockPos absAltar) {
        return helper.getLevel().getEntitiesOfClass(EmbercrownSovereignEntity.class, new AABB(absAltar).inflate(6.0));
    }

    private static void cleanUpAltar(GameTestHelper helper, BlockPos absAltar, ServerPlayer player) {
        helper.getLevel().removeBlock(absAltar, false);
        sovereignsNear(helper, absAltar).forEach(EmbercrownSovereignEntity::discard);
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

    private static void cleanUpEntities(ServerLevel level, EmbercrownSovereignEntity boss, ServerPlayer... players) {
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
    public static void embercrownSovereignAttributesElementsTagEggFireImmunityAndLootTable(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        EmbercrownSovereignEntity boss = helper.spawn(ArtilianEntities.EMBERCROWN_SOVEREIGN.get(), 9, 1, 9);
        boss.setNoAi(true);
        ServerPlayer player = CustomTntGameTests.spawnSurvivalPlayer(helper, "sovereign-tester", new Vec3(14.5, 1.0, 9.5));

        try {
            helper.assertTrue(boss.getPrimaryElement() == PetElement.DRAGON
                            && boss.getSecondaryElement().equals(Optional.of(PetElement.FIRE)),
                    "Embercrown Sovereign must be DRAGON / FIRE");
            helper.assertTrue(boss.getMaxHealth() == 300.0F,
                    "Embercrown Sovereign max health must be 300.0, got " + boss.getMaxHealth());
            helper.assertTrue(ArtilianEntities.EMBERCROWN_SOVEREIGN.get().is(ArtilianTags.EntityTypes.BOSSES),
                    "EMBERCROWN_SOVEREIGN must be in artilian:bosses");
            helper.assertTrue(SpawnEggItem.byId(ArtilianEntities.EMBERCROWN_SOVEREIGN.get()) != null,
                    "EMBERCROWN_SOVEREIGN must have a spawn egg");
            helper.assertTrue(boss.fireImmune(), "Embercrown Sovereign must be fire-immune");
            helper.assertTrue(!boss.causeFallDamage(12.0F, 1.0F, level.damageSources().fall()),
                    "Embercrown Sovereign must be immune to fall damage");
            helper.assertTrue(PetService.isValidBattleTarget(player, boss),
                    "Tamed pets must be allowed to target Embercrown Sovereign");
            helper.assertTrue(new ItemStack(ArtilianItems.SOVEREIGNS_EMBER_HEART.get()).is(ArtilianTags.Items.BOSS_DROPS),
                    "Sovereign's Ember Heart must be in artilian:boss_drops");

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

            int hearts = countItem(drops, ArtilianItems.SOVEREIGNS_EMBER_HEART.get());
            int scales = countItem(drops, ArtilianItems.DRAGON_SCALE.get());
            int moss = countItem(drops, ArtilianItems.AMBER_MOSS_ITEM.get());
            int starlight = countItem(drops, ArtilianItems.STARLIGHT_CRYSTAL.get());
            helper.assertTrue(hearts == 1, "Expected 1 Sovereign's Ember Heart, got " + hearts);
            helper.assertTrue(scales >= 3 && scales <= 6, "Expected 3-6 Dragon Scales, got " + scales);
            helper.assertTrue(moss >= 2 && moss <= 4, "Expected 2-4 Amber Moss, got " + moss);
            helper.assertTrue(starlight >= 1 && starlight <= 2, "Expected 1-2 Starlight Crystals, got " + starlight);
            helper.succeed();
        } finally {
            cleanUpEntities(level, boss, player);
        }
    }

    @GameTest(template = "structure_arena", timeoutTicks = 120)
    public static void embercrownSovereignTelegraphsBreathRoarAndPrimalFuryMeteorSlam(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        EmbercrownSovereignEntity boss = helper.spawn(ArtilianEntities.EMBERCROWN_SOVEREIGN.get(), 9, 1, 9);
        boss.setNoAi(true);
        ServerPlayer target = CustomTntGameTests.spawnSurvivalPlayer(helper, "sovereign-target", new Vec3(12.5, 1.0, 9.5));

        helper.runAfterDelay(61, () -> {
            try {
                for (TelegraphedAttack attack : List.of(
                        EmbercrownSovereignEntity.EMBER_BREATH,
                        EmbercrownSovereignEntity.SOVEREIGN_ROAR,
                        EmbercrownSovereignEntity.METEOR_SLAM)) {
                    helper.assertTrue(attack.windupTicks() >= 30,
                            attack + " windup must be >= 30 ticks for kid readability");
                }

                helper.assertTrue(!boss.isPrimalFury(), "Embercrown Sovereign must start out of Primal Fury");
                helper.assertTrue(boss.getBossEvent().getColor() == BossEvent.BossBarColor.YELLOW,
                        "Phase 1 boss bar must be YELLOW");

                // 1. Sovereign Roar
                float hpBefore = target.getHealth();
                helper.assertTrue(boss.startTelegraph(EmbercrownSovereignEntity.SOVEREIGN_ROAR),
                        "Sovereign Roar must start");
                for (int t = 0; t < EmbercrownSovereignEntity.SOVEREIGN_ROAR.windupTicks(); t++) {
                    boss.tickTelegraph();
                }
                helper.assertTrue(target.getHealth() < hpBefore, "Sovereign Roar must damage in-ring player");
                helper.assertTrue(target.hasEffect(MobEffects.WEAKNESS), "Sovereign Roar must apply Weakness");

                // 2. Ember Breath locked onto target position
                float hpBeforeBreath = target.getHealth();
                Vec3 breathCenter = boss.telegraphCenterFor(EmbercrownSovereignEntity.EMBER_BREATH, target);
                helper.assertTrue(boss.startTelegraph(EmbercrownSovereignEntity.EMBER_BREATH, breathCenter),
                        "Ember Breath must start");
                for (int t = 0; t < EmbercrownSovereignEntity.EMBER_BREATH.windupTicks(); t++) {
                    boss.tickTelegraph();
                }
                helper.assertTrue(target.getHealth() < hpBeforeBreath, "Ember Breath must damage in-ring player");
                helper.assertTrue(target.getRemainingFireTicks() > 0, "Ember Breath must ignite in-ring player");

                // 3. Half-health Primal Fury & Meteor Slam
                boss.setHealth(boss.getMaxHealth() * 0.45F);
                boss.checkPrimalFury();
                helper.assertTrue(boss.isPrimalFury(), "Embercrown Sovereign must enter Primal Fury at <= 50% HP");
                helper.assertTrue(boss.getBossEvent().getColor() == BossEvent.BossBarColor.RED,
                        "Primal Fury boss bar must turn RED");

                CompoundTag saved = new CompoundTag();
                boss.addAdditionalSaveData(saved);
                helper.assertTrue(saved.getBoolean("PrimalFury"), "PrimalFury flag must serialize to NBT");

                float hpBeforeSlam = target.getHealth();
                helper.assertTrue(boss.startTelegraph(EmbercrownSovereignEntity.METEOR_SLAM),
                        "Meteor Slam must start");
                for (int t = 0; t < EmbercrownSovereignEntity.METEOR_SLAM.windupTicks(); t++) {
                    boss.tickTelegraph();
                }
                helper.assertTrue(target.getHealth() < hpBeforeSlam, "Meteor Slam must damage in-ring player");
                helper.succeed();
            } finally {
                cleanUpEntities(level, boss, target);
            }
        });
    }

    @GameTest(template = "structure_arena")
    public static void embercrownSovereignTelegraphsDoNotBreakBlocksAndSpareChester(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        EmbercrownSovereignEntity boss = helper.spawn(ArtilianEntities.EMBERCROWN_SOVEREIGN.get(), 9, 1, 9);
        boss.setNoAi(true);
        ChesterEntity chester = helper.spawn(ArtilianEntities.CHESTER.get(), 11, 1, 9);
        chester.setNoAi(true);

        BlockPos glassPos = new BlockPos(10, 1, 9);
        helper.setBlock(glassPos, Blocks.GLASS);

        try {
            float chesterHp = chester.getHealth();
            for (TelegraphedAttack attack : List.of(
                    EmbercrownSovereignEntity.SOVEREIGN_ROAR,
                    EmbercrownSovereignEntity.EMBER_BREATH,
                    EmbercrownSovereignEntity.METEOR_SLAM)) {
                Vec3 center = attack == EmbercrownSovereignEntity.EMBER_BREATH ? chester.position() : boss.position();
                boss.startTelegraph(attack, center);
                for (int t = 0; t < attack.windupTicks(); t++) {
                    boss.tickTelegraph();
                }
            }
            helper.assertBlockPresent(Blocks.GLASS, glassPos);
            helper.assertTrue(chester.getHealth() == chesterHp && chester.isAlive(),
                    "Embercrown Sovereign telegraphs must never harm Chester");
            helper.succeed();
        } finally {
            boss.discard();
            chester.discard();
        }
    }

    @GameTest(template = "structure_arena", timeoutTicks = 200)
    public static void bossAltarSummonsEmbercrownSovereignWithPrimalDragonCrestAndAllowsRematch(GameTestHelper helper) {
        BlockPos relAltar = new BlockPos(9, 1, 9);
        BlockPos absAltar = helper.absolutePos(relAltar);
        helper.setBlock(relAltar, ArtilianBlocks.BOSS_ALTAR.get());

        ServerPlayer player = CustomTntGameTests.spawnSurvivalPlayer(helper, "sovereign-summoner", new Vec3(7.5, 1.0, 9.5));
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(ArtilianItems.PRIMAL_DRAGON_CREST.get(), 2));
        BossFrameworkGameTests.useAltar(helper, player, absAltar);

        checkOrCleanUp(helper, absAltar, player, () -> {
            BossAltarBlockEntity altar = helper.getBlockEntity(relAltar);
            helper.assertTrue(altar.isBusy(helper.getLevel()), "Altar must become busy after Primal Dragon Crest summon");
            helper.assertTrue(player.getMainHandItem().getCount() == 1, "Primal Dragon Crest must consume 1 item");
        });

        helper.runAfterDelay(BossAltarBlockEntity.COUNTDOWN_TICKS + 2, () -> checkOrCleanUp(helper, absAltar, player, () -> {
            List<EmbercrownSovereignEntity> first = sovereignsNear(helper, absAltar);
            helper.assertTrue(first.size() == 1,
                    "Expected 1 Embercrown Sovereign after countdown, found " + first.size());

            // Busy lock refuses second summon
            BossFrameworkGameTests.useAltar(helper, player, absAltar);
            helper.assertTrue(player.getMainHandItem().getCount() == 1,
                    "Busy altar must not consume second Primal Dragon Crest");

            // Defeat boss -> altar unlocks for rematch
            first.get(0).discard();
            BossAltarBlockEntity altar = helper.getBlockEntity(relAltar);
            helper.assertTrue(!altar.isBusy(helper.getLevel()),
                    "Altar must unlock once Embercrown Sovereign is defeated");

            BossFrameworkGameTests.useAltar(helper, player, absAltar);
            helper.assertTrue(altar.isBusy(helper.getLevel()), "Altar must allow rematch summon after defeat");

            cleanUpAltar(helper, absAltar, player);
            helper.succeed();
        }));
    }
}
