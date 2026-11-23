package com.silkpanda.artilian.gametest;

import com.silkpanda.artilian.ArtilianMod;
import com.silkpanda.artilian.chester.ChesterEntity;
import com.silkpanda.artilian.gear.ArtilianArmorItem;
import com.silkpanda.artilian.mana.ManaData;
import com.silkpanda.artilian.pet.CrystalFoxEntity;
import com.silkpanda.artilian.registry.ArtilianEntities;
import com.silkpanda.artilian.registry.ArtilianItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import java.util.List;

/** Phase 5 Stage 9: Twin-Star Endgame Rewards uniting Alex's Eye Realm and Steve's Dragon Jungle. */
@GameTestHolder(ArtilianMod.MODID)
@PrefixGameTestTemplate(false)
public class TwinStarEndgameGameTests {

    @GameTest(template = "empty")
    public static void twinStarCoreRecipeUnitesBothRealmsAndAllItemsHaveStatsAndTags(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        CraftingInput coreInput = CraftingInput.of(3, 3, List.of(
                new ItemStack(ArtilianItems.DRAGON_SCALE.get()),
                new ItemStack(ArtilianItems.STARLIGHT_CRYSTAL.get()),
                new ItemStack(ArtilianItems.DRAGON_SCALE.get()),
                new ItemStack(ArtilianItems.ALL_SEERS_LENS.get()),
                new ItemStack(ArtilianItems.SUNSTEEL_INGOT.get()),
                new ItemStack(ArtilianItems.SOVEREIGNS_EMBER_HEART.get()),
                new ItemStack(ArtilianItems.DRAGON_SCALE.get()),
                new ItemStack(ArtilianItems.STARLIGHT_CRYSTAL.get()),
                new ItemStack(ArtilianItems.DRAGON_SCALE.get())));
        var coreRecipe = level.getRecipeManager().getRecipeFor(RecipeType.CRAFTING, coreInput, level);
        helper.assertTrue(coreRecipe.isPresent(), "Twin-Star Core recipe must be registered");
        ItemStack assembledCore = coreRecipe.get().value().assemble(coreInput, level.registryAccess());
        helper.assertTrue(assembledCore.is(ArtilianItems.TWIN_STAR_CORE.get()) && assembledCore.getCount() == 2,
                "Twin-Star Core recipe should yield 2 Twin-Star Cores for co-op play");

        List<Item> all = List.of(
                ArtilianItems.TWIN_STAR_CORE.get(),
                ArtilianItems.TWIN_STAR_BLADE.get(),
                ArtilianItems.TWIN_STAR_STAFF.get(),
                ArtilianItems.TWIN_STAR_HELMET.get(),
                ArtilianItems.TWIN_STAR_CHESTPLATE.get(),
                ArtilianItems.TWIN_STAR_LEGGINGS.get(),
                ArtilianItems.TWIN_STAR_BOOTS.get());

        for (Item item : all) {
            helper.assertTrue(new ItemStack(item).has(DataComponents.FIRE_RESISTANT),
                    item + " must be fire resistant");
        }

        ItemStack blade = new ItemStack(ArtilianItems.TWIN_STAR_BLADE.get());
        helper.assertTrue(blade.getMaxDamage() == 2800,
                "Twin-Star Blade should have 2800 durability, got " + blade.getMaxDamage());
        helper.assertTrue(blade.is(ItemTags.SWORDS),
                "Twin-Star Blade must be in ItemTags.SWORDS");
        final double[] bladeAttack = {0.0};
        blade.forEachModifier(EquipmentSlot.MAINHAND, (attribute, modifier) -> {
            if (attribute.is(Attributes.ATTACK_DAMAGE)) {
                bladeAttack[0] += modifier.amount();
            }
        });
        helper.assertTrue(bladeAttack[0] > 9.0,
                "Twin-Star Blade should grant > 9.0 bonus attack damage, got " + bladeAttack[0]);

        helper.assertTrue(new ItemStack(ArtilianItems.TWIN_STAR_HELMET.get()).is(ItemTags.HEAD_ARMOR),
                "Twin-Star Helmet should be in HEAD_ARMOR");
        helper.assertTrue(new ItemStack(ArtilianItems.TWIN_STAR_CHESTPLATE.get()).is(ItemTags.CHEST_ARMOR),
                "Twin-Star Chestplate should be in CHEST_ARMOR");
        helper.assertTrue(new ItemStack(ArtilianItems.TWIN_STAR_LEGGINGS.get()).is(ItemTags.LEG_ARMOR),
                "Twin-Star Leggings should be in LEG_ARMOR");
        helper.assertTrue(new ItemStack(ArtilianItems.TWIN_STAR_BOOTS.get()).is(ItemTags.FOOT_ARMOR),
                "Twin-Star Boots should be in FOOT_ARMOR");

        int totalDefense = ((ArmorItem) ArtilianItems.TWIN_STAR_HELMET.get()).getDefense()
                + ((ArmorItem) ArtilianItems.TWIN_STAR_CHESTPLATE.get()).getDefense()
                + ((ArmorItem) ArtilianItems.TWIN_STAR_LEGGINGS.get()).getDefense()
                + ((ArmorItem) ArtilianItems.TWIN_STAR_BOOTS.get()).getDefense();
        helper.assertTrue(totalDefense == 30,
                "Twin-Star Armor total defense should be 30, got " + totalDefense);

        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void twinStarBladeIgnitesWeakensAndRevealsTargets(GameTestHelper helper) {
        ServerPlayer player = ChesterTestSupport.playerInTestArea(helper, "Alex");
        Zombie zombie = helper.spawnWithNoFreeWill(EntityType.ZOMBIE, new BlockPos(2, 1, 2));
        ItemStack blade = new ItemStack(ArtilianItems.TWIN_STAR_BLADE.get());

        blade.getItem().hurtEnemy(blade, zombie, player);

        helper.assertTrue(zombie.getRemainingFireTicks() > 0,
                "Twin-Star Blade should ignite hit targets");
        helper.assertTrue(zombie.hasEffect(MobEffects.WEAKNESS),
                "Twin-Star Blade should apply Weakness to hit targets");
        helper.assertTrue(zombie.hasEffect(MobEffects.GLOWING),
                "Twin-Star Blade should apply Glowing to hit targets");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void twinStarFullArmorSetGrantsAllFourEndgameBuffs(GameTestHelper helper) {
        ServerPlayer player = ChesterTestSupport.playerInTestArea(helper, "Steve");
        player.setItemSlot(EquipmentSlot.HEAD, new ItemStack(ArtilianItems.TWIN_STAR_HELMET.get()));
        player.setItemSlot(EquipmentSlot.CHEST, new ItemStack(ArtilianItems.TWIN_STAR_CHESTPLATE.get()));
        player.setItemSlot(EquipmentSlot.LEGS, new ItemStack(ArtilianItems.TWIN_STAR_LEGGINGS.get()));
        player.setItemSlot(EquipmentSlot.FEET, new ItemStack(ArtilianItems.TWIN_STAR_BOOTS.get()));

        ArtilianArmorItem.applySetBonus(player);

        helper.assertTrue(player.hasEffect(MobEffects.FIRE_RESISTANCE),
                "Full Twin-Star set should grant Fire Resistance");
        helper.assertTrue(player.hasEffect(MobEffects.DAMAGE_BOOST),
                "Full Twin-Star set should grant Strength");
        helper.assertTrue(player.hasEffect(MobEffects.NIGHT_VISION),
                "Full Twin-Star set should grant Night Vision");
        helper.assertTrue(player.hasEffect(MobEffects.REGENERATION),
                "Full Twin-Star set should grant Regeneration");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void twinStarHarmonyStaffHealsAlliesAndBlastsHostilesSafely(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        ServerPlayer player = ChesterTestSupport.playerInTestArea(helper, "Alex");
        ManaData mana = ManaData.get(player);
        mana.setMana(100);

        ChesterEntity chester = ArtilianEntities.CHESTER.get().create(level);
        helper.assertTrue(chester != null, "Chester should be created");
        chester.setOwner(player);
        Vec3 playerPos = player.position();
        chester.moveTo(playerPos.x + 1.5, playerPos.y, playerPos.z, 0.0F, 0.0F);
        level.addFreshEntity(chester);

        CrystalFoxEntity allyPet = helper.spawnWithNoFreeWill(ArtilianEntities.CRYSTAL_FOX.get(), new BlockPos(2, 1, 2));
        allyPet.setOwner(player);
        allyPet.setHealth(allyPet.getMaxHealth() - 4.0F);
        float hpBefore = allyPet.getHealth();

        Zombie zombie = helper.spawnWithNoFreeWill(EntityType.ZOMBIE, new BlockPos(3, 1, 3));
        float zombieHpBefore = zombie.getHealth();

        ItemStack staff = new ItemStack(ArtilianItems.TWIN_STAR_STAFF.get());
        player.setItemInHand(InteractionHand.MAIN_HAND, staff);
        staff.use(level, player, InteractionHand.MAIN_HAND);

        helper.assertTrue(mana.getMana() == 50,
                "Twin-Star Harmony Staff should spend 50 Mana, remaining: " + mana.getMana());
        helper.assertTrue(allyPet.getHealth() > hpBefore,
                "Twin-Star Harmony Staff should heal nearby allied pets");
        helper.assertTrue(allyPet.hasEffect(MobEffects.REGENERATION) && allyPet.hasEffect(MobEffects.MOVEMENT_SPEED),
                "Twin-Star Harmony Staff should grant Regeneration and Speed to allies");
        helper.assertTrue(chester.isAlive() && chester.getHealth() >= chester.getMaxHealth(),
                "Twin-Star Harmony Staff must never harm Chester");
        helper.assertTrue(zombie.getHealth() < zombieHpBefore,
                "Twin-Star Harmony Staff should damage nearby hostiles");
        helper.assertTrue(zombie.hasEffect(MobEffects.GLOWING),
                "Twin-Star Harmony Staff should reveal nearby hostiles with Glowing");
        helper.succeed();
    }
}
