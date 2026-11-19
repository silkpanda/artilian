package com.silkpanda.artilian.datagen;

import com.silkpanda.artilian.ArtilianMod;
import com.silkpanda.artilian.registry.ArtilianItems;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.client.model.generators.ItemModelProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

public class ArtilianItemModelProvider extends ItemModelProvider {
    public ArtilianItemModelProvider(PackOutput output, ExistingFileHelper existingFileHelper) {
        super(output, ArtilianMod.MODID, existingFileHelper);
    }

    @Override
    protected void registerModels() {
        this.basicItem(ArtilianItems.CHESTER_WHISTLE.get());
        this.withExistingParent("chester_spawn_egg", mcLoc("item/template_spawn_egg"));
        this.basicItem(ArtilianItems.BIOME_TREAT.get());
        this.basicItem(ArtilianItems.GOLEM_CORE.get());
        this.basicItem(ArtilianItems.COLOSSUS_HEART.get());
        this.basicItem(ArtilianItems.PRISM_CORE.get());
        this.withExistingParent("crystal_colossus_spawn_egg", mcLoc("item/template_spawn_egg"));
        this.basicItem(ArtilianItems.BRIMSTONE_COMPASS.get());
        this.basicItem(ArtilianItems.BRIMSTONE_DOUBLOON.get());
        this.withExistingParent("captain_brimstone_spawn_egg", mcLoc("item/template_spawn_egg"));
        this.withExistingParent("crystal_fox_spawn_egg", mcLoc("item/template_spawn_egg"));
        this.withExistingParent("shadow_wisp_spawn_egg", mcLoc("item/template_spawn_egg"));
        this.withExistingParent("cinder_crag_spawn_egg", mcLoc("item/template_spawn_egg"));
        this.withExistingParent("zaphox_spawn_egg", mcLoc("item/template_spawn_egg"));
        this.withExistingParent("galecorn_spawn_egg", mcLoc("item/template_spawn_egg"));
        this.withExistingParent("ember_drake_spawn_egg", mcLoc("item/template_spawn_egg"));
        this.withExistingParent("peepkin_spawn_egg", mcLoc("item/template_spawn_egg"));
        this.withExistingParent("prismling_spawn_egg", mcLoc("item/template_spawn_egg"));
        this.withExistingParent("flurrywing_spawn_egg", mcLoc("item/template_spawn_egg"));
        this.withExistingParent("sunbeam_spawn_egg", mcLoc("item/template_spawn_egg"));
        this.withExistingParent("frogduck_spawn_egg", mcLoc("item/template_spawn_egg"));
        this.withExistingParent("zapkin_spawn_egg", mcLoc("item/template_spawn_egg"));
        this.withExistingParent("prism_sentinel_spawn_egg", mcLoc("item/template_spawn_egg"));
        this.withExistingParent("cinder_raider_spawn_egg", mcLoc("item/template_spawn_egg"));
        this.withExistingParent("astral_stalker_spawn_egg", mcLoc("item/template_spawn_egg"));
        captureCrystalModels();
        this.basicItem(ArtilianItems.RAW_SUNSTEEL.get());
        this.basicItem(ArtilianItems.SUNSTEEL_INGOT.get());
        this.basicItem(ArtilianItems.STARLIGHT_CRYSTAL.get());

        this.handheldItem(ArtilianItems.SUNSTEEL_SWORD.get());
        this.handheldItem(ArtilianItems.SUNSTEEL_PICKAXE.get());
        this.handheldItem(ArtilianItems.SUNSTEEL_AXE.get());
        this.handheldItem(ArtilianItems.SUNSTEEL_SHOVEL.get());
        this.handheldItem(ArtilianItems.SUNSTEEL_HOE.get());

        this.handheldItem(ArtilianItems.STARLIGHT_SWORD.get());
        this.handheldItem(ArtilianItems.STARLIGHT_PICKAXE.get());
        this.handheldItem(ArtilianItems.STARLIGHT_AXE.get());
        this.handheldItem(ArtilianItems.STARLIGHT_SHOVEL.get());
        this.handheldItem(ArtilianItems.STARLIGHT_HOE.get());

        this.handheldItem(ArtilianItems.PRISMATIC_SWORD.get());
        this.handheldItem(ArtilianItems.PRISMATIC_PICKAXE.get());
        this.handheldItem(ArtilianItems.PRISMATIC_AXE.get());
        this.handheldItem(ArtilianItems.PRISMATIC_SHOVEL.get());
        this.handheldItem(ArtilianItems.PRISMATIC_HOE.get());

        this.basicItem(ArtilianItems.SUNSTEEL_HELMET.get());
        this.basicItem(ArtilianItems.SUNSTEEL_CHESTPLATE.get());
        this.basicItem(ArtilianItems.SUNSTEEL_LEGGINGS.get());
        this.basicItem(ArtilianItems.SUNSTEEL_BOOTS.get());

        this.basicItem(ArtilianItems.STARLIGHT_HELMET.get());
        this.basicItem(ArtilianItems.STARLIGHT_CHESTPLATE.get());
        this.basicItem(ArtilianItems.STARLIGHT_LEGGINGS.get());
        this.basicItem(ArtilianItems.STARLIGHT_BOOTS.get());

        this.basicItem(ArtilianItems.PRISMATIC_HELMET.get());
        this.basicItem(ArtilianItems.PRISMATIC_CHESTPLATE.get());
        this.basicItem(ArtilianItems.PRISMATIC_LEGGINGS.get());
        this.basicItem(ArtilianItems.PRISMATIC_BOOTS.get());

        this.handheldItem(ArtilianItems.SPARK_WAND.get());
        this.handheldItem(ArtilianItems.HEART_BLOOM_STAFF.get());
        this.handheldItem(ArtilianItems.ZEPHYR_DASH_WAND.get());

        this.basicItem(ArtilianItems.CRYSTAL_CANOPY_DOOR_ITEM.get());
        this.basicItem(ArtilianItems.EMBERWOOD_DOOR_ITEM.get());

        flatBlockItem("crystal_canopy_sapling");
        flatBlockItem("crystal_bloom");
        flatBlockItem("emberwood_sapling");
        flatBlockItem("ember_bloom");
        flatBlockItem("starlight_blossom");
    }

    private void flatBlockItem(String name) {
        this.singleTexture(name, mcLoc("item/generated"), "layer0", modLoc("block/" + name));
    }

    private void captureCrystalModels() {
        var fainted = this.singleTexture("capture_crystal_fainted", mcLoc("item/generated"),
                "layer0", modLoc("item/capture_crystal_fainted"));
        var filled = this.singleTexture("capture_crystal_filled", mcLoc("item/generated"),
                "layer0", modLoc("item/capture_crystal_filled"));
        var summoned = this.singleTexture("capture_crystal_summoned", mcLoc("item/generated"),
                "layer0", modLoc("item/capture_crystal_summoned"));

        this.withExistingParent("capture_crystal", mcLoc("item/generated"))
                .texture("layer0", modLoc("item/capture_crystal"))
                .override().predicate(modLoc("crystal_state"), 0.25F).model(fainted).end()
                .override().predicate(modLoc("crystal_state"), 0.5F).model(filled).end()
                .override().predicate(modLoc("crystal_state"), 1.0F).model(summoned).end();
    }
}
