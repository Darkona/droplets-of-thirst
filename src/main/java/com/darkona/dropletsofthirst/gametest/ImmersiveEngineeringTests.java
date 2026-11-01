package com.darkona.dropletsofthirst.gametest;

import static com.darkona.dropletsofthirst.gametest.TestSupport.assertValueEqual;

import blusunrize.immersiveengineering.common.blocks.metal.FluidPumpBlockEntity;
import blusunrize.immersiveengineering.common.util.Utils;
import com.darkona.dropletsofthirst.DropletsOfThirst;
import com.darkona.dropletsofthirst.content.purity.WaterPurity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction;
import net.minecraftforge.fluids.capability.templates.FluidTank;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

@PrefixGameTestTemplate(false)
public class ImmersiveEngineeringTests
{
    @GameTest(template = "empty", templateNamespace = DropletsOfThirst.ID)
    public static void drainedBlockKeepsPurity(GameTestHelper helper)
    {
        BlockPos source = helper.absolutePos(new BlockPos(2, 1, 2));
        helper.getLevel().setBlockAndUpdate(source, Blocks.WATER.defaultBlockState());
        FluidStack drained = Utils.drainFluidBlock(helper.getLevel(), source, FluidAction.EXECUTE);
        helper.assertFalse(drained.isEmpty(), "nothing was drained from the water source");
        helper.assertTrue(WaterPurity.hasPurity(drained), "water drained by Immersive Engineering has no purity");
        assertValueEqual(helper, WaterPurity.getPurity(drained), WaterPurity.getWaterPurity(helper.getLevel(), source, true), "purity of drained water");
        helper.succeed();
    }

    @GameTest(template = "empty", templateNamespace = DropletsOfThirst.ID, timeoutTicks = 100)
    public static void pumpGatheringKeepsPurity(GameTestHelper helper) throws ReflectiveOperationException
    {
        Block pumpBlock = BuiltInRegistries.BLOCK.get(new ResourceLocation("immersiveengineering:fluid_pump"));
        BlockPos pumpPos = new BlockPos(2, 2, 2);
        helper.setBlock(pumpPos, pumpBlock);
        for (int x = 1; x <= 3; x++)
            for (int z = 1; z <= 3; z++)
                helper.getLevel().setBlockAndUpdate(helper.absolutePos(new BlockPos(x, 1, z)), Blocks.WATER.defaultBlockState());
        FluidPumpBlockEntity pump = (FluidPumpBlockEntity) helper.getBlockEntity(pumpPos);
        Field energy = FluidPumpBlockEntity.class.getDeclaredField("energyStorage");
        energy.setAccessible(true);
        ((blusunrize.immersiveengineering.api.energy.MutableEnergyStorage) energy.get(pump)).receiveEnergy(1000000, false);
        Field tankField = FluidPumpBlockEntity.class.getDeclaredField("tank");
        tankField.setAccessible(true);
        FluidTank tank = (FluidTank) tankField.get(pump);
        Method gather = FluidPumpBlockEntity.class.getDeclaredMethod("gatherInfiniteFluidFromWorld", Direction.class);
        gather.setAccessible(true);
        helper.succeedWhen(() ->
        {
            try
            {
                gather.invoke(pump, Direction.DOWN);
            }
            catch (ReflectiveOperationException e)
            {
                throw new IllegalStateException(e);
            }
            helper.assertFalse(tank.getFluid().isEmpty(), "the pump has not gathered water yet");
            helper.assertTrue(WaterPurity.hasPurity(tank.getFluid()), "water gathered by an Immersive Engineering pump has no purity");
        });
    }
}
