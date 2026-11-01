package com.darkona.dropletsofthirst.index;

import com.darkona.dropletsofthirst.DropletsOfThirst;
import com.darkona.dropletsofthirst.config.LootConfigCondition;
import com.darkona.dropletsofthirst.config.LootConfigLootCondition;
import com.darkona.dropletsofthirst.config.PurityEnabledCondition;
import com.darkona.dropletsofthirst.config.PurityEnabledLootCondition;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditionType;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public class DropletsConditions {
    public static final DeferredRegister<LootItemConditionType> LOOT_CONDITIONS = DeferredRegister.create(Registries.LOOT_CONDITION_TYPE, DropletsOfThirst.ID);

    public static final RegistryObject<LootItemConditionType> PURITY_ENABLED_LOOT_CONDITION = LOOT_CONDITIONS.register("purity_enabled", () -> new LootItemConditionType(PurityEnabledLootCondition.SERIALIZER));
    public static final RegistryObject<LootItemConditionType> LOOT_CONFIG_LOOT_CONDITION = LOOT_CONDITIONS.register("loot_config", () -> new LootItemConditionType(LootConfigLootCondition.SERIALIZER));

    public static void register(IEventBus modBus) {
        CraftingHelper.register(LootConfigCondition.SERIALIZER);
        CraftingHelper.register(PurityEnabledCondition.SERIALIZER);
        LOOT_CONDITIONS.register(modBus);
    }
}
