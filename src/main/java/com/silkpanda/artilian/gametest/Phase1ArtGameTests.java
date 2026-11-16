package com.silkpanda.artilian.gametest;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.silkpanda.artilian.ArtilianMod;
import com.silkpanda.artilian.tnt.TntVariant;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** Phase 1 Stage 6: Resource and GeckoLib model/animation/texture integrity checks. */
@GameTestHolder(ArtilianMod.MODID)
@PrefixGameTestTemplate(false)
public class Phase1ArtGameTests {

    @GameTest(template = "empty")
    public static void chesterGeckoLibAssetsHaveRequiredBonesAndAnimations(GameTestHelper helper) {
        Set<String> bones = readGeoBoneNames("assets/artilian/geo/entity/chester.geo.json");
        for (String requiredBone : List.of("root", "feet", "foot_left", "foot_right", "body", "lid", "eyes", "eye_left", "eye_right", "awake_extras", "teeth_upper")) {
            helper.assertTrue(bones.contains(requiredBone),
                    "Chester geo model missing required bone: " + requiredBone);
        }

        Set<String> animations = readAnimationNames("assets/artilian/animations/entity/chester.animation.json");
        for (String requiredAnim : List.of("animation.chester.idle", "animation.chester.walk", "animation.chester.sleep", "animation.chester.chomp")) {
            helper.assertTrue(animations.contains(requiredAnim),
                    "Chester animation file missing required clip: " + requiredAnim);
        }

        assertResourceExists(helper, "assets/artilian/textures/entity/chester.png");
        assertResourceExists(helper, "assets/artilian/textures/item/chester_whistle.png");
        helper.succeed();
    }

    /**
     * Asleep, Chester is drawn from a second model mapped onto the game's own chest texture. It
     * must stay a plain chest: body, lid, and nothing that only an awake Chester has.
     */
    @GameTest(template = "empty")
    public static void sleepingChesterModelIsAPlainChest(GameTestHelper helper) {
        Set<String> bones = readGeoBoneNames("assets/artilian/geo/entity/chester_sleep.geo.json");
        helper.assertTrue(bones.equals(Set.of("root", "body", "lid")),
                "Sleeping Chester model must have exactly the bones root, body, lid; found " + bones);
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void allPhase1TexturesAndArmorGeosExist(GameTestHelper helper) {
        helper.assertTrue(!readGeoBoneNames("assets/artilian/geo/item/armor/sunsteel_armor.geo.json").isEmpty(),
                "Sunsteel armor geo must define bones");
        helper.assertTrue(!readGeoBoneNames("assets/artilian/geo/item/armor/starlight_armor.geo.json").isEmpty(),
                "Starlight armor geo must define bones");
        assertResourceExists(helper, "assets/artilian/textures/item/armor/sunsteel_armor.png");
        assertResourceExists(helper, "assets/artilian/textures/item/armor/starlight_armor.png");

        for (TntVariant variant : TntVariant.values()) {
            String base = "assets/artilian/textures/block/" + variant.getSerializedName();
            assertResourceExists(helper, base + "_side.png");
            assertResourceExists(helper, base + "_top.png");
            assertResourceExists(helper, base + "_bottom.png");
        }

        for (String wandTex : List.of("spark_wand.png", "heart_bloom_staff.png", "zephyr_dash_wand.png")) {
            assertResourceExists(helper, "assets/artilian/textures/item/" + wandTex);
        }

        for (String gearTex : List.of(
                "block/sunsteel_block.png",
                "block/starlight_crystal_block.png",
                "item/sunsteel_ingot.png",
                "item/starlight_crystal.png",
                "item/starlight_helmet.png",
                "item/sunsteel_sword.png",
                "item/starlight_sword.png")) {
            assertResourceExists(helper, "assets/artilian/textures/" + gearTex);
        }

        helper.succeed();
    }

    private static void assertResourceExists(GameTestHelper helper, String resourcePath) {
        try (InputStream stream = Phase1ArtGameTests.class.getClassLoader().getResourceAsStream(resourcePath)) {
            helper.assertTrue(stream != null, "Missing Phase 1 art resource: " + resourcePath);
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
        try (InputStream stream = Phase1ArtGameTests.class.getClassLoader().getResourceAsStream(resourcePath)) {
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
