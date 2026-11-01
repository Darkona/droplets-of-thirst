package com.darkona.dropletsofthirst.compat.travelersbackpack;

import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.event.lifecycle.FMLLoadCompleteEvent;

public final class TravelersBackpackCompat
{
    public static final boolean LOADED = ModList.get().isLoaded("travelersbackpack");

    private TravelersBackpackCompat() {}

    public static void init(IEventBus modBus)
    {
        if (LOADED)
            modBus.addListener(TravelersBackpackCompat::register);
    }

    private static void register(FMLLoadCompleteEvent event)
    {
        event.enqueueWork(HoseWaterEffect::register);
    }
}
