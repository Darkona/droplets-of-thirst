package com.darkona.dropletsofthirst.gametest;

import com.darkona.dropletsofthirst.DropletsOfThirst;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterGameTestsEvent;

@EventBusSubscriber(modid = DropletsOfThirst.ID, bus = EventBusSubscriber.Bus.MOD)
public final class DropletsGameTests
{
    private DropletsGameTests() {}

    @SubscribeEvent
    public static void register(RegisterGameTestsEvent event)
    {
        if (ModList.get().isLoaded("create"))
            event.register(CreateTests.class);
        if (ModList.get().isLoaded("travelersbackpack"))
            event.register(TravelersBackpackTests.class);
        if (ModList.get().isLoaded("cold_sweat"))
            event.register(ColdSweatTests.class);
        if (ModList.get().isLoaded("reliquary"))
            event.register(ReliquaryTests.class);
        if (ModList.get().isLoaded("sereneseasons"))
            event.register(SereneSeasonsTests.class);
        if (ModList.get().isLoaded("kubejs"))
            event.register(KubeJSTests.class);
        if (ModList.get().isLoaded("extradelight") && ModList.get().isLoaded("farm_and_charm") && ModList.get().isLoaded("herbalbrews") && ModList.get().isLoaded("brewery"))
            event.register(DelightTests.class);
        if (ModList.get().isLoaded("minersdelight"))
            event.register(MinersDelightTests.class);
        if (ModList.get().isLoaded("minersdelight") && ModList.get().isLoaded("create"))
            event.register(MinersDelightCreateTests.class);
        if (ModList.get().isLoaded("culturaldelights"))
            event.register(CulturalDelightsTests.class);
        if (ModList.get().isLoaded("vampirism"))
            event.register(VampirismTests.class);
    }
}
