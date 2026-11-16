package com.silkpanda.artilian.gametest;

import com.silkpanda.artilian.ArtilianMod;
import com.silkpanda.artilian.registry.ArtilianBlocks;
import com.silkpanda.artilian.registry.ArtilianItems;
import com.silkpanda.artilian.worldgen.ArtilianWorldGen;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import java.util.List;

/** Phase 1 Stage 3: Sunsteel and Starlight Crystal ores, worldgen, and starter gear. */
@GameTestHolder(ArtilianMod.MODID)
@PrefixGameTestTemplate(false)
public class OresAndGearGameTests {

    @GameTest(template = "empty")
    public static void sunsteelOreDropsRawSunsteelWithStonePickaxe(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        ServerPlayer player = ChesterTestSupport.playerInTestArea(helper, "Steve");
        BlockPos relPos = new BlockPos(1, 1, 1);
        BlockPos absPos = helper.absolutePos(relPos);

        for (Block ore : List.of(ArtilianBlocks.SUNSTEEL_ORE.get(), ArtilianBlocks.DEEPSLATE_SUNSTEEL_ORE.get())) {
            helper.setBlock(relPos, ore);
            BlockState state = level.getBlockState(absPos);

            ItemStack woodenPick = new ItemStack(Items.WOODEN_PICKAXE);
            player.setItemInHand(InteractionHand.MAIN_HAND, woodenPick);
            helper.assertFalse(player.hasCorrectToolForDrops(state, level, absPos),
                    "Wooden pickaxe should not harvest " + ore.getDescriptionId());

            ItemStack stonePick = new ItemStack(Items.STONE_PICKAXE);
            player.setItemInHand(InteractionHand.MAIN_HAND, stonePick);
            helper.assertTrue(player.hasCorrectToolForDrops(state, level, absPos),
                    "Stone pickaxe should harvest " + ore.getDescriptionId());

            List<ItemStack> drops = Block.getDrops(state, level, absPos, null, player, stonePick);
            helper.assertTrue(drops.stream().anyMatch(s -> s.is(ArtilianItems.RAW_SUNSTEEL.get()) && s.getCount() >= 1),
                    ore.getDescriptionId() + " should drop Raw Sunsteel, got " + drops);
        }
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void starlightOreRequiresIronPickaxeAndSilkTouchWorks(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        ServerPlayer player = ChesterTestSupport.playerInTestArea(helper, "Alex");
        BlockPos relPos = new BlockPos(1, 1, 1);
        BlockPos absPos = helper.absolutePos(relPos);

        helper.setBlock(relPos, ArtilianBlocks.STARLIGHT_CRYSTAL_ORE.get());
        BlockState state = level.getBlockState(absPos);

        ItemStack stonePick = new ItemStack(Items.STONE_PICKAXE);
        player.setItemInHand(InteractionHand.MAIN_HAND, stonePick);
        helper.assertFalse(player.hasCorrectToolForDrops(state, level, absPos),
                "Stone pickaxe should not harvest Starlight Crystal Ore");

        ItemStack ironPick = new ItemStack(Items.IRON_PICKAXE);
        player.setItemInHand(InteractionHand.MAIN_HAND, ironPick);
        helper.assertTrue(player.hasCorrectToolForDrops(state, level, absPos),
                "Iron pickaxe should harvest Starlight Crystal Ore");

        List<ItemStack> normalDrops = Block.getDrops(state, level, absPos, null, player, ironPick);
        helper.assertTrue(normalDrops.stream().anyMatch(s -> s.is(ArtilianItems.STARLIGHT_CRYSTAL.get())),
                "Starlight Crystal Ore should drop Starlight Crystal, got " + normalDrops);

        ItemStack silkPick = new ItemStack(Items.IRON_PICKAXE);
        Holder<Enchantment> silkTouch = level.registryAccess()
                .registryOrThrow(Registries.ENCHANTMENT)
                .getHolderOrThrow(Enchantments.SILK_TOUCH);
        silkPick.enchant(silkTouch, 1);
        List<ItemStack> silkDrops = Block.getDrops(state, level, absPos, null, player, silkPick);
        helper.assertTrue(silkDrops.stream().anyMatch(s -> s.is(ArtilianItems.STARLIGHT_CRYSTAL_ORE_ITEM.get())),
                "Silk Touch Iron Pickaxe should drop Starlight Crystal Ore block, got " + silkDrops);

        int stoneOreLight = ArtilianBlocks.STARLIGHT_CRYSTAL_ORE.get().defaultBlockState().getLightEmission(level, absPos);
        int deepslateOreLight = ArtilianBlocks.DEEPSLATE_STARLIGHT_CRYSTAL_ORE.get().defaultBlockState().getLightEmission(level, absPos);
        int blockLight = ArtilianBlocks.STARLIGHT_CRYSTAL_BLOCK.get().defaultBlockState().getLightEmission(level, absPos);
        helper.assertTrue(stoneOreLight == 10, "Expected Starlight Crystal Ore light level 10, got " + stoneOreLight);
        helper.assertTrue(deepslateOreLight == 10, "Expected Deepslate Starlight Crystal Ore light level 10, got " + deepslateOreLight);
        helper.assertTrue(blockLight == 14, "Expected Starlight Crystal Block light level 14, got " + blockLight);

        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void smeltingAndStorageRecipesExist(GameTestHelper helper) {
        RecipeManager recipes = helper.getLevel().getRecipeManager();
        List<String> expectedIds = List.of(
                "sunsteel_ingot_from_smelting_sunsteel_ore",
                "sunsteel_ingot_from_smelting_deepslate_sunsteel_ore",
                "sunsteel_ingot_from_smelting_raw_sunsteel",
                "sunsteel_ingot_from_blasting_sunsteel_ore",
                "sunsteel_ingot_from_blasting_deepslate_sunsteel_ore",
                "sunsteel_ingot_from_blasting_raw_sunsteel",
                "starlight_crystal_from_smelting_starlight_crystal_ore",
                "starlight_crystal_from_smelting_deepslate_starlight_crystal_ore",
                "starlight_crystal_from_blasting_starlight_crystal_ore",
                "starlight_crystal_from_blasting_deepslate_starlight_crystal_ore",
                "sunsteel_block",
                "sunsteel_ingot_from_sunsteel_block",
                "starlight_crystal_block",
                "starlight_crystal_from_starlight_crystal_block"
        );
        for (String path : expectedIds) {
            ResourceLocation id = ArtilianMod.id(path);
            helper.assertTrue(recipes.byKey(id).isPresent(), "Missing recipe: " + id);
        }
        helper.succeed();
    }

    @GameTest(template = "worldgen")
    public static void worldgenFeaturesPlaceStoneAndDeepslateOres(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        Registry<PlacedFeature> placedRegistry = level.registryAccess().registryOrThrow(Registries.PLACED_FEATURE);
        Holder<PlacedFeature> sunsteelPlaced = placedRegistry.getHolderOrThrow(ArtilianWorldGen.ORE_SUNSTEEL_PLACED);
        Holder<PlacedFeature> starlightPlaced = placedRegistry.getHolderOrThrow(ArtilianWorldGen.ORE_STARLIGHT_CRYSTAL_PLACED);

        Biome plains = level.registryAccess().registryOrThrow(Registries.BIOME).getHolderOrThrow(Biomes.PLAINS).value();
        List<HolderSet<PlacedFeature>> steps = plains.getGenerationSettings().features();
        int oreStep = GenerationStep.Decoration.UNDERGROUND_ORES.ordinal();
        helper.assertTrue(oreStep < steps.size(), "Overworld biome is missing UNDERGROUND_ORES generation step");
        HolderSet<PlacedFeature> oreFeatures = steps.get(oreStep);
        helper.assertTrue(oreFeatures.contains(sunsteelPlaced),
                "Sunsteel Ore placed feature is not injected into Overworld biomes");
        helper.assertTrue(oreFeatures.contains(starlightPlaced),
                "Starlight Crystal Ore placed feature is not injected into Overworld biomes");

        Registry<ConfiguredFeature<?, ?>> configuredRegistry =
                level.registryAccess().registryOrThrow(Registries.CONFIGURED_FEATURE);
        ConfiguredFeature<?, ?> sunsteelFeature = configuredRegistry.getHolderOrThrow(ArtilianWorldGen.ORE_SUNSTEEL).value();
        ConfiguredFeature<?, ?> starlightFeature = configuredRegistry.getHolderOrThrow(ArtilianWorldGen.ORE_STARLIGHT_CRYSTAL).value();
        ChunkGenerator generator = level.getChunkSource().getGenerator();
        BlockPos origin = helper.absolutePos(new BlockPos(3, 3, 3));

        assertPlacesOreInHost(helper, level, Blocks.STONE, sunsteelFeature, generator, origin,
                ArtilianBlocks.SUNSTEEL_ORE.get(), "Sunsteel Ore in stone");
        assertPlacesOreInHost(helper, level, Blocks.STONE, starlightFeature, generator, origin,
                ArtilianBlocks.STARLIGHT_CRYSTAL_ORE.get(), "Starlight Crystal Ore in stone");
        assertPlacesOreInHost(helper, level, Blocks.DEEPSLATE, sunsteelFeature, generator, origin,
                ArtilianBlocks.DEEPSLATE_SUNSTEEL_ORE.get(), "Deepslate Sunsteel Ore in deepslate");
        assertPlacesOreInHost(helper, level, Blocks.DEEPSLATE, starlightFeature, generator, origin,
                ArtilianBlocks.DEEPSLATE_STARLIGHT_CRYSTAL_ORE.get(), "Deepslate Starlight Crystal Ore in deepslate");

        helper.succeed();
    }

    private static void assertPlacesOreInHost(
            GameTestHelper helper,
            ServerLevel level,
            Block hostBlock,
            ConfiguredFeature<?, ?> feature,
            ChunkGenerator generator,
            BlockPos origin,
            Block expectedOre,
            String label) {
        fillCube(helper, 1, 5, hostBlock);
        boolean placed = feature.place(level, generator, RandomSource.create(42L), origin);
        helper.assertTrue(placed && countBlocks(helper, 1, 5, expectedOre) > 0,
                label + " did not generate inside test volume");
    }

    private static void fillCube(GameTestHelper helper, int min, int max, Block block) {
        for (int x = min; x <= max; x++) {
            for (int y = min; y <= max; y++) {
                for (int z = min; z <= max; z++) {
                    helper.setBlock(new BlockPos(x, y, z), block);
                }
            }
        }
    }

    private static int countBlocks(GameTestHelper helper, int min, int max, Block target) {
        int count = 0;
        for (int x = min; x <= max; x++) {
            for (int y = min; y <= max; y++) {
                for (int z = min; z <= max; z++) {
                    if (helper.getBlockState(new BlockPos(x, y, z)).is(target)) {
                        count++;
                    }
                }
            }
        }
        return count;
    }

    @GameTest(template = "empty")
    public static void sunsteelPickaxeMinesStarlightAndStarlightMinesObsidian(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        ServerPlayer player = ChesterTestSupport.playerInTestArea(helper, "Steve");
        BlockPos starlightRel = new BlockPos(1, 1, 1);
        BlockPos obsidianRel = new BlockPos(1, 1, 2);
        BlockPos starlightPos = helper.absolutePos(starlightRel);
        BlockPos obsidianPos = helper.absolutePos(obsidianRel);
        helper.setBlock(starlightRel, ArtilianBlocks.STARLIGHT_CRYSTAL_ORE.get());
        helper.setBlock(obsidianRel, Blocks.OBSIDIAN);
        BlockState starlightState = level.getBlockState(starlightPos);
        BlockState obsidianState = level.getBlockState(obsidianPos);

        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(ArtilianItems.SUNSTEEL_PICKAXE.get()));
        helper.assertTrue(player.hasCorrectToolForDrops(starlightState, level, starlightPos),
                "Sunsteel Pickaxe should harvest Starlight Crystal Ore");
        helper.assertFalse(player.hasCorrectToolForDrops(obsidianState, level, obsidianPos),
                "Sunsteel Pickaxe should not harvest Obsidian");

        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(ArtilianItems.STARLIGHT_PICKAXE.get()));
        helper.assertTrue(player.hasCorrectToolForDrops(starlightState, level, starlightPos),
                "Starlight Pickaxe should harvest Starlight Crystal Ore");
        helper.assertTrue(player.hasCorrectToolForDrops(obsidianState, level, obsidianPos),
                "Starlight Pickaxe should harvest Obsidian");

        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void everyStarterGearPieceIsCraftable(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        var craftingRecipes = level.getRecipeManager().getAllRecipesFor(RecipeType.CRAFTING);
        List<Item> gear = List.of(
                ArtilianItems.SUNSTEEL_SWORD.get(),
                ArtilianItems.SUNSTEEL_PICKAXE.get(),
                ArtilianItems.SUNSTEEL_AXE.get(),
                ArtilianItems.SUNSTEEL_SHOVEL.get(),
                ArtilianItems.SUNSTEEL_HOE.get(),
                ArtilianItems.STARLIGHT_SWORD.get(),
                ArtilianItems.STARLIGHT_PICKAXE.get(),
                ArtilianItems.STARLIGHT_AXE.get(),
                ArtilianItems.STARLIGHT_SHOVEL.get(),
                ArtilianItems.STARLIGHT_HOE.get(),
                ArtilianItems.SUNSTEEL_HELMET.get(),
                ArtilianItems.SUNSTEEL_CHESTPLATE.get(),
                ArtilianItems.SUNSTEEL_LEGGINGS.get(),
                ArtilianItems.SUNSTEEL_BOOTS.get(),
                ArtilianItems.STARLIGHT_HELMET.get(),
                ArtilianItems.STARLIGHT_CHESTPLATE.get(),
                ArtilianItems.STARLIGHT_LEGGINGS.get(),
                ArtilianItems.STARLIGHT_BOOTS.get()
        );
        for (Item piece : gear) {
            boolean craftable = craftingRecipes.stream()
                    .anyMatch(holder -> holder.value().getResultItem(level.registryAccess()).is(piece));
            helper.assertTrue(craftable, "Missing crafting recipe for " + piece.getDescriptionId());
        }
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void weaponsAndArmorApplyStatsAndRepair(GameTestHelper helper) {
        int sunsteelDefense = totalDefense(
                ArtilianItems.SUNSTEEL_HELMET.get(),
                ArtilianItems.SUNSTEEL_CHESTPLATE.get(),
                ArtilianItems.SUNSTEEL_LEGGINGS.get(),
                ArtilianItems.SUNSTEEL_BOOTS.get());
        int starlightDefense = totalDefense(
                ArtilianItems.STARLIGHT_HELMET.get(),
                ArtilianItems.STARLIGHT_CHESTPLATE.get(),
                ArtilianItems.STARLIGHT_LEGGINGS.get(),
                ArtilianItems.STARLIGHT_BOOTS.get());
        helper.assertTrue(sunsteelDefense == 17, "Expected Sunsteel armor defense 17, got " + sunsteelDefense);
        helper.assertTrue(starlightDefense == 20, "Expected Starlight armor defense 20, got " + starlightDefense);

        double ironDamage = attackDamageBonus(new ItemStack(Items.IRON_SWORD));
        double sunsteelDamage = attackDamageBonus(new ItemStack(ArtilianItems.SUNSTEEL_SWORD.get()));
        double starlightDamage = attackDamageBonus(new ItemStack(ArtilianItems.STARLIGHT_SWORD.get()));
        helper.assertTrue(sunsteelDamage > ironDamage,
                "Sunsteel Sword damage (" + sunsteelDamage + ") should exceed Iron Sword (" + ironDamage + ")");
        helper.assertTrue(starlightDamage > sunsteelDamage,
                "Starlight Sword damage (" + starlightDamage + ") should exceed Sunsteel Sword (" + sunsteelDamage + ")");

        ItemStack sunsteelArmor = new ItemStack(ArtilianItems.SUNSTEEL_CHESTPLATE.get());
        ItemStack sunsteelSword = new ItemStack(ArtilianItems.SUNSTEEL_SWORD.get());
        ItemStack sunsteelIngot = new ItemStack(ArtilianItems.SUNSTEEL_INGOT.get());
        ItemStack starlightArmor = new ItemStack(ArtilianItems.STARLIGHT_CHESTPLATE.get());
        ItemStack starlightSword = new ItemStack(ArtilianItems.STARLIGHT_SWORD.get());
        ItemStack starlightCrystal = new ItemStack(ArtilianItems.STARLIGHT_CRYSTAL.get());

        helper.assertTrue(sunsteelArmor.getItem().isValidRepairItem(sunsteelArmor, sunsteelIngot),
                "Sunsteel armor should repair with Sunsteel Ingot");
        helper.assertTrue(sunsteelSword.getItem().isValidRepairItem(sunsteelSword, sunsteelIngot),
                "Sunsteel Sword should repair with Sunsteel Ingot");
        helper.assertFalse(sunsteelArmor.getItem().isValidRepairItem(sunsteelArmor, starlightCrystal),
                "Sunsteel armor should not repair with Starlight Crystal");
        helper.assertTrue(starlightArmor.getItem().isValidRepairItem(starlightArmor, starlightCrystal),
                "Starlight armor should repair with Starlight Crystal");
        helper.assertTrue(starlightSword.getItem().isValidRepairItem(starlightSword, starlightCrystal),
                "Starlight Sword should repair with Starlight Crystal");

        helper.succeed();
    }

    private static int totalDefense(ArmorItem... pieces) {
        int sum = 0;
        for (ArmorItem piece : pieces) {
            sum += piece.getDefense();
        }
        return sum;
    }

    private static double attackDamageBonus(ItemStack stack) {
        final double[] total = {0.0};
        stack.forEachModifier(EquipmentSlot.MAINHAND, (attribute, modifier) -> {
            if (attribute.is(Attributes.ATTACK_DAMAGE)) {
                total[0] += modifier.amount();
            }
        });
        return total[0];
    }
}
