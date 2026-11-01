package com.darkona.dropletsofthirst.gametest;

import net.minecraft.world.level.block.state.BlockState;
import com.momosoftworks.coldsweat.common.item.WaterskinItem;
import com.momosoftworks.coldsweat.common.blockentity.BoilerBlockEntity;
import com.darkona.dropletsofthirst.content.thirst.ExhaustionFactors;
import com.darkona.dropletsofthirst.content.purity.CauldronPurity;
import com.darkona.dropletsofthirst.DropletsOfThirst;
import com.darkona.dropletsofthirst.api.ThirstValues;
import com.darkona.dropletsofthirst.compat.coldsweat.ColdSweatCompat;
import com.darkona.dropletsofthirst.content.purity.WaterPurity;
import com.darkona.dropletsofthirst.content.thirst.PlayerThirst;
import com.darkona.dropletsofthirst.content.thirst.ThirstHelper;
import com.darkona.dropletsofthirst.core.NumberRows;
import com.darkona.dropletsofthirst.config.CompatConfig;
import com.darkona.dropletsofthirst.config.GameplayConfig;
import com.momosoftworks.coldsweat.util.registries.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LayeredCauldronBlock;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import static com.darkona.dropletsofthirst.gametest.TestSupport.player;

import static com.darkona.dropletsofthirst.gametest.TestSupport.assertValueEqual;

@PrefixGameTestTemplate(false)
public class ColdSweatTests
{
    @GameTest(template = "empty", templateNamespace = DropletsOfThirst.ID)
    public static void emptiedWaterskinLosesItsPurity(GameTestHelper helper)
    {
        ItemStack filled = WaterPurity.addPurity(new ItemStack(ModItems.FILLED_WATERSKIN), 0);
        ItemStack empty = filled.getCraftingRemainingItem();
        helper.assertTrue(empty.is(ModItems.WATERSKIN), "the remainder is not an empty waterskin");
        helper.assertFalse(WaterPurity.hasPurity(empty), "the emptied waterskin kept its purity");
        helper.assertTrue(ItemStack.isSameItemSameTags(empty, new ItemStack(ModItems.WATERSKIN)), "the emptied waterskin does not stack with a new one");
        helper.succeed();
    }

    @GameTest(template = "empty", templateNamespace = DropletsOfThirst.ID)
    public static void theBoilerPurifiesWaterUpToAcceptable(GameTestHelper helper)
    {
        BlockPos pos = helper.absolutePos(new BlockPos(1, 2, 1));
        BlockState state = com.momosoftworks.coldsweat.util.registries.ModBlocks.BOILER.defaultBlockState();
        helper.getLevel().setBlockAndUpdate(pos, state);
        BoilerBlockEntity boiler = (BoilerBlockEntity) helper.getLevel().getBlockEntity(pos);
        boiler.setFuel(1000);
        ItemStack dirty = WaterPurity.addPurity(PotionUtils.setPotion(new ItemStack(Items.POTION), Potions.WATER), 1);
        ItemStack clean = WaterPurity.addPurity(PotionUtils.setPotion(new ItemStack(Items.POTION), Potions.WATER), 4);
        ItemStack canteen = new ItemStack(com.darkona.dropletsofthirst.index.DropletsItems.CANTEEN.get());
        canteen.getCapability(ForgeCapabilities.FLUID_HANDLER_ITEM).orElseThrow(IllegalStateException::new).fill(WaterPurity.addPurity(new FluidStack(Fluids.WATER, 250), 2), IFluidHandler.FluidAction.EXECUTE);
        boiler.setItem(1, dirty);
        boiler.setItem(2, clean);
        boiler.setItem(3, canteen);
        for (int tick = 0; tick < 1000; tick++)
            boiler.tick(helper.getLevel(), state, pos);
        assertValueEqual(helper, WaterPurity.getPurity(boiler.getItem(1)), 3, "dirty water stops at acceptable");
        assertValueEqual(helper, WaterPurity.getPurity(boiler.getItem(2)), 4, "clean water is left alone");
        FluidStack boiled = boiler.getItem(3).getCapability(ForgeCapabilities.FLUID_HANDLER_ITEM).orElseThrow(IllegalStateException::new).getFluidInTank(0);
        assertValueEqual(helper, WaterPurity.getPurity(boiled), 3, "the canteen boils too");
        helper.assertTrue(ColdSweatCompat.canteenTemperature(boiler.getItem(3)) > 0, "the boiler heats the canteen");
        ItemStack hot = boiler.getItem(3);
        ServerPlayer player = TestSupport.player(helper);
        hot.getItem().finishUsingItem(hot, helper.getLevel(), player);
        assertValueEqual(helper, ColdSweatCompat.canteenTemperature(hot), 0.0, "an emptied canteen has no temperature");
        helper.succeed();
    }

    @GameTest(template = "empty", templateNamespace = DropletsOfThirst.ID)
    public static void waterskinFilledFromATankTakesItsPurity(GameTestHelper helper)
    {
        ServerPlayer player = TestSupport.player(helper);
        ItemStack empty = new ItemStack(ModItems.WATERSKIN);
        player.setItemInHand(InteractionHand.MAIN_HAND, empty);
        FluidStack tank = new FluidStack(Fluids.WATER, 250);
        WaterPurity.addPurity(tank, 0);
        WaterskinItem.handleFillWaterskin(player, empty, InteractionHand.MAIN_HAND, helper.absolutePos(new BlockPos(1, 2, 1)), tank);
        ItemStack filled = player.getItemInHand(InteractionHand.MAIN_HAND);
        helper.assertTrue(filled.is(ModItems.FILLED_WATERSKIN), "the hand holds " + filled);
        assertValueEqual(helper, WaterPurity.getPurity(filled), 0, "purity of the tank water");
        helper.succeed();
    }

    @GameTest(template = "empty", templateNamespace = DropletsOfThirst.ID)
    public static void waterskinIsAWaterDrinkThatCooks(GameTestHelper helper)
    {
        ItemStack waterskin = new ItemStack(ModItems.FILLED_WATERSKIN);
        ThirstValues values = ThirstHelper.valuesOf(waterskin);
        helper.assertTrue(values != null && values.thirst() == 6 && values.quenched() == 3, "filled waterskin drink values " + values);
        helper.assertTrue(WaterPurity.isWaterFilledContainer(waterskin), "the filled waterskin is not a water container");
        ItemStack dirty = WaterPurity.addPurity(waterskin.copy(), 0);
        ItemStack cooked = helper.getLevel().getRecipeManager().getRecipeFor(RecipeType.SMELTING, new SimpleContainer(dirty), helper.getLevel())
                .map(recipe -> recipe.assemble(new SimpleContainer(dirty), helper.getLevel().registryAccess())).orElse(ItemStack.EMPTY);
        helper.assertTrue(cooked.is(ModItems.FILLED_WATERSKIN), "a furnace does not purify a dirty waterskin");
        assertValueEqual(helper, WaterPurity.getPurity(cooked), 2, "purity of a dirty waterskin out of a furnace");
        helper.succeed();
    }

    @GameTest(template = "box", templateNamespace = DropletsOfThirst.ID)
    public static void waterskinTakesThePurityOfACauldron(GameTestHelper helper)
    {
        BlockPos cauldron = helper.absolutePos(new BlockPos(2, 2, 2));
        helper.getLevel().setBlockAndUpdate(cauldron.below(), Blocks.CAMPFIRE.defaultBlockState());
        helper.getLevel().setBlockAndUpdate(cauldron, Blocks.WATER_CAULDRON.defaultBlockState().setValue(LayeredCauldronBlock.LEVEL, 3));
        ServerPlayer player = player(helper);
        ItemStack empties = new ItemStack(ModItems.WATERSKIN, 2);
        player.setItemInHand(InteractionHand.MAIN_HAND, empties);
        player.gameMode.useItemOn(player, helper.getLevel(), empties, InteractionHand.MAIN_HAND, new BlockHitResult(Vec3.atCenterOf(cauldron), Direction.UP, cauldron, false));
        ItemStack filled = filledWaterskin(player);
        helper.assertTrue(!filled.isEmpty(), "no waterskin was filled from the cauldron");
        assertValueEqual(helper, WaterPurity.getPurity(filled), CauldronPurity.HEATED_CAULDRON_PURITY, "purity of a waterskin filled from a cauldron on a campfire");
        helper.succeedWhen(() -> helper.assertFalse(WaterPurity.hasPurity(empties), "the empty waterskins kept a purity"));
    }

    @GameTest(template = "box", templateNamespace = DropletsOfThirst.ID)
    public static void waterskinTakesThePurityOfWorldWater(GameTestHelper helper)
    {
        BlockPos water = helper.absolutePos(new BlockPos(2, 1, 2));
        helper.getLevel().setBlockAndUpdate(water, Blocks.WATER.defaultBlockState());
        ServerPlayer player = player(helper);
        player.moveTo(water.getX() + 0.5, water.getY() + 1, water.getZ() + 0.5, 0, 90);
        ItemStack empty = new ItemStack(ModItems.WATERSKIN);
        player.setItemInHand(InteractionHand.MAIN_HAND, empty);
        player.gameMode.useItem(player, helper.getLevel(), empty, InteractionHand.MAIN_HAND);
        ItemStack filled = player.getItemInHand(InteractionHand.MAIN_HAND);
        helper.assertTrue(filled.is(ModItems.FILLED_WATERSKIN), "no waterskin was filled from the water source, got " + filled);
        helper.assertTrue(WaterPurity.hasPurity(filled), "a waterskin filled from the world has no purity");
        assertValueEqual(helper, WaterPurity.getPurity(filled), WaterPurity.getWaterPurity(helper.getLevel(), water, true), "purity of a waterskin filled from the world");
        helper.succeed();
    }

    @GameTest(template = "box", templateNamespace = DropletsOfThirst.ID)
    public static void waterskinTakesThePurityOfATank(GameTestHelper helper)
    {
        Block tankBlock = BuiltInRegistries.BLOCK.get(new ResourceLocation("create", "fluid_tank"));
        if (tankBlock == Blocks.AIR)
        {
            helper.succeed();
            return;
        }
        BlockPos tank = helper.absolutePos(new BlockPos(2, 2, 2));
        helper.getLevel().setBlockAndUpdate(tank, tankBlock.defaultBlockState());
        IFluidHandler handler = helper.getLevel().getBlockEntity(tank).getCapability(ForgeCapabilities.FLUID_HANDLER, Direction.UP).orElse(null);
        helper.assertTrue(handler != null, "the tank has no fluid handler");
        handler.fill(WaterPurity.addPurity(new FluidStack(Fluids.WATER, 1000), 0), IFluidHandler.FluidAction.EXECUTE);
        ServerPlayer player = player(helper);
        ItemStack empties = new ItemStack(ModItems.WATERSKIN, 2);
        player.setItemInHand(InteractionHand.MAIN_HAND, empties);
        player.gameMode.useItemOn(player, helper.getLevel(), empties, InteractionHand.MAIN_HAND, new BlockHitResult(Vec3.atCenterOf(tank), Direction.UP, tank, false));
        ItemStack filled = filledWaterskin(player);
        helper.assertTrue(!filled.isEmpty(), "no waterskin was filled from the tank");
        assertValueEqual(helper, WaterPurity.getPurity(filled), 0, "purity of a waterskin filled from a tank of dirty water");
        helper.assertFalse(WaterPurity.hasPurity(empties), "the empty waterskins got a purity");
        helper.succeed();
    }

    private static ItemStack filledWaterskin(ServerPlayer player)
    {
        for (ItemStack stack : player.getInventory().items)
            if (stack.is(ModItems.FILLED_WATERSKIN))
                return stack;
        return ItemStack.EMPTY;
    }

    @GameTest(template = "empty", templateNamespace = DropletsOfThirst.ID)
    public static void drinkingWaterCoolsAndBodyTemperatureDrivesThirst(GameTestHelper helper)
    {
        double cooling = CompatConfig.COLD_SWEAT_DRINK_COOLING.get();
        int ticks = CompatConfig.COLD_SWEAT_DRINK_COOLING_TICKS.get();
        try
        {
            TestSupport.set(CompatConfig.COLD_SWEAT_DRINK_COOLING, 50.0);
            TestSupport.set(CompatConfig.COLD_SWEAT_DRINK_COOLING_TICKS, 0);
            ServerPlayer player = player(helper);
            double before = ColdSweatCompat.bodyTemperature(player);
            PlayerThirst.drink(player, PotionUtils.setPotion(new ItemStack(Items.POTION), Potions.WATER), 6, 8, 2);
            double after = ColdSweatCompat.bodyTemperature(player);
            helper.assertTrue(Math.abs(after - (before - 50)) < 0.001, "body temperature " + before + " -> " + after + " after a cooling drink of 50");

            PlayerThirst.drink(player, new ItemStack(ModItems.FILLED_WATERSKIN), 6, 3, 2);
            helper.assertTrue(Math.abs(ColdSweatCompat.bodyTemperature(player) - after) < 0.001, "the waterskin cooled as well as its own temperature");

            double expected = GameplayConfig.DEPLETION_MULTIPLIER.get() * NumberRows.curve(NumberRows.parse(CompatConfig.COLD_SWEAT_BODY_TEMPERATURE_CURVE.get(), 2), after);
            helper.assertTrue(Math.abs(ExhaustionFactors.getExhaustionBiomeModifier(player) - expected) < 0.001,
                    "climate multiplier " + ExhaustionFactors.getExhaustionBiomeModifier(player) + " at body temperature " + after + ", expected " + expected);
        }
        finally
        {
            TestSupport.set(CompatConfig.COLD_SWEAT_DRINK_COOLING, cooling);
            TestSupport.set(CompatConfig.COLD_SWEAT_DRINK_COOLING_TICKS, ticks);
        }
        helper.succeed();
    }
}
