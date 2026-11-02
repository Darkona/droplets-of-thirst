package com.darkona.dropletsofthirst.network;

import com.darkona.dropletsofthirst.content.thirst.ThirstHelper;
import com.darkona.dropletsofthirst.content.purity.WaterPurity;
import com.darkona.dropletsofthirst.config.SyncedValues;
import com.darkona.dropletsofthirst.network.client.ClientPayloadHandlers;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.Item;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public record ThirstValuesSyncMessage(Map<Item, int[]> drinks, Map<Item, int[]> foods, List<Item> estimated, List<Item> containers, int defaultPurity, int waterBottleStackSize, boolean purityEnabled, boolean canFillFromFlowingWater,
                                      int pureThirstBonus, int pureHydrationBonus)
{
    public void encode(FriendlyByteBuf buffer)
    {
        writeTable(buffer, drinks);
        writeTable(buffer, foods);
        buffer.writeCollection(estimated, (buf, item) -> buf.writeId(BuiltInRegistries.ITEM, item));
        buffer.writeCollection(containers, (buf, item) -> buf.writeId(BuiltInRegistries.ITEM, item));
        buffer.writeVarInt(defaultPurity);
        buffer.writeVarInt(waterBottleStackSize);
        buffer.writeVarInt(pureThirstBonus);
        buffer.writeVarInt(pureHydrationBonus);
        buffer.writeBoolean(purityEnabled);
        buffer.writeBoolean(canFillFromFlowingWater);
    }

    public static ThirstValuesSyncMessage decode(FriendlyByteBuf buffer)
    {
        Map<Item, int[]> drinks = readTable(buffer);
        Map<Item, int[]> foods = readTable(buffer);
        List<Item> estimated = buffer.readList(buf -> buf.readById(BuiltInRegistries.ITEM));
        List<Item> containers = buffer.readList(buf -> buf.readById(BuiltInRegistries.ITEM));
        int defaultPurity = buffer.readVarInt();
        int stackSize = buffer.readVarInt();
        int pureThirst = buffer.readVarInt();
        int pureHydration = buffer.readVarInt();
        boolean purityEnabled = buffer.readBoolean();
        boolean flowing = buffer.readBoolean();
        return new ThirstValuesSyncMessage(drinks, foods, estimated, containers, defaultPurity, stackSize, purityEnabled, flowing, pureThirst, pureHydration);
    }

    private static void writeTable(FriendlyByteBuf buffer, Map<Item, int[]> table)
    {
        buffer.writeMap(table, (buf, item) -> buf.writeId(BuiltInRegistries.ITEM, item), (buf, values) -> {
            buf.writeVarInt(values[0]);
            buf.writeVarInt(values[1]);
            buf.writeVarInt(values[2]);
        });
    }

    private static Map<Item, int[]> readTable(FriendlyByteBuf buffer)
    {
        return buffer.readMap(HashMap::new, buf -> buf.readById(BuiltInRegistries.ITEM), buf -> new int[]{buf.readVarInt(), buf.readVarInt(), buf.readVarInt()});
    }

    public static ThirstValuesSyncMessage fromTables()
    {
        return new ThirstValuesSyncMessage(ThirstHelper.drinkTable(), ThirstHelper.foodTable(), List.copyOf(ThirstHelper.estimatedItems()), WaterPurity.dataContainerItems(),
                SyncedValues.defaultPurity(), SyncedValues.waterBottleStackSize(), SyncedValues.purityEnabled(), SyncedValues.canFillFromFlowingWater(),
                SyncedValues.pureThirstBonus(), SyncedValues.pureHydrationBonus());
    }

    public static void clientHandle(final ThirstValuesSyncMessage message, final Supplier<NetworkEvent.Context> context)
    {
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> ClientPayloadHandlers.values(message));
    }
}
