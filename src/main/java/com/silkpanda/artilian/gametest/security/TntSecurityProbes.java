package com.silkpanda.artilian.gametest.security;

import com.silkpanda.artilian.ArtilianMod;
import com.silkpanda.artilian.chester.ChesterEntity;
import com.silkpanda.artilian.chester.ChesterService;
import com.silkpanda.artilian.config.ArtilianConfig;
import com.silkpanda.artilian.gametest.CustomTntGameTests;
import com.silkpanda.artilian.registry.ArtilianBlocks;
import com.silkpanda.artilian.registry.ArtilianEntities;
import com.silkpanda.artilian.registry.ArtilianItems;
import com.silkpanda.artilian.tnt.ArtilianPrimedTnt;
import com.silkpanda.artilian.tnt.TntVariant;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.PrimedTnt;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.block.entity.DispenserBlockEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;

/**
 * Security probes for Phase 1 Stage 4 Custom TNTs ({@code P1-07} – {@code P1-10}), indexed in
 * {@code security/tnt/README.md} and logged in {@code security/FINDINGS.md}.
 */
@GameTestHolder(ArtilianMod.MODID)
@PrefixGameTestTemplate(false)
public class TntSecurityProbes {

    private static List<BlockPos> buildVulnerableShell(GameTestHelper helper) {
        for (int x = 4; x <= 10; x++) {
            for (int z = 4; z <= 10; z++) {
                helper.setBlock(new BlockPos(x, 1, z), Blocks.STONE);
            }
        }
        List<BlockPos> targets = List.of(
                new BlockPos(7, 2, 5),
                new BlockPos(7, 2, 9),
                new BlockPos(5, 2, 7),
                new BlockPos(9, 2, 7),
                new BlockPos(6, 2, 6),
                new BlockPos(8, 2, 8)
        );
        for (int i = 0; i < targets.size(); i++) {
            helper.setBlock(targets.get(i), i % 2 == 0 ? Blocks.GLASS : Blocks.OAK_PLANKS);
        }
        return targets;
    }

    private static void assertShellIntact(GameTestHelper helper, List<BlockPos> shell, String probeId) {
        BlockPos centerFloor = new BlockPos(7, 1, 7);
        helper.assertTrue(helper.getBlockState(centerFloor).is(Blocks.STONE),
                "EXPLOITED " + probeId + ": floor block broken at " + centerFloor
                        + ", found " + helper.getBlockState(centerFloor).getBlock());
        for (int i = 0; i < shell.size(); i++) {
            var expected = i % 2 == 0 ? Blocks.GLASS : Blocks.OAK_PLANKS;
            BlockPos pos = shell.get(i);
            helper.assertTrue(helper.getBlockState(pos).is(expected),
                    "EXPLOITED " + probeId + ": block broken or altered at " + pos
                            + ", found " + helper.getBlockState(pos).getBlock());
        }
    }

    /** P1-07: Under the default config (allowDestructiveCustomTnt = false), zero blocks break across all 4 TNTs. */
    @GameTest(template = "tnt_arena", timeoutTicks = 80)
    public static void p1_07_defaultConfigBreaksZeroBlocksForAllFourTnts(GameTestHelper helper) {
        helper.assertFalse(ArtilianConfig.isDestructiveTntAllowed(),
                "Default config must disable destructive custom TNTs");
        ServerLevel level = helper.getLevel();
        List<BlockPos> shell = buildVulnerableShell(helper);
        Vec3 center = helper.absoluteVec(new Vec3(7.5, 2.0, 7.5));

        for (TntVariant variant : TntVariant.values()) {
            ArtilianPrimedTnt tnt = new ArtilianPrimedTnt(level, center.x, center.y, center.z, null, variant);
            tnt.setFuse(5);
            level.addFreshEntity(tnt);
        }

        // Wait 32 ticks so primary TNTs (5t) and Cluster Quarry bomblets (+20t) have all detonated.
        helper.runAfterDelay(32, () -> {
            assertShellIntact(helper, shell, "P1-07");
            helper.succeed();
        });
    }

    /**
     * P1-08: Cluster Quarry primary and generation-1 bomblets check allowDestructiveCustomTnt at detonation time,
     * so disabling the setting stops bomblets even if the primary spawned earlier.
     */
    @GameTest(template = "tnt_arena", timeoutTicks = 80)
    public static void p1_08_clusterBombletsRespectSafeConfigOnDetonation(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        List<BlockPos> shell = buildVulnerableShell(helper);
        Vec3 center = helper.absoluteVec(new Vec3(7.5, 3.5, 7.5));
        AABB arenaBox = new AABB(helper.absolutePos(new BlockPos(0, 0, 0))).minmax(
                new AABB(helper.absolutePos(new BlockPos(14, 14, 14))));

        // Simulate primary detonating while allowed, spawning 4 bomblets, then safe mode turning back ON.
        try {
            ArtilianConfig.setTestOverride(true);
            ArtilianPrimedTnt primary = new ArtilianPrimedTnt(
                    level, center.x, center.y, center.z, null, TntVariant.CLUSTER_QUARRY);
            primary.setFuse(1);
            level.addFreshEntity(primary);
            primary.tick();
        } finally {
            ArtilianConfig.setTestOverride(null);
        }

        List<ArtilianPrimedTnt> bomblets = level.getEntitiesOfClass(
                ArtilianPrimedTnt.class, arenaBox, tnt -> tnt.getClusterGeneration() == 1);
        helper.assertTrue(bomblets.size() == 4,
                "Expected 4 Cluster Quarry bomblets, found " + bomblets.size());
        // Rebuild floor/shell cleanly after the synchronous primary blast before bomblets go off in safe mode.
        List<BlockPos> safeShell = buildVulnerableShell(helper);

        helper.runAfterDelay(26, () -> {
            List<ArtilianPrimedTnt> remaining = level.getEntitiesOfClass(
                    ArtilianPrimedTnt.class, arenaBox, tnt -> tnt.getClusterGeneration() == 1);
            helper.assertTrue(remaining.isEmpty(),
                    "Bomblets should have finished detonating, found " + remaining.size());
            assertShellIntact(helper, safeShell, "P1-08");
            helper.succeed();
        });
    }

    /**
     * P1-09: Redstone, chain blast (wasExploded), flaming arrow, and dispenser ignition all prime
     * ArtilianPrimedTnt (never vanilla PrimedTnt) and break zero blocks under default config.
     */
    @GameTest(template = "tnt_arena", timeoutTicks = 150)
    public static void p1_09_indirectIgnitionNeverSpawnsVanillaTntOrBreaksBlocks(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        for (int x = 1; x <= 13; x++) {
            for (int z = 1; z <= 13; z++) {
                helper.setBlock(new BlockPos(x, 1, z), Blocks.STONE);
            }
        }
        BlockPos glassMarker = new BlockPos(7, 2, 7);
        BlockPos planksMarker = new BlockPos(7, 2, 8);
        helper.setBlock(glassMarker, Blocks.GLASS);
        helper.setBlock(planksMarker, Blocks.OAK_PLANKS);

        ServerPlayer archer = CustomTntGameTests.spawnSurvivalPlayer(helper, "AliceArcher", new Vec3(9.5, 4.0, 3.5));

        // 1. Redstone ignition on Party TNT
        BlockPos partyPos = new BlockPos(3, 2, 3);
        helper.setBlock(partyPos, ArtilianBlocks.PARTY_TNT.get());
        helper.setBlock(new BlockPos(2, 2, 3), Blocks.REDSTONE_BLOCK);

        // 2. Chain explosion (wasExploded) on Bounce TNT
        BlockPos bouncePos = new BlockPos(6, 2, 3);
        helper.setBlock(bouncePos, ArtilianBlocks.BOUNCE_TNT.get());
        BlockPos bounceAbs = helper.absolutePos(bouncePos);
        Explosion fakeExplosion = new Explosion(
                level, null, bounceAbs.getX(), bounceAbs.getY(), bounceAbs.getZ(),
                2.0F, false, Explosion.BlockInteraction.KEEP);
        ArtilianBlocks.BOUNCE_TNT.get().wasExploded(level, bounceAbs, fakeExplosion);
        level.removeBlock(bounceAbs, false);

        // 3. Flaming Arrow hitting Frost Nova TNT
        BlockPos frostPos = new BlockPos(9, 2, 3);
        helper.setBlock(frostPos, ArtilianBlocks.FROST_NOVA_TNT.get());
        Vec3 arrowStart = helper.absoluteVec(new Vec3(9.5, 3.2, 3.5));
        Arrow flamingArrow = new Arrow(level, arrowStart.x, arrowStart.y, arrowStart.z,
                new ItemStack(Items.ARROW), null);
        flamingArrow.setOwner(archer);
        flamingArrow.igniteForSeconds(5.0F);
        flamingArrow.setDeltaMovement(0.0, -0.6, 0.0);
        level.addFreshEntity(flamingArrow);

        // 4. Live Dispenser firing Cluster Quarry TNT
        BlockPos dispenserPos = new BlockPos(11, 2, 3);
        helper.setBlock(dispenserPos, Blocks.DISPENSER.defaultBlockState().setValue(DispenserBlock.FACING, Direction.SOUTH));
        DispenserBlockEntity dispenser = helper.getBlockEntity(dispenserPos);
        dispenser.setItem(0, new ItemStack(ArtilianItems.CLUSTER_QUARRY_TNT_ITEM.get()));
        helper.setBlock(new BlockPos(12, 2, 3), Blocks.REDSTONE_BLOCK);

        AABB arenaBox = new AABB(helper.absolutePos(new BlockPos(0, 0, 0))).minmax(
                new AABB(helper.absolutePos(new BlockPos(14, 14, 14))));

        helper.runAfterDelay(8, () -> {
            List<PrimedTnt> allPrimed = level.getEntitiesOfClass(PrimedTnt.class, arenaBox);
            Set<TntVariant> seenVariants = EnumSet.noneOf(TntVariant.class);
            for (PrimedTnt entity : allPrimed) {
                helper.assertTrue(entity.getType() == ArtilianEntities.PRIMED_TNT.get()
                                && entity instanceof ArtilianPrimedTnt,
                        "EXPLOITED P1-09: indirect ignition spawned non-Artilian TNT entity " + entity.getType());
                seenVariants.add(((ArtilianPrimedTnt) entity).getVariant());
            }
            helper.assertTrue(seenVariants.size() == 4,
                    "Expected all 4 variants primed indirectly, saw " + seenVariants);

            helper.runAfterDelay(110, () -> {
                helper.assertBlockPresent(Blocks.GLASS, glassMarker);
                helper.assertBlockPresent(Blocks.OAK_PLANKS, planksMarker);
                helper.assertBlockPresent(Blocks.STONE, new BlockPos(7, 1, 7));

                flamingArrow.discard();
                CustomTntGameTests.removeTestPlayer(level, archer);
                helper.succeed();
            });
        });
    }

    /**
     * P1-10: Even when allowDestructiveCustomTnt = true, Party, Bounce, and Frost Nova break zero solid
     * blocks and deal zero damage to Players/Chester, while Cluster Quarry spares Chests, Players, and Chester.
     */
    @GameTest(template = "tnt_arena", timeoutTicks = 40)
    public static void p1_10_safeBlastTntsNeverBreakBlocksEvenWhenConfigEnabled(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        List<BlockPos> shell = buildVulnerableShell(helper);
        BlockPos chestPos = new BlockPos(7, 2, 4);
        helper.setBlock(chestPos, Blocks.CHEST);

        ServerPlayer bob = CustomTntGameTests.spawnSurvivalPlayer(helper, "BobConfigOn", new Vec3(6.0, 2.0, 7.5));
        ChesterEntity chester = ChesterService.summon(bob);
        Vec3 chesterAbs = helper.absoluteVec(new Vec3(8.5, 2.0, 7.5));
        chester.moveTo(chesterAbs.x, chesterAbs.y, chesterAbs.z, 0.0F, 0.0F);
        Vec3 center = helper.absoluteVec(new Vec3(7.5, 2.0, 7.5));

        AABB arenaBox = new AABB(helper.absolutePos(new BlockPos(0, 0, 0))).minmax(
                new AABB(helper.absolutePos(new BlockPos(14, 14, 14))));

        try {
            ArtilianConfig.setTestOverride(true);

            for (TntVariant safeVariant : List.of(TntVariant.PARTY, TntVariant.BOUNCE, TntVariant.FROST_NOVA)) {
                ArtilianPrimedTnt safeTnt = new ArtilianPrimedTnt(
                        level, center.x, center.y, center.z, bob, safeVariant);
                safeTnt.setFuse(1);
                level.addFreshEntity(safeTnt);
                safeTnt.tick();
            }

            assertShellIntact(helper, shell, "P1-10");
            helper.assertBlockPresent(Blocks.CHEST, chestPos);
            helper.assertTrue(bob.getHealth() == bob.getMaxHealth(),
                    "EXPLOITED P1-10: safe custom TNTs damaged player when config enabled");
            helper.assertTrue(chester.isAlive(),
                    "EXPLOITED P1-10: safe custom TNTs harmed Chester when config enabled");

            // Now confirm Cluster Quarry actually mines stone when config is true, while sparing Chest, Player & Chester.
            ArtilianPrimedTnt quarry = new ArtilianPrimedTnt(
                    level, center.x, center.y, center.z, bob, TntVariant.CLUSTER_QUARRY);
            quarry.setFuse(1);
            level.addFreshEntity(quarry);
            quarry.tick();

            boolean brokeAnyShellOrFloor = !helper.getBlockState(new BlockPos(7, 1, 7)).is(Blocks.STONE)
                    || shell.stream().anyMatch(pos -> helper.getBlockState(pos).isAir());
            helper.assertTrue(brokeAnyShellOrFloor,
                    "Cluster Quarry TNT should mine blocks when allowDestructiveCustomTnt is true");
            helper.assertBlockPresent(Blocks.CHEST, chestPos);
            helper.assertTrue(bob.getHealth() == bob.getMaxHealth(),
                    "EXPLOITED P1-10: Cluster Quarry harmed player");
            helper.assertTrue(chester.isAlive(),
                    "EXPLOITED P1-10: Cluster Quarry harmed Chester");
        } finally {
            ArtilianConfig.setTestOverride(null);
            level.getEntitiesOfClass(ArtilianPrimedTnt.class, arenaBox).forEach(ArtilianPrimedTnt::discard);
            chester.discard();
            CustomTntGameTests.removeTestPlayer(level, bob);
        }
        helper.succeed();
    }
}
