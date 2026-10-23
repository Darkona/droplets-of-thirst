package com.darkona.dropletsofthirst.purity;

import com.darkona.dropletsofthirst.DropletsOfThirst;
import com.mojang.serialization.MapCodec;
import java.util.function.Supplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditionType;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.jetbrains.annotations.NotNull;

public record PurityEnabledLootCondition() implements LootItemCondition
{
    public static final DeferredRegister<LootItemConditionType> LOOT_CONDITIONS = DeferredRegister.create(Registries.LOOT_CONDITION_TYPE, DropletsOfThirst.ID);

    public static final Supplier<LootItemConditionType> TYPE = LOOT_CONDITIONS.register("purity_enabled", () -> new LootItemConditionType(PurityEnabledLootCondition.CODEC));

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
        return TYPE.get();
    }
}
