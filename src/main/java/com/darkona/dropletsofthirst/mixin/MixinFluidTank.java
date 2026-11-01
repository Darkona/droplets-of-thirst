package com.darkona.dropletsofthirst.mixin;

import com.darkona.dropletsofthirst.content.purity.WaterPurity;
import com.llamalad7.mixinextras.sugar.Local;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(value = FluidTank.class, remap = false)
public abstract class MixinFluidTank
{
    @Shadow
    protected FluidStack fluid;

    @Shadow
    public abstract int getSpace();

    @Shadow
    public abstract boolean isFluidValid(FluidStack stack);

    @ModifyVariable(method = "fill", at = @At("HEAD"), argsOnly = true)
    private FluidStack droplets_of_thirst$mixWater(FluidStack resource, @Local(argsOnly = true) IFluidHandler.FluidAction action)
    {
        return getSpace() > 0 && isFluidValid(resource) ? WaterPurity.mixInto(fluid, resource, action.execute()) : resource;
    }
}
