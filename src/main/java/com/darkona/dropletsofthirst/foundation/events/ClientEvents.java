package com.darkona.dropletsofthirst.foundation.events;

import com.darkona.dropletsofthirst.DropletsOfThirst;
import com.darkona.dropletsofthirst.network.DrinkByHandMessage;
import com.darkona.dropletsofthirst.index.DropletsAttachmentTypes;
import com.darkona.dropletsofthirst.content.thirst.ThirstHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.level.ClipContext;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.network.PacketDistributor;

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
        if (event.getHand() == InteractionHand.MAIN_HAND && event.getEntity().level().isClientSide && event.getEntity().getData(DropletsAttachmentTypes.PLAYER_THIRST).handDrinkingAllowed())
            drinkByHand();
    }

    private static void drinkByHand()
    {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null || !player.isCrouching() || !player.getMainHandItem().isEmpty()
                || player.getData(DropletsAttachmentTypes.PLAYER_THIRST).needsBothHandsToDrink() && !player.getOffhandItem().isEmpty())
            return;
        if (player.level().getFluidState(DrinkByHandMessage.pickFluid(player, ClipContext.Fluid.ANY).getBlockPos()).is(FluidTags.WATER))
            PacketDistributor.sendToServer(DrinkByHandMessage.INSTANCE);
    }
}
