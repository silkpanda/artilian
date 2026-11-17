package com.silkpanda.artilian.datagen;

import com.google.common.hash.HashCode;
import com.google.common.hash.Hashing;
import com.silkpanda.artilian.ArtilianMod;
import com.silkpanda.artilian.registry.ArtilianBlocks;
import com.silkpanda.artilian.registry.ArtilianEntities;
import com.silkpanda.artilian.worldgen.ArtilianStructures;
import net.minecraft.SharedConstants;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.DoubleTag;
import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootTable;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Generates the three Phase 2 Stage 6 Overworld biome structure templates ({@code .nbt}) with
 * loot chests and persistent {@code artilian:structure_enemies} guardians.
 */
public class ArtilianStructureTemplateProvider implements DataProvider {
    private final PackOutput output;

    public ArtilianStructureTemplateProvider(PackOutput output) {
        this.output = output;
    }

    @Override
    public CompletableFuture<?> run(CachedOutput cachedOutput) {
        return CompletableFuture.runAsync(() -> {
            writeTemplate(cachedOutput, "crystal_grove_shrine", buildCrystalGroveShrine());
            writeTemplate(cachedOutput, "emberbloom_outpost", buildEmberbloomOutpost());
            writeTemplate(cachedOutput, "starlight_watchtower", buildStarlightWatchtower());
        });
    }

    private static CompoundTag buildCrystalGroveShrine() {
        TemplateBuilder b = new TemplateBuilder(9, 7, 9);
        BlockState planks = ArtilianBlocks.CRYSTAL_CANOPY_PLANKS.get().defaultBlockState();
        BlockState log = ArtilianBlocks.CRYSTAL_CANOPY_LOG.get().defaultBlockState();
        BlockState turf = ArtilianBlocks.CRYSTAL_TURF.get().defaultBlockState();
        BlockState leaves = ArtilianBlocks.CRYSTAL_CANOPY_LEAVES.get().defaultBlockState()
                .setValue(LeavesBlock.PERSISTENT, true);
        BlockState crystalBlock = ArtilianBlocks.STARLIGHT_CRYSTAL_BLOCK.get().defaultBlockState();
        BlockState bloom = ArtilianBlocks.CRYSTAL_BLOOM.get().defaultBlockState();

        b.fillBox(0, 0, 0, 8, 0, 8, planks);
        b.fillBox(1, 1, 1, 7, 4, 7, Blocks.AIR.defaultBlockState());
        for (int cx : new int[]{0, 8}) {
            for (int cz : new int[]{0, 8}) {
                b.set(cx, 0, cz, turf);
            }
        }
        b.set(4, 0, 4, crystalBlock);

        // Corner pillars
        for (int px : new int[]{1, 7}) {
            for (int pz : new int[]{1, 7}) {
                b.fillBox(px, 1, pz, px, 4, pz, log);
            }
        }

        // Leaf canopy roof
        b.fillBox(1, 5, 1, 7, 5, 7, leaves);
        b.fillBox(2, 6, 2, 6, 6, 6, leaves);
        b.set(4, 6, 4, crystalBlock);
        b.set(4, 5, 4, crystalBlock);

        // Interior shrine centerpiece, blooms & reward chest
        b.set(2, 1, 2, bloom);
        b.set(6, 1, 2, bloom);
        b.set(4, 1, 4, crystalBlock);
        b.set(4, 2, 4, Blocks.AMETHYST_CLUSTER.defaultBlockState());
        b.setChest(4, 1, 2, Direction.SOUTH, ArtilianStructures.CRYSTAL_GROVE_SHRINE_CHEST);

        b.addGuardian(ArtilianEntities.PRISM_SENTINEL.get(), 2.5, 1.0, 5.5);
        b.addGuardian(ArtilianEntities.PRISM_SENTINEL.get(), 6.5, 1.0, 5.5);
        return b.build();
    }

    private static CompoundTag buildEmberbloomOutpost() {
        TemplateBuilder b = new TemplateBuilder(9, 7, 9);
        BlockState planks = ArtilianBlocks.EMBERWOOD_PLANKS.get().defaultBlockState();
        BlockState log = ArtilianBlocks.EMBERWOOD_LOG.get().defaultBlockState();
        BlockState stoneBricks = Blocks.STONE_BRICKS.defaultBlockState();
        BlockState wall = Blocks.STONE_BRICK_WALL.defaultBlockState();
        BlockState sunsteel = ArtilianBlocks.SUNSTEEL_BLOCK.get().defaultBlockState();
        BlockState bloom = ArtilianBlocks.EMBER_BLOOM.get().defaultBlockState();
        BlockState lantern = Blocks.LANTERN.defaultBlockState();

        b.fillBox(0, 0, 0, 8, 0, 8, planks);
        b.fillBox(1, 1, 1, 7, 4, 7, Blocks.AIR.defaultBlockState());
        // Stone-brick perimeter border
        for (int i = 0; i <= 8; i++) {
            b.set(i, 0, 0, stoneBricks);
            b.set(i, 0, 8, stoneBricks);
            b.set(0, 0, i, stoneBricks);
            b.set(8, 0, i, stoneBricks);
        }
        b.set(4, 0, 4, sunsteel);

        // Corner timber posts topped with lanterns + side walls (open N/S gates)
        for (int px : new int[]{1, 7}) {
            for (int pz : new int[]{1, 7}) {
                b.fillBox(px, 1, pz, px, 4, pz, log);
                b.set(px, 5, pz, lantern);
            }
        }
        for (int z = 2; z <= 6; z++) {
            b.set(1, 1, z, wall);
            b.set(7, 1, z, wall);
        }

        // Cross-beam awning
        b.fillBox(2, 4, 2, 6, 4, 6, planks);
        b.set(4, 5, 4, sunsteel);

        // Planters & reward chest
        b.set(2, 0, 2, ArtilianBlocks.EMBER_TURF.get().defaultBlockState());
        b.set(6, 0, 2, ArtilianBlocks.EMBER_TURF.get().defaultBlockState());
        b.set(2, 1, 2, bloom);
        b.set(6, 1, 2, bloom);
        b.setChest(4, 1, 2, Direction.SOUTH, ArtilianStructures.EMBERBLOOM_OUTPOST_CHEST);

        b.addGuardian(ArtilianEntities.CINDER_RAIDER.get(), 2.5, 1.0, 5.5);
        b.addGuardian(ArtilianEntities.CINDER_RAIDER.get(), 6.5, 1.0, 5.5);
        return b.build();
    }

    private static CompoundTag buildStarlightWatchtower() {
        TemplateBuilder b = new TemplateBuilder(9, 10, 9);
        BlockState slate = ArtilianBlocks.STARLIGHT_SLATE.get().defaultBlockState();
        BlockState bricks = Blocks.DEEPSLATE_BRICKS.defaultBlockState();
        BlockState turf = ArtilianBlocks.STARLIGHT_TURF.get().defaultBlockState();
        BlockState blossom = ArtilianBlocks.STARLIGHT_BLOSSOM.get().defaultBlockState();
        BlockState crystalBlock = ArtilianBlocks.STARLIGHT_CRYSTAL_BLOCK.get().defaultBlockState();
        BlockState soulLantern = Blocks.SOUL_LANTERN.defaultBlockState();

        b.fillBox(0, 0, 0, 8, 0, 8, slate);
        b.fillBox(1, 1, 1, 7, 7, 7, Blocks.AIR.defaultBlockState());
        for (int i = 0; i <= 8; i++) {
            b.set(i, 0, 0, bricks);
            b.set(i, 0, 8, bricks);
            b.set(0, 0, i, bricks);
            b.set(8, 0, i, bricks);
        }

        // Watchtower walls (open South arch & window slits)
        for (int y = 1; y <= 6; y++) {
            for (int i = 1; i <= 7; i++) {
                BlockState wallBlock = (y % 2 == 0) ? bricks : slate;
                b.set(i, y, 1, wallBlock);
                b.set(1, y, i, wallBlock);
                b.set(7, y, i, wallBlock);
                if (y >= 4 || i <= 2 || i >= 6) {
                    b.set(i, y, 7, wallBlock);
                }
            }
        }
        // Clear interior air inside tower
        b.fillBox(2, 1, 2, 6, 6, 6, Blocks.AIR.defaultBlockState());

        // Top deck, battlements & starlight beacon
        b.fillBox(1, 7, 1, 7, 7, 7, slate);
        for (int px : new int[]{1, 4, 7}) {
            for (int pz : new int[]{1, 4, 7}) {
                if (px != 4 || pz != 4) {
                    b.set(px, 8, pz, bricks);
                }
            }
        }
        for (int cx : new int[]{1, 7}) {
            for (int cz : new int[]{1, 7}) {
                b.set(cx, 9, cz, soulLantern);
            }
        }
        b.set(4, 8, 4, crystalBlock);

        // Interior blossoms & reward chest
        b.set(2, 0, 2, turf);
        b.set(6, 0, 2, turf);
        b.set(2, 1, 2, blossom);
        b.set(6, 1, 2, blossom);
        b.setChest(4, 1, 2, Direction.SOUTH, ArtilianStructures.STARLIGHT_WATCHTOWER_CHEST);

        b.addGuardian(ArtilianEntities.ASTRAL_STALKER.get(), 2.5, 1.0, 5.5);
        b.addGuardian(ArtilianEntities.ASTRAL_STALKER.get(), 6.5, 1.0, 5.5);
        return b.build();
    }

    private void writeTemplate(CachedOutput cachedOutput, String name, CompoundTag tag) {
        Path path = this.output.getOutputFolder(PackOutput.Target.DATA_PACK)
                .resolve(ArtilianMod.MODID)
                .resolve("structure")
                .resolve(name + ".nbt");
        try {
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            NbtIo.writeCompressed(tag, buffer);
            byte[] bytes = buffer.toByteArray();

            @SuppressWarnings("deprecation") // Guava's sha1 helper is what CachedOutput expects here.
            HashCode hash = Hashing.sha1().hashBytes(bytes);
            cachedOutput.writeIfNeeded(path, bytes, hash);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public String getName() {
        return "Artilian Biome Structure Templates";
    }

    private static final class TemplateBuilder {
        private final int sx;
        private final int sy;
        private final int sz;
        private final Map<BlockState, Integer> paletteIndex = new LinkedHashMap<>();
        private final ListTag palette = new ListTag();
        private final Map<Long, BlockEntry> blocks = new LinkedHashMap<>();
        private final ListTag entities = new ListTag();

        private record BlockEntry(int x, int y, int z, int state, CompoundTag nbt) {}

        TemplateBuilder(int sx, int sy, int sz) {
            this.sx = sx;
            this.sy = sy;
            this.sz = sz;
        }

        void set(int x, int y, int z, BlockState state) {
            setWithNbt(x, y, z, state, null);
        }

        void setChest(int x, int y, int z, Direction facing, ResourceKey<LootTable> lootTable) {
            CompoundTag chestNbt = new CompoundTag();
            chestNbt.putString("id", "minecraft:chest");
            chestNbt.putString("LootTable", lootTable.location().toString());
            setWithNbt(x, y, z, Blocks.CHEST.defaultBlockState().setValue(ChestBlock.FACING, facing), chestNbt);
        }

        void fillBox(int x1, int y1, int z1, int x2, int y2, int z2, BlockState state) {
            for (int x = x1; x <= x2; x++) {
                for (int y = y1; y <= y2; y++) {
                    for (int z = z1; z <= z2; z++) {
                        set(x, y, z, state);
                    }
                }
            }
        }

        void addGuardian(EntityType<?> entityType, double x, double y, double z) {
            CompoundTag entityNbt = new CompoundTag();
            entityNbt.putString("id", BuiltInRegistries.ENTITY_TYPE.getKey(entityType).toString());
            entityNbt.putBoolean("PersistenceRequired", true);

            ListTag pos = new ListTag();
            pos.add(DoubleTag.valueOf(x));
            pos.add(DoubleTag.valueOf(y));
            pos.add(DoubleTag.valueOf(z));

            ListTag blockPos = new ListTag();
            blockPos.add(IntTag.valueOf((int) Math.floor(x)));
            blockPos.add(IntTag.valueOf((int) Math.floor(y)));
            blockPos.add(IntTag.valueOf((int) Math.floor(z)));

            CompoundTag entry = new CompoundTag();
            entry.put("pos", pos);
            entry.put("blockPos", blockPos);
            entry.put("nbt", entityNbt);
            this.entities.add(entry);
        }

        private void setWithNbt(int x, int y, int z, BlockState state, CompoundTag nbt) {
            int stateId = this.paletteIndex.computeIfAbsent(state, s -> {
                int idx = this.palette.size();
                this.palette.add(NbtUtils.writeBlockState(s));
                return idx;
            });
            long key = (((long) x & 0x3FF) << 20) | (((long) y & 0x3FF) << 10) | ((long) z & 0x3FF);
            this.blocks.put(key, new BlockEntry(x, y, z, stateId, nbt));
        }

        CompoundTag build() {
            CompoundTag root = new CompoundTag();
            root.putInt("DataVersion", SharedConstants.getCurrentVersion().getDataVersion().getVersion());

            ListTag size = new ListTag();
            size.add(IntTag.valueOf(this.sx));
            size.add(IntTag.valueOf(this.sy));
            size.add(IntTag.valueOf(this.sz));
            root.put("size", size);
            root.put("palette", this.palette);

            ListTag blockList = new ListTag();
            for (BlockEntry entry : this.blocks.values()) {
                ListTag pos = new ListTag();
                pos.add(IntTag.valueOf(entry.x()));
                pos.add(IntTag.valueOf(entry.y()));
                pos.add(IntTag.valueOf(entry.z()));

                CompoundTag blockTag = new CompoundTag();
                blockTag.put("pos", pos);
                blockTag.putInt("state", entry.state());
                if (entry.nbt() != null) {
                    blockTag.put("nbt", entry.nbt());
                }
                blockList.add(blockTag);
            }
            root.put("blocks", blockList);
            root.put("entities", this.entities);
            return root;
        }
    }
}
