package com.darkona.dropletsofthirst.compat.create;

import com.darkona.dropletsofthirst.DropletsOfThirst;
import com.darkona.dropletsofthirst.config.CompatConfig;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraftforge.fml.ModList;

public final class CreateCompat
{
    public static final boolean LOADED = ModList.get().isLoaded("create");

    private CreateCompat() {}

    public static int sandFilterPurity(int purity)
    {
        int max = CompatConfig.SAND_FILTER_MAX_PURITY.get();
        return Math.max(purity, Math.min(purity + CompatConfig.SAND_FILTER_FILTRATION_AMOUNT.get(), max));
    }

    public static Item sandFilter()
    {
        return LOADED ? BuiltInRegistries.ITEM.get(DropletsOfThirst.asResource("sand_filter")) : Items.AIR;
    }
}
