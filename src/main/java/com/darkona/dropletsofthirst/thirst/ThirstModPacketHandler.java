package com.darkona.dropletsofthirst.thirst;

import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

public class ThirstModPacketHandler
{
    private static final String PROTOCOL_VERSION = "0.1.12";

    public static void register(final RegisterPayloadHandlersEvent event) {
        final PayloadRegistrar registrar = event.registrar(PROTOCOL_VERSION);
        registrar.playToServer(
                DrinkByHandMessage.TYPE,
                DrinkByHandMessage.STREAM_CODEC,
                DrinkByHandMessage::serverHandle
        );
        registrar.playToClient(
                ThirstValuesSyncMessage.TYPE,
                ThirstValuesSyncMessage.STREAM_CODEC,
                ThirstValuesSyncMessage::clientHandle
        );
        registrar.playToClient(
                PlayerThirstSyncMessage.TYPE,
                PlayerThirstSyncMessage.STREAM_CODEC,
                PlayerThirstSyncMessage::clientHandle
        );
    }
}
