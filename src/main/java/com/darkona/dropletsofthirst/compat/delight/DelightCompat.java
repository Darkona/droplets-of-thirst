package com.darkona.dropletsofthirst.compat.delight;

import com.darkona.dropletsofthirst.index.DropletsTags;
import com.darkona.dropletsofthirst.content.purity.WaterPurity;
import com.darkona.dropletsofthirst.config.CompatConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import org.jetbrains.annotations.Nullable;

public final class DelightCompat
{
    private DelightCompat() {}

    public static void init()
    {
        MinecraftForge.EVENT_BUS.addListener(DelightCompat::rejectDirtyWater);
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
}
