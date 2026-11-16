package com.silkpanda.artilian.datagen;

import com.google.common.hash.HashCode;
import com.google.common.hash.Hashing;
import net.minecraft.SharedConstants;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtIo;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;

/**
 * Writes the empty GameTest structure templates ({@code empty} 3x3x3 and {@code worldgen} 7x7x7).
 */
public class GameTestStructureProvider implements DataProvider {
    private static final String NAMESPACE = "artilian";

    private final PackOutput output;

    public GameTestStructureProvider(PackOutput output) {
        this.output = output;
    }

    @Override
    public CompletableFuture<?> run(CachedOutput cachedOutput) {
        return CompletableFuture.runAsync(() -> {
            writeEmptyStructure(cachedOutput, "empty", 3, 3, 3);
            writeEmptyStructure(cachedOutput, "worldgen", 7, 7, 7);
            writeEmptyStructure(cachedOutput, "tnt_arena", 15, 15, 15);
        });
    }

    private void writeEmptyStructure(CachedOutput cachedOutput, String name, int sx, int sy, int sz) {
        CompoundTag tag = new CompoundTag();
        tag.putInt("DataVersion", SharedConstants.getCurrentVersion().getDataVersion().getVersion());

        ListTag size = new ListTag();
        size.add(IntTag.valueOf(sx));
        size.add(IntTag.valueOf(sy));
        size.add(IntTag.valueOf(sz));
        tag.put("size", size);

        CompoundTag air = new CompoundTag();
        air.putString("Name", "minecraft:air");
        ListTag palette = new ListTag();
        palette.add(air);
        tag.put("palette", palette);

        tag.put("blocks", new ListTag());
        tag.put("entities", new ListTag());

        Path path = this.output.getOutputFolder(PackOutput.Target.DATA_PACK)
                .resolve(NAMESPACE)
                .resolve("structure")
                .resolve(name + ".nbt");

        try {
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            NbtIo.writeCompressed(tag, buffer);
            byte[] bytes = buffer.toByteArray();

            @SuppressWarnings("deprecation") // Guava's sha1 helper is what CachedOutput still expects in this version.
            HashCode hash = Hashing.sha1().hashBytes(bytes);

            cachedOutput.writeIfNeeded(path, bytes, hash);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public String getName() {
        return "Artilian GameTest structures";
    }
}
