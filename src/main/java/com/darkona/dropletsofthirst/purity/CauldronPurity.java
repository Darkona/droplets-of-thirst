package com.darkona.dropletsofthirst.purity;

import com.darkona.dropletsofthirst.api.PurityLevel;
import com.darkona.dropletsofthirst.data.DropletsTags;
import com.darkona.dropletsofthirst.item.ItemInit;
import java.util.function.Supplier;
import net.minecraft.core.BlockPos;
import net.minecraft.core.cauldron.CauldronInteraction;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LayeredCauldronBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.gameevent.GameEvent;

public final class CauldronPurity
{
    public static final int CAULDRON_PURITY = PurityLevel.MURKY.level();
    public static final int HEATED_CAULDRON_PURITY = PurityLevel.CLEAN.level();

    private static final ThreadLocal<Integer> TAKING_FROM = new ThreadLocal<>();

    private CauldronPurity() {}

    public static void registerInteractions()
    {
        CauldronInteraction.WATER.map().put(ItemInit.TERRACOTTA_BOWL.get(), fillFromCauldron(() -> new ItemStack(ItemInit.TERRACOTTA_WATER_BOWL.get()), SoundEvents.BUCKET_FILL));
    }

    private static CauldronInteraction fillFromCauldron(Supplier<ItemStack> filled, SoundEvent sound)
    {
        return (state, level, pos, player, hand, stack) -> {
            if (!level.isClientSide())
            {
                player.setItemInHand(hand, ItemUtils.createFilledResult(stack, player, filled.get()));
                player.awardStat(Stats.USE_CAULDRON);
                player.awardStat(Stats.ITEM_USED.get(stack.getItem()));
                LayeredCauldronBlock.lowerFillLevel(state, level, pos);
                level.playSound(null, pos, sound, SoundSource.BLOCKS, 1.0F, 1.0F);
                level.gameEvent(null, GameEvent.FLUID_PICKUP, pos);
            }
            return ItemInteractionResult.sidedSuccess(level.isClientSide());
        };
    }

    public static int at(BlockGetter level, BlockPos pos)
    {
        BlockState below = level.getBlockState(pos.below());
        return below.is(DropletsTags.CAULDRON_HEAT_SOURCES) && below.getOptionalValue(BlockStateProperties.LIT).orElse(true)
                ? HEATED_CAULDRON_PURITY : CAULDRON_PURITY;
    }

    public static ItemInteractionResult interact(BlockState state, Level level, BlockPos pos, Supplier<ItemInteractionResult> interaction)
    {
        if (level.isClientSide() || !WaterPurity.enabled() || !state.is(Blocks.WATER_CAULDRON))
            return interaction.get();
        TAKING_FROM.set(at(level, pos));
        try
        {
            return interaction.get();
        }
        finally
        {
            TAKING_FROM.remove();
        }
    }

    public static void applyTo(ItemStack filled)
    {
        Integer purity = TAKING_FROM.get();
        if (purity != null && purity >= WaterPurity.MIN_PURITY && WaterPurity.isWaterFilledContainer(filled) && !WaterPurity.hasPurity(filled))
            WaterPurity.addPurity(filled, purity);
    }
}
