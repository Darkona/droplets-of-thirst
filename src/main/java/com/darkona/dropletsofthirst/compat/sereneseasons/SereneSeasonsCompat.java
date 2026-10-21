package com.darkona.dropletsofthirst.compat.sereneseasons;

import com.darkona.dropletsofthirst.foundation.config.CompatConfig;
import dev.darkona.jdlib.api.compat.Bridges;
import dev.darkona.jdlib.api.compat.climate.Season;
import dev.darkona.jdlib.api.compat.climate.Seasons;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;

public final class SereneSeasonsCompat
{
    public static final boolean LOADED = Bridges.isLoaded("sereneseasons");
    public static final boolean ACTIVE = Seasons.drivesClimate();

    private SereneSeasonsCompat() {}

    public static float temperature(Level level, Holder<Biome> biome, BlockPos pos, float base)
    {
        return CompatConfig.SERENE_SEASONS_ENABLED.get() ? Seasons.biomeTemperature(level, biome, pos, base) : base;
    }

    public static float seasonMultiplier(Level level, Holder<Biome> biome)
    {
        if (!CompatConfig.SERENE_SEASONS_ENABLED.get())
            return 1.0F;
        Season season = Seasons.at(level, biome);
        if (season == null)
            return 1.0F;
        return switch (season)
        {
            case SPRING -> CompatConfig.SERENE_SEASONS_SPRING.get().floatValue();
            case SUMMER -> CompatConfig.SERENE_SEASONS_SUMMER.get().floatValue();
            case AUTUMN -> CompatConfig.SERENE_SEASONS_AUTUMN.get().floatValue();
            case WINTER -> CompatConfig.SERENE_SEASONS_WINTER.get().floatValue();
            case TROPICAL_DRY -> CompatConfig.SERENE_SEASONS_TROPICAL_DRY.get().floatValue();
            case TROPICAL_WET -> CompatConfig.SERENE_SEASONS_TROPICAL_WET.get().floatValue();
        };
    }
}
