package com.darkona.dropletsofthirst.gametest;

import net.minecraftforge.fml.ModList;
import net.minecraftforge.event.RegisterGameTestsEvent;

public final class DropletsGameTests
{
    private DropletsGameTests() {}

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
        if (ModList.get().isLoaded("farm_and_charm") && ModList.get().isLoaded("herbalbrews") && ModList.get().isLoaded("brewery"))
            event.register(DelightTests.class);
        if (ModList.get().isLoaded("miners_delight"))
            event.register(MinersDelightTests.class);
        if (ModList.get().isLoaded("miners_delight") && ModList.get().isLoaded("create"))
            event.register(MinersDelightCreateTests.class);
        if (ModList.get().isLoaded("vampirism"))
            event.register(VampirismTests.class);
        if (ModList.get().isLoaded("supplementaries"))
            event.register(SupplementariesTests.class);
        if (ModList.get().isLoaded("mekanism"))
            event.register(MekanismTests.class);
        if (ModList.get().isLoaded("immersiveengineering"))
            event.register(ImmersiveEngineeringTests.class);
    }
}
