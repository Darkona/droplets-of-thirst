package com.darkona.dropletsofthirst.config;

import com.darkona.dropletsofthirst.DropletsOfThirst;
import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.crafting.conditions.ICondition;
import net.minecraftforge.common.crafting.conditions.IConditionSerializer;

public final class LootConfigCondition implements ICondition
{
    public static final ResourceLocation ID = DropletsOfThirst.asResource("loot_config");
    public static final LootConfigCondition INSTANCE = new LootConfigCondition();
    public static final IConditionSerializer<LootConfigCondition> SERIALIZER = new IConditionSerializer<>()
    {
        @Override
        public void write(JsonObject json, LootConfigCondition value) {}

        @Override
        public LootConfigCondition read(JsonObject json)
        {
            return INSTANCE;
        }

        @Override
        public ResourceLocation getID()
        {
            return ID;
        }
    };

    private LootConfigCondition() {}

    @Override
    public ResourceLocation getID()
    {
        return ID;
    }

    @Override
    public boolean test(IContext context)
    {
        return GameplayConfig.LOOT.get();
    }
}
