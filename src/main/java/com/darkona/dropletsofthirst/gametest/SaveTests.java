package com.darkona.dropletsofthirst.gametest;

import com.darkona.dropletsofthirst.DropletsOfThirst;
import com.darkona.dropletsofthirst.api.event.ThirstChangeEvent;
import com.darkona.dropletsofthirst.content.thirst.DrinkValues;
import com.darkona.dropletsofthirst.content.purity.WaterPurity;
import com.darkona.dropletsofthirst.content.purity.PurityNbt;
import com.darkona.dropletsofthirst.content.thirst.PlayerThirst;
import com.darkona.dropletsofthirst.network.PlayerThirstSyncMessage;
import com.darkona.dropletsofthirst.network.ThirstValuesSyncMessage;
import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import io.netty.buffer.Unpooled;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import static com.darkona.dropletsofthirst.gametest.TestSupport.player;
import static com.darkona.dropletsofthirst.gametest.TestSupport.thirst;

import static com.darkona.dropletsofthirst.gametest.TestSupport.assertValueEqual;

@GameTestHolder(DropletsOfThirst.ID)
@PrefixGameTestTemplate(false)
public class SaveTests
{
    @GameTest(template = "empty")
    public static void playerThirstSurvivesASave(GameTestHelper helper)
    {
        ServerPlayer player = player(helper);
        PlayerThirst thirst = thirst(player);
        thirst.change(player, 13, 7, ThirstChangeEvent.Cause.COMMAND);
        thirst.setExhaustion(2.5F);
        thirst.setShouldTickThirst(false);
        CompoundTag tag = thirst.serializeNBT();
        PlayerThirst loaded = new PlayerThirst();
        loaded.deserializeNBT(tag);
        assertValueEqual(helper, loaded.getThirst(), 13, "thirst after a save");
        assertValueEqual(helper, loaded.getQuenched(), 7, "quenched after a save");
        assertValueEqual(helper, loaded.getExhaustion(), 2.5F, "exhaustion after a save");
        helper.assertFalse(loaded.getShouldTickThirst(), "disabled thirst came back enabled");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void oldSavesLoadWithSaneValues(GameTestHelper helper)
    {
        CompoundTag tag = new CompoundTag();
        tag.putInt("thirst", 10);
        tag.putInt("quenched", 15);
        PlayerThirst loaded = new PlayerThirst();
        loaded.deserializeNBT(tag);
        assertValueEqual(helper, loaded.getQuenched(), 10, "quenched above thirst in an old save");
        helper.assertTrue(loaded.getShouldTickThirst(), "a save without the enable flag is disabled");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void purityComponentSurvivesASave(GameTestHelper helper)
    {
        ItemStack bucket = WaterPurity.addPurity(new ItemStack(Items.WATER_BUCKET), 1);
        ItemStack loaded = ItemStack.of(bucket.save(new CompoundTag()));
        assertValueEqual(helper, PurityNbt.get(loaded), 1, "purity after a save");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void invalidPurityLoadsAsDefault(GameTestHelper helper)
    {
        ItemStack bucket = new ItemStack(Items.WATER_BUCKET);
        bucket.getOrCreateTag().putInt(PurityNbt.PURITY, 7);
        ItemStack loaded = ItemStack.of(bucket.save(new CompoundTag()));
        helper.assertTrue(loaded.is(Items.WATER_BUCKET), "a bucket with purity 7 was lost on load");
        assertValueEqual(helper, PurityNbt.get(loaded), WaterPurity.defaultPurity(), "purity 7 after a load");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void purityComponentCrossesTheNetwork(GameTestHelper helper)
    {
        FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
        buf.writeItem(WaterPurity.addPurity(new ItemStack(Items.WATER_BUCKET), 1));
        ItemStack received = buf.readItem();
        assertValueEqual(helper, PurityNbt.get(received), 1, "purity after the network");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void drinksDataMapAcceptsSaltyAndRejectsOutOfRange(GameTestHelper helper)
    {
        helper.assertTrue(parse("{\"thirst\": -2, \"quenched\": -2}"), "salty entry rejected");
        helper.assertTrue(parse("{\"thirst\": 6, \"quenched\": 8, \"purity\": 5}"), "water entry of purity 5 rejected");
        helper.assertFalse(parse("{\"thirst\": 21, \"quenched\": 0}"), "thirst 21 accepted");
        helper.assertFalse(parse("{\"thirst\": 2, \"quenched\": -30}"), "quenched -30 accepted");
        helper.assertFalse(parse("{\"thirst\": 2, \"quenched\": 2, \"purity\": 6}"), "purity 6 accepted");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void thirstSyncPayloadRoundTrips(GameTestHelper helper)
    {
        FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
        PlayerThirstSyncMessage sent = new PlayerThirstSyncMessage(17, 9, 1.25F, PlayerThirst.SYNC_ENABLED | PlayerThirst.SYNC_SPRINT_BLOCKED | 6 << PlayerThirst.SYNC_SPRINT_MIN_SHIFT);
        sent.encode(buf);
        assertValueEqual(helper, PlayerThirstSyncMessage.decode(buf), sent, "thirst sync payload");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void drinkTablePayloadRoundTrips(GameTestHelper helper)
    {
        FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
        ThirstValuesSyncMessage sent = ThirstValuesSyncMessage.fromTables();
        helper.assertTrue(sent.drinks().containsKey(Items.POTION), "the resolved drink table has no water bottle");
        sent.encode(buf);
        ThirstValuesSyncMessage received = ThirstValuesSyncMessage.decode(buf);
        assertValueEqual(helper, received.drinks().size(), sent.drinks().size(), "drinks sent");
        assertValueEqual(helper, received.foods().size(), sent.foods().size(), "foods sent");
        assertValueEqual(helper, received.drinks().get(Items.POTION)[0], sent.drinks().get(Items.POTION)[0], "water bottle thirst");
        assertValueEqual(helper, received.defaultPurity(), sent.defaultPurity(), "defaultPurity sent");
        assertValueEqual(helper, received.pureThirstBonus(), sent.pureThirstBonus(), "pureWater.thirstBonus sent");
        assertValueEqual(helper, received.pureQuenchedBonus(), sent.pureQuenchedBonus(), "pureWater.quenchedBonus sent");
        helper.succeed();
    }

    private static boolean parse(String json)
    {
        return DrinkValues.CODEC.parse(JsonOps.INSTANCE, JsonParser.parseString(json)).result().isPresent();
    }
}
