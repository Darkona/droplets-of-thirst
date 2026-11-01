package com.darkona.dropletsofthirst.core.events.client;

import com.darkona.dropletsofthirst.DropletsOfThirst;
import com.darkona.dropletsofthirst.content.thirst.ThirstHelper;
import com.darkona.dropletsofthirst.index.DropletsCapabilities;
import com.darkona.dropletsofthirst.network.DrinkByHandMessage;
import com.darkona.dropletsofthirst.network.DropletsPackets;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.level.ClipContext;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

@EventBusSubscriber(modid = DropletsOfThirst.ID, value = Dist.CLIENT)
public final class ClientEvents
{
    private ClientEvents() {}

    @SubscribeEvent
    public static void onLoggingOut(ClientPlayerNetworkEvent.LoggingOut event)
    {
        if (ThirstHelper.hasServerTables())
            ThirstHelper.clearServerTables();
    }

    @SubscribeEvent
    public static void drinkByHand(PlayerInteractEvent.RightClickBlock event)
    {
        drinkByHandIfAllowed(event);
    }

    @SubscribeEvent
    public static void drinkByHand(PlayerInteractEvent.RightClickEmpty event)
    {
        drinkByHandIfAllowed(event);
    }

    private static void drinkByHandIfAllowed(PlayerInteractEvent event)
    {
        if (event.getHand() == InteractionHand.MAIN_HAND && event.getEntity().level().isClientSide && DropletsCapabilities.thirst(event.getEntity()).handDrinkingAllowed())
            drinkByHand();
    }

    private static void drinkByHand()
    {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null || !player.isCrouching() || !player.getMainHandItem().isEmpty()
                || DropletsCapabilities.thirst(player).needsBothHandsToDrink() && !player.getOffhandItem().isEmpty())
            return;
        if (player.level().getFluidState(DrinkByHandMessage.pickFluid(player, ClipContext.Fluid.ANY).getBlockPos()).is(FluidTags.WATER))
            DropletsPackets.sendToServer(DrinkByHandMessage.INSTANCE);
    }
}
