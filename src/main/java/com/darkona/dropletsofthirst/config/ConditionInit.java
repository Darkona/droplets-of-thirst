package com.darkona.dropletsofthirst.config;

import com.darkona.dropletsofthirst.DropletsOfThirst;
import com.darkona.dropletsofthirst.purity.WaterPurity;
import com.mojang.serialization.MapCodec;
import java.util.function.Supplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditionType;
import net.neoforged.neoforge.common.conditions.ICondition;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import org.jetbrains.annotations.NotNull;

public class ConditionInit {
    public static final DeferredRegister<MapCodec<? extends ICondition>> CONDITION_CODECS = DeferredRegister.create(NeoForgeRegistries.Keys.CONDITION_CODECS, DropletsOfThirst.ID);

    public static final Supplier<MapCodec<LootConfigCondition>> LOOT_CONFIG_CONDITION = CONDITION_CODECS.register("loot_config", () -> LootConfigCondition.CODEC);
    public static final Supplier<MapCodec<PurityEnabledCondition>> PURITY_ENABLED_CONDITION = CONDITION_CODECS.register("purity_enabled", () -> PurityEnabledCondition.CODEC);

    public static final DeferredRegister<LootItemConditionType> LOOT_CONDITIONS = DeferredRegister.create(Registries.LOOT_CONDITION_TYPE, DropletsOfThirst.ID);

    public static final Supplier<LootItemConditionType> PURITY_ENABLED_LOOT_CONDITION = LOOT_CONDITIONS.register("purity_enabled", () -> new LootItemConditionType(PurityEnabledLootCondition.CODEC));

    public record LootConfigCondition() implements ICondition
    {
        public static final LootConfigCondition INSTANCE = new LootConfigCondition();
        public static final MapCodec<LootConfigCondition> CODEC = MapCodec.unit(INSTANCE).stable();

        @Override
        public boolean test(ICondition.@NotNull IContext context) {
            return GameplayConfig.LOOT.get();
        }

        @Override
        public @NotNull MapCodec<? extends ICondition> codec() {
            return CODEC;
        }
    }

    public record PurityEnabledCondition() implements ICondition
    {
        public static final PurityEnabledCondition INSTANCE = new PurityEnabledCondition();
        public static final MapCodec<PurityEnabledCondition> CODEC = MapCodec.unit(INSTANCE).stable();

        @Override
        public boolean test(ICondition.@NotNull IContext context)
        {
            return PurityConfig.ENABLED.get();
        }

        @Override
        public @NotNull MapCodec<? extends ICondition> codec()
        {
            return CODEC;
        }
    }

    public record PurityEnabledLootCondition() implements LootItemCondition
    {
        public static final PurityEnabledLootCondition INSTANCE = new PurityEnabledLootCondition();
        public static final MapCodec<PurityEnabledLootCondition> CODEC = MapCodec.unit(INSTANCE);

        @Override
        public boolean test(LootContext context)
        {
            return WaterPurity.enabled();
        }

        @Override
        public @NotNull LootItemConditionType getType()
        {
            return ConditionInit.PURITY_ENABLED_LOOT_CONDITION.get();
        }
    }
}
