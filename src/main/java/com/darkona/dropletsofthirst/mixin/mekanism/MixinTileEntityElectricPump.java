package com.darkona.dropletsofthirst.mixin.mekanism;

import com.darkona.dropletsofthirst.content.purity.WaterPurity;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import mekanism.common.tile.machine.TileEntityElectricPump;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.fluids.FluidStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value = TileEntityElectricPump.class, remap = false)
public abstract class MixinTileEntityElectricPump
{
    @WrapOperation(
            method = "suck(Lnet/minecraft/core/BlockPos;ZZ)Z",
            at = @At(value = "INVOKE", target = "Lmekanism/common/tile/machine/TileEntityElectricPump;getOutput(Lnet/minecraft/world/level/material/Fluid;Z)Lnet/minecraftforge/fluids/FluidStack;"),
            remap = false)
    private FluidStack droplets_of_thirst$addPurity(TileEntityElectricPump pump, Fluid fluid, boolean hasFilter, Operation<FluidStack> original, @Local(argsOnly = true) BlockPos pos)
    {
        FluidStack output = original.call(pump, fluid, hasFilter);
        Level level = pump.getLevel();
        if (level != null && WaterPurity.enabled() && !output.isEmpty() && output.getFluid().is(FluidTags.WATER) && !WaterPurity.hasPurity(output))
            WaterPurity.addPurity(output, WaterPurity.getWaterPurity(level, pos, true));
        return output;
    }
}
