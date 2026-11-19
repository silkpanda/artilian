package com.silkpanda.artilian.gametest;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.silkpanda.artilian.ArtilianMod;
import com.silkpanda.artilian.boss.BossAltarBlockEntity;
import com.silkpanda.artilian.boss.CrystalColossusEntity;
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
import net.minecraft.world.effect.MobEffects;
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

/** Phase 3 Stage 2: Crystal Colossus boss, three telegraphed attacks, Phase 2 Overcharge, loot, and assets. */
@GameTestHolder(ArtilianMod.MODID)
@PrefixGameTestTemplate(false)
public class CrystalColossusGameTests {

    private static List<CrystalColossusEntity> colossiNear(GameTestHelper helper, BlockPos absAltar) {
        return helper.getLevel().getEntitiesOfClass(CrystalColossusEntity.class, new AABB(absAltar).inflate(6.0));
    }

    private static void cleanUpAltar(GameTestHelper helper, BlockPos absAltar, ServerPlayer player) {
        helper.getLevel().removeBlock(absAltar, false);
        colossiNear(helper, absAltar).forEach(CrystalColossusEntity::discard);
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

    private static void cleanUpEntities(ServerLevel level, CrystalColossusEntity boss, ServerPlayer... players) {
        boss.discard();
        for (ServerPlayer player : players) {
            CustomTntGameTests.removeTestPlayer(level, player);
        }
    }

    private static void checkOrCleanUpEntities(
            ServerLevel level, CrystalColossusEntity boss, Runnable check, ServerPlayer... players) {
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
    public static void crystalColossusAttributesElementsTagEggAndLootTable(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        CrystalColossusEntity boss = helper.spawn(ArtilianEntities.CRYSTAL_COLOSSUS.get(), 9, 1, 9);
        boss.setNoAi(true);
        ServerPlayer player = CustomTntGameTests.spawnSurvivalPlayer(helper, "colossus-tester", new Vec3(14.5, 1.0, 9.5));

        try {
            helper.assertTrue(boss.getPrimaryElement() == PetElement.CRYSTAL
                            && boss.getSecondaryElement().equals(Optional.of(PetElement.STARLIGHT)),
                    "Crystal Colossus must be CRYSTAL / STARLIGHT");
            helper.assertTrue(boss.getMaxHealth() == 200.0F,
                    "Crystal Colossus max health must be 200.0, got " + boss.getMaxHealth());
            helper.assertTrue(ArtilianEntities.CRYSTAL_COLOSSUS.get().is(ArtilianTags.EntityTypes.BOSSES),
                    "CRYSTAL_COLOSSUS must be in artilian:bosses");
            helper.assertTrue(SpawnEggItem.byId(ArtilianEntities.CRYSTAL_COLOSSUS.get()) != null,
                    "CRYSTAL_COLOSSUS must have a spawn egg");
            helper.assertTrue(PetService.isValidBattleTarget(player, boss),
                    "Tamed pets must be allowed to target the Crystal Colossus");

            ItemStack emptyCrystal = new ItemStack(ArtilianItems.CAPTURE_CRYSTAL.get());
            player.setItemInHand(InteractionHand.MAIN_HAND, emptyCrystal);
            player.interactOn(boss, InteractionHand.MAIN_HAND);
            helper.assertTrue(boss.isAlive() && !emptyCrystal.has(ArtilianDataComponents.CAPTURED_PET.get()),
                    "Capture Crystal must never capture a boss");

            LootTable lootTable = level.getServer().reloadableRegistries()
                    .getLootTable(ArtilianEntities.CRYSTAL_COLOSSUS.get().getDefaultLootTable());
            helper.assertTrue(lootTable != LootTable.EMPTY, "Crystal Colossus must have a non-empty loot table");
            LootParams params = new LootParams.Builder(level)
                    .withParameter(LootContextParams.THIS_ENTITY, boss)
                    .withParameter(LootContextParams.ORIGIN, boss.position())
                    .withParameter(LootContextParams.DAMAGE_SOURCE, level.damageSources().playerAttack(player))
                    .withOptionalParameter(LootContextParams.ATTACKING_ENTITY, player)
                    .create(LootContextParamSets.ENTITY);
            List<ItemStack> drops = lootTable.getRandomItems(params);

            int prismCores = countItem(drops, ArtilianItems.PRISM_CORE.get());
            helper.assertTrue(prismCores >= 2 && prismCores <= 3,
                    "Crystal Colossus must drop 2-3 Prism Cores, got " + prismCores);
            helper.assertTrue(countItem(drops, ArtilianItems.STARLIGHT_CRYSTAL.get()) >= 4,
                    "Crystal Colossus must drop Starlight Crystals");
            helper.assertTrue(countItem(drops, Items.AMETHYST_SHARD) >= 4,
                    "Crystal Colossus must drop Amethyst Shards");
            helper.assertTrue(countItem(drops, ArtilianItems.BIOME_TREAT.get()) >= 2,
                    "Crystal Colossus must drop Biome Treats");
        } finally {
            cleanUpEntities(level, boss, player);
        }
        helper.succeed();
    }

    @GameTest(template = "structure_arena", timeoutTicks = 140)
    public static void starlightSpireLocksTargetPositionAndCanBeDodged(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        CrystalColossusEntity boss = helper.spawn(ArtilianEntities.CRYSTAL_COLOSSUS.get(), 9, 1, 9);
        boss.setNoAi(true);
        ServerPlayer dodger = CustomTntGameTests.spawnSurvivalPlayer(helper, "spire-dodger", new Vec3(15.5, 1.0, 9.5));
        ServerPlayer stationary = CustomTntGameTests.spawnSurvivalPlayer(helper, "spire-stand", new Vec3(15.5, 1.0, 11.0));
        float dodgerMaxHp = dodger.getMaxHealth();
        float stationaryMaxHp = stationary.getMaxHealth();

        // Wait for 60-tick spawn protection to expire before checking telegraph damage.
        helper.runAfterDelay(61, () -> checkOrCleanUpEntities(level, boss, () -> {
            Vec3 initialTargetPos = dodger.position();
            Vec3 lockedCenter = boss.telegraphCenterFor(CrystalColossusEntity.STARLIGHT_SPIRE, dodger);
            helper.assertTrue(lockedCenter.distanceTo(initialTargetPos) < 0.05,
                    "Starlight Spire center must lock onto the target's position, not the boss");
            helper.assertTrue(lockedCenter.distanceTo(boss.position()) > 4.0,
                    "Starlight Spire test requires the target to stand well away from the boss");

            helper.assertTrue(boss.startTelegraph(CrystalColossusEntity.STARLIGHT_SPIRE, lockedCenter),
                    "Starlight Spire must start when no telegraph is active");
            helper.assertTrue(boss.getTelegraphCenter().distanceTo(lockedCenter) < 0.05,
                    "Active telegraph center must match the locked target coordinate");

            for (int tick = 0; tick < 25; tick++) {
                boss.tickTelegraph();
            }
            dodger.moveTo(helper.absoluteVec(new Vec3(2.5, 1.0, 2.5)));
            for (int tick = 0; tick < 19; tick++) {
                boss.tickTelegraph();
            }
            helper.assertTrue(stationary.getHealth() == stationaryMaxHp,
                    "Starlight Spire must deal 0 damage before tick 45, got " + stationary.getHealth());
            helper.assertTrue(boss.isTelegraphing(), "Starlight Spire must still be telegraphing at tick 44");

            boss.tickTelegraph();
            helper.assertFalse(boss.isTelegraphing(), "Starlight Spire must end on tick 45");
            helper.assertTrue(dodger.getHealth() == dodgerMaxHp,
                    "Player who stepped out of Starlight Spire ring must take 0 damage, got " + dodger.getHealth());
            helper.assertTrue(stationary.getHealth() < stationaryMaxHp,
                    "Stationary player inside the locked ring must take Starlight Spire damage");

            cleanUpEntities(level, boss, dodger, stationary);
            helper.succeed();
        }, dodger, stationary));
    }

    @GameTest(template = "structure_arena", timeoutTicks = 140)
    public static void crystalColossusEntersOverchargeAtHalfHealthAndUnlocksPrismNova(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        CrystalColossusEntity boss = helper.spawn(ArtilianEntities.CRYSTAL_COLOSSUS.get(), 9, 1, 9);
        boss.setNoAi(true);
        ServerPlayer player = CustomTntGameTests.spawnSurvivalPlayer(helper, "nova-target", new Vec3(15.5, 1.0, 9.5));

        checkOrCleanUpEntities(level, boss, () -> {
            helper.assertFalse(boss.isOvercharged(), "Full-health Colossus must not be overcharged");
            helper.assertTrue(boss.getBossEvent().getColor() == BossEvent.BossBarColor.BLUE,
                    "Phase 1 Crystal Colossus bar must be BLUE");
            helper.assertTrue(boss.attackCooldownTicks() == 60,
                    "Phase 1 attack cooldown must be 60 ticks, got " + boss.attackCooldownTicks());
            for (int i = 0; i < 6; i++) {
                TelegraphedAttack pick = boss.chooseTelegraphedAttack(player);
                helper.assertTrue(pick != CrystalColossusEntity.PRISM_NOVA,
                        "Prism Nova must stay locked above 50% health");
            }

            boss.setHealth(100.0F);
            boss.checkOvercharge();
            helper.assertTrue(boss.isOvercharged(), "Colossus must enter Crystal Overcharge at 50% HP");
            helper.assertTrue(boss.getBossEvent().getColor() == BossEvent.BossBarColor.PURPLE,
                    "Overcharged Crystal Colossus bar must turn PURPLE");
            helper.assertTrue(boss.attackCooldownTicks() == 45,
                    "Overcharged attack cooldown must tighten to 45 ticks, got " + boss.attackCooldownTicks());

            CompoundTag saved = new CompoundTag();
            boss.addAdditionalSaveData(saved);
            CrystalColossusEntity reloaded = ArtilianEntities.CRYSTAL_COLOSSUS.get().create(level);
            helper.assertTrue(reloaded != null, "Reloaded Crystal Colossus must instantiate");
            reloaded.readAdditionalSaveData(saved);
            boolean reloadedOvercharged = reloaded.isOvercharged();
            BossEvent.BossBarColor reloadedColor = reloaded.getBossEvent().getColor();
            reloaded.discard();
            helper.assertTrue(reloadedOvercharged, "Overcharged state must persist across NBT save/load");
            helper.assertTrue(reloadedColor == BossEvent.BossBarColor.PURPLE,
                    "Overcharged PURPLE boss bar color must persist across NBT save/load");
        }, player);

        helper.runAfterDelay(61, () -> checkOrCleanUpEntities(level, boss, () -> {
            TelegraphedAttack selected = null;
            for (int i = 0; i < 3; i++) {
                TelegraphedAttack candidate = boss.chooseTelegraphedAttack(player);
                if (candidate == CrystalColossusEntity.PRISM_NOVA) {
                    selected = candidate;
                    break;
                }
            }
            helper.assertTrue(selected == CrystalColossusEntity.PRISM_NOVA,
                    "Overcharged Colossus must select Prism Nova within its attack cycle");

            boss.startTelegraph(CrystalColossusEntity.PRISM_NOVA,
                    boss.telegraphCenterFor(CrystalColossusEntity.PRISM_NOVA, player));
            float beforeHp = player.getHealth();
            for (int tick = 0; tick < CrystalColossusEntity.PRISM_NOVA.windupTicks(); tick++) {
                boss.tickTelegraph();
            }
            helper.assertTrue(player.getHealth() < beforeHp,
                    "Prism Nova (radius 7) must hit a player standing 6 blocks away");
            helper.assertTrue(player.hasEffect(MobEffects.GLOWING),
                    "Prism Nova impact must apply Glowing");

            cleanUpEntities(level, boss, player);
            helper.succeed();
        }, player));
    }

    @GameTest(template = "structure_arena", timeoutTicks = 200)
    public static void bossAltarSummonsCrystalColossusWithColossusHeartAndAllowsRematch(GameTestHelper helper) {
        helper.setBlock(BossFrameworkGameTests.ALTAR, ArtilianBlocks.BOSS_ALTAR.get());
        BlockPos altar = helper.absolutePos(BossFrameworkGameTests.ALTAR);
        ServerPlayer player = CustomTntGameTests.spawnSurvivalPlayer(helper, "colossus-summoner", new Vec3(17.5, 1.0, 9.5));
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(ArtilianItems.COLOSSUS_HEART.get(), 2));

        BossFrameworkGameTests.useAltar(helper, player, altar);
        checkOrCleanUp(helper, altar, player, () -> helper.assertTrue(player.getMainHandItem().getCount() == 1,
                "Using the altar must consume 1 Colossus Heart, have " + player.getMainHandItem().getCount()));

        helper.runAfterDelay(BossAltarBlockEntity.COUNTDOWN_TICKS + 1, () -> checkOrCleanUp(helper, altar, player, () -> {
            List<CrystalColossusEntity> first = colossiNear(helper, altar);
            helper.assertTrue(first.size() == 1, "Countdown must summon 1 Crystal Colossus, found " + first.size());

            BossFrameworkGameTests.useAltar(helper, player, altar);
            helper.assertTrue(player.getMainHandItem().getCount() == 1,
                    "Busy altar must refuse second Colossus Heart without consuming it");

            ItemStack savedHeart = player.getMainHandItem().copy();
            player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(ArtilianItems.GOLEM_CORE.get(), 1));
            BossFrameworkGameTests.useAltar(helper, player, altar);
            helper.assertTrue(player.getMainHandItem().getCount() == 1,
                    "Busy altar must also refuse Golem Core while Crystal Colossus lives");

            first.get(0).kill();
            player.setItemInHand(InteractionHand.MAIN_HAND, savedHeart);
            BossFrameworkGameTests.useAltar(helper, player, altar);
            helper.assertTrue(player.getMainHandItem().isEmpty(),
                    "After Crystal Colossus dies, altar must accept rematch and consume second Colossus Heart");
        }));

        helper.runAfterDelay(2 * BossAltarBlockEntity.COUNTDOWN_TICKS + 4, () -> checkOrCleanUp(helper, altar, player, () -> {
            long living = colossiNear(helper, altar).stream().filter(CrystalColossusEntity::isAlive).count();
            helper.assertTrue(living == 1, "Rematch must summon a fresh Crystal Colossus, found " + living + " alive");
            cleanUpAltar(helper, altar, player);
            helper.succeed();
        }));
    }

    @GameTest(template = "empty")
    public static void crystalColossusGeckoLibAssetsAndGlowmaskExist(GameTestHelper helper) {
        Set<String> bones = readGeoBoneNames("assets/artilian/geo/entity/crystal_colossus.geo.json");
        for (String requiredBone : List.of(
                "root", "body", "head", "shoulder_left", "shoulder_right",
                "arm_left", "arm_right", "leg_left", "leg_right", "floating_shards")) {
            helper.assertTrue(bones.contains(requiredBone),
                    "crystal_colossus.geo.json missing required bone: " + requiredBone);
        }

        Set<String> animations = readAnimationNames("assets/artilian/animations/entity/crystal_colossus.animation.json");
        for (String requiredAnim : List.of(
                "animation.crystal_colossus.idle",
                "animation.crystal_colossus.walk",
                "animation.crystal_colossus.attack",
                "animation.crystal_colossus.telegraph")) {
            helper.assertTrue(animations.contains(requiredAnim),
                    "crystal_colossus.animation.json missing required clip: " + requiredAnim);
        }

        assertResourceExists(helper, "assets/artilian/textures/entity/crystal_colossus.png");
        assertResourceExists(helper, "assets/artilian/textures/entity/crystal_colossus_glowmask.png");
        assertResourceExists(helper, "assets/artilian/textures/item/colossus_heart.png");
        assertResourceExists(helper, "assets/artilian/textures/item/prism_core.png");
        helper.succeed();
    }

    private static void assertResourceExists(GameTestHelper helper, String resourcePath) {
        try (InputStream stream = CrystalColossusGameTests.class.getClassLoader().getResourceAsStream(resourcePath)) {
            helper.assertTrue(stream != null, "Missing Crystal Colossus art resource: " + resourcePath);
        } catch (IOException e) {
            helper.fail("Failed to read resource " + resourcePath + ": " + e.getMessage());
        }
    }

    private static Set<String> readGeoBoneNames(String resourcePath) {
        JsonObject root = readJson(resourcePath);
        JsonArray geos = root.getAsJsonArray("minecraft:geometry");
        Set<String> names = new HashSet<>();
        if (geos == null || geos.isEmpty()) {
            return names;
        }
        JsonArray bones = geos.get(0).getAsJsonObject().getAsJsonArray("bones");
        if (bones == null) {
            return names;
        }
        for (JsonElement element : bones) {
            names.add(element.getAsJsonObject().get("name").getAsString());
        }
        return names;
    }

    private static Set<String> readAnimationNames(String resourcePath) {
        JsonObject root = readJson(resourcePath);
        JsonObject animations = root.getAsJsonObject("animations");
        return animations != null ? animations.keySet() : Set.of();
    }

    private static JsonObject readJson(String resourcePath) {
        try (InputStream stream = CrystalColossusGameTests.class.getClassLoader().getResourceAsStream(resourcePath)) {
            if (stream == null) {
                throw new IllegalStateException("Missing JSON resource: " + resourcePath);
            }
            try (InputStreamReader reader = new InputStreamReader(stream, StandardCharsets.UTF_8)) {
                return JsonParser.parseReader(reader).getAsJsonObject();
            }
        } catch (IOException e) {
            throw new IllegalStateException("Failed to parse JSON resource: " + resourcePath, e);
        }
    }
}
