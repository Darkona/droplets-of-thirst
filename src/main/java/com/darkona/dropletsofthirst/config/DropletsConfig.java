package com.darkona.dropletsofthirst.config;

import com.darkona.dropletsofthirst.DropletsOfThirst;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.config.ModConfig;

public final class DropletsConfig
{
    private DropletsConfig() {}

    public static void register(ModContainer modContainer)
    {
        modContainer.registerConfig(ModConfig.Type.COMMON, GameplayConfig.SPEC, DropletsOfThirst.ID + "/gameplay.toml");
        modContainer.registerConfig(ModConfig.Type.COMMON, PurityConfig.SPEC, DropletsOfThirst.ID + "/purity.toml");
        modContainer.registerConfig(ModConfig.Type.COMMON, ItemsConfig.SPEC, DropletsOfThirst.ID + "/items.toml");
        modContainer.registerConfig(ModConfig.Type.COMMON, CompatConfig.SPEC, DropletsOfThirst.ID + "/compat.toml");
        modContainer.registerConfig(ModConfig.Type.CLIENT, ClientConfig.SPEC, DropletsOfThirst.ID + "/client.toml");
    }
}
