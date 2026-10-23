package com.darkona.dropletsofthirst.purity;

import com.darkona.dropletsofthirst.DropletsOfThirst;
import com.darkona.dropletsofthirst.item.ItemInit;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;

@EventBusSubscriber(modid = DropletsOfThirst.ID, value = Dist.CLIENT)
public final class PurityTintClient
{
    @SubscribeEvent
    public static void registerItemColors(RegisterColorHandlersEvent.Item event)
    {
        event.register((stack, tintIndex) -> tintIndex == 1 ? PurityTint.VANILLA_WATER : -1, ItemInit.TERRACOTTA_WATER_BOWL.get());
    }

    private PurityTintClient() {}
}
