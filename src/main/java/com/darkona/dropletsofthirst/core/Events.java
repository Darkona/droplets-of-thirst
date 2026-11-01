package com.darkona.dropletsofthirst.core;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.Event;

public final class Events
{
    private Events() {}

    public static <E extends Event> E post(E event)
    {
        MinecraftForge.EVENT_BUS.post(event);
        return event;
    }
}
