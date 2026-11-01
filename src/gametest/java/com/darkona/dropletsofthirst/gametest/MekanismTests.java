package com.darkona.dropletsofthirst.gametest;

import com.darkona.dropletsofthirst.DropletsOfThirst;
import com.darkona.dropletsofthirst.content.purity.WaterPurity;
import mekanism.common.tile.machine.TileEntityElectricPump;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import java.lang.reflect.Method;

@PrefixGameTestTemplate(false)
public class MekanismTests
{
    @GameTest(template = "empty", templateNamespace = DropletsOfThirst.ID)
    public static void electricPumpKeepsPurity(GameTestHelper helper) throws ReflectiveOperationException
    {
        Block pumpBlock = BuiltInRegistries.BLOCK.get(ResourceLocation.parse("mekanism:electric_pump"));
        helper.setBlock(new BlockPos(2, 2, 2), pumpBlock);
        BlockPos source = helper.absolutePos(new BlockPos(2, 1, 2));
        helper.getLevel().setBlockAndUpdate(source, Blocks.WATER.defaultBlockState());
        TileEntityElectricPump pump = (TileEntityElectricPump) helper.getBlockEntity(new BlockPos(2, 2, 2));
        Method suck = TileEntityElectricPump.class.getDeclaredMethod("suck", BlockPos.class, boolean.class, boolean.class);
        suck.setAccessible(true);
        helper.assertTrue((boolean) suck.invoke(pump, source, false, true), "the pump did not take the water source");
        FluidStack pumped = pump.fluidTank.getFluid();
        helper.assertFalse(pumped.isEmpty(), "the pump tank is empty");
        helper.assertTrue(WaterPurity.hasPurity(pumped), "water pumped by a Mekanism electric pump has no purity");
        helper.assertValueEqual(WaterPurity.getPurity(pumped), WaterPurity.getWaterPurity(helper.getLevel(), source, true), "purity of pumped water");
        helper.succeed();
    }
}
