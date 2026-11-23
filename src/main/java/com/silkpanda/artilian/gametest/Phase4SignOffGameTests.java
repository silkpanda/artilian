package com.silkpanda.artilian.gametest;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.silkpanda.artilian.ArtilianMod;
import com.silkpanda.artilian.boss.GreatAllSeerEntity;
import com.silkpanda.artilian.boss.TelegraphedAttack;
import com.silkpanda.artilian.dimension.EyeRealmDimension;
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

/** Phase 4 Stage 7: Eye Realm art pass integrity, boss telegraph floor, and progression sign-off. */
@GameTestHolder(ArtilianMod.MODID)
@PrefixGameTestTemplate(false)
public class Phase4SignOffGameTests {

    @GameTest(template = "empty")
    public static void allPhase4ArtAssetsAndGeckoLibModelsExist(GameTestHelper helper) {
        assertEntityAssets(helper, "peeper",
                List.of("root", "body", "wing_left", "wing_right", "tail_stalk", "orbit_motes"),
                List.of("idle", "walk", "attack"),
                128, 128);
        assertEntityAssets(helper, "oculus_slime",
                List.of("root", "body", "eyestalks", "tentacles"),
                List.of("idle", "walk", "attack"),
                128, 128);
        assertEntityAssets(helper, "lens_guardian",
                List.of("root", "torso", "focus_lens", "orbit_shards", "head",
                        "arm_left", "arm_right", "leg_left", "leg_right"),
                List.of("idle", "walk", "attack"),
                128, 128);
        assertEntityAssets(helper, "blinky",
                List.of("root", "body", "lid_top", "lid_bottom", "wing_left", "wing_right", "crown_halo", "tail"),
                List.of("idle", "walk", "sit", "attack"),
                128, 128);
        assertEntityAssets(helper, "great_all_seer",
                List.of("body", "orbiters", "eye_sclera", "eye_iris", "lid_top", "lid_bottom",
                        "crown_base", "crown_crystal", "wing_upper_left", "wing_upper_right",
                        "wing_lower_left", "wing_lower_right", "tail_1", "tail_2", "tail_3", "tail_gem"),
                List.of("idle", "walk", "attack", "telegraph"),
                256, 128);

        for (String animatedBlock : List.of("gazing_portal", "blinking_eye_block")) {
            assertResourceExists(helper, "assets/artilian/textures/block/" + animatedBlock + ".png");
            assertResourceExists(helper, "assets/artilian/textures/block/" + animatedBlock + ".png.mcmeta");
        }

        for (String blockTex : List.of(
                "eye_turf_top", "eye_turf_side", "gaze_stone",
                "ocular_log", "ocular_log_top", "ocular_planks", "ocular_leaves", "ocular_sapling")) {
            assertResourceExists(helper, "assets/artilian/textures/block/" + blockTex + ".png");
        }

        for (String itemTex : List.of("gazing_eye", "ocular_key", "all_seers_lens")) {
            assertResourceExists(helper, "assets/artilian/textures/item/" + itemTex + ".png");
        }

        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void phase4TelegraphsRecipesDimensionAndProgressionReady(GameTestHelper helper) {
        List<TelegraphedAttack> bossAttacks = List.of(
                GreatAllSeerEntity.GAZING_BEAM,
                GreatAllSeerEntity.TWILIGHT_PULSE,
                GreatAllSeerEntity.ALL_SEEING_NOVA);
        for (TelegraphedAttack attack : bossAttacks) {
            helper.assertTrue(attack.windupTicks() >= TelegraphedAttack.MIN_WINDUP_TICKS,
                    "Great All-Seer attack " + attack.id() + " windup must be >= 30 ticks");
        }

        RecipeManager recipes = helper.getLevel().getRecipeManager();
        for (String recipeId : List.of(
                "gazing_eye", "ocular_key",
                "ocular_planks", "ocular_stairs", "ocular_slab",
                "gaze_stone_stairs", "gaze_stone_slab", "gaze_stone_wall")) {
            ResourceLocation id = ResourceLocation.fromNamespaceAndPath(ArtilianMod.MODID, recipeId);
            helper.assertTrue(recipes.byKey(id).isPresent(),
                    "Missing Phase 4 crafting recipe: " + id);
        }

        MinecraftServer server = helper.getLevel().getServer();
        var registries = helper.getLevel().registryAccess();
        helper.assertTrue(
                registries.registryOrThrow(Registries.DIMENSION_TYPE)
                        .containsKey(EyeRealmDimension.EYE_REALM_TYPE),
                "Eye Realm DimensionType must be registered");
        helper.assertTrue(
                registries.registryOrThrow(Registries.BIOME)
                        .containsKey(EyeRealmDimension.EYE_REALM_BIOME),
                "Eye Realm biome must be registered");
        assertResourceExists(helper, "data/artilian/dimension/eye_realm.json");
        assertResourceExists(helper, "data/artilian/dimension_type/eye_realm.json");

        for (String lootPath : List.of(
                "chests/ocular_observatory",
                "chests/gazing_sanctum",
                "entities/great_all_seer")) {
            ResourceKey<LootTable> lootKey = ResourceKey.create(
                    Registries.LOOT_TABLE,
                    ResourceLocation.fromNamespaceAndPath(ArtilianMod.MODID, lootPath));
            LootTable table = server.reloadableRegistries().getLootTable(lootKey);
            helper.assertTrue(table != LootTable.EMPTY, "Missing Phase 4 loot table: " + lootPath);
        }

        helper.succeed();
    }

    private static void assertEntityAssets(
            GameTestHelper helper,
            String id,
            List<String> requiredBones,
            List<String> requiredClips,
            int minW,
            int minH) {
        Set<String> bones = readGeoBoneNames("assets/artilian/geo/entity/" + id + ".geo.json");
        for (String bone : requiredBones) {
            helper.assertTrue(bones.contains(bone), id + " geo missing required bone: " + bone);
        }

        Set<String> anims = readAnimationNames("assets/artilian/animations/entity/" + id + ".animation.json");
        for (String clip : requiredClips) {
            String key = "animation." + id + "." + clip;
            helper.assertTrue(anims.contains(key), id + " missing required animation clip: " + key);
        }

        assertImageAtLeast(helper, "assets/artilian/textures/entity/" + id + ".png", minW, minH);
        assertResourceExists(helper, "assets/artilian/textures/entity/" + id + "_glowmask.png");
    }

    private static void assertImageAtLeast(GameTestHelper helper, String resourcePath, int minW, int minH) {
        try (InputStream stream = Phase4SignOffGameTests.class.getClassLoader().getResourceAsStream(resourcePath)) {
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
        try (InputStream stream = Phase4SignOffGameTests.class.getClassLoader().getResourceAsStream(resourcePath)) {
            helper.assertTrue(stream != null, "Missing Phase 4 art resource: " + resourcePath);
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
        try (InputStream stream = Phase4SignOffGameTests.class.getClassLoader().getResourceAsStream(resourcePath)) {
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
