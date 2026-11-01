package com.darkona.dropletsofthirst.compat.coldsweat;

import com.darkona.dropletsofthirst.api.event.DrinkEvent;
import com.darkona.dropletsofthirst.config.CompatConfig;
import com.darkona.dropletsofthirst.content.purity.WaterPurity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.ModList;

public final class ColdSweatCompat
{
    public static final boolean LOADED = ModList.get().isLoaded("cold_sweat");

    private ColdSweatCompat() {}

    public static void init()
    {
        if (!LOADED)
            return;
        MinecraftForge.EVENT_BUS.addListener(ColdSweatCompat::coolAfterDrinking);
        ColdSweatBridge.listen();
    }

    public static void fillCanteen(ItemStack canteen, Level level, BlockPos pos)
    {
        if (LOADED)
            ColdSweatBridge.fillCanteen(canteen, level, pos);
    }

    public static double canteenTemperature(ItemStack canteen)
    {
        return LOADED ? ColdSweatBridge.canteenTemperature(canteen) : 0.0;
    }

    public static boolean warmCanteen(ItemStack canteen, double delta)
    {
        return LOADED && ColdSweatBridge.warmCanteen(canteen, delta);
    }

    public static void sipCanteen(ItemStack canteen, LivingEntity entity)
    {
        if (LOADED)
            ColdSweatBridge.sipCanteen(canteen, entity);
    }

    public static void emptyCanteen(ItemStack canteen)
    {
        if (LOADED)
            ColdSweatBridge.emptyCanteen(canteen);
    }

    public static double bodyTemperature(Player player)
    {
        return LOADED ? ColdSweatBridge.bodyTemperature(player) : 0;
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
