package com.darkona.dropletsofthirst;

import com.darkona.dropletsofthirst.compat.create.CreateCompat;
import com.darkona.dropletsofthirst.compat.create.ponder.ThirstPonderPlugin;
import com.darkona.dropletsofthirst.compat.supernatural.SupernaturalCompat;
import com.darkona.dropletsofthirst.compat.vampirism.VampirismCompat;
import com.darkona.dropletsofthirst.content.purity.PurityTint;
import com.darkona.dropletsofthirst.content.thirst.DrinkTooltip;
import com.darkona.dropletsofthirst.content.thirst.PlayerThirstManager;
import com.darkona.dropletsofthirst.content.thirst.ThirstBarRenderer;
import com.darkona.dropletsofthirst.content.thirst.ThirstBarStyles;
import com.darkona.dropletsofthirst.index.DropletsItems;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.RegisterClientTooltipComponentFactoriesEvent;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;
import net.neoforged.neoforge.common.NeoForge;

@Mod(value = DropletsOfThirst.ID, dist = Dist.CLIENT)
public class DropletsClient
{
    public DropletsClient(IEventBus modBus)
    {
        ThirstBarRenderer.registerLayer();
        ThirstBarStyles.registerBuiltIns();
        VampirismCompat.initClient();
        SupernaturalCompat.initClient();
        modBus.addListener(DropletsClient::registerTooltipComponents);
        modBus.addListener(DropletsClient::registerItemColors);
        modBus.addListener(DropletsClient::clientSetup);
        NeoForge.EVENT_BUS.addListener(EventPriority.LOW, DrinkTooltip::gather);
        NeoForge.EVENT_BUS.addListener(DrinkTooltip::renderPurityTooltip);
        NeoForge.EVENT_BUS.addListener(PlayerThirstManager::estimatedTooltip);
    }

    private static void registerTooltipComponents(RegisterClientTooltipComponentFactoriesEvent event)
    {
        event.register(DrinkTooltip.class, tooltip -> tooltip);
    }

    private static void registerItemColors(RegisterColorHandlersEvent.Item event)
    {
        event.register((stack, tintIndex) -> tintIndex == 1 ? PurityTint.VANILLA_WATER : -1, DropletsItems.TERRACOTTA_WATER_BOWL.get());
    }

    private static void clientSetup(FMLClientSetupEvent event)
    {
        if (CreateCompat.LOADED)
        {
            event.enqueueWork(() -> ThirstPonderPlugin.register());
        }
    }
}
