package com.darkona.dropletsofthirst.index;

import com.darkona.dropletsofthirst.DropletsOfThirst;
import com.darkona.dropletsofthirst.content.purity.AmphoraBlock;
import com.darkona.dropletsofthirst.content.purity.CharcoalFilterBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class DropletsBlocks
{
    private static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(DropletsOfThirst.ID);

    public static final DeferredBlock<AmphoraBlock> AMPHORA = BLOCKS.register("amphora", () -> new AmphoraBlock(BlockBehaviour.Properties.of()
            .mapColor(MapColor.TERRACOTTA_ORANGE)
            .strength(0.5F)
            .sound(SoundType.DECORATED_POT)
            .pushReaction(PushReaction.DESTROY)
            .noOcclusion()));

    public static final DeferredBlock<CharcoalFilterBlock> CHARCOAL_FILTER = BLOCKS.register("charcoal_filter", () -> new CharcoalFilterBlock(BlockBehaviour.Properties.of()
            .mapColor(MapColor.SAND)
            .strength(0.6F)
            .sound(SoundType.GRAVEL)
            .noOcclusion()));

    private DropletsBlocks() {}

    public static void register(IEventBus modBus)
    {
        BLOCKS.register(modBus);
    }
}
