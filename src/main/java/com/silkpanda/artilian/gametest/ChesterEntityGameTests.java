package com.silkpanda.artilian.gametest;

import com.silkpanda.artilian.ArtilianMod;
import com.silkpanda.artilian.chester.ChesterData;
import com.silkpanda.artilian.chester.ChesterEntity;
import com.silkpanda.artilian.chester.ChesterService;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSources;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import java.util.UUID;

/** Chester Plan Stage 2: one Chester per player on first login, and nothing can hurt him. */
@GameTestHolder(ArtilianMod.MODID)
@PrefixGameTestTemplate(false)
public class ChesterEntityGameTests {

    @GameTest(template = "empty")
    public static void firstLoginSpawnsOneChester(GameTestHelper helper) {
        ServerPlayer alice = ChesterTestSupport.playerInTestArea(helper, "Alice");

        ChesterService.handleLogin(alice);

        var chesters = ChesterTestSupport.chestersOf(helper, alice);
        helper.assertTrue(chesters.size() == 1, "First login should spawn 1 Chester but spawned " + chesters.size());
        ChesterEntity chester = chesters.get(0);
        helper.assertTrue(ChesterData.get(alice).getChesterEntityId().filter(chester.getUUID()::equals).isPresent(),
                "The player record does not point at their Chester");
        helper.assertTrue(ChesterData.get(alice).isFirstSpawnDone(), "First login was not remembered");
        helper.assertTrue("Alice".equals(chester.getOwnerName()), "Chester does not know who owns him");

        ChesterTestSupport.removeChestersOf(helper, alice);
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void secondLoginSpawnsNothing(GameTestHelper helper) {
        ServerPlayer bob = ChesterTestSupport.playerInTestArea(helper, "Bob");
        ChesterService.handleLogin(bob);
        UUID first = ChesterData.get(bob).getChesterEntityId().orElseThrow();

        ChesterService.handleLogin(bob);

        UUID afterSecondLogin = ChesterData.get(bob).getChesterEntityId().orElseThrow();
        helper.assertTrue(first.equals(afterSecondLogin), "A second login replaced the Chester with a new one");
        helper.assertTrue(ChesterTestSupport.chestersOf(helper, bob).size() == 1,
                "A second login changed how many Chesters the player has");

        ChesterTestSupport.removeChestersOf(helper, bob);
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void eachPlayerGetsTheirOwn(GameTestHelper helper) {
        ServerPlayer alice = ChesterTestSupport.playerInTestArea(helper, "Alice");
        ServerPlayer bob = ChesterTestSupport.playerInTestArea(helper, "Bob");

        ChesterEntity alices = ChesterService.summon(alice);
        ChesterEntity bobs = ChesterService.summon(bob);

        helper.assertTrue(alices.isOwner(alice) && !alices.isOwner(bob), "The first Chester has the wrong owner");
        helper.assertTrue(bobs.isOwner(bob) && !bobs.isOwner(alice), "The second Chester has the wrong owner");
        helper.assertFalse(alices.getUUID().equals(bobs.getUUID()), "Both players were given the same Chester");

        ChesterTestSupport.removeChestersOf(helper, alice, bob);
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void nothingHurtsChester(GameTestHelper helper) {
        ServerPlayer owner = ChesterTestSupport.playerInTestArea(helper, "Alice");
        ServerPlayer other = ChesterTestSupport.playerInTestArea(helper, "Bob");
        ChesterEntity chester = ChesterService.summon(owner);
        Zombie zombie = EntityType.ZOMBIE.create(helper.getLevel());
        DamageSources sources = helper.getLevel().damageSources();
        float full = chester.getHealth();

        chester.hurt(sources.playerAttack(other), 50.0F);
        chester.hurt(sources.playerAttack(owner), 50.0F);
        chester.hurt(sources.mobAttack(zombie), 50.0F);
        chester.hurt(sources.lava(), 50.0F);
        chester.hurt(sources.explosion(null, null), 50.0F);
        chester.hurt(sources.fall(), 50.0F);

        helper.assertTrue(chester.isAlive() && chester.getHealth() == full,
                "Chester was hurt: health went from " + full + " to " + chester.getHealth());

        ChesterTestSupport.removeChestersOf(helper, owner);
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void killingChesterLosesNothing(GameTestHelper helper) {
        ServerPlayer owner = ChesterTestSupport.playerInTestArea(helper, "Alice");
        ChesterEntity chester = ChesterService.summon(owner);
        ChesterData.get(owner).getInventory().setStackInSlot(3, new ItemStack(Items.DIAMOND, 7));

        chester.kill();

        helper.assertTrue(chester.isDeadOrDying(), "/kill should still remove Chester");
        ItemStack kept = ChesterData.get(owner).getInventory().getStackInSlot(3);
        helper.assertTrue(kept.is(Items.DIAMOND) && kept.getCount() == 7,
                "Killing Chester lost what was inside, found " + kept);
        helper.assertTrue(helper.getLevel().getEntities(EntityType.ITEM, item -> item.distanceToSqr(chester) < 16.0).isEmpty(),
                "Killing Chester dropped items on the ground");

        chester.discard();
        helper.succeed();
    }
}
