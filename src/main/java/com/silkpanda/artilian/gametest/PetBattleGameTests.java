package com.silkpanda.artilian.gametest;

import com.silkpanda.artilian.ArtilianMod;
import com.silkpanda.artilian.pet.CapturedPetData;
import com.silkpanda.artilian.pet.CrystalFoxEntity;
import com.silkpanda.artilian.pet.PetElement;
import com.silkpanda.artilian.registry.ArtilianDataComponents;
import com.silkpanda.artilian.registry.ArtilianEntities;
import com.silkpanda.artilian.registry.ArtilianItems;
import com.silkpanda.artilian.registry.ArtilianTags;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.EnderMan;
import net.minecraft.world.entity.monster.Husk;
import net.minecraft.world.entity.monster.Pillager;
import net.minecraft.world.entity.monster.Silverfish;
import net.minecraft.world.entity.monster.Skeleton;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import java.util.List;

/** Phase 2 Stage 4: Pet battle AI, owner defense, structure-enemy targeting, and elemental combat. */
@GameTestHolder(ArtilianMod.MODID)
@PrefixGameTestTemplate(false)
public class PetBattleGameTests {

    @GameTest(template = "tnt_arena", timeoutTicks = 100)
    public static void tamedCrystalFoxAttacksHostileAndStructureEnemiesWhileSittingClearsTarget(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        ServerPlayer alex = ChesterTestSupport.playerInTestArea(helper, "AlexBattle");

        helper.assertTrue(EntityType.PILLAGER.is(ArtilianTags.EntityTypes.STRUCTURE_ENEMIES),
                "Pillager should be tagged artilian:structure_enemies");
        helper.assertTrue(EntityType.BREEZE.is(ArtilianTags.EntityTypes.STRUCTURE_ENEMIES),
                "Breeze should be tagged artilian:structure_enemies");

        CrystalFoxEntity fox = summonTamedFox(helper, alex, new BlockPos(7, 0, 7));
        Pillager pillager = EntityType.PILLAGER.create(level);
        BlockPos enemyPos = helper.absolutePos(new BlockPos(9, 0, 7));
        pillager.moveTo(enemyPos.getX() + 0.5, enemyPos.getY(), enemyPos.getZ() + 0.5, 0.0F, 0.0F);
        pillager.setNoAi(true);
        level.addFreshEntity(pillager);

        helper.succeedWhen(() -> {
            helper.assertTrue(pillager.getHealth() < pillager.getMaxHealth(),
                    "Expected Crystal Fox to engage and damage Pillager; target=" + fox.getTarget()
                            + ", dist=" + fox.distanceTo(pillager));
            fox.setSitting(true);
            helper.assertTrue(fox.getTarget() == null,
                    "Sitting Crystal Fox must clear its combat target immediately");
            pillager.discard();
            fox.discard();
        });
    }

    @GameTest(template = "tnt_arena", timeoutTicks = 100)
    public static void tamedCrystalFoxDefendsOwnerAndAppliesCrystalElementPerk(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        ServerPlayer steve = ChesterTestSupport.playerInTestArea(helper, "SteveDefend");
        CrystalFoxEntity fox = summonTamedFox(helper, steve, new BlockPos(7, 0, 7));

        Husk husk = EntityType.HUSK.create(level);
        BlockPos zPos = helper.absolutePos(new BlockPos(9, 0, 7));
        husk.moveTo(zPos.getX() + 0.5, zPos.getY(), zPos.getZ() + 0.5, 0.0F, 0.0F);
        husk.setNoAi(true);
        level.addFreshEntity(husk);

        // Simulate husk striking Steve so PetOwnerHurtByTargetGoal engages immediately.
        steve.setLastHurtByMob(husk);

        helper.succeedWhen(() -> {
            helper.assertTrue(husk.getHealth() < husk.getMaxHealth(),
                    "Expected Crystal Fox to defend Steve against attacker");
            helper.assertTrue(husk.hasEffect(MobEffects.MOVEMENT_SLOWDOWN)
                            && husk.hasEffect(MobEffects.GLOWING),
                    "Expected Crystal signature hit to apply Slowness and Glowing");
            husk.discard();
            fox.discard();
        });
    }

    @GameTest(template = "tnt_arena")
    public static void elementalMatchupsScaleBattleDamageUpForSuperEffectiveAndDownForResisted(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        ServerPlayer alex = ChesterTestSupport.playerInTestArea(helper, "AlexElementDmg");
        CrystalFoxEntity fox = summonTamedFox(helper, alex, new BlockPos(7, 0, 7));

        EnderMan shadowTarget = EntityType.ENDERMAN.create(level);
        BlockPos pos = helper.absolutePos(new BlockPos(8, 0, 7));
        shadowTarget.moveTo(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, 0.0F, 0.0F);
        level.addFreshEntity(shadowTarget);
        float beforeShadow = shadowTarget.getHealth();
        fox.doHurtTarget(shadowTarget);
        float shadowDamage = beforeShadow - shadowTarget.getHealth();
        helper.assertTrue(Math.abs(shadowDamage - 6.0F) < 0.05F,
                "Crystal (4.0 base) vs Shadow should deal 6.0 (1.5x), got " + shadowDamage);
        shadowTarget.discard();

        Silverfish earthTarget = EntityType.SILVERFISH.create(level);
        earthTarget.moveTo(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, 0.0F, 0.0F);
        level.addFreshEntity(earthTarget);
        float beforeEarth = earthTarget.getHealth();
        fox.doHurtTarget(earthTarget);
        float earthDamage = beforeEarth - earthTarget.getHealth();
        helper.assertTrue(Math.abs(earthDamage - 3.0F) < 0.05F,
                "Crystal (4.0 base) vs Earth should deal 3.0 (0.75x), got " + earthDamage);
        earthTarget.discard();

        Skeleton neutralTarget = EntityType.SKELETON.create(level);
        neutralTarget.moveTo(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, 0.0F, 0.0F);
        level.addFreshEntity(neutralTarget);
        float beforeNeutral = neutralTarget.getHealth();
        fox.doHurtTarget(neutralTarget);
        float neutralDamage = beforeNeutral - neutralTarget.getHealth();
        helper.assertTrue(Math.abs(neutralDamage - 4.0F) < 0.05F,
                "Crystal (4.0 base) vs neutral Skeleton should deal 4.0 (1.0x), got " + neutralDamage);
        neutralTarget.discard();

        fox.discard();
        helper.succeed();
    }

    @GameTest(template = "tnt_arena", timeoutTicks = 80)
    public static void petDisengagesToFollowFarOwnerAndNeverTargetsCreepersPassively(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        ServerPlayer alex = ChesterTestSupport.playerInTestArea(helper, "AlexFollow");
        CrystalFoxEntity fox = summonTamedFox(helper, alex, new BlockPos(1, 0, 1));

        net.minecraft.world.entity.monster.Creeper creeper = EntityType.CREEPER.create(level);
        BlockPos cPos = helper.absolutePos(new BlockPos(3, 0, 1));
        creeper.moveTo(cPos.getX() + 0.5, cPos.getY(), cPos.getZ() + 0.5, 0.0F, 0.0F);
        level.addFreshEntity(creeper);

        Husk husk = EntityType.HUSK.create(level);
        BlockPos zPos = helper.absolutePos(new BlockPos(1, 0, 4));
        husk.moveTo(zPos.getX() + 0.5, zPos.getY(), zPos.getZ() + 0.5, 0.0F, 0.0F);
        husk.setNoAi(true);
        level.addFreshEntity(husk);
        fox.setTarget(husk);

        // Move Alex > 18 blocks across the arena so PetFollowOwnerGoal disengages combat and hops to Alex.
        BlockPos farPos = helper.absolutePos(new BlockPos(14, 0, 14));
        alex.moveTo(farPos.getX() + 0.5, farPos.getY(), farPos.getZ() + 0.5, 0.0F, 0.0F);

        helper.succeedWhen(() -> {
            helper.assertFalse(fox.getTarget() instanceof net.minecraft.world.entity.monster.Creeper,
                    "Crystal Fox must not passively target Creepers");
            helper.assertTrue(fox.distanceToSqr(alex) < 8.0 * 8.0 && fox.getTarget() == null,
                    "Crystal Fox should disengage combat and follow far owner");
            creeper.discard();
            husk.discard();
            fox.discard();
        });
    }

    private static CrystalFoxEntity summonTamedFox(GameTestHelper helper, ServerPlayer owner, BlockPos rel) {
        CrystalFoxEntity wild = ArtilianEntities.CRYSTAL_FOX.get().create(helper.getLevel());
        BlockPos abs = helper.absolutePos(rel);
        wild.moveTo(abs.getX() + 0.5, abs.getY(), abs.getZ() + 0.5, 0.0F, 0.0F);
        wild.addAffinity(100);
        helper.getLevel().addFreshEntity(wild);

        ItemStack crystal = new ItemStack(ArtilianItems.CAPTURE_CRYSTAL.get());
        owner.setItemInHand(InteractionHand.MAIN_HAND, crystal);
        owner.interactOn(wild, InteractionHand.MAIN_HAND);
        crystal.use(helper.getLevel(), owner, InteractionHand.MAIN_HAND);
        List<? extends CrystalFoxEntity> foxes = helper.getLevel().getEntities(ArtilianEntities.CRYSTAL_FOX.get(),
                fox -> fox.isAlive() && fox.isOwner(owner));
        CrystalFoxEntity summoned = foxes.get(0);
        summoned.moveTo(abs.getX() + 0.5, abs.getY(), abs.getZ() + 0.5, 0.0F, 0.0F);
        return summoned;
    }
}
