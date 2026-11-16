package com.silkpanda.artilian.gametest;

import com.silkpanda.artilian.ArtilianMod;
import com.silkpanda.artilian.mana.ManaService;
import com.silkpanda.artilian.pet.CapturedPetData;
import com.silkpanda.artilian.pet.CaptureCrystalItem;
import com.silkpanda.artilian.pet.CrystalFoxEntity;
import com.silkpanda.artilian.pet.PetElement;
import com.silkpanda.artilian.registry.ArtilianDataComponents;
import com.silkpanda.artilian.registry.ArtilianEntities;
import com.silkpanda.artilian.registry.ArtilianItems;
import com.silkpanda.artilian.worldgen.ArtilianBiomes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.biome.Biome;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/** Phase 2 Stage 3: Expanded element system, Crystal Fox, Biome Treats, and Capture Crystals. */
@GameTestHolder(ArtilianMod.MODID)
@PrefixGameTestTemplate(false)
public class PetCaptureGameTests {

    @GameTest(template = "empty")
    public static void petElementMatchupsAndDualElementMultipliersWork(GameTestHelper helper) {
        helper.assertTrue(PetElement.values().length == 11,
                "Expected 11 PetElements in expanded element system, got " + PetElement.values().length);
        for (PetElement element : PetElement.values()) {
            helper.assertTrue(element.particle() != null, "Missing particle for element " + element);
            helper.assertTrue(element.colorRgb() != 0, "Missing color for element " + element);
        }
        helper.assertTrue(Math.abs(PetElement.CRYSTAL.multiplierAgainst(PetElement.ELECTRIC) - 1.5F) < 0.001F,
                "Crystal should be 1.5x strong vs Electric");
        helper.assertTrue(Math.abs(PetElement.CRYSTAL.multiplierAgainst(PetElement.EARTH) - 0.75F) < 0.001F,
                "Crystal should be 0.75x resisted vs Earth");
        helper.assertTrue(Math.abs(PetElement.ICE.multiplierAgainst(PetElement.DRAGON) - 1.5F) < 0.001F,
                "Ice should be 1.5x strong vs Dragon");
        helper.assertTrue(Math.abs(
                PetElement.FIRE.multiplierAgainst(PetElement.CRYSTAL, Optional.of(PetElement.NATURE)) - 2.25F) < 0.001F,
                "Fire vs dual Crystal/Nature should multiply cleanly to 2.25x");
        helper.succeed();
    }

    @GameTest(template = "tnt_arena")
    public static void biomeTreatBefriendsWildCrystalFoxAndCaptureCrystalCapturesIt(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        ServerPlayer alex = ChesterTestSupport.playerInTestArea(helper, "AlexPet");
        CrystalFoxEntity wildFox = spawnFox(helper, new BlockPos(7, 1, 7));

        ItemStack treats = new ItemStack(ArtilianItems.BIOME_TREAT.get(), 3);
        alex.setItemInHand(InteractionHand.MAIN_HAND, treats);
        alex.interactOn(wildFox, InteractionHand.MAIN_HAND);
        helper.assertTrue(wildFox.getAffinity() == 50 && !wildFox.isBefriended(),
                "First Biome Treat should raise affinity to 50, got " + wildFox.getAffinity());

        alex.interactOn(wildFox, InteractionHand.MAIN_HAND);
        helper.assertTrue(wildFox.isBefriended() && treats.getCount() == 1,
                "Second Biome Treat should befriend wild Crystal Fox and leave 1 treat");

        ItemStack crystal = new ItemStack(ArtilianItems.CAPTURE_CRYSTAL.get());
        alex.setItemInHand(InteractionHand.MAIN_HAND, crystal);
        alex.interactOn(wildFox, InteractionHand.MAIN_HAND);

        helper.assertFalse(wildFox.isAlive(), "Captured wild Crystal Fox should leave the world into the crystal");
        CapturedPetData data = crystal.get(ArtilianDataComponents.CAPTURED_PET.get());
        helper.assertTrue(data != null, "Capture Crystal must store CapturedPetData after capture");
        helper.assertTrue(data.primaryElement() == PetElement.CRYSTAL
                        && data.secondaryElement().equals(Optional.of(PetElement.NATURE)),
                "Crystal Fox should store Crystal / Nature elements");
        helper.assertTrue(data.isOwnedBy(alex.getUUID()) && !data.fainted() && !data.summoned(),
                "Captured crystal should belong to Alex and be ready to summon");
        helper.assertTrue(Math.abs(CaptureCrystalItem.getVisualState(crystal) - 0.5F) < 0.01F,
                "Filled ready crystal visual state should be 0.5");

        helper.succeed();
    }

    @GameTest(template = "tnt_arena")
    public static void captureCrystalSummonsAndRecallsCrystalFoxPreservingIdentityAndHealth(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        ServerPlayer alex = ChesterTestSupport.playerInTestArea(helper, "AlexSummon");
        CrystalFoxEntity wildFox = spawnFox(helper, new BlockPos(7, 1, 7));
        wildFox.addAffinity(100);

        ItemStack crystal = new ItemStack(ArtilianItems.CAPTURE_CRYSTAL.get());
        alex.setItemInHand(InteractionHand.MAIN_HAND, crystal);
        alex.interactOn(wildFox, InteractionHand.MAIN_HAND);

        UUID originalPetId = crystal.get(ArtilianDataComponents.CAPTURED_PET.get()).petId();

        // Summon Crystal Fox
        crystal.use(level, alex, InteractionHand.MAIN_HAND);
        List<? extends CrystalFoxEntity> foxes = foxesOf(helper, alex);
        helper.assertTrue(foxes.size() == 1, "Expected 1 summoned Crystal Fox, found " + foxes.size());
        CrystalFoxEntity summoned = foxes.get(0);
        helper.assertTrue(summoned.getPetId().equals(Optional.of(originalPetId)),
                "Summoned fox must keep original petId");
        helper.assertTrue(Math.abs(CaptureCrystalItem.getVisualState(crystal) - 1.0F) < 0.01F,
                "Summoned crystal visual state should be 1.0");

        // Injure and rename, then recall
        summoned.setHealth(12.0F);
        summoned.setCustomName(Component.literal("Sparkle"));
        crystal.use(level, alex, InteractionHand.MAIN_HAND);

        helper.assertFalse(summoned.isAlive(), "Recalling should remove Crystal Fox entity from the world");
        CapturedPetData recalledData = crystal.get(ArtilianDataComponents.CAPTURED_PET.get());
        helper.assertTrue(!recalledData.summoned()
                        && Math.abs(recalledData.health() - 12.0F) < 0.01F
                        && recalledData.customName().equals(Optional.of("Sparkle")),
                "Recalled crystal must preserve 12.0 HP and custom name 'Sparkle'");

        // Re-summon and verify state persisted
        crystal.use(level, alex, InteractionHand.MAIN_HAND);
        List<? extends CrystalFoxEntity> resummoned = foxesOf(helper, alex);
        helper.assertTrue(resummoned.size() == 1, "Expected 1 re-summoned Crystal Fox");
        CrystalFoxEntity fox2 = resummoned.get(0);
        helper.assertTrue(fox2.getPetId().equals(Optional.of(originalPetId))
                        && Math.abs(fox2.getHealth() - 12.0F) < 0.01F
                        && "Sparkle".equals(fox2.getCustomName() != null ? fox2.getCustomName().getString() : null),
                "Re-summoned Crystal Fox must restore petId, 12.0 HP, and custom name 'Sparkle'");

        fox2.discard();
        helper.succeed();
    }

    @GameTest(template = "tnt_arena")
    public static void biomeTreatHealsOwnedPetAndWakesFaintedCrystal(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        ServerPlayer steve = ChesterTestSupport.playerInTestArea(helper, "SteveTreat");
        CrystalFoxEntity wildFox = spawnFox(helper, new BlockPos(7, 1, 7));
        wildFox.addAffinity(100);

        ItemStack crystal = new ItemStack(ArtilianItems.CAPTURE_CRYSTAL.get());
        steve.setItemInHand(InteractionHand.MAIN_HAND, crystal);
        steve.interactOn(wildFox, InteractionHand.MAIN_HAND);
        crystal.use(level, steve, InteractionHand.MAIN_HAND);

        CrystalFoxEntity fox = foxesOf(helper, steve).get(0);
        fox.setHealth(12.0F);
        // Keep the bound crystal in Steve's inventory (slot 1) while feeding from main hand.
        steve.getInventory().setItem(1, crystal);
        ItemStack treat = new ItemStack(ArtilianItems.BIOME_TREAT.get(), 2);
        steve.setItemInHand(InteractionHand.MAIN_HAND, treat);
        steve.interactOn(fox, InteractionHand.MAIN_HAND);
        helper.assertTrue(Math.abs(fox.getHealth() - 18.0F) < 0.01F && treat.getCount() == 1,
                "Biome Treat should heal tamed pet by 6 HP (12 -> 18), got " + fox.getHealth());

        // Faint the pet, then wake it early using Biome Treat in offhand
        fox.hurt(level.damageSources().generic(), 100.0F);
        helper.assertFalse(fox.isAlive(), "Lethal damage should faint and recall the pet entity");
        ItemStack faintedCrystal = steve.getInventory().getItem(1);
        helper.assertTrue(faintedCrystal.get(ArtilianDataComponents.CAPTURED_PET.get()).fainted(),
                "Crystal should be marked fainted after lethal hit");

        steve.setItemInHand(InteractionHand.MAIN_HAND, faintedCrystal);
        steve.setItemInHand(InteractionHand.OFF_HAND, treat);
        faintedCrystal.use(level, steve, InteractionHand.MAIN_HAND);

        List<? extends CrystalFoxEntity> woken = foxesOf(helper, steve);
        helper.assertTrue(woken.size() == 1 && treat.isEmpty(),
                "Biome Treat in offhand should wake fainted Crystal Fox immediately and consume 1 treat");
        helper.assertTrue(Math.abs(woken.get(0).getHealth() - woken.get(0).getMaxHealth()) < 0.01F,
                "Woken pet should summon at full health");

        woken.get(0).discard();
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void biomeTreatAndCaptureCrystalRecipesWork(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();

        CraftingInput crystalInput = CraftingInput.of(3, 3, List.of(
                ItemStack.EMPTY, new ItemStack(Items.GLASS), ItemStack.EMPTY,
                new ItemStack(ArtilianItems.SUNSTEEL_INGOT.get()),
                new ItemStack(ArtilianItems.STARLIGHT_CRYSTAL.get()),
                new ItemStack(ArtilianItems.SUNSTEEL_INGOT.get()),
                ItemStack.EMPTY, new ItemStack(Items.GLASS), ItemStack.EMPTY
        ));
        Optional<RecipeHolder<CraftingRecipe>> crystalRecipe =
                level.getRecipeManager().getRecipeFor(RecipeType.CRAFTING, crystalInput, level);
        helper.assertTrue(crystalRecipe.isPresent(), "Expected Capture Crystal crafting recipe");
        ItemStack crystalOut = crystalRecipe.get().value().assemble(crystalInput, level.registryAccess());
        helper.assertTrue(crystalOut.is(ArtilianItems.CAPTURE_CRYSTAL.get()) && crystalOut.getCount() == 2,
                "Capture Crystal recipe should craft 2x Capture Crystal, got " + crystalOut);

        for (ItemStack bloom : List.of(
                new ItemStack(ArtilianItems.CRYSTAL_BLOOM_ITEM.get()),
                new ItemStack(ArtilianItems.EMBER_BLOOM_ITEM.get()),
                new ItemStack(ArtilianItems.STARLIGHT_BLOSSOM_ITEM.get()))) {
            CraftingInput treatInput = CraftingInput.of(3, 1, List.of(
                    bloom, new ItemStack(Items.SWEET_BERRIES), new ItemStack(Items.SUGAR)));
            Optional<RecipeHolder<CraftingRecipe>> treatRecipe =
                    level.getRecipeManager().getRecipeFor(RecipeType.CRAFTING, treatInput, level);
            helper.assertTrue(treatRecipe.isPresent(), "Expected Biome Treat recipe for " + bloom);
            ItemStack treatOut = treatRecipe.get().value().assemble(treatInput, level.registryAccess());
            helper.assertTrue(treatOut.is(ArtilianItems.BIOME_TREAT.get()) && treatOut.getCount() == 4,
                    "Biome Treat recipe should craft 4x Biome Treat, got " + treatOut);
        }
        helper.succeed();
    }

    @GameTest(template = "tnt_arena")
    public static void crystalFoxSpawnsInCrystalCanopyWoodsAndIsSparedByWands(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        Registry<Biome> biomes = level.registryAccess().registryOrThrow(Registries.BIOME);
        Holder.Reference<Biome> crystalWoods = biomes.getHolder(ArtilianBiomes.CRYSTAL_CANOPY_WOODS)
                .orElseThrow(() -> new AssertionError("Missing Crystal Canopy Woods"));
        boolean hasCrystalFoxSpawn = crystalWoods.value().getMobSettings().getMobs(MobCategory.CREATURE).unwrap()
                .stream().anyMatch(data -> data.type == ArtilianEntities.CRYSTAL_FOX.get());
        helper.assertTrue(hasCrystalFoxSpawn,
                "Crystal Canopy Woods must include Crystal Fox in creature spawns");

        ServerPlayer alex = ChesterTestSupport.playerInTestArea(helper, "AlexSafety");
        CrystalFoxEntity tamed = spawnFox(helper, new BlockPos(7, 1, 7));
        tamed.setOwner(alex);
        helper.assertFalse(ManaService.isValidOffensiveTarget(alex, tamed),
                "Owned Crystal Fox must be spared by offensive wands");
        helper.assertTrue(ManaService.isValidHealingTarget(tamed),
                "Owned Crystal Fox must be healed by Heart-Bloom Staff");
        tamed.discard();
        helper.succeed();
    }

    private static CrystalFoxEntity spawnFox(GameTestHelper helper, BlockPos rel) {
        CrystalFoxEntity fox = ArtilianEntities.CRYSTAL_FOX.get().create(helper.getLevel());
        BlockPos abs = helper.absolutePos(rel);
        fox.moveTo(abs.getX() + 0.5, abs.getY(), abs.getZ() + 0.5, 0.0F, 0.0F);
        helper.getLevel().addFreshEntity(fox);
        return fox;
    }

    private static List<? extends CrystalFoxEntity> foxesOf(GameTestHelper helper, ServerPlayer owner) {
        return helper.getLevel().getEntities(ArtilianEntities.CRYSTAL_FOX.get(),
                fox -> fox.isAlive() && fox.isOwner(owner));
    }
}
