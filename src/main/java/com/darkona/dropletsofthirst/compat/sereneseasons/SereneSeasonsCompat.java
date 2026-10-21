package com.darkona.dropletsofthirst.compat.sereneseasons;

import com.darkona.dropletsofthirst.compat.coldsweat.ColdSweatCompat;
import com.darkona.dropletsofthirst.foundation.config.CompatConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import dev.darkona.jdlib.api.compat.Bridges;

public final class SereneSeasonsCompat
{
    public static final boolean LOADED = Bridges.isLoaded("sereneseasons");
    public static final boolean ACTIVE = LOADED && !ColdSweatCompat.LOADED;

    private SereneSeasonsCompat() {}

    public static float temperature(Level level, Holder<Biome> biome, BlockPos pos, float base)
    {
        return CompatConfig.SERENE_SEASONS_ENABLED.get() ? SereneSeasonsBridge.temperature(level, biome, pos) : base;
    }

    public static float seasonMultiplier(Level level, Holder<Biome> biome)
    {
        if (!CompatConfig.SERENE_SEASONS_ENABLED.get())
            return 1.0F;
        return switch (SereneSeasonsBridge.season(level, biome))
        {
            case 0 -> CompatConfig.SERENE_SEASONS_SPRING.get().floatValue();
            case 1 -> CompatConfig.SERENE_SEASONS_SUMMER.get().floatValue();
            case 2 -> CompatConfig.SERENE_SEASONS_AUTUMN.get().floatValue();
            case 3 -> CompatConfig.SERENE_SEASONS_WINTER.get().floatValue();
            case SereneSeasonsBridge.TROPICAL_DRY -> CompatConfig.SERENE_SEASONS_TROPICAL_DRY.get().floatValue();
            case SereneSeasonsBridge.TROPICAL_WET -> CompatConfig.SERENE_SEASONS_TROPICAL_WET.get().floatValue();
            default -> 1.0F;
        };
    }
}
