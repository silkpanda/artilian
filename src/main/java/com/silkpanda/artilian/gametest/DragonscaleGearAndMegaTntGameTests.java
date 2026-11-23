package com.silkpanda.artilian.gametest;

import com.silkpanda.artilian.ArtilianMod;
import com.silkpanda.artilian.chester.ChesterEntity;
import com.silkpanda.artilian.config.ArtilianConfig;
import com.silkpanda.artilian.gear.ArtilianArmorItem;
import com.silkpanda.artilian.registry.ArtilianBlocks;
import com.silkpanda.artilian.registry.ArtilianEntities;
import com.silkpanda.artilian.registry.ArtilianItems;
import com.silkpanda.artilian.tnt.ArtilianPrimedTnt;
import com.silkpanda.artilian.tnt.TntVariant;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import java.util.List;

/** Phase 5 Stage 7: Dragonscale armor/weapons/tools and Dragonfire Mega TNT. */
@GameTestHolder(ArtilianMod.MODID)
@PrefixGameTestTemplate(false)
public class DragonscaleGearAndMegaTntGameTests {

    @GameTest(template = "empty")
    public static void dragonscaleGearStatsFireResistanceAndTags(GameTestHelper helper) {
        List<Item> all = List.of(
                ArtilianItems.DRAGONSCALE_SWORD.get(),
                ArtilianItems.DRAGONSCALE_PICKAXE.get(),
                ArtilianItems.DRAGONSCALE_AXE.get(),
                ArtilianItems.DRAGONSCALE_SHOVEL.get(),
                ArtilianItems.DRAGONSCALE_HOE.get(),
                ArtilianItems.DRAGONSCALE_HELMET.get(),
                ArtilianItems.DRAGONSCALE_CHESTPLATE.get(),
                ArtilianItems.DRAGONSCALE_LEGGINGS.get(),
                ArtilianItems.DRAGONSCALE_BOOTS.get());

        for (Item item : all) {
            ItemStack stack = new ItemStack(item);
            helper.assertTrue(stack.has(DataComponents.FIRE_RESISTANT),
                    item + " must be fire resistant");
        }

        ItemStack pick = new ItemStack(ArtilianItems.DRAGONSCALE_PICKAXE.get());
        helper.assertTrue(pick.getMaxDamage() == 2350,
                "Dragonscale pickaxe should have 2350 durability, got " + pick.getMaxDamage());
        helper.assertTrue(pick.isCorrectToolForDrops(Blocks.OBSIDIAN.defaultBlockState()),
                "Dragonscale pickaxe must harvest Obsidian");
        helper.assertTrue(pick.isCorrectToolForDrops(Blocks.ANCIENT_DEBRIS.defaultBlockState()),
                "Dragonscale pickaxe must harvest Ancient Debris");

        helper.assertTrue(new ItemStack(ArtilianItems.DRAGONSCALE_SWORD.get()).is(ItemTags.SWORDS),
                "Dragonscale sword should be in ItemTags.SWORDS");
        helper.assertTrue(pick.is(ItemTags.PICKAXES),
                "Dragonscale pickaxe should be in ItemTags.PICKAXES");
        helper.assertTrue(new ItemStack(ArtilianItems.DRAGONSCALE_AXE.get()).is(ItemTags.AXES),
                "Dragonscale axe should be in ItemTags.AXES");
        helper.assertTrue(new ItemStack(ArtilianItems.DRAGONSCALE_SHOVEL.get()).is(ItemTags.SHOVELS),
                "Dragonscale shovel should be in ItemTags.SHOVELS");
        helper.assertTrue(new ItemStack(ArtilianItems.DRAGONSCALE_HOE.get()).is(ItemTags.HOES),
                "Dragonscale hoe should be in ItemTags.HOES");
        helper.assertTrue(new ItemStack(ArtilianItems.DRAGONSCALE_HELMET.get()).is(ItemTags.HEAD_ARMOR),
                "Dragonscale helmet should be in ItemTags.HEAD_ARMOR");

        int totalDefense = ((ArmorItem) ArtilianItems.DRAGONSCALE_HELMET.get()).getDefense()
                + ((ArmorItem) ArtilianItems.DRAGONSCALE_CHESTPLATE.get()).getDefense()
                + ((ArmorItem) ArtilianItems.DRAGONSCALE_LEGGINGS.get()).getDefense()
                + ((ArmorItem) ArtilianItems.DRAGONSCALE_BOOTS.get()).getDefense();
        helper.assertTrue(totalDefense == 26,
                "Dragonscale armor total defense should be 26, got " + totalDefense);

        ItemStack sword = new ItemStack(ArtilianItems.DRAGONSCALE_SWORD.get());
        final double[] swordAttack = {0.0};
        sword.forEachModifier(EquipmentSlot.MAINHAND, (attribute, modifier) -> {
            if (attribute.is(Attributes.ATTACK_DAMAGE)) {
                swordAttack[0] += modifier.amount();
            }
        });
        helper.assertTrue(swordAttack[0] > 8.0,
                "Dragonscale sword should grant > 8.0 bonus attack damage, got " + swordAttack[0]);

        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void dragonscaleSwordScorchesAndWeakensTargets(GameTestHelper helper) {
        ServerPlayer player = ChesterTestSupport.playerInTestArea(helper, "Alex");
        Zombie zombie = helper.spawnWithNoFreeWill(EntityType.ZOMBIE, new BlockPos(2, 1, 2));
        ItemStack sword = new ItemStack(ArtilianItems.DRAGONSCALE_SWORD.get());

        sword.getItem().hurtEnemy(sword, zombie, player);

        helper.assertTrue(zombie.getRemainingFireTicks() > 0,
                "Dragonscale sword should ignite hit targets");
        helper.assertTrue(zombie.hasEffect(MobEffects.WEAKNESS),
                "Dragonscale sword should apply Weakness to hit targets");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void dragonscaleFullArmorSetGrantsFireResistanceAndStrength(GameTestHelper helper) {
        ServerPlayer player = ChesterTestSupport.playerInTestArea(helper, "Steve");
        player.setItemSlot(EquipmentSlot.HEAD, new ItemStack(ArtilianItems.DRAGONSCALE_HELMET.get()));
        player.setItemSlot(EquipmentSlot.CHEST, new ItemStack(ArtilianItems.DRAGONSCALE_CHESTPLATE.get()));
        player.setItemSlot(EquipmentSlot.LEGS, new ItemStack(ArtilianItems.DRAGONSCALE_LEGGINGS.get()));
        player.setItemSlot(EquipmentSlot.FEET, new ItemStack(ArtilianItems.DRAGONSCALE_BOOTS.get()));

        helper.assertTrue(ArtilianArmorItem.hasFullSet(player, "dragonscale"),
                "hasFullSet should return true when wearing all 4 Dragonscale pieces");

        ArtilianArmorItem.applySetBonus(player);
        helper.assertTrue(player.hasEffect(MobEffects.FIRE_RESISTANCE),
                "Full Dragonscale armor set must grant Fire Resistance");
        helper.assertTrue(player.hasEffect(MobEffects.DAMAGE_BOOST),
                "Full Dragonscale armor set must grant Strength");

        player.setItemSlot(EquipmentSlot.HEAD, ItemStack.EMPTY);
        helper.assertFalse(ArtilianArmorItem.hasFullSet(player, "dragonscale"),
                "hasFullSet should return false when helmet is removed");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void dragonscaleGearAndMegaTntRecipesCraftFromDragonScales(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        ItemStack scale = new ItemStack(ArtilianItems.DRAGON_SCALE.get());
        ItemStack stick = new ItemStack(Items.STICK);
        ItemStack blaze = new ItemStack(Items.BLAZE_POWDER);
        ItemStack tnt = new ItemStack(Items.TNT);

        CraftingInput swordInput = CraftingInput.of(1, 3, List.of(scale, scale, stick));
        var swordRecipe = level.getRecipeManager().getRecipeFor(RecipeType.CRAFTING, swordInput, level);
        helper.assertTrue(swordRecipe.isPresent()
                        && swordRecipe.get().value().assemble(swordInput, level.registryAccess())
                                .is(ArtilianItems.DRAGONSCALE_SWORD.get()),
                "Dragonscale Sword recipe should craft from Dragon Scales + Stick");

        CraftingInput helmetInput = CraftingInput.of(3, 2, List.of(
                scale, scale, scale,
                scale, ItemStack.EMPTY, scale));
        var helmetRecipe = level.getRecipeManager().getRecipeFor(RecipeType.CRAFTING, helmetInput, level);
        helper.assertTrue(helmetRecipe.isPresent()
                        && helmetRecipe.get().value().assemble(helmetInput, level.registryAccess())
                                .is(ArtilianItems.DRAGONSCALE_HELMET.get()),
                "Dragonscale Helmet recipe should craft from Dragon Scales");

        CraftingInput tntInput = CraftingInput.of(3, 3, List.of(
                ItemStack.EMPTY, scale, ItemStack.EMPTY,
                blaze, tnt, blaze,
                ItemStack.EMPTY, scale, ItemStack.EMPTY));
        var tntRecipe = level.getRecipeManager().getRecipeFor(RecipeType.CRAFTING, tntInput, level);
        helper.assertTrue(tntRecipe.isPresent()
                        && tntRecipe.get().value().assemble(tntInput, level.registryAccess())
                                .is(ArtilianBlocks.DRAGONFIRE_MEGA_TNT.get().asItem()),
                "Dragonfire Mega TNT recipe should craft from Dragon Scales + Blaze Powder + TNT");

        helper.succeed();
    }

    @GameTest(template = "tnt_arena", timeoutTicks = 60)
    public static void dragonfireMegaTntDestroysBlocksOnlyWhenEnabledAndSparesAllies(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos stonePos = new BlockPos(7, 2, 6);
        BlockPos chestPos = new BlockPos(7, 2, 4);
        helper.setBlock(stonePos, Blocks.STONE);
        helper.setBlock(chestPos, Blocks.CHEST);

        ServerPlayer steve = CustomTntGameTests.spawnSurvivalPlayer(helper, "SteveMegaTnt", new Vec3(7.5, 2.0, 7.5));
        ChesterEntity chester = new ChesterEntity(ArtilianEntities.CHESTER.get(), level);
        Vec3 allyPos = helper.absoluteVec(new Vec3(6.5, 2.0, 7.5));
        chester.setPos(allyPos.x, allyPos.y, allyPos.z);
        chester.setOwner(steve);
        level.addFreshEntity(chester);

        Zombie foe = helper.spawnWithNoFreeWill(EntityType.ZOMBIE, new BlockPos(8, 2, 6));
        AABB arenaBox = new AABB(helper.absolutePos(new BlockPos(0, 0, 0))).minmax(
                new AABB(helper.absolutePos(new BlockPos(14, 14, 14))));

        boolean previous = ArtilianConfig.ALLOW_DESTRUCTIVE_CUSTOM_TNT.get();
        try {
            ArtilianConfig.ALLOW_DESTRUCTIVE_CUSTOM_TNT.set(true);
            Vec3 center = helper.absoluteVec(new Vec3(7.5, 2.0, 7.5));
            ArtilianPrimedTnt megaTnt = new ArtilianPrimedTnt(
                    level, center.x, center.y, center.z, steve, TntVariant.DRAGONFIRE_MEGA);
            megaTnt.setFuse(1);
            level.addFreshEntity(megaTnt);
            megaTnt.tick();

            helper.assertBlockNotPresent(Blocks.STONE, stonePos);
            helper.assertBlockPresent(Blocks.CHEST, chestPos);
            helper.assertTrue(foe.getHealth() < foe.getMaxHealth(),
                    "Dragonfire Mega TNT should damage hostile Zombie");
            helper.assertTrue(steve.getHealth() == steve.getMaxHealth(),
                    "Dragonfire Mega TNT must spare players");
            helper.assertTrue(chester.isAlive() && chester.getHealth() == chester.getMaxHealth(),
                    "Dragonfire Mega TNT must spare Chester");
        } finally {
            ArtilianConfig.ALLOW_DESTRUCTIVE_CUSTOM_TNT.set(previous);
            level.getEntitiesOfClass(ArtilianPrimedTnt.class, arenaBox).forEach(ArtilianPrimedTnt::discard);
            chester.discard();
            CustomTntGameTests.removeTestPlayer(level, steve);
        }
        helper.succeed();
    }
}
