package com.darkona.dropletsofthirst.index;

import com.darkona.dropletsofthirst.content.thirst.DrinkValues;
import com.darkona.dropletsofthirst.content.purity.BiomeWater;
import com.darkona.dropletsofthirst.content.purity.DimensionWater;
import com.darkona.dropletsofthirst.core.DataMapType;
import com.darkona.dropletsofthirst.DropletsOfThirst;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.event.TagsUpdatedEvent;

import java.util.List;

public final class DropletsDataMaps
{

    public static final DataMapType<Item, DrinkValues> DRINKS = DataMapType
            .create(DropletsOfThirst.asResource("drinks"), Registries.ITEM, DrinkValues.CODEC);

    public static final DataMapType<Fluid, Item> DRINK_FLUIDS = DataMapType
            .createOptional(DropletsOfThirst.asResource("drink_fluids"), Registries.FLUID, BuiltInRegistries.ITEM.byNameCodec().fieldOf("item").codec());

    public static final DataMapType<Block, DrinkValues> HYDRATING_BLOCKS = DataMapType
            .create(DropletsOfThirst.asResource("hydrating_blocks"), Registries.BLOCK, DrinkValues.CODEC);

    public static final DataMapType<Biome, BiomeWater> BIOME_WATER = DataMapType
            .create(DropletsOfThirst.asResource("biome_water"), Registries.BIOME, BiomeWater.CODEC);

    public static final DataMapType<DimensionType, DimensionWater> DIMENSION_WATER = DataMapType
            .create(DropletsOfThirst.asResource("dimension_water"), Registries.DIMENSION_TYPE, DimensionWater.CODEC);

    private static final List<DataMapType<?, ?>> ALL = List.of(DRINKS, DRINK_FLUIDS, HYDRATING_BLOCKS, BIOME_WATER, DIMENSION_WATER);

    private DropletsDataMaps() {}

    public static void addReloadListeners(AddReloadListenerEvent event)
    {
        for (DataMapType<?, ?> type : ALL)
            event.addListener(type.reloadListener(event.getConditionContext()));
    }

    public static void bind(TagsUpdatedEvent event)
    {
        if (event.getUpdateCause() != TagsUpdatedEvent.UpdateCause.SERVER_DATA_LOAD)
            return;
        for (DataMapType<?, ?> type : ALL)
            type.bind(event.getRegistryAccess());
    }
}
