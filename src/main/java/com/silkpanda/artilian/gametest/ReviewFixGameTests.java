package com.silkpanda.artilian.gametest;

import com.silkpanda.artilian.ArtilianMod;
import com.silkpanda.artilian.registry.ArtilianItems;
import com.silkpanda.artilian.tnt.ArtilianPrimedTnt;
import com.silkpanda.artilian.tnt.TntVariant;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.animal.Sheep;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import net.neoforged.neoforge.network.registration.NetworkRegistry;

/** Fixes from the Phase 1 code review that are about play, not protection (QA-01 to QA-03). */
@GameTestHolder(ArtilianMod.MODID)
@PrefixGameTestTemplate(false)
public class ReviewFixGameTests {

    private static void buildStoneFloor(GameTestHelper helper) {
        for (int x = 2; x <= 12; x++) {
            for (int z = 2; z <= 12; z++) {
                helper.setBlock(new BlockPos(x, 1, z), Blocks.STONE);
            }
        }
    }

    private static ServerPlayer spawnCaster(GameTestHelper helper, String name) {
        ServerPlayer player = CustomTntGameTests.spawnSurvivalPlayer(helper, name, new Vec3(3.5, 2.0, 7.5));
        NetworkRegistry.configureMockConnection(player.connection.getConnection());
        return player;
    }

    /** Aims the caster at the target and casts one Spark Wand bolt. */
    private static void castSparkAt(GameTestHelper helper, ServerPlayer caster, LivingEntity target) {
        caster.lookAt(EntityAnchorArgument.Anchor.EYES, target.getEyePosition());
        ItemStack sparkWand = new ItemStack(ArtilianItems.SPARK_WAND.get());
        caster.setItemInHand(InteractionHand.MAIN_HAND, sparkWand);
        helper.assertTrue(sparkWand.use(helper.getLevel(), caster, InteractionHand.MAIN_HAND).getResult().consumesAction(),
                "Spark Wand cast should succeed");
    }

    private static void detonate(GameTestHelper helper, TntVariant variant) {
        ServerLevel level = helper.getLevel();
        Vec3 center = helper.absoluteVec(new Vec3(7.5, 2.0, 7.5));
        ArtilianPrimedTnt tnt = new ArtilianPrimedTnt(level, center.x, center.y, center.z, null, variant);
        tnt.setFuse(1);
        level.addFreshEntity(tnt);
        tnt.tick();
    }

    /** QA-01: a sheep standing in front of a zombie does not take the bolt meant for the zombie. */
    @GameTest(template = "tnt_arena", timeoutTicks = 40)
    public static void sparkWandPrefersMonsterOverCloserAnimal(GameTestHelper helper) {
        buildStoneFloor(helper);
        ServerPlayer alice = spawnCaster(helper, "AliceAim");
        // Sheep 2 blocks ahead, zombie 8: six apart, so the sheep is outside the 4.5-block chain too.
        Sheep sheep = helper.spawnWithNoFreeWill(EntityType.SHEEP, new Vec3(5.5, 2.0, 7.5));
        Zombie zombie = helper.spawnWithNoFreeWill(EntityType.ZOMBIE, new Vec3(11.5, 2.0, 7.5));
        float sheepHp = sheep.getHealth();
        float zombieHp = zombie.getHealth();

        try {
            castSparkAt(helper, alice, zombie);
            helper.assertTrue(zombie.getHealth() < zombieHp,
                    "Spark Wand should hit the zombie behind the sheep, zombie hp " + zombie.getHealth());
            helper.assertTrue(sheep.getHealth() == sheepHp && !sheep.hasEffect(MobEffects.GLOWING),
                    "Spark Wand hit the sheep in front instead of the zombie, sheep hp " + sheep.getHealth());
        } finally {
            sheep.discard();
            zombie.discard();
            CustomTntGameTests.removeTestPlayer(helper.getLevel(), alice);
        }
        helper.succeed();
    }

    /** QA-01: the bolt stops at a wall. */
    @GameTest(template = "tnt_arena", timeoutTicks = 40)
    public static void sparkWandBlockedByWall(GameTestHelper helper) {
        buildStoneFloor(helper);
        ServerPlayer alice = spawnCaster(helper, "AliceWall");
        for (int y = 2; y <= 4; y++) {
            for (int z = 6; z <= 8; z++) {
                helper.setBlock(new BlockPos(6, y, z), Blocks.STONE);
            }
        }
        Zombie zombie = helper.spawnWithNoFreeWill(EntityType.ZOMBIE, new Vec3(9.5, 2.0, 7.5));
        float zombieHp = zombie.getHealth();

        try {
            castSparkAt(helper, alice, zombie);
            helper.assertTrue(zombie.getHealth() == zombieHp && !zombie.hasEffect(MobEffects.GLOWING),
                    "Spark Wand reached through a stone wall, zombie hp " + zombieHp + " -> " + zombie.getHealth());
        } finally {
            zombie.discard();
            CustomTntGameTests.removeTestPlayer(helper.getLevel(), alice);
        }
        helper.succeed();
    }

    /** QA-01: a village iron golem is never a target, so it never turns on the caster. */
    @GameTest(template = "tnt_arena", timeoutTicks = 40)
    public static void sparkWandSparesIronGolem(GameTestHelper helper) {
        buildStoneFloor(helper);
        ServerPlayer alice = spawnCaster(helper, "AliceGolem");
        IronGolem golem = helper.spawnWithNoFreeWill(EntityType.IRON_GOLEM, new Vec3(7.5, 2.0, 7.5));
        float golemHp = golem.getHealth();

        try {
            castSparkAt(helper, alice, golem);
            helper.assertTrue(golem.getHealth() == golemHp && !golem.hasEffect(MobEffects.GLOWING),
                    "Spark Wand hurt an iron golem, hp " + golemHp + " -> " + golem.getHealth());
        } finally {
            golem.discard();
            CustomTntGameTests.removeTestPlayer(helper.getLevel(), alice);
        }
        helper.succeed();
    }

    /** QA-02: Party TNT gives Speed and Regeneration to players, not to monsters. */
    @GameTest(template = "tnt_arena", timeoutTicks = 40)
    public static void partyTntDoesNotBuffMonsters(GameTestHelper helper) {
        buildStoneFloor(helper);
        ServerPlayer bob = CustomTntGameTests.spawnSurvivalPlayer(helper, "BobParty", new Vec3(5.5, 2.0, 7.5));
        Zombie zombie = helper.spawnWithNoFreeWill(EntityType.ZOMBIE, new Vec3(9.5, 2.0, 7.5));

        try {
            detonate(helper, TntVariant.PARTY);
            helper.assertTrue(bob.hasEffect(MobEffects.MOVEMENT_SPEED),
                    "Party TNT should still give players Speed");
            helper.assertTrue(!zombie.hasEffect(MobEffects.MOVEMENT_SPEED) && !zombie.hasEffect(MobEffects.REGENERATION),
                    "Party TNT gave a zombie Speed or Regeneration");
        } finally {
            zombie.discard();
            CustomTntGameTests.removeTestPlayer(helper.getLevel(), bob);
        }
        helper.succeed();
    }

    /** QA-03: Frost Nova freezes still water and leaves flowing water alone. */
    @GameTest(template = "tnt_arena", timeoutTicks = 40)
    public static void frostNovaFreezesOnlySourceWater(GameTestHelper helper) {
        buildStoneFloor(helper);
        BlockPos sourcePos = new BlockPos(6, 1, 7);
        BlockPos flowingPos = new BlockPos(9, 1, 7);
        helper.setBlock(sourcePos, Blocks.WATER);
        // Level 3 is flowing water. It is set and frozen in the same tick, before any fluid tick can change it.
        BlockState flowing = Blocks.WATER.defaultBlockState().setValue(LiquidBlock.LEVEL, 3);
        helper.setBlock(flowingPos, flowing);
        helper.assertTrue(!helper.getBlockState(flowingPos).getFluidState().isSource(),
                "Test setup: the second water block should be flowing, not a source");

        detonate(helper, TntVariant.FROST_NOVA);

        helper.assertBlockPresent(Blocks.FROSTED_ICE, sourcePos);
        helper.assertTrue(!helper.getBlockState(flowingPos).is(Blocks.FROSTED_ICE),
                "Frost Nova froze flowing water, which melts back into a new water source");
        helper.succeed();
    }
}
