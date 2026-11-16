package com.silkpanda.artilian.gametest;

import com.silkpanda.artilian.ArtilianMod;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(ArtilianMod.MODID)
@PrefixGameTestTemplate(false)
public class ScaffoldGameTests {

    @GameTest(template = "empty")
    public static void modIsLoaded(GameTestHelper helper) {
        helper.assertTrue(ModList.get().isLoaded(ArtilianMod.MODID), "artilian mod is not loaded");
        helper.succeed();
    }
}
