package com.darkona.dropletsofthirst.config;

import com.darkona.dropletsofthirst.DropletsOfThirst;
import com.mojang.serialization.MapCodec;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;
import net.neoforged.neoforge.common.conditions.ICondition;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import org.jetbrains.annotations.NotNull;

public class ConditionInit {
    public static final DeferredRegister<MapCodec<? extends ICondition>> CONDITION_CODECS = DeferredRegister.create(NeoForgeRegistries.Keys.CONDITION_CODECS, DropletsOfThirst.ID);

    public static final Supplier<MapCodec<ConfigCondition>> LOOT_CONFIG_CONDITION = CONDITION_CODECS.register("loot_config", () -> ConfigCondition.codec(() -> GameplayConfig.LOOT.get()));
    public static final Supplier<MapCodec<ConfigCondition>> PURITY_ENABLED_CONDITION = CONDITION_CODECS.register("purity_enabled", () -> ConfigCondition.codec(() -> PurityConfig.ENABLED.get()));

    public record ConfigCondition(BooleanSupplier flag) implements ICondition
    {
        private static final Map<BooleanSupplier, MapCodec<ConfigCondition>> CODECS = new HashMap<>();

        public static MapCodec<ConfigCondition> codec(BooleanSupplier flag)
        {
            return CODECS.computeIfAbsent(flag, key -> MapCodec.unit(new ConfigCondition(key)).stable());
        }

        @Override
        public boolean test(ICondition.@NotNull IContext context)
        {
            return flag.getAsBoolean();
        }

        @Override
        public @NotNull MapCodec<? extends ICondition> codec()
        {
            return CODECS.get(flag);
        }
    }
}
