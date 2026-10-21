package com.darkona.dropletsofthirst.compat.sereneseasons;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import sereneseasons.api.season.SeasonHelper;
import sereneseasons.init.ModConfig;
import sereneseasons.init.ModTags;
import sereneseasons.season.SeasonHooks;

final class SereneSeasonsBridge
{
    private SereneSeasonsBridge() {}

    static float temperature(Level level, Holder<Biome> biome, BlockPos pos)
    {
        return SeasonHooks.getBiomeTemperature(level, biome, pos);
    }

    static final int NONE = -1;
    static final int TROPICAL_DRY = 4, TROPICAL_WET = 5;

    static int season(Level level, Holder<Biome> biome)
    {
        if (biome.is(ModTags.Biomes.BLACKLISTED_BIOMES) || !ModConfig.seasons.isDimensionWhitelisted(level.dimension()))
            return NONE;
        if (SeasonHelper.usesTropicalSeasons(biome))
            return SeasonHelper.getSeasonState(level).getTropicalSeason().ordinal() < 3 ? TROPICAL_DRY : TROPICAL_WET;
        return SeasonHelper.getSeasonState(level).getSeason().ordinal();
    }
}
