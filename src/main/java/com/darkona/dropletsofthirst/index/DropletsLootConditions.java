package com.darkona.dropletsofthirst.index;

import com.darkona.dropletsofthirst.DropletsOfThirst;
import com.darkona.dropletsofthirst.purity.PurityEnabledLootCondition;
import java.util.function.Supplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditionType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class DropletsLootConditions
{
    private static final DeferredRegister<LootItemConditionType> LOOT_CONDITIONS = DeferredRegister.create(Registries.LOOT_CONDITION_TYPE, DropletsOfThirst.ID);

    public static final Supplier<LootItemConditionType> PURITY_ENABLED = LOOT_CONDITIONS.register("purity_enabled", () -> new LootItemConditionType(PurityEnabledLootCondition.CODEC));

    private DropletsLootConditions() {}

    public static void register(IEventBus modBus)
    {
        LOOT_CONDITIONS.register(modBus);
    }
}
