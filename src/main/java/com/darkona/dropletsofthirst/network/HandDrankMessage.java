package com.darkona.dropletsofthirst.network;

import com.darkona.dropletsofthirst.network.client.ClientPayloadHandlers;
import java.util.function.Supplier;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

public record HandDrankMessage(int playerId)
{
    public void encode(FriendlyByteBuf buffer)
    {
        buffer.writeVarInt(playerId);
    }

    public static HandDrankMessage decode(FriendlyByteBuf buffer)
    {
        return new HandDrankMessage(buffer.readVarInt());
    }

    public static void clientHandle(final HandDrankMessage message, final Supplier<NetworkEvent.Context> context)
    {
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> ClientPayloadHandlers.handDrank(message));
    }
}
