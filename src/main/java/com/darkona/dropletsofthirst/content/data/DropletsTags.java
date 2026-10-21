package com.darkona.dropletsofthirst.content.data;

import com.darkona.dropletsofthirst.DropletsOfThirst;
import com.darkona.dropletsofthirst.api.PurityLevel;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.Fluid;

public final class DropletsTags
{
    public static final TagKey<Biome>[] WATER_PURITY = waterPurityTags();
    public static final TagKey<Biome> SALT_WATER = TagKey.create(Registries.BIOME, DropletsOfThirst.asResource("salt_water"));

    public static final TagKey<Fluid> CARRIES_PURITY = TagKey.create(Registries.FLUID, DropletsOfThirst.asResource("carries_purity"));
    public static final TagKey<Item> PURITY_OPT_OUT = TagKey.create(Registries.ITEM, DropletsOfThirst.asResource("purity_opt_out"));
    public static final TagKey<Item> PURITY_CONTAINERS = TagKey.create(Registries.ITEM, DropletsOfThirst.asResource("purity_containers"));
    public static final TagKey<Block> CAULDRON_HEAT_SOURCES = TagKey.create(Registries.BLOCK, DropletsOfThirst.asResource("cauldron_heat_sources"));
    public static final TagKey<Block> REJECTS_DIRTY_WATER = TagKey.create(Registries.BLOCK, DropletsOfThirst.asResource("rejects_dirty_water"));
    public static final TagKey<Item> NO_THIRST = TagKey.create(Registries.ITEM, DropletsOfThirst.asResource("no_thirst"));
    public static final TagKey<Item> SALTY = TagKey.create(Registries.ITEM, DropletsOfThirst.asResource("salty"));
    public static final TagKey<Item> BLOOD = TagKey.create(Registries.ITEM, DropletsOfThirst.asResource("blood"));

    public static final TagKey<MobEffect> PAUSES_THIRST = TagKey.create(Registries.MOB_EFFECT, DropletsOfThirst.asResource("pauses_thirst"));
    public static final TagKey<MobEffect> STOPS_THIRST = TagKey.create(Registries.MOB_EFFECT, DropletsOfThirst.asResource("stops_thirst"));

    private DropletsTags() {}

    @SuppressWarnings("unchecked")
    private static TagKey<Biome>[] waterPurityTags()
    {
        TagKey<Biome>[] tags = new TagKey[PurityLevel.MAX + 1];
        for (int purity = 0; purity < tags.length; purity++)
            tags[purity] = TagKey.create(Registries.BIOME, DropletsOfThirst.asResource("water_purity/" + purity));
        return tags;
    }
}
