package com.darkona.dropletsofthirst.thirst;

import com.darkona.dropletsofthirst.DropletsOfThirst;
import com.darkona.dropletsofthirst.purity.WaterPurity;
import com.darkona.dropletsofthirst.config.SyncedValues;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public record ThirstValuesSyncMessage(Map<Item, int[]> drinks, Map<Item, int[]> foods, List<Item> estimated, List<Item> containers, int defaultPurity, int waterBottleStackSize, boolean purityEnabled, boolean canFillFromFlowingWater,
                                      int pureThirstBonus, int pureQuenchedBonus) implements CustomPacketPayload
{
    public static final CustomPacketPayload.Type<ThirstValuesSyncMessage> TYPE = new Type<>(DropletsOfThirst.asResource("thirst_values"));

    private static final StreamCodec<RegistryFriendlyByteBuf, Item> ITEM = ByteBufCodecs.registry(Registries.ITEM);
    private static final StreamCodec<ByteBuf, int[]> VALUES = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, values -> values[0],
            ByteBufCodecs.VAR_INT, values -> values[1],
            ByteBufCodecs.VAR_INT, values -> values[2],
            (thirst, quenched, purity) -> new int[]{thirst, quenched, purity});
    private static final StreamCodec<RegistryFriendlyByteBuf, Map<Item, int[]>> TABLE = ByteBufCodecs.map(HashMap::new, ITEM, VALUES);

    private static final StreamCodec<ByteBuf, boolean[]> FLAGS = StreamCodec.composite(
            ByteBufCodecs.BOOL, flags -> flags[0],
            ByteBufCodecs.BOOL, flags -> flags[1],
            (first, second) -> new boolean[]{first, second});

    private static final StreamCodec<ByteBuf, int[]> NUMBERS = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, numbers -> numbers[0],
            ByteBufCodecs.VAR_INT, numbers -> numbers[1],
            ByteBufCodecs.VAR_INT, numbers -> numbers[2],
            ByteBufCodecs.VAR_INT, numbers -> numbers[3],
            (first, second, third, fourth) -> new int[]{first, second, third, fourth});

    public static final StreamCodec<RegistryFriendlyByteBuf, ThirstValuesSyncMessage> STREAM_CODEC = StreamCodec.composite(
            TABLE, ThirstValuesSyncMessage::drinks,
            TABLE, ThirstValuesSyncMessage::foods,
            ITEM.apply(ByteBufCodecs.list()), ThirstValuesSyncMessage::estimated,
            ITEM.apply(ByteBufCodecs.list()), ThirstValuesSyncMessage::containers,
            NUMBERS, message -> new int[]{message.defaultPurity, message.waterBottleStackSize, message.pureThirstBonus, message.pureQuenchedBonus},
            FLAGS, message -> new boolean[]{message.purityEnabled, message.canFillFromFlowingWater},
            (drinks, foods, estimated, containers, numbers, flags) -> new ThirstValuesSyncMessage(drinks, foods, estimated, containers,
                    numbers[0], numbers[1], flags[0], flags[1], numbers[2], numbers[3]));

    public static ThirstValuesSyncMessage fromTables()
    {
        return new ThirstValuesSyncMessage(ThirstHelper.drinkTable(), ThirstHelper.foodTable(), List.copyOf(ThirstHelper.estimatedItems()), WaterPurity.dataContainerItems(),
                SyncedValues.defaultPurity(), SyncedValues.waterBottleStackSize(), SyncedValues.purityEnabled(), SyncedValues.canFillFromFlowingWater(),
                SyncedValues.pureThirstBonus(), SyncedValues.pureQuenchedBonus());
    }

    public static void clientHandle(final ThirstValuesSyncMessage message, final IPayloadContext context)
    {
        context.enqueueWork(() -> {
            ThirstHelper.useServerTables(message.drinks, message.foods, message.containers, message.estimated);
            SyncedValues.useServerValues(message.defaultPurity, message.waterBottleStackSize, message.purityEnabled, message.canFillFromFlowingWater,
                    message.pureThirstBonus, message.pureQuenchedBonus);
        });
    }

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
