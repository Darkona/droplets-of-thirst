package com.darkona.dropletsofthirst.compat.delight;

import com.darkona.dropletsofthirst.data.DropletsTags;
import com.darkona.dropletsofthirst.purity.WaterPurity;
import com.darkona.dropletsofthirst.foundation.config.CompatConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.fluids.FluidStack;
import org.jetbrains.annotations.Nullable;

public final class DelightCompat
{
    private DelightCompat() {}

    public static void init()
    {
        NeoForge.EVENT_BUS.addListener(DelightCompat::rejectDirtyWater);
    }

    public static boolean tooDirtyForKettle(ItemStack stack)
    {
        return WaterPurity.enabled() && WaterPurity.isWaterFilledContainer(stack) && WaterPurity.getPurity(stack) < CompatConfig.KETTLE_MIN_PURITY.get();
    }

    private static void rejectDirtyWater(PlayerInteractEvent.RightClickBlock event)
    {
        if (!tooDirtyForKettle(event.getItemStack()) || !event.getLevel().getBlockState(event.getPos()).is(DropletsTags.REJECTS_DIRTY_WATER))
            return;
        event.setCanceled(true);
        event.setCancellationResult(InteractionResult.FAIL);
        if (!event.getLevel().isClientSide())
            event.getEntity().displayClientMessage(Component.translatable("droplets_of_thirst.message.water_too_dirty"), true);
    }

    public static int sourcePurity(@Nullable Level level, BlockPos pos)
    {
        if (level == null || !WaterPurity.enabled() || !CompatConfig.WORLD_PURITY_WATER_SOURCES.get())
            return -1;
        return WaterPurity.getWaterPurity(level, pos, true);
    }

    public static ItemStack withSourcePurity(ItemStack filled, Level level, BlockPos pos)
    {
        if (WaterPurity.isWaterFilledContainer(filled) && !WaterPurity.hasPurity(filled))
        {
            int purity = sourcePurity(level, pos);
            if (purity >= WaterPurity.MIN_PURITY)
                WaterPurity.addPurity(filled, purity);
        }
        return filled;
    }

    public static FluidStack withSourcePurity(FluidStack water, Level level, BlockPos pos)
    {
        if (!water.isEmpty() && water.getFluid().isSame(Fluids.WATER) && !WaterPurity.hasPurity(water))
        {
            int purity = sourcePurity(level, pos);
            if (purity >= WaterPurity.MIN_PURITY)
                WaterPurity.addPurity(water, purity);
        }
        return water;
    }

    public static FluidStack withSourcePurity(FluidStack water, @Nullable BlockEntity source)
    {
        return source == null || source.getLevel() == null ? water : withSourcePurity(water, source.getLevel(), source.getBlockPos());
    }
}
