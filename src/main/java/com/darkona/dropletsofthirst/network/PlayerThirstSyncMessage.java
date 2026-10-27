package com.darkona.dropletsofthirst.network;

import com.darkona.dropletsofthirst.thirst.PlayerThirst;
import com.darkona.dropletsofthirst.thirst.ModAttachment;
import com.darkona.dropletsofthirst.DropletsOfThirst;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

public record PlayerThirstSyncMessage(int thirst, int quenched, float exhaustion, int flags) implements CustomPacketPayload
{

    public static final CustomPacketPayload.Type<PlayerThirstSyncMessage> TYPE = new Type<>(DropletsOfThirst.asResource("thirstsync"));

    public static final StreamCodec<ByteBuf, PlayerThirstSyncMessage> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.INT,
            PlayerThirstSyncMessage::thirst,
            ByteBufCodecs.INT,
            PlayerThirstSyncMessage::quenched,
            ByteBufCodecs.FLOAT,
            PlayerThirstSyncMessage::exhaustion,
            ByteBufCodecs.VAR_INT,
            PlayerThirstSyncMessage::flags,
            PlayerThirstSyncMessage::new
    );

    public static void clientHandle(final PlayerThirstSyncMessage message, final IPayloadContext context)
    {
        context.enqueueWork(() -> {
            Player player = context.player();
            PlayerThirst cap = player.getData(ModAttachment.PLAYER_THIRST);
            cap.setThirst(message.thirst);
            cap.setQuenched(message.quenched);
            cap.setExhaustion(message.exhaustion);
            cap.setSyncedRules(message.flags);
        });
    }

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}