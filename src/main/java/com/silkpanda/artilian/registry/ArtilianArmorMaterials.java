package com.silkpanda.artilian.registry;

import com.silkpanda.artilian.ArtilianMod;
import net.minecraft.Util;
import net.minecraft.core.registries.Registries;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.EnumMap;
import java.util.List;
import java.util.function.Supplier;

public class ArtilianArmorMaterials {
    public static final DeferredRegister<ArmorMaterial> ARMOR_MATERIALS =
            DeferredRegister.create(Registries.ARMOR_MATERIAL, ArtilianMod.MODID);

    public static final DeferredHolder<ArmorMaterial, ArmorMaterial> SUNSTEEL = register(
            "sunsteel",
            2, 5, 7, 3, 6,
            16,
            SoundEvents.ARMOR_EQUIP_GOLD,
            () -> ArtilianItems.SUNSTEEL_INGOT.get(),
            1.0F,
            0.0F);

    public static final DeferredHolder<ArmorMaterial, ArmorMaterial> STARLIGHT = register(
            "starlight",
            3, 6, 8, 3, 9,
            20,
            SoundEvents.ARMOR_EQUIP_DIAMOND,
            () -> ArtilianItems.STARLIGHT_CRYSTAL.get(),
            2.0F,
            0.05F);

    public static final DeferredHolder<ArmorMaterial, ArmorMaterial> PRISMATIC = register(
            "prismatic",
            4, 7, 9, 4, 11,
            24,
            SoundEvents.ARMOR_EQUIP_NETHERITE,
            () -> ArtilianItems.PRISM_CORE.get(),
            3.0F,
            0.15F);

    public static final DeferredHolder<ArmorMaterial, ArmorMaterial> DRAGONSCALE = register(
            "dragonscale",
            4, 8, 10, 4, 12,
            26,
            SoundEvents.ARMOR_EQUIP_NETHERITE,
            () -> ArtilianItems.DRAGON_SCALE.get(),
            3.5F,
            0.20F);

    public static final DeferredHolder<ArmorMaterial, ArmorMaterial> TWIN_STAR = register(
            "twin_star",
            5, 9, 11, 5, 14,
            30,
            SoundEvents.ARMOR_EQUIP_NETHERITE,
            () -> ArtilianItems.TWIN_STAR_CORE.get(),
            4.0F,
            0.25F);

    private static DeferredHolder<ArmorMaterial, ArmorMaterial> register(
            String name,
            int boots,
            int leggings,
            int chestplate,
            int helmet,
            int body,
            int enchantability,
            net.minecraft.core.Holder<SoundEvent> equipSound,
            Supplier<ItemLike> repairItem,
            float toughness,
            float knockbackResistance) {
        EnumMap<ArmorItem.Type, Integer> defense = Util.make(new EnumMap<>(ArmorItem.Type.class), map -> {
            map.put(ArmorItem.Type.BOOTS, boots);
            map.put(ArmorItem.Type.LEGGINGS, leggings);
            map.put(ArmorItem.Type.CHESTPLATE, chestplate);
            map.put(ArmorItem.Type.HELMET, helmet);
            map.put(ArmorItem.Type.BODY, body);
        });
        return ARMOR_MATERIALS.register(name, () -> new ArmorMaterial(
                defense,
                enchantability,
                equipSound,
                () -> Ingredient.of(repairItem.get()),
                List.of(new ArmorMaterial.Layer(ArtilianMod.id(name))),
                toughness,
                knockbackResistance));
    }

    public static void register(IEventBus bus) {
        ARMOR_MATERIALS.register(bus);
    }
}
