package com.darkona.dropletsofthirst.index;

import com.darkona.dropletsofthirst.DropletsOfThirst;
import com.darkona.dropletsofthirst.content.loot.AddTableModifier;
import com.darkona.dropletsofthirst.content.loot.OptionalItemEntry;
import com.mojang.serialization.Codec;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryType;
import net.minecraftforge.common.loot.IGlobalLootModifier;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class DropletsLoot
{
    private static final DeferredRegister<Codec<? extends IGlobalLootModifier>> MODIFIERS = DeferredRegister.create(ForgeRegistries.Keys.GLOBAL_LOOT_MODIFIER_SERIALIZERS, DropletsOfThirst.ID);
    private static final DeferredRegister<LootPoolEntryType> ENTRIES = DeferredRegister.create(Registries.LOOT_POOL_ENTRY_TYPE, DropletsOfThirst.ID);

    public static final RegistryObject<Codec<AddTableModifier>> ADD_TABLE = MODIFIERS.register("add_table", () -> AddTableModifier.CODEC);
    public static final RegistryObject<LootPoolEntryType> OPTIONAL_ITEM = ENTRIES.register("optional_item", () -> new LootPoolEntryType(new OptionalItemEntry.Serializer()));

    private DropletsLoot() {}

    public static void register(IEventBus modBus)
    {
        MODIFIERS.register(modBus);
        ENTRIES.register(modBus);
    }
}
