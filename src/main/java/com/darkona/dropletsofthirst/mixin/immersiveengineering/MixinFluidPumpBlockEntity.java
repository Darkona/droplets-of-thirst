package com.darkona.dropletsofthirst.mixin.immersiveengineering;

import blusunrize.immersiveengineering.common.blocks.metal.FluidPumpBlockEntity;
import com.darkona.dropletsofthirst.content.purity.WaterPurity;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.FluidTags;
import net.minecraftforge.fluids.FluidStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value = FluidPumpBlockEntity.class, remap = false)
public abstract class MixinFluidPumpBlockEntity
{
    @ModifyExpressionValue(
            method = "gatherInfiniteFluidFromWorld",
            at = @At(value = "NEW", target = "(Lnet/minecraft/world/level/material/Fluid;I)Lnet/minecraftforge/fluids/FluidStack;"),
            remap = false)
    private FluidStack droplets_of_thirst$addPurity(FluidStack gathered, @Local BlockPos neighborPos)
    {
        FluidPumpBlockEntity pump = (FluidPumpBlockEntity) (Object) this;
        if (pump.getLevel() != null && WaterPurity.enabled() && gathered.getFluid().is(FluidTags.WATER) && !WaterPurity.hasPurity(gathered))
            WaterPurity.addPurity(gathered, WaterPurity.getWaterPurity(pump.getLevel(), neighborPos, true));
        return gathered;
    }
}
