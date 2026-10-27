package com.darkona.dropletsofthirst.index;

import com.darkona.dropletsofthirst.config.ConfigCondition;
import com.darkona.dropletsofthirst.config.PurityConfig;
import com.darkona.dropletsofthirst.config.GameplayConfig;
import com.darkona.dropletsofthirst.DropletsOfThirst;
import com.mojang.serialization.MapCodec;
import java.util.function.Supplier;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.conditions.ICondition;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

public class DropletsConditions {
    private static final DeferredRegister<MapCodec<? extends ICondition>> CONDITION_CODECS = DeferredRegister.create(NeoForgeRegistries.Keys.CONDITION_CODECS, DropletsOfThirst.ID);

    public static final Supplier<MapCodec<ConfigCondition>> LOOT_CONFIG_CONDITION = CONDITION_CODECS.register("loot_config", () -> ConfigCondition.codec(() -> GameplayConfig.LOOT.get()));
    public static final Supplier<MapCodec<ConfigCondition>> PURITY_ENABLED_CONDITION = CONDITION_CODECS.register("purity_enabled", () -> ConfigCondition.codec(() -> PurityConfig.ENABLED.get()));

    public static void register(IEventBus modBus) {
        CONDITION_CODECS.register(modBus);
    }
}
