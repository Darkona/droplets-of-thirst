package com.darkona.dropletsofthirst.data;

import com.darkona.dropletsofthirst.DropletsOfThirst;
import com.darkona.dropletsofthirst.content.purity.WaterPurity;
import com.darkona.dropletsofthirst.core.ThirstConstants;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.dimension.DimensionType;
import net.neoforged.neoforge.registries.datamaps.DataMapType;
import net.neoforged.neoforge.registries.datamaps.RegisterDataMapTypesEvent;

public final class DropletsDataMaps
{
    public static final DataMapType<Item, DrinkValues> DRINKS = DataMapType
            .builder(DropletsOfThirst.asResource("drinks"), Registries.ITEM, DrinkValues.CODEC)
            .synced(DrinkValues.CODEC, false)
            .build();
    public static final DataMapType<Block, DrinkValues> HYDRATING_BLOCKS = DataMapType
            .builder(DropletsOfThirst.asResource("hydrating_blocks"), Registries.BLOCK, DrinkValues.CODEC)
            .build();
    public static final DataMapType<Biome, BiomeWater> BIOME_WATER = DataMapType
            .builder(DropletsOfThirst.asResource("biome_water"), Registries.BIOME, BiomeWater.CODEC)
            .build();
    public static final DataMapType<DimensionType, DimensionWater> DIMENSION_WATER = DataMapType
            .builder(DropletsOfThirst.asResource("dimension_water"), Registries.DIMENSION_TYPE, DimensionWater.CODEC)
            .build();

    private DropletsDataMaps() {}

    public static void register(RegisterDataMapTypesEvent event)
    {
        event.register(DRINKS);
        event.register(HYDRATING_BLOCKS);
        event.register(BIOME_WATER);
        event.register(DIMENSION_WATER);
    }

    public record BiomeWater(Optional<Integer> base, int delta, int max)
    {
        public static final Codec<BiomeWater> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.intRange(WaterPurity.MIN_PURITY, WaterPurity.MAX_PURITY).optionalFieldOf("base").forGetter(BiomeWater::base),
                Codec.intRange(-WaterPurity.MAX_PURITY, WaterPurity.MAX_PURITY).optionalFieldOf("delta", 0).forGetter(BiomeWater::delta),
                Codec.intRange(WaterPurity.MIN_PURITY, WaterPurity.MAX_PURITY).optionalFieldOf("max", WaterPurity.MAX_PURITY).forGetter(BiomeWater::max)
        ).apply(instance, BiomeWater::new));
    }

    public record DimensionWater(Optional<Integer> base, Optional<Float> thirstMultiplier)
    {
        public static final Codec<DimensionWater> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.intRange(WaterPurity.MIN_PURITY, WaterPurity.MAX_PURITY).optionalFieldOf("base").forGetter(DimensionWater::base),
                Codec.floatRange(0.0F, 10.0F).optionalFieldOf("thirst_multiplier").forGetter(DimensionWater::thirstMultiplier)
        ).apply(instance, DimensionWater::new));
    }

    public record DrinkValues(int thirst, int quenched, Optional<Integer> purity)
    {
        public static final Codec<DrinkValues> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.intRange(-ThirstConstants.MAX_THIRST, ThirstConstants.MAX_THIRST).fieldOf("thirst").forGetter(DrinkValues::thirst),
                Codec.intRange(-ThirstConstants.MAX_THIRST, Integer.MAX_VALUE).fieldOf("quenched").forGetter(DrinkValues::quenched),
                Codec.intRange(WaterPurity.MIN_PURITY, WaterPurity.MAX_PURITY).optionalFieldOf("purity").forGetter(DrinkValues::purity)
        ).apply(instance, DrinkValues::new));
    }
}
