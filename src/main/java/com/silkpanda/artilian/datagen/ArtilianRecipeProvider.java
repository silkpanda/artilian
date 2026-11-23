package com.silkpanda.artilian.datagen;

import com.silkpanda.artilian.ArtilianMod;
import com.silkpanda.artilian.registry.ArtilianBlocks;
import com.silkpanda.artilian.registry.ArtilianItems;
import com.silkpanda.artilian.registry.ArtilianTags;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.data.recipes.ShapelessRecipeBuilder;
import net.minecraft.data.recipes.SimpleCookingRecipeBuilder;
import net.minecraft.data.recipes.SingleItemRecipeBuilder;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class ArtilianRecipeProvider extends RecipeProvider {
    public ArtilianRecipeProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries);
    }

    @Override
    protected void buildRecipes(RecipeOutput output) {
        // A stick over a gold nugget. Cheap on purpose: nobody should be stuck without a whistle.
        ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, ArtilianItems.CHESTER_WHISTLE.get())
                .pattern("S")
                .pattern("N")
                .define('S', Items.STICK)
                .define('N', Items.GOLD_NUGGET)
                .unlockedBy("has_gold_nugget", has(Items.GOLD_NUGGET))
                .save(output);

        List<ItemLike> sunsteelSmeltables = List.of(
                ArtilianBlocks.SUNSTEEL_ORE.get(),
                ArtilianBlocks.DEEPSLATE_SUNSTEEL_ORE.get(),
                ArtilianItems.RAW_SUNSTEEL.get()
        );
        oreSmeltingAndBlasting(output, sunsteelSmeltables, ArtilianItems.SUNSTEEL_INGOT.get(), 0.7F, "sunsteel_ingot");

        List<ItemLike> starlightSmeltables = List.of(
                ArtilianBlocks.STARLIGHT_CRYSTAL_ORE.get(),
                ArtilianBlocks.DEEPSLATE_STARLIGHT_CRYSTAL_ORE.get()
        );
        oreSmeltingAndBlasting(output, starlightSmeltables, ArtilianItems.STARLIGHT_CRYSTAL.get(), 1.0F, "starlight_crystal");

        storageBlock(output, ArtilianItems.SUNSTEEL_INGOT.get(), ArtilianBlocks.SUNSTEEL_BLOCK.get());
        storageBlock(output, ArtilianItems.STARLIGHT_CRYSTAL.get(), ArtilianBlocks.STARLIGHT_CRYSTAL_BLOCK.get());

        gearSet(output, ArtilianItems.SUNSTEEL_INGOT.get(),
                ArtilianItems.SUNSTEEL_SWORD.get(),
                ArtilianItems.SUNSTEEL_PICKAXE.get(),
                ArtilianItems.SUNSTEEL_AXE.get(),
                ArtilianItems.SUNSTEEL_SHOVEL.get(),
                ArtilianItems.SUNSTEEL_HOE.get(),
                ArtilianItems.SUNSTEEL_HELMET.get(),
                ArtilianItems.SUNSTEEL_CHESTPLATE.get(),
                ArtilianItems.SUNSTEEL_LEGGINGS.get(),
                ArtilianItems.SUNSTEEL_BOOTS.get());

        gearSet(output, ArtilianItems.STARLIGHT_CRYSTAL.get(),
                ArtilianItems.STARLIGHT_SWORD.get(),
                ArtilianItems.STARLIGHT_PICKAXE.get(),
                ArtilianItems.STARLIGHT_AXE.get(),
                ArtilianItems.STARLIGHT_SHOVEL.get(),
                ArtilianItems.STARLIGHT_HOE.get(),
                ArtilianItems.STARLIGHT_HELMET.get(),
                ArtilianItems.STARLIGHT_CHESTPLATE.get(),
                ArtilianItems.STARLIGHT_LEGGINGS.get(),
                ArtilianItems.STARLIGHT_BOOTS.get());

        prismaticGearSet(output);

        customTntRecipe(output, ArtilianBlocks.PARTY_TNT.get(), Items.SUGAR, ArtilianItems.SUNSTEEL_INGOT.get());
        customTntRecipe(output, ArtilianBlocks.BOUNCE_TNT.get(), Items.SLIME_BALL, ArtilianItems.SUNSTEEL_INGOT.get());
        customTntRecipe(output, ArtilianBlocks.FROST_NOVA_TNT.get(), Items.SNOWBALL, ArtilianItems.STARLIGHT_CRYSTAL.get());
        customTntRecipe(output, ArtilianBlocks.CLUSTER_QUARRY_TNT.get(), ArtilianItems.STARLIGHT_CRYSTAL.get(), ArtilianItems.SUNSTEEL_INGOT.get());
        customTntRecipe(output, ArtilianBlocks.STARLIGHT_GRAVITY_TNT.get(), ArtilianItems.PRISM_CORE.get(), ArtilianItems.STARLIGHT_CRYSTAL.get());
        customTntRecipe(output, ArtilianBlocks.BRIMSTONE_METEOR_TNT.get(), ArtilianItems.BRIMSTONE_DOUBLOON.get(), Items.BLAZE_POWDER);

        wandRecipes(output);

        logToPlanks(output, ArtilianBlocks.CRYSTAL_CANOPY_LOG.get(), ArtilianBlocks.CRYSTAL_CANOPY_PLANKS.get());
        logToPlanks(output, ArtilianBlocks.EMBERWOOD_LOG.get(), ArtilianBlocks.EMBERWOOD_PLANKS.get());
        logToPlanks(output, ArtilianBlocks.OCULAR_LOG.get(), ArtilianBlocks.OCULAR_PLANKS.get());

        ItemLike ocularPlanks = ArtilianBlocks.OCULAR_PLANKS.get();
        stairBuilder(ArtilianBlocks.OCULAR_STAIRS.get(), Ingredient.of(ocularPlanks))
                .unlockedBy(getHasName(ocularPlanks), has(ocularPlanks))
                .save(output);
        slab(output, RecipeCategory.BUILDING_BLOCKS, ArtilianBlocks.OCULAR_SLAB.get(), ocularPlanks);

        ItemLike gazeStone = ArtilianBlocks.GAZE_STONE.get();
        ItemLike gazeStairs = ArtilianBlocks.GAZE_STONE_STAIRS.get();
        ItemLike gazeSlab = ArtilianBlocks.GAZE_STONE_SLAB.get();
        ItemLike gazeWall = ArtilianBlocks.GAZE_STONE_WALL.get();
        stairBuilder(gazeStairs, Ingredient.of(gazeStone))
                .unlockedBy(getHasName(gazeStone), has(gazeStone))
                .save(output);
        slab(output, RecipeCategory.BUILDING_BLOCKS, gazeSlab, gazeStone);
        wall(output, RecipeCategory.BUILDING_BLOCKS, gazeWall, gazeStone);
        slateStonecutting(output, gazeStairs, gazeStone, 1);
        slateStonecutting(output, gazeSlab, gazeStone, 2);
        slateStonecutting(output, gazeWall, gazeStone, 1);

        // Building-block variants (Structure Rebuild Stage 1)
        woodVariantRecipes(output,
                ArtilianBlocks.CRYSTAL_CANOPY_PLANKS.get(),
                ArtilianBlocks.CRYSTAL_CANOPY_STAIRS.get(),
                ArtilianBlocks.CRYSTAL_CANOPY_SLAB.get(),
                ArtilianBlocks.CRYSTAL_CANOPY_FENCE.get(),
                ArtilianBlocks.CRYSTAL_CANOPY_FENCE_GATE.get(),
                ArtilianBlocks.CRYSTAL_CANOPY_DOOR.get(),
                ArtilianBlocks.CRYSTAL_CANOPY_TRAPDOOR.get());
        woodVariantRecipes(output,
                ArtilianBlocks.EMBERWOOD_PLANKS.get(),
                ArtilianBlocks.EMBERWOOD_STAIRS.get(),
                ArtilianBlocks.EMBERWOOD_SLAB.get(),
                ArtilianBlocks.EMBERWOOD_FENCE.get(),
                ArtilianBlocks.EMBERWOOD_FENCE_GATE.get(),
                ArtilianBlocks.EMBERWOOD_DOOR.get(),
                ArtilianBlocks.EMBERWOOD_TRAPDOOR.get());

        ItemLike slate = ArtilianBlocks.STARLIGHT_SLATE.get();
        ItemLike slateStairs = ArtilianBlocks.STARLIGHT_SLATE_STAIRS.get();
        ItemLike slateSlab = ArtilianBlocks.STARLIGHT_SLATE_SLAB.get();
        ItemLike slateWall = ArtilianBlocks.STARLIGHT_SLATE_WALL.get();
        stairBuilder(slateStairs, Ingredient.of(slate))
                .unlockedBy(getHasName(slate), has(slate))
                .save(output);
        slab(output, RecipeCategory.BUILDING_BLOCKS, slateSlab, slate);
        wall(output, RecipeCategory.BUILDING_BLOCKS, slateWall, slate);
        // Vanilla's stonecutterResultFromBase saves under minecraft:, so build these with artilian: ids.
        slateStonecutting(output, slateStairs, slate, 1);
        slateStonecutting(output, slateSlab, slate, 2);
        slateStonecutting(output, slateWall, slate, 1);

        // Pet Capture Crystals & Biome Treats (Phase 2 Stage 3)
        ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, ArtilianItems.CAPTURE_CRYSTAL.get(), 2)
                .pattern(" G ")
                .pattern("ICI")
                .pattern(" G ")
                .define('G', Items.GLASS)
                .define('I', ArtilianItems.SUNSTEEL_INGOT.get())
                .define('C', ArtilianItems.STARLIGHT_CRYSTAL.get())
                .unlockedBy("has_starlight_crystal", has(ArtilianItems.STARLIGHT_CRYSTAL.get()))
                .save(output);

        ShapelessRecipeBuilder.shapeless(RecipeCategory.FOOD, ArtilianItems.BIOME_TREAT.get(), 4)
                .requires(ArtilianTags.Items.BIOME_BLOOMS)
                .requires(Items.SWEET_BERRIES)
                .requires(Items.SUGAR)
                .unlockedBy("has_biome_bloom", has(ArtilianTags.Items.BIOME_BLOOMS))
                .save(output);

        // Phase 3 Stage 2: Boss Altar & Crystal Colossus summon item
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ArtilianItems.COLOSSUS_HEART.get())
                .pattern(" C ")
                .pattern("ABA")
                .pattern(" C ")
                .define('C', ArtilianItems.STARLIGHT_CRYSTAL.get())
                .define('A', Items.AMETHYST_SHARD)
                .define('B', ArtilianBlocks.CRYSTAL_BLOOM.get())
                .unlockedBy("has_starlight_crystal", has(ArtilianItems.STARLIGHT_CRYSTAL.get()))
                .save(output);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ArtilianBlocks.BOSS_ALTAR.get())
                .pattern("SCS")
                .pattern("SXS")
                .pattern("SSS")
                .define('S', ArtilianBlocks.STARLIGHT_SLATE.get())
                .define('C', ArtilianItems.STARLIGHT_CRYSTAL.get())
                .define('X', ArtilianItems.SUNSTEEL_INGOT.get())
                .unlockedBy("has_starlight_crystal", has(ArtilianItems.STARLIGHT_CRYSTAL.get()))
                .save(output);

        // Phase 3 Stage 3: Captain Brimstone summon item
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ArtilianItems.BRIMSTONE_COMPASS.get())
                .pattern(" S ")
                .pattern("SBS")
                .pattern(" E ")
                .define('S', ArtilianItems.SUNSTEEL_INGOT.get())
                .define('B', Items.BLAZE_POWDER)
                .define('E', ArtilianBlocks.EMBER_BLOOM.get())
                .unlockedBy("has_sunsteel_ingot", has(ArtilianItems.SUNSTEEL_INGOT.get()))
                .save(output);

        // Phase 4 Stage 6: The Great All-Seer summon item
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ArtilianItems.OCULAR_KEY.get())
                .pattern(" A ")
                .pattern("CGC")
                .pattern(" P ")
                .define('A', Items.AMETHYST_SHARD)
                .define('C', ArtilianItems.STARLIGHT_CRYSTAL.get())
                .define('G', ArtilianItems.GAZING_EYE.get())
                .define('P', Items.ENDER_PEARL)
                .unlockedBy("has_gazing_eye", has(ArtilianItems.GAZING_EYE.get()))
                .save(output);
    }

    private static void slateStonecutting(RecipeOutput output, ItemLike result, ItemLike base, int count) {
        SingleItemRecipeBuilder.stonecutting(Ingredient.of(base), RecipeCategory.BUILDING_BLOCKS, result, count)
                .unlockedBy(getHasName(base), has(base))
                .save(output, ArtilianMod.id(getConversionRecipeName(result, base) + "_stonecutting"));
    }

    private static void woodVariantRecipes(
            RecipeOutput output,
            ItemLike planks,
            ItemLike stairs,
            ItemLike slab,
            ItemLike fence,
            ItemLike fenceGate,
            ItemLike door,
            ItemLike trapdoor) {
        String unlock = getHasName(planks);

        stairBuilder(stairs, Ingredient.of(planks))
                .unlockedBy(unlock, has(planks))
                .save(output);
        slab(output, RecipeCategory.BUILDING_BLOCKS, slab, planks);
        fenceBuilder(fence, Ingredient.of(planks))
                .unlockedBy(unlock, has(planks))
                .save(output);
        fenceGateBuilder(fenceGate, Ingredient.of(planks))
                .unlockedBy(unlock, has(planks))
                .save(output);
        doorBuilder(door, Ingredient.of(planks))
                .unlockedBy(unlock, has(planks))
                .save(output);
        trapdoorBuilder(trapdoor, Ingredient.of(planks))
                .unlockedBy(unlock, has(planks))
                .save(output);
    }

    private static void logToPlanks(RecipeOutput output, ItemLike log, ItemLike planks) {
        ShapelessRecipeBuilder.shapeless(RecipeCategory.BUILDING_BLOCKS, planks, 4)
                .requires(log)
                .unlockedBy("has_" + BuiltInRegistries.ITEM.getKey(log.asItem()).getPath(), has(log))
                .save(output);
    }

    private static void prismaticGearSet(RecipeOutput output) {
        ItemLike prism = ArtilianItems.PRISM_CORE.get();
        ItemLike doubloon = ArtilianItems.BRIMSTONE_DOUBLOON.get();
        String hasPrism = "has_prism_core";

        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ArtilianItems.PRISMATIC_SWORD.get())
                .pattern("P")
                .pattern("D")
                .pattern("S")
                .define('P', prism)
                .define('D', doubloon)
                .define('S', Items.STICK)
                .unlockedBy(hasPrism, has(prism))
                .save(output);

        ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, ArtilianItems.PRISMATIC_PICKAXE.get())
                .pattern("PDP")
                .pattern(" S ")
                .pattern(" S ")
                .define('P', prism)
                .define('D', doubloon)
                .define('S', Items.STICK)
                .unlockedBy(hasPrism, has(prism))
                .save(output);

        ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, ArtilianItems.PRISMATIC_AXE.get())
                .pattern("PD")
                .pattern("PS")
                .pattern(" S")
                .define('P', prism)
                .define('D', doubloon)
                .define('S', Items.STICK)
                .unlockedBy(hasPrism, has(prism))
                .save(output);

        ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, ArtilianItems.PRISMATIC_SHOVEL.get())
                .pattern("D")
                .pattern("S")
                .pattern("S")
                .define('D', doubloon)
                .define('S', Items.STICK)
                .unlockedBy("has_brimstone_doubloon", has(doubloon))
                .save(output);

        ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, ArtilianItems.PRISMATIC_HOE.get())
                .pattern("PD")
                .pattern(" S")
                .pattern(" S")
                .define('P', prism)
                .define('D', doubloon)
                .define('S', Items.STICK)
                .unlockedBy(hasPrism, has(prism))
                .save(output);

        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ArtilianItems.PRISMATIC_HELMET.get())
                .pattern("PDP")
                .pattern("P P")
                .define('P', prism)
                .define('D', doubloon)
                .unlockedBy(hasPrism, has(prism))
                .save(output);

        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ArtilianItems.PRISMATIC_CHESTPLATE.get())
                .pattern("P P")
                .pattern("PDP")
                .pattern("PPP")
                .define('P', prism)
                .define('D', doubloon)
                .unlockedBy(hasPrism, has(prism))
                .save(output);

        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ArtilianItems.PRISMATIC_LEGGINGS.get())
                .pattern("PDP")
                .pattern("P P")
                .pattern("P P")
                .define('P', prism)
                .define('D', doubloon)
                .unlockedBy(hasPrism, has(prism))
                .save(output);

        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ArtilianItems.PRISMATIC_BOOTS.get())
                .pattern("P P")
                .pattern("D D")
                .define('P', prism)
                .define('D', doubloon)
                .unlockedBy(hasPrism, has(prism))
                .save(output);
    }

    private static void wandRecipes(RecipeOutput output) {
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ArtilianItems.SPARK_WAND.get())
                .pattern("  C")
                .pattern(" G ")
                .pattern("S  ")
                .define('C', ArtilianItems.STARLIGHT_CRYSTAL.get())
                .define('G', ArtilianItems.SUNSTEEL_INGOT.get())
                .define('S', Items.STICK)
                .unlockedBy("has_starlight_crystal", has(ArtilianItems.STARLIGHT_CRYSTAL.get()))
                .save(output);

        ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, ArtilianItems.HEART_BLOOM_STAFF.get())
                .pattern("GCG")
                .pattern(" S ")
                .pattern(" S ")
                .define('C', ArtilianItems.STARLIGHT_CRYSTAL.get())
                .define('G', Items.GOLD_INGOT)
                .define('S', Items.STICK)
                .unlockedBy("has_starlight_crystal", has(ArtilianItems.STARLIGHT_CRYSTAL.get()))
                .save(output);

        ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, ArtilianItems.ZEPHYR_DASH_WAND.get())
                .pattern(" FC")
                .pattern(" GF")
                .pattern("S  ")
                .define('C', ArtilianItems.STARLIGHT_CRYSTAL.get())
                .define('G', ArtilianItems.SUNSTEEL_INGOT.get())
                .define('F', Items.FEATHER)
                .define('S', Items.STICK)
                .unlockedBy("has_starlight_crystal", has(ArtilianItems.STARLIGHT_CRYSTAL.get()))
                .save(output);

        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ArtilianItems.PRISM_NOVA_STAFF.get())
                .pattern(" PC")
                .pattern(" SP")
                .pattern("S  ")
                .define('P', ArtilianItems.PRISM_CORE.get())
                .define('C', ArtilianItems.STARLIGHT_CRYSTAL.get())
                .define('S', Items.STICK)
                .unlockedBy("has_prism_core", has(ArtilianItems.PRISM_CORE.get()))
                .save(output);

        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ArtilianItems.BRIMSTONE_METEOR_STAFF.get())
                .pattern(" DB")
                .pattern(" SD")
                .pattern("S  ")
                .define('D', ArtilianItems.BRIMSTONE_DOUBLOON.get())
                .define('B', Items.BLAZE_POWDER)
                .define('S', Items.STICK)
                .unlockedBy("has_brimstone_doubloon", has(ArtilianItems.BRIMSTONE_DOUBLOON.get()))
                .save(output);

        ShapelessRecipeBuilder.shapeless(RecipeCategory.TOOLS, ArtilianItems.GAZING_EYE.get())
                .requires(Items.ENDER_EYE)
                .requires(ArtilianItems.STARLIGHT_CRYSTAL.get())
                .requires(Items.AMETHYST_SHARD)
                .unlockedBy("has_starlight_crystal", has(ArtilianItems.STARLIGHT_CRYSTAL.get()))
                .save(output);
    }

    private static void customTntRecipe(
            RecipeOutput output,
            ItemLike result,
            ItemLike topBottomAccent,
            ItemLike sideMaterial) {
        ShapedRecipeBuilder.shaped(RecipeCategory.REDSTONE, result, 2)
                .pattern(" A ")
                .pattern("MTM")
                .pattern(" A ")
                .define('A', topBottomAccent)
                .define('M', sideMaterial)
                .define('T', Items.TNT)
                .unlockedBy("has_tnt", has(Items.TNT))
                .save(output);
    }

    private static void gearSet(
            RecipeOutput output,
            ItemLike material,
            ItemLike sword,
            ItemLike pickaxe,
            ItemLike axe,
            ItemLike shovel,
            ItemLike hoe,
            ItemLike helmet,
            ItemLike chestplate,
            ItemLike leggings,
            ItemLike boots) {
        String unlock = getHasName(material);

        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, sword)
                .pattern("M").pattern("M").pattern("S")
                .define('M', material).define('S', Items.STICK)
                .unlockedBy(unlock, has(material))
                .save(output);

        ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, pickaxe)
                .pattern("MMM").pattern(" S ").pattern(" S ")
                .define('M', material).define('S', Items.STICK)
                .unlockedBy(unlock, has(material))
                .save(output);

        ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, axe)
                .pattern("MM").pattern("MS").pattern(" S")
                .define('M', material).define('S', Items.STICK)
                .unlockedBy(unlock, has(material))
                .save(output);

        ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, shovel)
                .pattern("M").pattern("S").pattern("S")
                .define('M', material).define('S', Items.STICK)
                .unlockedBy(unlock, has(material))
                .save(output);

        ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, hoe)
                .pattern("MM").pattern(" S").pattern(" S")
                .define('M', material).define('S', Items.STICK)
                .unlockedBy(unlock, has(material))
                .save(output);

        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, helmet)
                .pattern("MMM").pattern("M M")
                .define('M', material)
                .unlockedBy(unlock, has(material))
                .save(output);

        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, chestplate)
                .pattern("M M").pattern("MMM").pattern("MMM")
                .define('M', material)
                .unlockedBy(unlock, has(material))
                .save(output);

        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, leggings)
                .pattern("MMM").pattern("M M").pattern("M M")
                .define('M', material)
                .unlockedBy(unlock, has(material))
                .save(output);

        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, boots)
                .pattern("M M").pattern("M M")
                .define('M', material)
                .unlockedBy(unlock, has(material))
                .save(output);
    }

    private static void oreSmeltingAndBlasting(
            RecipeOutput output,
            List<ItemLike> inputs,
            ItemLike result,
            float xp,
            String group) {
        for (ItemLike input : inputs) {
            SimpleCookingRecipeBuilder.smelting(Ingredient.of(input), RecipeCategory.MISC, result, xp, 200)
                    .group(group)
                    .unlockedBy(getHasName(input), has(input))
                    .save(output, ArtilianMod.id(getItemName(result) + "_from_smelting_" + getItemName(input)));

            SimpleCookingRecipeBuilder.blasting(Ingredient.of(input), RecipeCategory.MISC, result, xp, 100)
                    .group(group)
                    .unlockedBy(getHasName(input), has(input))
                    .save(output, ArtilianMod.id(getItemName(result) + "_from_blasting_" + getItemName(input)));
        }
    }

    private static void storageBlock(RecipeOutput output, ItemLike unpacked, ItemLike packed) {
        nineBlockStorageRecipes(
                output,
                RecipeCategory.MISC, unpacked,
                RecipeCategory.BUILDING_BLOCKS, packed,
                ArtilianMod.id(getItemName(packed)).toString(), null,
                ArtilianMod.id(getItemName(unpacked) + "_from_" + getItemName(packed)).toString(),
                getItemName(unpacked));
    }
}
