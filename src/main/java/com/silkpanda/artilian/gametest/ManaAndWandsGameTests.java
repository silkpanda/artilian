package com.silkpanda.artilian.gametest;

import com.silkpanda.artilian.ArtilianMod;
import com.silkpanda.artilian.chester.ChesterEntity;
import com.silkpanda.artilian.chester.ChesterService;
import com.silkpanda.artilian.mana.ManaData;
import com.silkpanda.artilian.mana.ManaService;
import com.silkpanda.artilian.mana.ManaSyncPayload;
import com.silkpanda.artilian.registry.ArtilianItems;
import io.netty.channel.embedded.EmbeddedChannel;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Wolf;
import net.minecraft.world.entity.monster.Skeleton;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.util.FakePlayer;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import net.neoforged.neoforge.network.registration.NetworkRegistry;

import java.util.List;

/** Phase 1 Stage 5: Auto-regenerating Mana attachment, sync, and starter Wands & Staves tests. */
@GameTestHolder(ArtilianMod.MODID)
@PrefixGameTestTemplate(false)
public class ManaAndWandsGameTests {

    private static ServerPlayer spawnTestPlayer(GameTestHelper helper, String name, Vec3 relPos) {
        ServerPlayer player = CustomTntGameTests.spawnSurvivalPlayer(helper, name, relPos);
        NetworkRegistry.configureMockConnection(player.connection.getConnection());
        return player;
    }

    @GameTest(template = "tnt_arena", timeoutTicks = 80)
    public static void manaAttachmentPersistsRegeneratesAndSyncsToClient(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        ServerPlayer alice = spawnTestPlayer(helper, "AliceMana", new Vec3(7.5, 2.0, 7.5));

        ManaData data = ManaData.get(alice);
        helper.assertTrue(data.getMana() == ManaData.DEFAULT_MAX_MANA
                        && data.getMaxMana() == ManaData.DEFAULT_MAX_MANA,
                "Player must start with 100/100 Mana");

        boolean spent = ManaService.tryConsumeMana(alice, 40);
        helper.assertTrue(spent, "Should be able to spend 40 Mana from 100");
        helper.assertTrue(data.getMana() == 60, "Expected 60 Mana after spending 40, got " + data.getMana());
        helper.assertTrue(data.getRegenCooldownTicks() == ManaData.REGEN_DELAY_AFTER_CAST_TICKS,
                "Expected post-cast regen delay of 20 ticks");
        helper.assertTrue(data.getSyncCount() >= 1 && data.getLastSyncedMana() == 60,
                "Expected sync to client after Mana spend");

        // Verify a ClientboundCustomPayloadPacket(ManaSyncPayload) was queued on the player's channel.
        boolean sawManaPacket = false;
        if (alice.connection.getConnection().channel() instanceof EmbeddedChannel embedded) {
            for (Object msg : embedded.outboundMessages()) {
                if (msg instanceof ClientboundCustomPayloadPacket packet
                        && packet.payload() instanceof ManaSyncPayload payload
                        && payload.mana() == 60 && payload.maxMana() == 100) {
                    sawManaPacket = true;
                    break;
                }
            }
            embedded.outboundMessages().clear();
        }
        helper.assertTrue(sawManaPacket, "Expected ManaSyncPayload(60, 100) packet on player connection");

        // Verify NBT persistence round-trip preserves Mana values.
        CompoundTag saved = data.serializeNBT(level.registryAccess());
        ManaData restored = new ManaData();
        restored.deserializeNBT(level.registryAccess(), saved);
        helper.assertTrue(restored.getMana() == 60 && restored.getMaxMana() == 100
                        && restored.getRegenCooldownTicks() == ManaData.REGEN_DELAY_AFTER_CAST_TICKS,
                "ManaData NBT serialization must preserve mana, maxMana, and regen cooldown");
        ServerPlayer respawned = new FakePlayer(level, alice.getGameProfile());
        respawned.restoreFrom(alice, false);
        helper.assertTrue(ManaData.get(respawned).getMana() == 60 && ManaData.get(respawned).getMaxMana() == 100,
                "MANA attachment must copy data on death");

        int syncCountBeforeRegen = data.getSyncCount();
        helper.runAfterDelay(35, () -> {
            helper.assertTrue(data.getMana() >= 65,
                    "Mana must auto-regenerate after delay + regen interval (expected >= 65, got "
                            + data.getMana() + ")");
            helper.assertTrue(data.getSyncCount() > syncCountBeforeRegen && data.getLastSyncedMana() == data.getMana(),
                    "Server must sync Mana to client when auto-regeneration increases Mana");

            CustomTntGameTests.removeTestPlayer(level, alice);
            helper.succeed();
        });
    }

    @GameTest(template = "empty")
    public static void wandRecipesExistAndInsufficientManaBlocksCast(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        RecipeManager recipes = level.getRecipeManager();
        for (String path : List.of("spark_wand", "heart_bloom_staff", "zephyr_dash_wand")) {
            var recipe = recipes.byKey(ArtilianMod.id(path));
            helper.assertTrue(recipe.isPresent(), "Missing wand/staff recipe: " + path);
        }

        ServerPlayer bob = spawnTestPlayer(helper, "BobLowMana", new Vec3(1.5, 1.0, 1.5));
        ManaData data = ManaData.get(bob);
        data.setMana(10);

        ItemStack sparkWand = new ItemStack(ArtilianItems.SPARK_WAND.get());
        bob.setItemInHand(InteractionHand.MAIN_HAND, sparkWand);
        InteractionResultHolder<ItemStack> result = sparkWand.use(level, bob, InteractionHand.MAIN_HAND);

        helper.assertFalse(result.getResult().consumesAction(),
                "Spark Wand must fail to cast when Mana (10) is below cost (20)");
        helper.assertTrue(data.getMana() == 10,
                "Failed cast must not deduct Mana, got " + data.getMana());
        helper.assertFalse(bob.getCooldowns().isOnCooldown(ArtilianItems.SPARK_WAND.get()),
                "Failed cast must not trigger item cooldown");

        CustomTntGameTests.removeTestPlayer(level, bob);
        helper.succeed();
    }

    private static void buildStoneFloor(GameTestHelper helper) {
        for (int x = 2; x <= 12; x++) {
            for (int z = 2; z <= 12; z++) {
                helper.setBlock(new BlockPos(x, 1, z), Blocks.STONE);
            }
        }
    }

    @GameTest(template = "tnt_arena", timeoutTicks = 40)
    public static void sparkWandZapsHostilesWithChainSparkWhileSparingPlayersChesterAndPets(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        buildStoneFloor(helper);

        ServerPlayer alice = spawnTestPlayer(helper, "AliceSpark", new Vec3(3.5, 2.0, 7.5));
        ServerPlayer bob = spawnTestPlayer(helper, "BobAlly", new Vec3(6.0, 2.0, 7.5));
        ChesterEntity chester = ChesterService.summon(alice);
        Vec3 chesterAbs = helper.absoluteVec(new Vec3(8.5, 2.0, 6.5));
        chester.moveTo(chesterAbs.x, chesterAbs.y, chesterAbs.z, 0.0F, 0.0F);
        float chesterStartHp = chester.getHealth();

        Wolf wolf = helper.spawnWithNoFreeWill(EntityType.WOLF, new Vec3(8.5, 2.0, 8.5));
        wolf.tame(alice);
        float wolfStartHp = wolf.getHealth();

        Villager villager = helper.spawnWithNoFreeWill(EntityType.VILLAGER, new Vec3(5.0, 2.0, 7.5));
        float villagerStartHp = villager.getHealth();

        Zombie primary = helper.spawnWithNoFreeWill(EntityType.ZOMBIE, new Vec3(8.5, 2.0, 7.5));
        primary.setHealth(20.0F);
        Zombie chain1 = helper.spawnWithNoFreeWill(EntityType.ZOMBIE, new Vec3(10.5, 2.0, 9.0));
        chain1.setHealth(20.0F);
        Skeleton chain2 = helper.spawnWithNoFreeWill(EntityType.SKELETON, new Vec3(10.5, 2.0, 6.0));
        chain2.setHealth(20.0F);

        alice.lookAt(EntityAnchorArgument.Anchor.EYES, primary.getEyePosition());
        ItemStack sparkWand = new ItemStack(ArtilianItems.SPARK_WAND.get());
        alice.setItemInHand(InteractionHand.MAIN_HAND, sparkWand);
        InteractionResultHolder<ItemStack> result = sparkWand.use(level, alice, InteractionHand.MAIN_HAND);

        helper.assertTrue(result.getResult().consumesAction(), "Spark Wand cast should succeed");
        helper.assertTrue(ManaData.get(alice).getMana() == 80,
                "Spark Wand should consume 20 Mana (expected 80, got " + ManaData.get(alice).getMana() + ")");
        helper.assertTrue(alice.getCooldowns().isOnCooldown(ArtilianItems.SPARK_WAND.get()),
                "Spark Wand should go on cooldown after cast");

        helper.assertTrue(primary.getHealth() <= 14.0F,
                "Spark Wand primary hit must deal 6 magic damage, hp=" + primary.getHealth());
        helper.assertTrue(primary.hasEffect(MobEffects.GLOWING) && primary.hasEffect(MobEffects.MOVEMENT_SLOWDOWN),
                "Spark Wand primary hit must apply Glowing and Slowness");

        helper.assertTrue(chain1.getHealth() <= 16.0F && chain1.hasEffect(MobEffects.GLOWING),
                "Spark Wand must chain 4 magic damage + Glowing to nearby zombie, hp=" + chain1.getHealth());
        helper.assertTrue(chain2.getHealth() <= 16.0F && chain2.hasEffect(MobEffects.GLOWING),
                "Spark Wand must chain 4 magic damage + Glowing to nearby skeleton, hp=" + chain2.getHealth());

        helper.assertTrue(bob.getHealth() == bob.getMaxHealth() && !bob.hasEffect(MobEffects.GLOWING),
                "Spark Wand must strictly spare Players");
        helper.assertTrue(chester.isAlive() && chester.getHealth() == chesterStartHp
                        && !chester.hasEffect(MobEffects.GLOWING),
                "Spark Wand must strictly spare Chester");
        helper.assertTrue(wolf.isAlive() && wolf.getHealth() == wolfStartHp && !wolf.hasEffect(MobEffects.GLOWING),
                "Spark Wand must strictly spare tamed pets");
        helper.assertTrue(villager.isAlive() && villager.getHealth() == villagerStartHp
                        && !villager.hasEffect(MobEffects.GLOWING),
                "Spark Wand must strictly spare villagers");

        chester.discard();
        wolf.discard();
        villager.discard();
        primary.discard();
        chain1.discard();
        chain2.discard();
        CustomTntGameTests.removeTestPlayer(level, alice);
        CustomTntGameTests.removeTestPlayer(level, bob);
        helper.succeed();
    }

    @GameTest(template = "tnt_arena", timeoutTicks = 40)
    public static void heartBloomStaffHealsAndShieldsPlayersChesterAndPetsWhileIgnoringHostiles(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        buildStoneFloor(helper);

        ServerPlayer alice = spawnTestPlayer(helper, "AliceBloom", new Vec3(7.5, 2.0, 7.5));
        alice.setHealth(10.0F);
        alice.setRemainingFireTicks(100);
        alice.addEffect(new MobEffectInstance(MobEffects.POISON, 120, 0));
        alice.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 120, 0));

        ServerPlayer bob = spawnTestPlayer(helper, "BobBloom", new Vec3(9.5, 2.0, 7.5));
        bob.setHealth(10.0F);
        bob.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 120, 0));

        ChesterEntity chester = ChesterService.summon(alice);
        Vec3 chesterAbs = helper.absoluteVec(new Vec3(7.5, 2.0, 9.5));
        chester.moveTo(chesterAbs.x, chesterAbs.y, chesterAbs.z, 0.0F, 0.0F);
        chester.setHealth(10.0F);

        Wolf wolf = helper.spawnWithNoFreeWill(EntityType.WOLF, new Vec3(7.5, 2.0, 5.5));
        wolf.tame(alice);
        wolf.setHealth(4.0F);

        Zombie zombie = helper.spawnWithNoFreeWill(EntityType.ZOMBIE, new Vec3(5.5, 2.0, 7.5));
        zombie.setHealth(10.0F);

        ItemStack staff = new ItemStack(ArtilianItems.HEART_BLOOM_STAFF.get());
        alice.setItemInHand(InteractionHand.MAIN_HAND, staff);
        InteractionResultHolder<ItemStack> result = staff.use(level, alice, InteractionHand.MAIN_HAND);

        helper.assertTrue(result.getResult().consumesAction(), "Heart-Bloom Staff cast should succeed");
        helper.assertTrue(ManaData.get(alice).getMana() == 70,
                "Heart-Bloom Staff should consume 30 Mana (expected 70, got " + ManaData.get(alice).getMana() + ")");

        helper.assertTrue(alice.getHealth() >= 16.0F,
                "Heart-Bloom Staff must heal caster by 6 HP, got " + alice.getHealth());
        helper.assertTrue(alice.hasEffect(MobEffects.REGENERATION) && alice.hasEffect(MobEffects.ABSORPTION),
                "Heart-Bloom Staff must grant Regeneration II and Absorption to caster");
        helper.assertFalse(alice.hasEffect(MobEffects.POISON) || alice.hasEffect(MobEffects.MOVEMENT_SLOWDOWN),
                "Heart-Bloom Staff must clear negative effects on caster");
        helper.assertTrue(alice.getRemainingFireTicks() <= 0,
                "Heart-Bloom Staff must extinguish fire on caster");

        helper.assertTrue(bob.getHealth() >= 16.0F
                        && bob.hasEffect(MobEffects.REGENERATION)
                        && bob.hasEffect(MobEffects.ABSORPTION)
                        && !bob.hasEffect(MobEffects.WEAKNESS),
                "Heart-Bloom Staff must heal, shield, and cleanse nearby Players");
        helper.assertTrue(chester.getHealth() >= 16.0F && chester.hasEffect(MobEffects.REGENERATION),
                "Heart-Bloom Staff must heal and buff Chester");
        helper.assertTrue(wolf.getHealth() >= 10.0F && wolf.hasEffect(MobEffects.REGENERATION),
                "Heart-Bloom Staff must heal and buff tamed pets");

        helper.assertTrue(zombie.getHealth() == 10.0F
                        && !zombie.hasEffect(MobEffects.REGENERATION)
                        && !zombie.hasEffect(MobEffects.ABSORPTION),
                "Heart-Bloom Staff must ignore hostile monsters, zombie hp=" + zombie.getHealth());

        chester.discard();
        wolf.discard();
        zombie.discard();
        CustomTntGameTests.removeTestPlayer(level, alice);
        CustomTntGameTests.removeTestPlayer(level, bob);
        helper.succeed();
    }

    @GameTest(template = "tnt_arena", timeoutTicks = 40)
    public static void zephyrDashWandPropelsCasterWithSlowFallingAndRepelsHostilesWithoutMovingSleepingChester(
            GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        buildStoneFloor(helper);

        ServerPlayer bob = spawnTestPlayer(helper, "BobDash", new Vec3(7.5, 2.0, 7.5));
        bob.setYRot(-90.0F);
        bob.setXRot(0.0F);
        bob.fallDistance = 12.0F;

        ServerPlayer alice = spawnTestPlayer(helper, "AliceFriend", new Vec3(7.5, 2.0, 5.5));
        Vec3 aliceStart = alice.position();

        ChesterEntity sleepingChester = ChesterService.summon(alice);
        Vec3 chesterAbs = helper.absoluteVec(new Vec3(7.5, 2.0, 9.5));
        sleepingChester.moveTo(chesterAbs.x, chesterAbs.y, chesterAbs.z, 0.0F, 0.0F);
        sleepingChester.toggleSitAndSleep(alice);
        helper.assertTrue(sleepingChester.isAsleep(), "Chester should be asleep before Zephyr Dash");

        Wolf wolf = helper.spawnWithNoFreeWill(EntityType.WOLF, new Vec3(6.0, 2.0, 7.5));
        wolf.tame(bob);
        Vec3 wolfStart = wolf.position();

        Zombie zombie = helper.spawnWithNoFreeWill(EntityType.ZOMBIE, new Vec3(9.5, 2.0, 7.5));

        ItemStack zephyrWand = new ItemStack(ArtilianItems.ZEPHYR_DASH_WAND.get());
        bob.setItemInHand(InteractionHand.MAIN_HAND, zephyrWand);
        InteractionResultHolder<ItemStack> result = zephyrWand.use(level, bob, InteractionHand.MAIN_HAND);

        helper.assertTrue(result.getResult().consumesAction(), "Zephyr Dash Wand cast should succeed");
        helper.assertTrue(ManaData.get(bob).getMana() == 75,
                "Zephyr Dash Wand should consume 25 Mana (expected 75, got " + ManaData.get(bob).getMana() + ")");

        Vec3 dashVel = bob.getDeltaMovement();
        helper.assertTrue(dashVel.x > 0.8 && dashVel.y >= 0.5,
                "Zephyr Dash must propel caster forward and upward, got " + dashVel);
        helper.assertTrue(bob.fallDistance == 0.0F,
                "Zephyr Dash must reset fallDistance");
        helper.assertTrue(bob.hasEffect(MobEffects.SLOW_FALLING) && bob.hasEffect(MobEffects.MOVEMENT_SPEED),
                "Zephyr Dash must grant Slow Falling and Speed II");

        Vec3 zombieVel = zombie.getDeltaMovement();
        helper.assertTrue(zombieVel.x > 0.4 && zombieVel.y > 0.2,
                "Zephyr Dash gust must repel nearby hostile mobs, got " + zombieVel);

        helper.assertTrue(sleepingChester.getDeltaMovement().horizontalDistance() < 0.05
                        && sleepingChester.distanceToSqr(chesterAbs) < 0.05,
                "Zephyr Dash must not push sleeping Chester");
        helper.assertTrue(alice.getDeltaMovement().horizontalDistance() < 0.05
                        && alice.distanceToSqr(aliceStart) < 0.05,
                "Zephyr Dash must not push nearby Players");
        helper.assertTrue(wolf.getDeltaMovement().horizontalDistance() < 0.05
                        && wolf.distanceToSqr(wolfStart) < 0.05,
                "Zephyr Dash must not push tamed pets");

        sleepingChester.discard();
        wolf.discard();
        zombie.discard();
        CustomTntGameTests.removeTestPlayer(level, bob);
        CustomTntGameTests.removeTestPlayer(level, alice);
        helper.succeed();
    }
}
