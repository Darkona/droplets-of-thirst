package com.darkona.dropletsofthirst.thirst;

import com.darkona.dropletsofthirst.purity.WaterPurity;
import com.darkona.dropletsofthirst.foundation.network.message.DrinkByHandMessage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.level.ClipContext;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.network.PacketDistributor;

@OnlyIn(Dist.CLIENT)
public class DrinkByHandClient
{
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
