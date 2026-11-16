package com.silkpanda.artilian.gametest;

import com.mojang.authlib.GameProfile;
import com.silkpanda.artilian.ArtilianMod;
import com.silkpanda.artilian.chester.ChesterEntity;
import com.silkpanda.artilian.chester.ChesterService;
import com.silkpanda.artilian.config.ArtilianConfig;
import com.silkpanda.artilian.registry.ArtilianBlocks;
import com.silkpanda.artilian.registry.ArtilianEntities;
import com.silkpanda.artilian.tnt.ArtilianPrimedTnt;
import com.silkpanda.artilian.tnt.ArtilianTntBlock;
import com.silkpanda.artilian.tnt.TntVariant;
import io.netty.channel.embedded.EmbeddedChannel;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Pig;
import net.minecraft.world.entity.animal.Wolf;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import java.util.List;
import java.util.UUID;

/** Phase 1 Stage 4: Custom TNTs (Party, Bounce, Frost Nova, Cluster Quarry) and telegraph tests. */
@GameTestHolder(ArtilianMod.MODID)
@PrefixGameTestTemplate(false)
public class CustomTntGameTests {

    /**
     * Spawns a survival-mode {@link ServerPlayer} into the level without firing login events so
     * explosion damage, knockback, and potion effects behave identically to live play.
     */
    public static ServerPlayer spawnSurvivalPlayer(GameTestHelper helper, String name, Vec3 relPos) {
        ServerLevel level = helper.getLevel();
        CommonListenerCookie cookie = CommonListenerCookie.createInitial(
                new GameProfile(UUID.randomUUID(), name), false);
        ServerPlayer player = new ServerPlayer(
                level.getServer(), level, cookie.gameProfile(), cookie.clientInformation()) {
            @Override
            public boolean isSpectator() {
                return false;
            }

            @Override
            public boolean isCreative() {
                return false;
            }
        };
        Connection connection = new Connection(PacketFlow.SERVERBOUND);
        new EmbeddedChannel(connection);
        player.connection = new ServerGamePacketListenerImpl(level.getServer(), connection, player, cookie);
        Vec3 abs = helper.absoluteVec(relPos);
        player.moveTo(abs.x, abs.y, abs.z, 0.0F, 0.0F);
        level.addNewPlayer(player);
        return player;
    }

    public static void removeTestPlayer(ServerLevel level, ServerPlayer player) {
        if (!player.isRemoved()) {
            level.removePlayerImmediately(player, Entity.RemovalReason.DISCARDED);
        }
    }

    @GameTest(template = "empty")
    public static void defaultConfigIsKidSafeAndRecipesExist(GameTestHelper helper) {
        helper.assertFalse(ArtilianConfig.ALLOW_DESTRUCTIVE_CUSTOM_TNT.getDefault(),
                "Default allowDestructiveCustomTnt config must be false for kid-safe play");
        helper.assertFalse(ArtilianConfig.isDestructiveTntAllowed(),
                "isDestructiveTntAllowed() must be false under default config");

        RecipeManager recipes = helper.getLevel().getRecipeManager();
        for (String path : List.of("party_tnt", "bounce_tnt", "frost_nova_tnt", "cluster_quarry_tnt")) {
            ResourceLocation id = ArtilianMod.id(path);
            var recipe = recipes.byKey(id);
            helper.assertTrue(recipe.isPresent(), "Missing custom TNT recipe: " + id);
            helper.assertTrue(recipe.get().value().getResultItem(helper.getLevel().registryAccess()).getCount() == 2,
                    "Expected recipe " + id + " to craft 2 blocks");
        }
        helper.succeed();
    }

    @GameTest(template = "tnt_arena", timeoutTicks = 100)
    public static void primedCustomTntEmitsTelegraphBeforeDetonation(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos floorPos = new BlockPos(7, 1, 7);
        helper.setBlock(floorPos, Blocks.STONE);
        BlockPos tntPos = floorPos.above();
        helper.setBlock(tntPos, ArtilianBlocks.PARTY_TNT.get());

        BlockPos absPos = helper.absolutePos(tntPos);
        ArtilianTntBlock block = ArtilianBlocks.PARTY_TNT.get();
        ArtilianPrimedTnt primed = block.prime(level, absPos, null);
        level.removeBlock(absPos, false);

        helper.assertTrue(primed != null && primed.getType() == ArtilianEntities.PRIMED_TNT.get(),
                "Custom TNT block must prime an ArtilianPrimedTnt entity");
        primed.setFuse(35);
        helper.assertFalse(primed.isTelegraphActive(), "Telegraph should not start before final 30 ticks");
        helper.assertTrue(primed.getVariant() == TntVariant.PARTY, "Expected PARTY variant");

        helper.runAfterDelay(5, () -> {
            helper.assertTrue(primed.isTelegraphActive(), "Telegraph should be active at fuse 30");
            float earlyPitch = primed.getTelegraphPitch();
            helper.assertTrue(primed.getTelegraphPulsesEmitted() >= 1,
                    "Expected at least 1 telegraph pulse by fuse 30");

            helper.runAfterDelay(25, () -> {
                float latePitch = primed.getTelegraphPitch();
                helper.assertTrue(latePitch > earlyPitch + 0.5F,
                        "Expected rising telegraph pitch (" + earlyPitch + " -> " + latePitch + ")");
                helper.assertTrue(primed.getTelegraphPulsesEmitted() >= 5,
                        "Expected >= 5 telegraph pulses over the 1.5s telegraph window, got "
                                + primed.getTelegraphPulsesEmitted());
                helper.succeed();
            });
        });
    }

    @GameTest(template = "tnt_arena", timeoutTicks = 60)
    public static void partyTntBuffsPlayersWithoutDamageOrBlockBreak(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        for (int x = 5; x <= 9; x++) {
            for (int z = 5; z <= 9; z++) {
                helper.setBlock(new BlockPos(x, 1, z), Blocks.GLASS);
            }
        }
        BlockPos planksPos = new BlockPos(7, 2, 5);
        helper.setBlock(planksPos, Blocks.OAK_PLANKS);

        ServerPlayer alice = spawnSurvivalPlayer(helper, "AliceParty", new Vec3(5.5, 2.0, 7.5));
        alice.setHealth(12.0F);
        Pig pig = helper.spawnWithNoFreeWill(EntityType.PIG, new Vec3(9.5, 2.0, 7.5));

        Vec3 tntAbs = helper.absoluteVec(new Vec3(7.5, 2.0, 7.5));
        ArtilianPrimedTnt partyTnt = new ArtilianPrimedTnt(
                level, tntAbs.x, tntAbs.y, tntAbs.z, alice, TntVariant.PARTY);
        partyTnt.setFuse(5);
        level.addFreshEntity(partyTnt);

        helper.runAfterDelay(10, () -> {
            helper.assertTrue(alice.hasEffect(MobEffects.MOVEMENT_SPEED),
                    "Party TNT should grant Speed to nearby players");
            helper.assertTrue(alice.hasEffect(MobEffects.REGENERATION),
                    "Party TNT should grant Regeneration to nearby players");
            helper.assertTrue(alice.getHealth() >= 12.0F,
                    "Party TNT must deal zero damage, health was " + alice.getHealth());
            helper.assertTrue(pig.hasEffect(MobEffects.MOVEMENT_SPEED),
                    "Party TNT should buff nearby living entities too");
            helper.assertBlockPresent(Blocks.OAK_PLANKS, planksPos);
            helper.assertBlockPresent(Blocks.GLASS, new BlockPos(7, 1, 7));

            removeTestPlayer(level, alice);
            pig.discard();
            helper.succeed();
        });
    }

    @GameTest(template = "tnt_arena", timeoutTicks = 60)
    public static void bounceTntLaunchesEntitiesWithSlowFallingAndLeavesSleepingChesterPut(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        for (int x = 4; x <= 10; x++) {
            for (int z = 4; z <= 10; z++) {
                helper.setBlock(new BlockPos(x, 1, z), Blocks.STONE);
            }
        }

        ServerPlayer bob = spawnSurvivalPlayer(helper, "BobBounce", new Vec3(5.5, 2.0, 7.5));
        ServerPlayer alice = spawnSurvivalPlayer(helper, "AliceBounce", new Vec3(2.5, 2.0, 2.5));
        Pig pig = helper.spawnWithNoFreeWill(EntityType.PIG, new Vec3(9.5, 2.0, 7.5));

        ChesterEntity awakeChester = ChesterService.summon(bob);
        Vec3 awakeAbs = helper.absoluteVec(new Vec3(7.5, 2.0, 5.5));
        awakeChester.moveTo(awakeAbs.x, awakeAbs.y, awakeAbs.z, 0.0F, 0.0F);

        ChesterEntity sleepingChester = ChesterService.summon(alice);
        Vec3 sleepAbs = helper.absoluteVec(new Vec3(7.5, 2.0, 9.5));
        sleepingChester.moveTo(sleepAbs.x, sleepAbs.y, sleepAbs.z, 0.0F, 0.0F);
        sleepingChester.toggleSitAndSleep(alice);
        helper.assertTrue(sleepingChester.isAsleep(), "Sleeping Chester should be asleep before Bounce TNT");

        Vec3 tntAbs = helper.absoluteVec(new Vec3(7.5, 2.0, 7.5));
        ArtilianPrimedTnt bounceTnt = new ArtilianPrimedTnt(
                level, tntAbs.x, tntAbs.y, tntAbs.z, bob, TntVariant.BOUNCE);
        bounceTnt.setFuse(5);
        level.addFreshEntity(bounceTnt);

        helper.runAfterDelay(6, () -> {
            helper.assertTrue(bob.hasEffect(MobEffects.SLOW_FALLING),
                    "Bounce TNT must grant Slow Falling so players land safely");
            helper.assertTrue(bob.hasEffect(MobEffects.JUMP),
                    "Bounce TNT must grant Jump Boost");
            helper.assertTrue(bob.getDeltaMovement().y > 0.5,
                    "Bounce TNT should launch player upward, got y=" + bob.getDeltaMovement().y);
            helper.assertTrue(pig.hasEffect(MobEffects.SLOW_FALLING) && pig.getDeltaMovement().y > 0.5,
                    "Bounce TNT should launch nearby pig with Slow Falling");
            helper.assertTrue(awakeChester.getDeltaMovement().y > 0.5,
                    "Bounce TNT should launch awake Chester");
            helper.assertTrue(sleepingChester.getDeltaMovement().y <= 0.05
                            && sleepingChester.distanceToSqr(sleepAbs) < 0.05,
                    "Sleeping Chester must stay put when Bounce TNT detonates");
            helper.assertBlockPresent(Blocks.STONE, new BlockPos(7, 1, 7));

            awakeChester.discard();
            sleepingChester.discard();
            pig.discard();
            removeTestPlayer(level, bob);
            removeTestPlayer(level, alice);
            helper.succeed();
        });
    }

    @GameTest(template = "tnt_arena", timeoutTicks = 60)
    public static void frostNovaFreezesHostilesAndWaterWhileSparingPlayersAndChester(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        for (int x = 4; x <= 10; x++) {
            for (int z = 4; z <= 10; z++) {
                helper.setBlock(new BlockPos(x, 1, z), Blocks.STONE);
            }
        }
        BlockPos waterPos = new BlockPos(6, 1, 7);
        BlockPos planksPos = new BlockPos(7, 2, 5);
        helper.setBlock(waterPos, Blocks.WATER);
        helper.setBlock(planksPos, Blocks.OAK_PLANKS);

        ServerPlayer alice = spawnSurvivalPlayer(helper, "AliceFrost", new Vec3(5.5, 2.0, 7.5));
        ChesterEntity chester = ChesterService.summon(alice);
        Vec3 chesterAbs = helper.absoluteVec(new Vec3(7.5, 2.0, 9.5));
        chester.moveTo(chesterAbs.x, chesterAbs.y, chesterAbs.z, 0.0F, 0.0F);

        Wolf wolf = helper.spawnWithNoFreeWill(EntityType.WOLF, new Vec3(8.5, 2.0, 9.0));
        wolf.tame(alice);
        float wolfStartHealth = wolf.getHealth();

        Creeper creeper = helper.spawnWithNoFreeWill(EntityType.CREEPER, new Vec3(9.5, 2.0, 7.5));
        creeper.setHealth(20.0F);

        Vec3 tntAbs = helper.absoluteVec(new Vec3(7.5, 2.0, 7.5));
        ArtilianPrimedTnt frostTnt = new ArtilianPrimedTnt(
                level, tntAbs.x, tntAbs.y, tntAbs.z, alice, TntVariant.FROST_NOVA);
        frostTnt.setFuse(5);
        level.addFreshEntity(frostTnt);

        helper.runAfterDelay(10, () -> {
            helper.assertBlockPresent(Blocks.FROSTED_ICE, waterPos);
            helper.assertBlockPresent(Blocks.OAK_PLANKS, planksPos);
            helper.assertBlockPresent(Blocks.STONE, new BlockPos(7, 1, 7));

            helper.assertTrue(creeper.getHealth() <= 14.0F,
                    "Frost Nova TNT should deal 6 freeze damage to hostile mobs, hp=" + creeper.getHealth());
            helper.assertTrue(creeper.getTicksFrozen() >= 190,
                    "Frost Nova TNT should freeze hostile mobs, ticksFrozen=" + creeper.getTicksFrozen());
            helper.assertTrue(creeper.hasEffect(MobEffects.MOVEMENT_SLOWDOWN)
                            && creeper.hasEffect(MobEffects.WEAKNESS),
                    "Frost Nova TNT should apply Slowness and Weakness to hostile mobs");

            helper.assertTrue(alice.getHealth() == alice.getMaxHealth(),
                    "Frost Nova TNT must not damage players, hp=" + alice.getHealth());
            helper.assertFalse(alice.hasEffect(MobEffects.MOVEMENT_SLOWDOWN),
                    "Frost Nova TNT must not slow players");
            helper.assertTrue(chester.isAlive() && !chester.hasEffect(MobEffects.MOVEMENT_SLOWDOWN),
                    "Frost Nova TNT must spare Chester");
            helper.assertTrue(wolf.getHealth() == wolfStartHealth && !wolf.hasEffect(MobEffects.MOVEMENT_SLOWDOWN),
                    "Frost Nova TNT must spare tamed pets");

            chester.discard();
            wolf.discard();
            creeper.discard();
            removeTestPlayer(level, alice);
            helper.succeed();
        });
    }
}
