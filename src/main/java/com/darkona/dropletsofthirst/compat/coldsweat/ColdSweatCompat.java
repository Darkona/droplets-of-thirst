package com.darkona.dropletsofthirst.compat.coldsweat;

import com.darkona.dropletsofthirst.api.event.DrinkEvent;
import com.darkona.dropletsofthirst.purity.WaterPurity;
import com.darkona.dropletsofthirst.purity.ThirstComponent;
import com.darkona.dropletsofthirst.foundation.config.CompatConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.TickTask;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import dev.darkona.jdlib.api.compat.Bridges;
import dev.darkona.jdlib.api.compat.climate.BodyTemperature;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;

public final class ColdSweatCompat
{
    public static final boolean LOADED = Bridges.isLoaded("cold_sweat");

    private ColdSweatCompat() {}

    public static void init()
    {
        if (!LOADED)
            return;
        NeoForge.EVENT_BUS.addListener(ColdSweatCompat::coolAfterDrinking);
    }

    public static double bodyTemperature(Player player)
    {
        return BodyTemperature.of(player, 0);
    }

    private static void coolAfterDrinking(DrinkEvent.Post event)
    {
        double cooling = CompatConfig.COLD_SWEAT_DRINK_COOLING.get();
        ItemStack item = event.getItem();
        if (cooling <= 0 || !item.isEmpty() && (!WaterPurity.isWaterFilledContainer(item) || ColdSweatBridge.isWaterskin(item)))
            return;
        ColdSweatBridge.cool(event.getEntity(), cooling, CompatConfig.COLD_SWEAT_DRINK_COOLING_TICKS.get());
    }
}
