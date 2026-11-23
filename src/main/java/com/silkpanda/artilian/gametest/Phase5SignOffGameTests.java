package com.silkpanda.artilian.gametest;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.silkpanda.artilian.ArtilianMod;
import com.silkpanda.artilian.boss.EmbercrownSovereignEntity;
import com.silkpanda.artilian.boss.TelegraphedAttack;
import com.silkpanda.artilian.config.ArtilianConfig;
import com.silkpanda.artilian.dimension.DragonJungleDimension;
import com.silkpanda.artilian.dimension.EyeRealmDimension;
import com.silkpanda.artilian.registry.ArtilianAttachments;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.storage.loot.LootTable;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** Phase 5 Stage 10: Dragon Jungle art pass integrity, boss telegraph floor, and Grand Finale sign-off. */
@GameTestHolder(ArtilianMod.MODID)
@PrefixGameTestTemplate(false)
public class Phase5SignOffGameTests {

    @GameTest(template = "empty")
    public static void allPhase5ArtAssetsAndGeckoLibModelsExist(GameTestHelper helper) {
        // Canopy Wyvern uses shared dragon_rig geo & animations with its own 256x128 texture + glowmask
        assertEntityAssets(helper, "canopy_wyvern", "dragon_rig", "dragon_rig", "dragon_rig",
                List.of("root", "body", "neck_base", "neck_upper", "head", "jaw",
                        "left_wing_inner", "left_wing_outer", "right_wing_inner", "right_wing_outer",
                        "front_left_leg", "front_right_leg", "back_left_leg", "back_right_leg",
                        "tail_1", "tail_2", "tail_3"),
                List.of("idle", "walk", "fly", "attack"),
                256, 128);

        assertEntityAssets(helper, "amber_wyvern", "amber_wyvern", "amber_wyvern", "dragon_rig",
                List.of("root", "body", "neck_base", "neck_upper", "head", "jaw",
                        "left_wing_arm", "left_wing_outer", "right_wing_arm", "right_wing_outer",
                        "left_leg", "right_leg", "tail_1", "tail_2", "tail_stinger"),
                List.of("idle", "walk", "fly", "attack"),
                256, 128);

        assertEntityAssets(helper, "storm_glidewing", "storm_glidewing", "storm_glidewing", "dragon_rig",
                List.of("root", "body", "neck", "head", "jaw",
                        "left_wing_inner", "left_wing_mid", "left_wing_tip",
                        "right_wing_inner", "right_wing_mid", "right_wing_tip",
                        "coil_1", "coil_2", "coil_3", "tail_plume"),
                List.of("idle", "walk", "fly", "attack"),
                256, 128);

        assertEntityAssets(helper, "canopy_dragonling", "canopy_dragonling", "canopy_dragonling", "canopy_dragonling",
                List.of("body", "head", "jaw", "left_wing", "right_wing",
                        "front_left_leg", "front_right_leg", "back_left_leg", "back_right_leg",
                        "tail_1", "tail_2"),
                List.of("idle", "walk", "sit", "attack"),
                128, 128);

        assertEntityAssets(helper, "amber_dragonling", "amber_dragonling", "amber_dragonling", "amber_dragonling",
                List.of("body", "head", "jaw", "left_wing", "right_wing",
                        "back_left_leg", "back_right_leg", "tail_1", "tail_2"),
                List.of("idle", "walk", "sit", "attack"),
                128, 128);

        assertEntityAssets(helper, "storm_dragonling", "storm_dragonling", "storm_dragonling", "storm_dragonling",
                List.of("body", "head", "jaw", "left_wing", "right_wing", "tail_1", "tail_2"),
                List.of("idle", "walk", "sit", "attack"),
                128, 128);

        assertEntityAssets(helper, "embercrown_sovereign", "embercrown_sovereign", "embercrown_sovereign", "embercrown_sovereign",
                List.of("root", "body", "dorsal_armor", "neck_lower", "neck_upper", "head", "jaw",
                        "crown_center", "horn_inner_left", "horn_inner_right", "horn_outer_left", "horn_outer_right",
                        "front_left_leg", "front_right_leg", "back_left_leg", "back_right_leg",
                        "left_wing_inner", "left_wing_mid", "left_wing_outer",
                        "right_wing_inner", "right_wing_mid", "right_wing_outer",
                        "tail_1", "tail_2", "tail_3"),
                List.of("idle", "walk", "attack", "telegraph"),
                256, 128);

        for (String armorId : List.of("dragonscale_armor", "twin_star_armor")) {
            assertResourceExists(helper, "assets/artilian/geo/item/armor/" + armorId + ".geo.json");
            assertImageAtLeast(helper, "assets/artilian/textures/item/armor/" + armorId + ".png", 64, 64);
        }

        assertResourceExists(helper, "assets/artilian/textures/block/primal_portal.png");
        assertResourceExists(helper, "assets/artilian/textures/block/primal_portal.png.mcmeta");

        for (String species : List.of("canopy_wyvern", "amber_wyvern", "storm_glidewing")) {
            for (int stage = 0; stage <= 2; stage++) {
                assertResourceExists(helper, "assets/artilian/textures/block/" + species + "_egg_" + stage + ".png");
            }
        }

        for (String blockTex : List.of(
                "jungle_turf_top", "jungle_turf_side", "primal_basalt", "amber_moss",
                "dragonwood_log", "dragonwood_log_top", "dragonwood_planks", "dragonwood_leaves", "dragonwood_sapling",
                "dragonfire_mega_tnt_top", "dragonfire_mega_tnt_side", "dragonfire_mega_tnt_bottom")) {
            assertResourceExists(helper, "assets/artilian/textures/block/" + blockTex + ".png");
        }

        for (String itemTex : List.of(
                "dragon_scale", "primal_dragon_crest", "sovereigns_ember_heart",
                "dragonscale_sword", "dragonscale_pickaxe", "dragonscale_axe", "dragonscale_shovel", "dragonscale_hoe",
                "dragonscale_helmet", "dragonscale_chestplate", "dragonscale_leggings", "dragonscale_boots",
                "twin_star_core", "twin_star_blade", "twin_star_staff",
                "twin_star_helmet", "twin_star_chestplate", "twin_star_leggings", "twin_star_boots")) {
            assertResourceExists(helper, "assets/artilian/textures/item/" + itemTex + ".png");
        }

        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void phase5TelegraphsRecipesDimensionAndLootReady(GameTestHelper helper) {
        List<TelegraphedAttack> bossAttacks = List.of(
                EmbercrownSovereignEntity.EMBER_BREATH,
                EmbercrownSovereignEntity.SOVEREIGN_ROAR,
                EmbercrownSovereignEntity.METEOR_SLAM);
        for (TelegraphedAttack attack : bossAttacks) {
            helper.assertTrue(attack.windupTicks() >= TelegraphedAttack.MIN_WINDUP_TICKS,
                    "Embercrown Sovereign attack " + attack.id() + " windup must be >= 30 ticks");
        }

        RecipeManager recipes = helper.getLevel().getRecipeManager();
        for (String recipeId : List.of(
                "dragonwood_planks", "dragonwood_stairs", "dragonwood_slab",
                "primal_basalt_stairs", "primal_basalt_slab", "primal_basalt_wall",
                "dragonfire_mega_tnt", "primal_dragon_crest",
                "dragonscale_sword", "dragonscale_pickaxe", "dragonscale_helmet",
                "twin_star_core", "twin_star_blade", "twin_star_staff",
                "twin_star_helmet", "twin_star_chestplate", "twin_star_leggings", "twin_star_boots")) {
            ResourceLocation id = ResourceLocation.fromNamespaceAndPath(ArtilianMod.MODID, recipeId);
            helper.assertTrue(recipes.byKey(id).isPresent(),
                    "Missing Phase 5 crafting recipe: " + id);
        }

        MinecraftServer server = helper.getLevel().getServer();
        var registries = helper.getLevel().registryAccess();
        helper.assertTrue(
                registries.registryOrThrow(Registries.DIMENSION_TYPE)
                        .containsKey(DragonJungleDimension.DRAGON_JUNGLE_TYPE),
                "Dragon Jungle DimensionType must be registered");
        helper.assertTrue(
                registries.registryOrThrow(Registries.BIOME)
                        .containsKey(DragonJungleDimension.DRAGON_JUNGLE_BIOME),
                "Dragon Jungle biome must be registered");
        helper.assertTrue(
                registries.registryOrThrow(Registries.BIOME)
                        .containsKey(DragonJungleDimension.AMBER_GROVE_BIOME),
                "Amber Volcanic Grove biome must be registered");
        assertResourceExists(helper, "data/artilian/dimension/dragon_jungle.json");
        assertResourceExists(helper, "data/artilian/dimension_type/dragon_jungle.json");

        for (String lootPath : List.of(
                "chests/dragon_roost",
                "chests/amber_temple",
                "entities/canopy_wyvern",
                "entities/amber_wyvern",
                "entities/storm_glidewing",
                "entities/embercrown_sovereign")) {
            ResourceKey<LootTable> lootKey = ResourceKey.create(
                    Registries.LOOT_TABLE,
                    ResourceLocation.fromNamespaceAndPath(ArtilianMod.MODID, lootPath));
            LootTable table = server.reloadableRegistries().getLootTable(lootKey);
            helper.assertTrue(table != LootTable.EMPTY, "Missing Phase 5 loot table: " + lootPath);
        }

        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void finaleAllFivePhasesCoreProgressionIntegrated(GameTestHelper helper) {
        // Kid-safe defaults remain intact across all 5 phases
        helper.assertFalse(ArtilianConfig.isDestructiveTntAllowed(),
                "allowDestructiveCustomTnt must default to false for kid-safe co-op play");
        helper.assertTrue(ArtilianAttachments.CHESTER.isBound(),
                "Chester player attachment must be bound");

        // Both Alex's Eye Realm (Phase 4) and Steve's Dragon Jungle (Phase 5) are registered
        var dimTypes = helper.getLevel().registryAccess().registryOrThrow(Registries.DIMENSION_TYPE);
        helper.assertTrue(dimTypes.containsKey(EyeRealmDimension.EYE_REALM_TYPE),
                "Eye Realm DimensionType must remain registered in finale");
        helper.assertTrue(dimTypes.containsKey(DragonJungleDimension.DRAGON_JUNGLE_TYPE),
                "Dragon Jungle DimensionType must be registered in finale");

        // Twin-Star Core unites Phase 4 + Phase 5 boss trophies
        ResourceLocation twinStarCoreId = ResourceLocation.fromNamespaceAndPath(ArtilianMod.MODID, "twin_star_core");
        helper.assertTrue(helper.getLevel().getRecipeManager().byKey(twinStarCoreId).isPresent(),
                "Twin-Star Core finale recipe must be present");

        helper.succeed();
    }

    private static void assertEntityAssets(
            GameTestHelper helper,
            String texId,
            String geoId,
            String animFileId,
            String animClipPrefix,
            List<String> requiredBones,
            List<String> requiredClips,
            int minW,
            int minH) {
        Set<String> bones = readGeoBoneNames("assets/artilian/geo/entity/" + geoId + ".geo.json");
        for (String bone : requiredBones) {
            helper.assertTrue(bones.contains(bone), geoId + " geo missing required bone: " + bone);
        }

        Set<String> anims = readAnimationNames("assets/artilian/animations/entity/" + animFileId + ".animation.json");
        for (String clip : requiredClips) {
            String key = "animation." + animClipPrefix + "." + clip;
            helper.assertTrue(anims.contains(key), animFileId + " missing required animation clip: " + key);
        }

        assertImageAtLeast(helper, "assets/artilian/textures/entity/" + texId + ".png", minW, minH);
        assertResourceExists(helper, "assets/artilian/textures/entity/" + texId + "_glowmask.png");
    }

    private static void assertImageAtLeast(GameTestHelper helper, String resourcePath, int minW, int minH) {
        try (InputStream stream = Phase5SignOffGameTests.class.getClassLoader().getResourceAsStream(resourcePath)) {
            helper.assertTrue(stream != null, "Missing texture resource: " + resourcePath);
            BufferedImage img = ImageIO.read(stream);
            helper.assertTrue(img != null, "Unreadable PNG resource: " + resourcePath);
            helper.assertTrue(img.getWidth() >= minW && img.getHeight() >= minH,
                    resourcePath + " must be at least " + minW + "x" + minH
                            + " (found " + img.getWidth() + "x" + img.getHeight() + ")");
        } catch (IOException e) {
            helper.fail("Failed reading PNG " + resourcePath + ": " + e.getMessage());
        }
    }

    private static void assertResourceExists(GameTestHelper helper, String resourcePath) {
        try (InputStream stream = Phase5SignOffGameTests.class.getClassLoader().getResourceAsStream(resourcePath)) {
            helper.assertTrue(stream != null, "Missing Phase 5 art resource: " + resourcePath);
        } catch (IOException e) {
            helper.fail("Failed to read resource " + resourcePath + ": " + e.getMessage());
        }
    }

    private static Set<String> readGeoBoneNames(String resourcePath) {
        JsonObject root = readJson(resourcePath);
        JsonArray geos = root.getAsJsonArray("minecraft:geometry");
        Set<String> names = new HashSet<>();
        if (geos == null || geos.isEmpty()) {
            return names;
        }
        JsonArray bones = geos.get(0).getAsJsonObject().getAsJsonArray("bones");
        if (bones == null) {
            return names;
        }
        for (JsonElement element : bones) {
            names.add(element.getAsJsonObject().get("name").getAsString());
        }
        return names;
    }

    private static Set<String> readAnimationNames(String resourcePath) {
        JsonObject root = readJson(resourcePath);
        JsonObject animations = root.getAsJsonObject("animations");
        return animations != null ? animations.keySet() : Set.of();
    }

    private static JsonObject readJson(String resourcePath) {
        try (InputStream stream = Phase5SignOffGameTests.class.getClassLoader().getResourceAsStream(resourcePath)) {
            if (stream == null) {
                throw new IllegalStateException("Missing JSON resource: " + resourcePath);
            }
            try (InputStreamReader reader = new InputStreamReader(stream, StandardCharsets.UTF_8)) {
                return JsonParser.parseReader(reader).getAsJsonObject();
            }
        } catch (IOException e) {
            throw new IllegalStateException("Failed to parse JSON resource: " + resourcePath, e);
        }
    }
}
