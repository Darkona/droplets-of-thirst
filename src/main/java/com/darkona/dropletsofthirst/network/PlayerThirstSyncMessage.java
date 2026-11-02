package com.darkona.dropletsofthirst.network;

import com.darkona.dropletsofthirst.network.client.ClientPayloadHandlers;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record PlayerThirstSyncMessage(int thirst, int hydration, float exhaustion, int flags)
{
    public void encode(FriendlyByteBuf buffer)
    {
        buffer.writeInt(thirst);
        buffer.writeInt(hydration);
        buffer.writeFloat(exhaustion);
        buffer.writeVarInt(flags);
    }

    public static PlayerThirstSyncMessage decode(FriendlyByteBuf buffer)
    {
        return new PlayerThirstSyncMessage(buffer.readInt(), buffer.readInt(), buffer.readFloat(), buffer.readVarInt());
    }

    public static void clientHandle(final PlayerThirstSyncMessage message, final Supplier<NetworkEvent.Context> context)
    {
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> ClientPayloadHandlers.thirst(message));
    }
}
