package com.darkona.dropletsofthirst.config;

import com.darkona.dropletsofthirst.DropletsOfThirst;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;

public final class DropletsConfig
{
    private DropletsConfig() {}

    public static void register()
    {
        ModLoadingContext context = ModLoadingContext.get();
        context.registerConfig(ModConfig.Type.COMMON, GameplayConfig.SPEC, DropletsOfThirst.ID + "/gameplay.toml");
        context.registerConfig(ModConfig.Type.COMMON, PurityConfig.SPEC, DropletsOfThirst.ID + "/purity.toml");
        context.registerConfig(ModConfig.Type.COMMON, ItemsConfig.SPEC, DropletsOfThirst.ID + "/items.toml");
        context.registerConfig(ModConfig.Type.COMMON, CompatConfig.SPEC, DropletsOfThirst.ID + "/compat.toml");
        context.registerConfig(ModConfig.Type.CLIENT, ClientConfig.SPEC, DropletsOfThirst.ID + "/client.toml");
    }
}
