package com.darkona.dropletsofthirst.config;

import com.darkona.dropletsofthirst.content.purity.WaterPurity;
import com.darkona.dropletsofthirst.index.DropletsConditions;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.Serializer;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditionType;
import org.jetbrains.annotations.NotNull;

public final class PurityEnabledLootCondition implements LootItemCondition
{
    public static final PurityEnabledLootCondition INSTANCE = new PurityEnabledLootCondition();
    public static final Serializer<PurityEnabledLootCondition> SERIALIZER = new Serializer<>()
    {
        @Override
        public void serialize(JsonObject json, PurityEnabledLootCondition value, JsonSerializationContext context) {}

        @Override
        public PurityEnabledLootCondition deserialize(JsonObject json, JsonDeserializationContext context)
        {
            return INSTANCE;
        }
    };

    private PurityEnabledLootCondition() {}

    @Override
    public boolean test(LootContext context)
    {
        return WaterPurity.enabled();
    }

    @Override
    public @NotNull LootItemConditionType getType()
    {
        return DropletsConditions.PURITY_ENABLED_LOOT_CONDITION.get();
    }
}
