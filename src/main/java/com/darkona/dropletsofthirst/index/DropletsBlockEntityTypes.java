package com.darkona.dropletsofthirst.index;

import com.darkona.dropletsofthirst.DropletsOfThirst;
import com.darkona.dropletsofthirst.content.purity.AmphoraBlockEntity;
import com.darkona.dropletsofthirst.content.purity.CharcoalFilterBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class DropletsBlockEntityTypes
{
    private static final DeferredRegister<BlockEntityType<?>> TYPES = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, DropletsOfThirst.ID);

    public static final RegistryObject<BlockEntityType<AmphoraBlockEntity>> AMPHORA = TYPES.register("amphora",
            () -> BlockEntityType.Builder.of(AmphoraBlockEntity::new, DropletsBlocks.AMPHORA.get()).build(null));

    public static final RegistryObject<BlockEntityType<CharcoalFilterBlockEntity>> CHARCOAL_FILTER = TYPES.register("charcoal_filter",
            () -> BlockEntityType.Builder.of(CharcoalFilterBlockEntity::new, DropletsBlocks.CHARCOAL_FILTER.get()).build(null));

    private DropletsBlockEntityTypes() {}

    public static void register(IEventBus modBus)
    {
        TYPES.register(modBus);
    }
}
