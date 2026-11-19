package com.silkpanda.artilian.gametest;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.silkpanda.artilian.ArtilianMod;
import com.silkpanda.artilian.boss.BossAltarBlockEntity;
import com.silkpanda.artilian.boss.CaptainBrimstoneEntity;
import com.silkpanda.artilian.boss.TelegraphedAttack;
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
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/** Phase 3 Stage 3: Captain Brimstone boss, three telegraphed attacks, Phase 2 Broadside, loot, and assets. */
@GameTestHolder(ArtilianMod.MODID)
@PrefixGameTestTemplate(false)
public class CaptainBrimstoneGameTests {

    private static List<CaptainBrimstoneEntity> captainsNear(GameTestHelper helper, BlockPos absAltar) {
        return helper.getLevel().getEntitiesOfClass(CaptainBrimstoneEntity.class, new AABB(absAltar).inflate(6.0));
    }

    private static void cleanUpAltar(GameTestHelper helper, BlockPos absAltar, ServerPlayer player) {
        helper.getLevel().removeBlock(absAltar, false);
        captainsNear(helper, absAltar).forEach(CaptainBrimstoneEntity::discard);
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

    private static void cleanUpEntities(ServerLevel level, CaptainBrimstoneEntity boss, ServerPlayer... players) {
        boss.discard();
        for (ServerPlayer player : players) {
            CustomTntGameTests.removeTestPlayer(level, player);
        }
    }

    private static void checkOrCleanUpEntities(
            ServerLevel level, CaptainBrimstoneEntity boss, Runnable check, ServerPlayer... players) {
        try {
            check.run();
        } catch (RuntimeException | AssertionError failure) {
            cleanUpEntities(level, boss, players);
            throw failure;
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
    public static void captainBrimstoneAttributesElementsTagEggAndLootTable(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        CaptainBrimstoneEntity boss = helper.spawn(ArtilianEntities.CAPTAIN_BRIMSTONE.get(), 9, 1, 9);
        boss.setNoAi(true);
        ServerPlayer player = CustomTntGameTests.spawnSurvivalPlayer(helper, "brimstone-check", new Vec3(14.5, 1.0, 9.5));
        try {
            helper.assertTrue(boss.getMaxHealth() == 220.0F,
                    "Expected 220 max health, got " + boss.getMaxHealth());
            helper.assertTrue(boss.getPrimaryElement() == PetElement.FIRE,
                    "Expected FIRE primary element, got " + boss.getPrimaryElement());
            helper.assertTrue(boss.getSecondaryElement().equals(Optional.of(PetElement.SHADOW)),
                    "Expected SHADOW secondary element, got " + boss.getSecondaryElement());
            helper.assertTrue(ArtilianEntities.CAPTAIN_BRIMSTONE.get().is(ArtilianTags.EntityTypes.BOSSES),
                    "CAPTAIN_BRIMSTONE must be in artilian:bosses");
            helper.assertTrue(SpawnEggItem.byId(ArtilianEntities.CAPTAIN_BRIMSTONE.get()) != null,
                    "CAPTAIN_BRIMSTONE must have a spawn egg");
            helper.assertTrue(PetService.isValidBattleTarget(player, boss),
                    "Pets should be able to target Captain Brimstone");

            ItemStack emptyCrystal = new ItemStack(ArtilianItems.CAPTURE_CRYSTAL.get());
            player.setItemInHand(InteractionHand.MAIN_HAND, emptyCrystal);
            player.interactOn(boss, InteractionHand.MAIN_HAND);
            helper.assertTrue(boss.isAlive() && !emptyCrystal.has(ArtilianDataComponents.CAPTURED_PET.get()),
                    "Capture Crystal must never capture a boss");

            LootTable table = level.getServer().reloadableRegistries()
                    .getLootTable(ArtilianEntities.CAPTAIN_BRIMSTONE.get().getDefaultLootTable());
            LootParams params = new LootParams.Builder(level)
                    .withParameter(LootContextParams.THIS_ENTITY, boss)
                    .withParameter(LootContextParams.ORIGIN, boss.position())
                    .withParameter(LootContextParams.DAMAGE_SOURCE, level.damageSources().playerAttack(player))
                    .withOptionalParameter(LootContextParams.ATTACKING_ENTITY, player)
                    .create(LootContextParamSets.ENTITY);
            List<ItemStack> drops = table.getRandomItems(params);
            int doubloons = countItem(drops, ArtilianItems.BRIMSTONE_DOUBLOON.get());
            int sunsteel = countItem(drops, ArtilianItems.SUNSTEEL_INGOT.get());
            int blaze = countItem(drops, Items.BLAZE_POWDER);
            helper.assertTrue(doubloons >= 2 && doubloons <= 3,
                    "Expected 2-3 Brimstone Doubloons, got " + doubloons);
            helper.assertTrue(sunsteel >= 4 && blaze >= 4,
                    "Expected >= 4 Sunsteel Ingots and >= 4 Blaze Powder, got " + sunsteel + " / " + blaze);
        } finally {
            cleanUpEntities(level, boss, player);
        }
        helper.succeed();
    }

    @GameTest(template = "structure_arena", timeoutTicks = 140)
    public static void powderKegTossLocksTargetPositionAndCanBeDodged(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        CaptainBrimstoneEntity boss = helper.spawn(ArtilianEntities.CAPTAIN_BRIMSTONE.get(), 4, 1, 9);
        boss.setNoAi(true);
        ServerPlayer dodger = CustomTntGameTests.spawnSurvivalPlayer(helper, "keg-dodger", new Vec3(15.5, 1.0, 9.5));
        ServerPlayer stationary = CustomTntGameTests.spawnSurvivalPlayer(helper, "keg-stand", new Vec3(15.5, 1.0, 11.0));
        float dodgerMaxHp = dodger.getMaxHealth();
        float stationaryMaxHp = stationary.getMaxHealth();

        // Wait for 60-tick spawn protection to expire before checking telegraph damage.
        helper.runAfterDelay(61, () -> checkOrCleanUpEntities(level, boss, () -> {
            Vec3 initialTargetPos = dodger.position();
            Vec3 lockedCenter = boss.telegraphCenterFor(CaptainBrimstoneEntity.POWDER_KEG_TOSS, dodger);
            helper.assertTrue(lockedCenter.distanceTo(initialTargetPos) < 0.05,
                    "Powder Keg Toss center must lock onto target starting position, not boss");

            helper.assertTrue(boss.startTelegraph(CaptainBrimstoneEntity.POWDER_KEG_TOSS, lockedCenter),
                    "Powder Keg Toss must start when no telegraph is active");
            helper.assertTrue(boss.getTelegraphCenter().distanceTo(lockedCenter) < 0.05,
                    "Active telegraph center must match locked target coordinate");

            for (int tick = 0; tick < 20; tick++) {
                boss.tickTelegraph();
            }
            dodger.moveTo(helper.absoluteVec(new Vec3(2.5, 1.0, 2.5)));
            for (int tick = 0; tick < 19; tick++) {
                boss.tickTelegraph();
            }
            helper.assertTrue(stationary.getHealth() == stationaryMaxHp,
                    "Powder Keg Toss must deal 0 damage before tick 40");
            helper.assertTrue(boss.isTelegraphing(), "Powder Keg Toss must still be telegraphing at tick 39");

            boss.tickTelegraph();
            helper.assertFalse(boss.isTelegraphing(), "Powder Keg Toss must end on tick 40");
            helper.assertTrue(dodger.getHealth() == dodgerMaxHp,
                    "Player who stepped out of Powder Keg ring must take 0 damage");
            helper.assertTrue(stationary.getHealth() < stationaryMaxHp,
                    "Stationary player inside locked ring must take Powder Keg Toss damage");

            cleanUpEntities(level, boss, dodger, stationary);
            helper.succeed();
        }, dodger, stationary));
    }

    @GameTest(template = "structure_arena", timeoutTicks = 140)
    public static void captainBrimstoneEntersBroadsideAtHalfHealthAndUnlocksBarrage(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        CaptainBrimstoneEntity boss = helper.spawn(ArtilianEntities.CAPTAIN_BRIMSTONE.get(), 9, 1, 9);
        boss.setNoAi(true);
        ServerPlayer player = CustomTntGameTests.spawnSurvivalPlayer(helper, "broadside-target", new Vec3(14.5, 1.0, 9.5));

        checkOrCleanUpEntities(level, boss, () -> {
            helper.assertFalse(boss.isBroadsideActive(), "Captain Brimstone starts out of Broadside phase");
            helper.assertTrue(boss.getBossEvent().getColor() == BossEvent.BossBarColor.RED,
                    "Phase 1 Captain Brimstone boss bar must be RED");
            helper.assertTrue(boss.attackCooldownTicks() == 60, "Default attack cooldown should be 60t");
            for (int i = 0; i < 6; i++) {
                TelegraphedAttack pick = boss.chooseTelegraphedAttack(player);
                helper.assertTrue(pick != CaptainBrimstoneEntity.BROADSIDE_BARRAGE,
                        "Broadside Barrage must stay locked above 50% health");
            }

            boss.setHealth(110.0F);
            boss.checkBroadsidePhase();
            helper.assertTrue(boss.isBroadsideActive(), "Broadside phase must activate at <= 50% HP");
            helper.assertTrue(boss.getBossEvent().getColor() == BossEvent.BossBarColor.YELLOW,
                    "Broadside boss bar must turn YELLOW");
            helper.assertTrue(boss.attackCooldownTicks() == 45, "Broadside attack cooldown must tighten to 45t");

            CompoundTag saved = new CompoundTag();
            boss.addAdditionalSaveData(saved);
            CaptainBrimstoneEntity reloaded = ArtilianEntities.CAPTAIN_BRIMSTONE.get().create(level);
            helper.assertTrue(reloaded != null, "Reloaded Captain Brimstone must instantiate");
            reloaded.readAdditionalSaveData(saved);
            boolean reloadedBroadside = reloaded.isBroadsideActive();
            BossEvent.BossBarColor reloadedColor = reloaded.getBossEvent().getColor();
            reloaded.discard();
            helper.assertTrue(reloadedBroadside, "Broadside state must persist across NBT save/load");
            helper.assertTrue(reloadedColor == BossEvent.BossBarColor.YELLOW,
                    "Broadside YELLOW boss bar color must restore on NBT load");
        }, player);

        helper.runAfterDelay(61, () -> checkOrCleanUpEntities(level, boss, () -> {
            TelegraphedAttack selected = null;
            for (int i = 0; i < 3; i++) {
                TelegraphedAttack candidate = boss.chooseTelegraphedAttack(player);
                if (candidate == CaptainBrimstoneEntity.BROADSIDE_BARRAGE) {
                    selected = candidate;
                    break;
                }
            }
            helper.assertTrue(selected == CaptainBrimstoneEntity.BROADSIDE_BARRAGE,
                    "Broadside Captain Brimstone must select Broadside Barrage within its attack cycle");

            boss.startTelegraph(CaptainBrimstoneEntity.BROADSIDE_BARRAGE,
                    boss.telegraphCenterFor(CaptainBrimstoneEntity.BROADSIDE_BARRAGE, player));
            float beforeHp = player.getHealth();
            for (int tick = 0; tick < CaptainBrimstoneEntity.BROADSIDE_BARRAGE.windupTicks(); tick++) {
                boss.tickTelegraph();
            }
            helper.assertTrue(player.getHealth() < beforeHp,
                    "Broadside Barrage (radius 6.5) must hit a player standing 5.5 blocks away");

            cleanUpEntities(level, boss, player);
            helper.succeed();
        }, player));
    }

    @GameTest(template = "structure_arena", timeoutTicks = 200)
    public static void bossAltarSummonsCaptainBrimstoneWithCompassAndAllowsRematch(GameTestHelper helper) {
        helper.setBlock(BossFrameworkGameTests.ALTAR, ArtilianBlocks.BOSS_ALTAR.get());
        BlockPos altar = helper.absolutePos(BossFrameworkGameTests.ALTAR);
        ServerPlayer player = CustomTntGameTests.spawnSurvivalPlayer(helper, "brimstone-summoner", new Vec3(17.5, 1.0, 9.5));
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(ArtilianItems.BRIMSTONE_COMPASS.get(), 2));

        BossFrameworkGameTests.useAltar(helper, player, altar);
        checkOrCleanUp(helper, altar, player, () -> helper.assertTrue(player.getMainHandItem().getCount() == 1,
                "Using the altar must consume 1 Brimstone Compass, have " + player.getMainHandItem().getCount()));

        helper.runAfterDelay(BossAltarBlockEntity.COUNTDOWN_TICKS + 1, () -> checkOrCleanUp(helper, altar, player, () -> {
            List<CaptainBrimstoneEntity> first = captainsNear(helper, altar);
            helper.assertTrue(first.size() == 1, "Countdown must summon 1 Captain Brimstone, found " + first.size());

            BossFrameworkGameTests.useAltar(helper, player, altar);
            helper.assertTrue(player.getMainHandItem().getCount() == 1,
                    "Busy altar must refuse second Brimstone Compass without consuming it");

            ItemStack savedCompass = player.getMainHandItem().copy();
            player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(ArtilianItems.COLOSSUS_HEART.get(), 1));
            BossFrameworkGameTests.useAltar(helper, player, altar);
            helper.assertTrue(player.getMainHandItem().getCount() == 1,
                    "Busy altar must refuse Colossus Heart while Captain Brimstone lives");

            first.get(0).kill();
            player.setItemInHand(InteractionHand.MAIN_HAND, savedCompass);
            BossFrameworkGameTests.useAltar(helper, player, altar);
            helper.assertTrue(player.getMainHandItem().isEmpty(),
                    "After Captain Brimstone dies, altar must accept rematch and consume second Brimstone Compass");
        }));

        helper.runAfterDelay(2 * BossAltarBlockEntity.COUNTDOWN_TICKS + 4, () -> checkOrCleanUp(helper, altar, player, () -> {
            List<CaptainBrimstoneEntity> rematched = captainsNear(helper, altar);
            helper.assertTrue(rematched.size() == 1,
                    "Rematch countdown must summon 1 Captain Brimstone, found " + rematched.size());
            cleanUpAltar(helper, altar, player);
            helper.succeed();
        }));
    }

    @GameTest(template = "structure_arena")
    public static void captainBrimstoneGeckoLibAssetsAndGlowmaskExist(GameTestHelper helper) {
        String[] requiredAssets = {
                "assets/artilian/geo/entity/captain_brimstone.geo.json",
                "assets/artilian/animations/entity/captain_brimstone.animation.json",
                "assets/artilian/textures/entity/captain_brimstone.png",
                "assets/artilian/textures/entity/captain_brimstone_glowmask.png",
                "assets/artilian/textures/item/brimstone_compass.png",
                "assets/artilian/textures/item/brimstone_doubloon.png"
        };
        for (String path : requiredAssets) {
            helper.assertTrue(ArtilianDataComponents.class.getClassLoader().getResource(path) != null,
                    "Missing Captain Brimstone asset: " + path);
        }

        Set<String> boneNames = new HashSet<>();
        try (InputStream stream = ArtilianDataComponents.class.getClassLoader()
                .getResourceAsStream("assets/artilian/geo/entity/captain_brimstone.geo.json")) {
            helper.assertTrue(stream != null, "Cannot read captain_brimstone.geo.json");
            JsonObject root = JsonParser.parseReader(new InputStreamReader(stream, StandardCharsets.UTF_8)).getAsJsonObject();
            JsonArray bones = root.getAsJsonArray("minecraft:geometry").get(0).getAsJsonObject().getAsJsonArray("bones");
            for (JsonElement el : bones) {
                boneNames.add(el.getAsJsonObject().get("name").getAsString());
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        for (String requiredBone : List.of("root", "body", "head", "arm_left", "arm_right", "leg_left", "leg_right")) {
            helper.assertTrue(boneNames.contains(requiredBone),
                    "captain_brimstone.geo.json missing bone: " + requiredBone);
        }

        try (InputStream stream = ArtilianDataComponents.class.getClassLoader()
                .getResourceAsStream("assets/artilian/animations/entity/captain_brimstone.animation.json")) {
            helper.assertTrue(stream != null, "Cannot read captain_brimstone.animation.json");
            JsonObject anims = JsonParser.parseReader(new InputStreamReader(stream, StandardCharsets.UTF_8))
                    .getAsJsonObject().getAsJsonObject("animations");
            for (String anim : List.of(
                    "animation.captain_brimstone.idle",
                    "animation.captain_brimstone.walk",
                    "animation.captain_brimstone.attack",
                    "animation.captain_brimstone.telegraph")) {
                helper.assertTrue(anims.has(anim), "Missing animation: " + anim);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        helper.succeed();
    }
}
