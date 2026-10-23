package com.darkona.dropletsofthirst.client;

import com.darkona.dropletsofthirst.DropletsOfThirst;
import com.darkona.dropletsofthirst.item.ItemInit;
import com.darkona.dropletsofthirst.network.DrinkByHandMessage;
import com.darkona.dropletsofthirst.purity.PurityTint;
import com.darkona.dropletsofthirst.purity.WaterPurity;
import com.darkona.dropletsofthirst.thirst.ModAttachment;
import com.darkona.dropletsofthirst.thirst.ThirstHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.level.ClipContext;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;
import net.neoforged.neoforge.network.PacketDistributor;

@EventBusSubscriber(modid = DropletsOfThirst.ID, value = Dist.CLIENT)
public final class ClientEvents
{
    private ClientEvents() {}

    @SubscribeEvent
    public static void registerItemColors(RegisterColorHandlersEvent.Item event)
    {
        event.register((stack, tintIndex) -> tintIndex == 1 ? PurityTint.VANILLA_WATER : -1, ItemInit.TERRACOTTA_WATER_BOWL.get());
    }

    @SubscribeEvent
    public static void onLoggingOut(ClientPlayerNetworkEvent.LoggingOut event)
    {
        if (ThirstHelper.hasServerTables())
            ThirstHelper.clearServerTables();
    }

    public static void drinkByHand()
    {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null || !player.isCrouching() || !player.getMainHandItem().isEmpty()
                || player.getData(ModAttachment.PLAYER_THIRST).needsBothHandsToDrink() && !player.getOffhandItem().isEmpty())
            return;
        if (player.level().getFluidState(WaterPurity.pickFluid(player, ClipContext.Fluid.ANY).getBlockPos()).is(FluidTags.WATER))
            PacketDistributor.sendToServer(DrinkByHandMessage.INSTANCE);
    }
}
