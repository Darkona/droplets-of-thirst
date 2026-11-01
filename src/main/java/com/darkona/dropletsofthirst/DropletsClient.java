package com.darkona.dropletsofthirst;

import com.darkona.dropletsofthirst.compat.create.CreateCompat;
import com.darkona.dropletsofthirst.compat.create.ponder.ThirstPonderPlugin;
import com.darkona.dropletsofthirst.compat.supernatural.SupernaturalCompat;
import com.darkona.dropletsofthirst.compat.vampirism.VampirismCompat;
import com.darkona.dropletsofthirst.content.purity.PurityTint;
import com.darkona.dropletsofthirst.content.thirst.PlayerThirstManager;
import com.darkona.dropletsofthirst.content.thirst.ThirstBarStyles;
import com.darkona.dropletsofthirst.content.thirst.client.DrinkTooltip;
import com.darkona.dropletsofthirst.content.thirst.client.ThirstBarRenderer;
import com.darkona.dropletsofthirst.index.DropletsItems;
import net.minecraft.world.item.Items;
import net.minecraftforge.client.event.RegisterClientTooltipComponentFactoriesEvent;
import net.minecraftforge.client.event.RegisterColorHandlersEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

public final class DropletsClient
{
    private DropletsClient() {}

    public static void init(IEventBus modBus)
    {
        modBus.addListener(ThirstBarRenderer::registerLayer);
        ThirstBarStyles.registerBuiltIns();
        VampirismCompat.initClient();
        SupernaturalCompat.initClient();
        modBus.addListener(DropletsClient::registerTooltipComponents);
        modBus.addListener(DropletsClient::registerItemColors);
        modBus.addListener(DropletsClient::clientSetup);
        MinecraftForge.EVENT_BUS.addListener(EventPriority.LOW, DrinkTooltip::gather);
        MinecraftForge.EVENT_BUS.addListener(DrinkTooltip::renderPurityTooltip);
        MinecraftForge.EVENT_BUS.addListener(PlayerThirstManager::estimatedTooltip);
    }

    private static void registerTooltipComponents(RegisterClientTooltipComponentFactoriesEvent event)
    {
        event.register(DrinkTooltip.class, tooltip -> tooltip);
    }

    private static void registerItemColors(RegisterColorHandlersEvent.Item event)
    {
        event.register((stack, tintIndex) -> tintIndex == 1 ? PurityTint.VANILLA_WATER : -1, DropletsItems.TERRACOTTA_WATER_BOWL.get(), Items.WATER_BUCKET);
    }

    private static void clientSetup(FMLClientSetupEvent event)
    {
        if (CreateCompat.LOADED)
            event.enqueueWork(() -> ThirstPonderPlugin.register());
    }
}
