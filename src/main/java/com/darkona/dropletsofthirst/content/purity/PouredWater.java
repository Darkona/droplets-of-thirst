package com.darkona.dropletsofthirst.content.purity;

import com.darkona.dropletsofthirst.foundation.common.capability.ModAttachment;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.longs.Long2ByteMap;
import it.unimi.dsi.fastutil.longs.Long2ByteOpenHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.SectionPos;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.material.FluidState;
import org.jetbrains.annotations.Nullable;

import java.nio.ByteBuffer;
import java.util.stream.LongStream;

public final class PouredWater
{
    private static final byte NONE = -1;
    private static final Direction[] HORIZONTAL = {Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST};

    public static final Codec<PouredWater> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.LONG_STREAM.fieldOf("positions").forGetter(water -> LongStream.of(water.sources.keySet().toLongArray())),
            Codec.BYTE_BUFFER.fieldOf("purities").forGetter(water -> ByteBuffer.wrap(water.sources.values().toByteArray()))
    ).apply(instance, PouredWater::new));

    private final Long2ByteOpenHashMap sources = new Long2ByteOpenHashMap();

    public PouredWater()
    {
        sources.defaultReturnValue(NONE);
    }

    private PouredWater(LongStream positions, ByteBuffer purities)
    {
        this();
        long[] keys = positions.toArray();
        int start = purities.position();
        for (int i = 0; i < keys.length && i < purities.remaining(); i++)
            sources.put(keys[i], purities.get(start + i));
    }

    public boolean isEmpty()
    {
        return sources.isEmpty();
    }

    public Long2ByteMap entries()
    {
        return sources;
    }

    public static void poured(Level level, BlockPos pos, int purity)
    {
        if (level.isClientSide() || !WaterPurity.enabled() || !isWaterSource(level.getFluidState(pos)))
            return;
        LevelChunk chunk = level.getChunkSource().getChunkNow(SectionPos.blockToSectionCoord(pos.getX()), SectionPos.blockToSectionCoord(pos.getZ()));
        if (chunk == null)
            return;
        chunk.getData(ModAttachment.POURED_WATER.get()).sources.put(pos.asLong(), (byte) WaterPurity.sanitizePurity(purity));
        chunk.setUnsaved(true);
    }

    public static int purityAt(Level level, BlockPos pos)
    {
        if (level.isClientSide())
            return -1;
        int own = stored(level, pos, true);
        if (own >= 0 || !isWaterSource(level.getFluidState(pos)))
            return own;
        return worstNeighbour(level, pos);
    }

    public static int pickedUp(Level level, BlockPos pos)
    {
        if (level.isClientSide())
            return -1;
        LevelChunk chunk = chunk(level, pos);
        PouredWater water = chunk == null ? null : chunk.getExistingDataOrNull(ModAttachment.POURED_WATER.get());
        if (water == null || !water.sources.containsKey(pos.asLong()))
            return worstNeighbour(level, pos);
        int purity = water.sources.remove(pos.asLong());
        chunk.setUnsaved(true);
        BlockPos.MutableBlockPos next = new BlockPos.MutableBlockPos();
        for (Direction direction : HORIZONTAL)
        {
            next.setWithOffset(pos, direction);
            if (isWaterSource(level.getFluidState(next)) && stored(level, next, false) < 0)
                poured(level, next, purity);
        }
        return purity;
    }

    private static int worstNeighbour(Level level, BlockPos pos)
    {
        int worst = -1;
        BlockPos.MutableBlockPos next = new BlockPos.MutableBlockPos();
        for (Direction direction : HORIZONTAL)
        {
            next.setWithOffset(pos, direction);
            int purity = stored(level, next, true);
            if (purity >= 0 && (worst < 0 || purity < worst))
                worst = purity;
        }
        return worst;
    }

    private static int stored(Level level, BlockPos pos, boolean clean)
    {
        LevelChunk chunk = chunk(level, pos);
        PouredWater water = chunk == null ? null : chunk.getExistingDataOrNull(ModAttachment.POURED_WATER.get());
        if (water == null)
            return -1;
        long key = pos.asLong();
        byte purity = water.sources.get(key);
        if (purity == NONE)
            return -1;
        if (isWaterSource(chunk.getFluidState(pos)))
            return purity;
        if (clean)
        {
            water.sources.remove(key);
            chunk.setUnsaved(true);
        }
        return -1;
    }

    private static @Nullable LevelChunk chunk(Level level, BlockPos pos)
    {
        return level.getChunkSource().getChunkNow(SectionPos.blockToSectionCoord(pos.getX()), SectionPos.blockToSectionCoord(pos.getZ()));
    }

    private static boolean isWaterSource(FluidState fluid)
    {
        return fluid.isSource() && fluid.is(FluidTags.WATER);
    }
}
