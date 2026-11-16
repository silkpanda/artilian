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
        ServerPlayer alex = ChesterTestSupport.playerInTestArea(helper, "Alex");

        ChesterService.handleLogin(alex);

        var chesters = ChesterTestSupport.chestersOf(helper, alex);
        helper.assertTrue(chesters.size() == 1, "First login should spawn 1 Chester but spawned " + chesters.size());
        ChesterEntity chester = chesters.get(0);
        helper.assertTrue(ChesterData.get(alex).getChesterEntityId().filter(chester.getUUID()::equals).isPresent(),
                "The player record does not point at their Chester");
        helper.assertTrue(ChesterData.get(alex).isFirstSpawnDone(), "First login was not remembered");
        helper.assertTrue("Alex".equals(chester.getOwnerName()), "Chester does not know who owns him");

        ChesterTestSupport.removeChestersOf(helper, alex);
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void secondLoginSpawnsNothing(GameTestHelper helper) {
        ServerPlayer steve = ChesterTestSupport.playerInTestArea(helper, "Steve");
        ChesterService.handleLogin(steve);
        UUID first = ChesterData.get(steve).getChesterEntityId().orElseThrow();

        ChesterService.handleLogin(steve);

        UUID afterSecondLogin = ChesterData.get(steve).getChesterEntityId().orElseThrow();
        helper.assertTrue(first.equals(afterSecondLogin), "A second login replaced the Chester with a new one");
        helper.assertTrue(ChesterTestSupport.chestersOf(helper, steve).size() == 1,
                "A second login changed how many Chesters the player has");

        ChesterTestSupport.removeChestersOf(helper, steve);
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void eachPlayerGetsTheirOwn(GameTestHelper helper) {
        ServerPlayer alex = ChesterTestSupport.playerInTestArea(helper, "Alex");
        ServerPlayer steve = ChesterTestSupport.playerInTestArea(helper, "Steve");

        ChesterEntity alexs = ChesterService.summon(alex);
        ChesterEntity steves = ChesterService.summon(steve);

        helper.assertTrue(alexs.isOwner(alex) && !alexs.isOwner(steve), "The first Chester has the wrong owner");
        helper.assertTrue(steves.isOwner(steve) && !steves.isOwner(alex), "The second Chester has the wrong owner");
        helper.assertFalse(alexs.getUUID().equals(steves.getUUID()), "Both players were given the same Chester");

        ChesterTestSupport.removeChestersOf(helper, alex, steve);
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void nothingHurtsChester(GameTestHelper helper) {
        ServerPlayer owner = ChesterTestSupport.playerInTestArea(helper, "Alex");
        ServerPlayer other = ChesterTestSupport.playerInTestArea(helper, "Steve");
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
        ServerPlayer owner = ChesterTestSupport.playerInTestArea(helper, "Alex");
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
