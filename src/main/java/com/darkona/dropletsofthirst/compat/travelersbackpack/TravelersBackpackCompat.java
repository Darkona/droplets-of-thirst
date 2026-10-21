package com.darkona.dropletsofthirst.compat.travelersbackpack;

import net.neoforged.bus.api.IEventBus;
import dev.darkona.jdlib.api.compat.Bridges;
import net.neoforged.fml.event.lifecycle.FMLLoadCompleteEvent;

public final class TravelersBackpackCompat
{
    public static final boolean LOADED = Bridges.isLoaded("travelersbackpack");

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
