package com.darkona.dropletsofthirst.purity;

import com.darkona.dropletsofthirst.index.DropletsLootConditions;
import com.mojang.serialization.MapCodec;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditionType;
import org.jetbrains.annotations.NotNull;

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
        return DropletsLootConditions.PURITY_ENABLED.get();
    }
}
