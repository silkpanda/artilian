package com.silkpanda.artilian.gear;

import com.silkpanda.artilian.client.ArtilianArmorRenderer;
import com.silkpanda.artilian.client.DragonscaleArmorRenderer;
import com.silkpanda.artilian.client.PrismaticArmorRenderer;
import com.silkpanda.artilian.client.StarlightArmorRenderer;
import com.silkpanda.artilian.client.SunsteelArmorRenderer;
import net.minecraft.ChatFormatting;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
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
                        case "dragonscale" -> new DragonscaleArmorRenderer();
                        case "prismatic" -> new PrismaticArmorRenderer();
                        case "starlight" -> new StarlightArmorRenderer();
                        case "sunsteel" -> new SunsteelArmorRenderer();
                        default -> new ArtilianArmorRenderer(ArtilianArmorItem.this.set);
                    };
                }

                return this.renderer;
            }
        });
    }

    public String getSetName() {
        return this.set;
    }

    /** True when {@code player} is wearing all 4 pieces of the named Artilian armor set. */
    public static boolean hasFullSet(Player player, String set) {
        for (ItemStack armorStack : player.getInventory().armor) {
            if (!(armorStack.getItem() instanceof ArtilianArmorItem artilianArmor)
                    || !set.equals(artilianArmor.set)) {
                return false;
            }
        }
        return true;
    }

    /** Applies passive full-set bonuses (Prismatic: Fire Resistance; Dragonscale: Fire Resistance + Strength). */
    public static void applySetBonus(Player player) {
        if (!player.level().isClientSide && player.isAlive()) {
            if (hasFullSet(player, "dragonscale")) {
                player.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 60, 0, true, false, true));
                player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 60, 0, true, false, true));
            } else if (hasFullSet(player, "prismatic")) {
                player.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 60, 0, true, false, true));
            }
        }
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, java.util.List<Component> tooltip, TooltipFlag flag) {
        if ("dragonscale".equals(this.set)) {
            tooltip.add(Component.translatable("item.artilian.dragonscale_armor.tooltip")
                    .withStyle(ChatFormatting.GREEN));
        } else if ("prismatic".equals(this.set)) {
            tooltip.add(Component.translatable("item.artilian.prismatic_armor.tooltip")
                    .withStyle(ChatFormatting.LIGHT_PURPLE));
        }
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
