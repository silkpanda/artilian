package com.silkpanda.artilian.gametest;

import com.silkpanda.artilian.ArtilianMod;
import com.silkpanda.artilian.gear.ArtilianArmorItem;
import com.silkpanda.artilian.registry.ArtilianItems;
import net.minecraft.core.BlockPos;
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
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import java.util.List;

/** Phase 3 Stage 4: Tier 3 Prismatic Brimstone gear, boss-forged Staves, and advanced TNTs. */
@GameTestHolder(ArtilianMod.MODID)
@PrefixGameTestTemplate(false)
public class Tier3ArsenalGameTests {

    @GameTest(template = "empty")
    public static void prismaticGearStatsFireResistanceAndTags(GameTestHelper helper) {
        List<Item> all = List.of(
                ArtilianItems.PRISMATIC_SWORD.get(),
                ArtilianItems.PRISMATIC_PICKAXE.get(),
                ArtilianItems.PRISMATIC_AXE.get(),
                ArtilianItems.PRISMATIC_SHOVEL.get(),
                ArtilianItems.PRISMATIC_HOE.get(),
                ArtilianItems.PRISMATIC_HELMET.get(),
                ArtilianItems.PRISMATIC_CHESTPLATE.get(),
                ArtilianItems.PRISMATIC_LEGGINGS.get(),
                ArtilianItems.PRISMATIC_BOOTS.get());

        for (Item item : all) {
            ItemStack stack = new ItemStack(item);
            helper.assertTrue(stack.has(net.minecraft.core.component.DataComponents.FIRE_RESISTANT),
                    item + " must be fire resistant");
        }

        ItemStack pick = new ItemStack(ArtilianItems.PRISMATIC_PICKAXE.get());
        helper.assertTrue(pick.getMaxDamage() == 2050,
                "Prismatic pickaxe should have 2050 durability, got " + pick.getMaxDamage());
        helper.assertTrue(pick.isCorrectToolForDrops(Blocks.OBSIDIAN.defaultBlockState()),
                "Prismatic pickaxe must harvest Obsidian");
        helper.assertTrue(pick.isCorrectToolForDrops(Blocks.ANCIENT_DEBRIS.defaultBlockState()),
                "Prismatic pickaxe must harvest Ancient Debris");

        helper.assertTrue(new ItemStack(ArtilianItems.PRISMATIC_SWORD.get()).is(ItemTags.SWORDS),
                "Prismatic sword should be in ItemTags.SWORDS");
        helper.assertTrue(pick.is(ItemTags.PICKAXES),
                "Prismatic pickaxe should be in ItemTags.PICKAXES");
        helper.assertTrue(new ItemStack(ArtilianItems.PRISMATIC_HELMET.get()).is(ItemTags.HEAD_ARMOR),
                "Prismatic helmet should be in ItemTags.HEAD_ARMOR");

        int totalDefense = ((ArmorItem) ArtilianItems.PRISMATIC_HELMET.get()).getDefense()
                + ((ArmorItem) ArtilianItems.PRISMATIC_CHESTPLATE.get()).getDefense()
                + ((ArmorItem) ArtilianItems.PRISMATIC_LEGGINGS.get()).getDefense()
                + ((ArmorItem) ArtilianItems.PRISMATIC_BOOTS.get()).getDefense();
        helper.assertTrue(totalDefense == 24,
                "Prismatic armor total defense should be 24, got " + totalDefense);

        ItemStack sword = new ItemStack(ArtilianItems.PRISMATIC_SWORD.get());
        final double[] swordAttack = {0.0};
        sword.forEachModifier(EquipmentSlot.MAINHAND, (attribute, modifier) -> {
            if (attribute.is(Attributes.ATTACK_DAMAGE)) {
                swordAttack[0] += modifier.amount();
            }
        });
        helper.assertTrue(swordAttack[0] > 7.5,
                "Prismatic sword should grant > 7.5 bonus attack damage, got " + swordAttack[0]);

        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void prismaticSwordLightsAndBurnsTargets(GameTestHelper helper) {
        ServerPlayer player = ChesterTestSupport.playerInTestArea(helper, "Alex");
        Zombie zombie = helper.spawnWithNoFreeWill(EntityType.ZOMBIE, new BlockPos(2, 1, 2));
        ItemStack sword = new ItemStack(ArtilianItems.PRISMATIC_SWORD.get());

        sword.getItem().hurtEnemy(sword, zombie, player);

        helper.assertTrue(zombie.hasEffect(MobEffects.GLOWING),
                "Prismatic sword should apply Glowing to hit targets");
        helper.assertTrue(zombie.getRemainingFireTicks() > 0,
                "Prismatic sword should ignite hit targets");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void prismaticFullArmorSetGrantsFireResistance(GameTestHelper helper) {
        ServerPlayer player = ChesterTestSupport.playerInTestArea(helper, "Steve");
        player.setItemSlot(EquipmentSlot.HEAD, new ItemStack(ArtilianItems.PRISMATIC_HELMET.get()));
        player.setItemSlot(EquipmentSlot.CHEST, new ItemStack(ArtilianItems.PRISMATIC_CHESTPLATE.get()));
        player.setItemSlot(EquipmentSlot.LEGS, new ItemStack(ArtilianItems.PRISMATIC_LEGGINGS.get()));
        player.setItemSlot(EquipmentSlot.FEET, new ItemStack(ArtilianItems.PRISMATIC_BOOTS.get()));

        helper.assertTrue(ArtilianArmorItem.hasFullSet(player, "prismatic"),
                "hasFullSet should return true when wearing all 4 Prismatic pieces");

        ArtilianArmorItem.applySetBonus(player);
        helper.assertTrue(player.hasEffect(MobEffects.FIRE_RESISTANCE),
                "Full Prismatic armor set must grant Fire Resistance");
        player.setItemSlot(EquipmentSlot.HEAD, ItemStack.EMPTY);
        helper.assertFalse(ArtilianArmorItem.hasFullSet(player, "prismatic"),
                "hasFullSet should return false when helmet is removed");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void prismaticGearRecipesCraftFromBossTrophies(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        ItemStack prism = new ItemStack(ArtilianItems.PRISM_CORE.get());
        ItemStack doubloon = new ItemStack(ArtilianItems.BRIMSTONE_DOUBLOON.get());
        ItemStack stick = new ItemStack(Items.STICK);

        CraftingInput swordInput = CraftingInput.of(1, 3, List.of(prism, doubloon, stick));
        var swordRecipe = level.getRecipeManager().getRecipeFor(RecipeType.CRAFTING, swordInput, level);
        helper.assertTrue(swordRecipe.isPresent()
                        && swordRecipe.get().value().assemble(swordInput, level.registryAccess())
                                .is(ArtilianItems.PRISMATIC_SWORD.get()),
                "Prismatic Sword recipe should craft from Prism Core + Brimstone Doubloon + Stick");

        CraftingInput helmetInput = CraftingInput.of(3, 2, List.of(
                prism, doubloon, prism,
                prism, ItemStack.EMPTY, prism));
        var helmetRecipe = level.getRecipeManager().getRecipeFor(RecipeType.CRAFTING, helmetInput, level);
        helper.assertTrue(helmetRecipe.isPresent()
                        && helmetRecipe.get().value().assemble(helmetInput, level.registryAccess())
                                .is(ArtilianItems.PRISMATIC_HELMET.get()),
                "Prismatic Helmet recipe should craft from Prism Core + Brimstone Doubloon");

        helper.succeed();
    }
}
