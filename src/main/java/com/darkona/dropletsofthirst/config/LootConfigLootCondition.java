package com.darkona.dropletsofthirst.config;

import com.darkona.dropletsofthirst.index.DropletsConditions;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.Serializer;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditionType;
import org.jetbrains.annotations.NotNull;

public final class LootConfigLootCondition implements LootItemCondition
{
    public static final LootConfigLootCondition INSTANCE = new LootConfigLootCondition();
    public static final Serializer<LootConfigLootCondition> SERIALIZER = new Serializer<>()
    {
        @Override
        public void serialize(JsonObject json, LootConfigLootCondition value, JsonSerializationContext context) {}

        @Override
        public LootConfigLootCondition deserialize(JsonObject json, JsonDeserializationContext context)
        {
            return INSTANCE;
        }
    };

    private LootConfigLootCondition() {}

    @Override
    public boolean test(LootContext context)
    {
        return GameplayConfig.LOOT.get();
    }

    @Override
    public @NotNull LootItemConditionType getType()
    {
        return DropletsConditions.LOOT_CONFIG_LOOT_CONDITION.get();
    }
}
