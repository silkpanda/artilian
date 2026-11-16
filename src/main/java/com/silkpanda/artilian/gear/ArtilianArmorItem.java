package com.silkpanda.artilian.gear;

import com.silkpanda.artilian.client.ArtilianArmorRenderer;
import com.silkpanda.artilian.client.StarlightArmorRenderer;
import com.silkpanda.artilian.client.SunsteelArmorRenderer;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.core.Holder;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.animatable.client.GeoRenderProvider;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.renderer.GeoArmorRenderer;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.function.Consumer;

/**
 * GeckoLib-rendered armor item for Artilian armor sets (Sunsteel, Starlight).
 * Uses a dedicated {@link GeoArmorRenderer} per set via {@link GeoRenderProvider}.
 */
public final class ArtilianArmorItem extends ArmorItem implements GeoItem {
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    private final String set;

    public ArtilianArmorItem(Holder<ArmorMaterial> material, Type type, Properties properties, String set) {
        super(material, type, properties);
        this.set = set;
    }

    @Override
    public void createGeoRenderer(Consumer<GeoRenderProvider> consumer) {
        consumer.accept(new GeoRenderProvider() {
            private GeoArmorRenderer<?> renderer;

            @Override
            public <T extends LivingEntity> HumanoidModel<?> getGeoArmorRenderer(
                    @Nullable T livingEntity,
                    ItemStack itemStack,
                    @Nullable EquipmentSlot equipmentSlot,
                    @Nullable HumanoidModel<T> original) {
                if (this.renderer == null) {
                    this.renderer = switch (ArtilianArmorItem.this.set) {
                        case "starlight" -> new StarlightArmorRenderer();
                        case "sunsteel" -> new SunsteelArmorRenderer();
                        default -> new ArtilianArmorRenderer(ArtilianArmorItem.this.set);
                    };
                }

                return this.renderer;
            }
        });
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        // Static 3D armor geometry until Blockbench animation exports are provided.
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }
}
