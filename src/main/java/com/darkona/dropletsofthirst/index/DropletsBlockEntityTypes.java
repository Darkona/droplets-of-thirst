package com.darkona.dropletsofthirst.index;

import com.darkona.dropletsofthirst.DropletsOfThirst;
import com.darkona.dropletsofthirst.content.purity.AmphoraBlockEntity;
import com.darkona.dropletsofthirst.content.purity.CharcoalFilterBlockEntity;
import java.util.function.Supplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class DropletsBlockEntityTypes
{
    private static final DeferredRegister<BlockEntityType<?>> TYPES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, DropletsOfThirst.ID);

    public static final Supplier<BlockEntityType<AmphoraBlockEntity>> AMPHORA = TYPES.register("amphora",
            () -> BlockEntityType.Builder.of(AmphoraBlockEntity::new, DropletsBlocks.AMPHORA.get()).build(null));

    public static final Supplier<BlockEntityType<CharcoalFilterBlockEntity>> CHARCOAL_FILTER = TYPES.register("charcoal_filter",
            () -> BlockEntityType.Builder.of(CharcoalFilterBlockEntity::new, DropletsBlocks.CHARCOAL_FILTER.get()).build(null));

    private DropletsBlockEntityTypes() {}

    public static void register(IEventBus modBus)
    {
        TYPES.register(modBus);
        modBus.addListener(DropletsBlockEntityTypes::registerCapabilities);
    }

    private static void registerCapabilities(RegisterCapabilitiesEvent event)
    {
        event.registerBlockEntity(Capabilities.FluidHandler.BLOCK, AMPHORA.get(), (amphora, side) -> amphora.tank());
        event.registerBlockEntity(Capabilities.FluidHandler.BLOCK, CHARCOAL_FILTER.get(), (filter, side) -> filter);
    }
}
