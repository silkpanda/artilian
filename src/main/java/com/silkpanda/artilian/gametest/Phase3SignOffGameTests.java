package com.silkpanda.artilian.gametest;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.silkpanda.artilian.ArtilianMod;
import com.silkpanda.artilian.boss.CaptainBrimstoneEntity;
import com.silkpanda.artilian.boss.CrystalColossusEntity;
import com.silkpanda.artilian.boss.TelegraphedAttack;
import com.silkpanda.artilian.boss.TrainingGolemEntity;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeManager;
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

/** Phase 3 Stage 5: Art pass integrity, boss telegraph floor, and Tier 3 progression sign-off. */
@GameTestHolder(ArtilianMod.MODID)
@PrefixGameTestTemplate(false)
public class Phase3SignOffGameTests {

    @GameTest(template = "empty")
    public static void allPhase3ArtAssetsAndGeckoLibModelsExist(GameTestHelper helper) {
        // Bosses: geo bones, 4 animations, minimum sheet dimensions, and glowmasks for full bosses
        assertBossAssets(helper, "training_golem",
                List.of("root", "body", "head", "arm_left", "arm_right", "leg_left", "leg_right"),
                128, 128, false);
        assertBossAssets(helper, "crystal_colossus",
                List.of("root", "body", "head", "shoulder_left", "shoulder_right",
                        "arm_left", "arm_right", "leg_left", "leg_right", "floating_shards"),
                256, 128, true);
        assertBossAssets(helper, "captain_brimstone",
                List.of("root", "body", "head", "arm_left", "arm_right",
                        "leg_left", "leg_right", "leg_back_left", "leg_back_right"),
                256, 128, true);

        // Prismatic 3D armor
        helper.assertTrue(!readGeoBoneNames("assets/artilian/geo/item/armor/prismatic_armor.geo.json").isEmpty(),
                "Prismatic armor geo must define bones");
        assertResourceExists(helper, "assets/artilian/textures/item/armor/prismatic_armor.png");

        // Block textures (Boss Altar + Phase 3 TNTs)
        assertResourceExists(helper, "assets/artilian/textures/block/boss_altar.png");
        for (String tnt : List.of("starlight_gravity_tnt", "brimstone_meteor_tnt")) {
            assertResourceExists(helper, "assets/artilian/textures/block/" + tnt + "_side.png");
            assertResourceExists(helper, "assets/artilian/textures/block/" + tnt + "_top.png");
            assertResourceExists(helper, "assets/artilian/textures/block/" + tnt + "_bottom.png");
        }

        // Phase 3 item textures (boss summon/trophies + Tier 3 gear & staves)
        for (String item : List.of(
                "golem_core", "colossus_heart", "prism_core", "brimstone_compass", "brimstone_doubloon",
                "prismatic_sword", "prismatic_pickaxe", "prismatic_axe", "prismatic_shovel", "prismatic_hoe",
                "prismatic_helmet", "prismatic_chestplate", "prismatic_leggings", "prismatic_boots",
                "prism_nova_staff", "brimstone_meteor_staff")) {
            assertResourceExists(helper, "assets/artilian/textures/item/" + item + ".png");
        }

        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void phase3BossesTelegraphsAreKidFriendlyAndTier3RecipesExist(GameTestHelper helper) {
        List<TelegraphedAttack> allAttacks = List.of(
                TrainingGolemEntity.GROUND_SLAM,
                CrystalColossusEntity.CRYSTAL_QUAKE,
                CrystalColossusEntity.STARLIGHT_SPIRE,
                CrystalColossusEntity.PRISM_NOVA,
                CaptainBrimstoneEntity.POWDER_KEG_TOSS,
                CaptainBrimstoneEntity.CUTLASS_CLEAVE,
                CaptainBrimstoneEntity.BROADSIDE_BARRAGE);
        for (TelegraphedAttack attack : allAttacks) {
            helper.assertTrue(attack.windupTicks() >= TelegraphedAttack.MIN_WINDUP_TICKS,
                    "Boss attack " + attack.id() + " windup must be >= 30 ticks");
        }

        RecipeManager recipes = helper.getLevel().getRecipeManager();
        for (String recipeId : List.of(
                "boss_altar", "colossus_heart", "brimstone_compass",
                "prismatic_sword", "prismatic_helmet",
                "prism_nova_staff", "brimstone_meteor_staff",
                "starlight_gravity_tnt", "brimstone_meteor_tnt")) {
            ResourceLocation id = ResourceLocation.fromNamespaceAndPath(ArtilianMod.MODID, recipeId);
            helper.assertTrue(recipes.byKey(id).isPresent(),
                    "Missing Phase 3 crafting recipe: " + id);
        }

        helper.succeed();
    }

    private static void assertBossAssets(
            GameTestHelper helper,
            String id,
            List<String> requiredBones,
            int minW,
            int minH,
            boolean requireGlowmask) {
        Set<String> bones = readGeoBoneNames("assets/artilian/geo/entity/" + id + ".geo.json");
        for (String bone : requiredBones) {
            helper.assertTrue(bones.contains(bone), id + " geo missing required bone: " + bone);
        }

        Set<String> anims = readAnimationNames("assets/artilian/animations/entity/" + id + ".animation.json");
        for (String clip : List.of("idle", "walk", "attack", "telegraph")) {
            String key = "animation." + id + "." + clip;
            helper.assertTrue(anims.contains(key), id + " missing required animation clip: " + key);
        }

        String texPath = "assets/artilian/textures/entity/" + id + ".png";
        assertImageAtLeast(helper, texPath, minW, minH);
        if (requireGlowmask) {
            assertResourceExists(helper, "assets/artilian/textures/entity/" + id + "_glowmask.png");
        }
    }

    private static void assertImageAtLeast(GameTestHelper helper, String resourcePath, int minW, int minH) {
        try (InputStream stream = Phase3SignOffGameTests.class.getClassLoader().getResourceAsStream(resourcePath)) {
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
        try (InputStream stream = Phase3SignOffGameTests.class.getClassLoader().getResourceAsStream(resourcePath)) {
            helper.assertTrue(stream != null, "Missing Phase 3 art resource: " + resourcePath);
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
        try (InputStream stream = Phase3SignOffGameTests.class.getClassLoader().getResourceAsStream(resourcePath)) {
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
