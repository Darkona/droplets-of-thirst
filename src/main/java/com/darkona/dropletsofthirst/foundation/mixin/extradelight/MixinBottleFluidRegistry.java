package com.darkona.dropletsofthirst.foundation.mixin.extradelight;

import com.darkona.dropletsofthirst.content.purity.WaterPurity;
import com.lance5057.extradelight.util.BottleFluidRegistry;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.fluids.FluidStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value = BottleFluidRegistry.class, remap = false)
public abstract class MixinBottleFluidRegistry
{
    @ModifyReturnValue(method = "getFluidFromBottle", at = @At("RETURN"))
    private static FluidStack droplets_of_thirst$bottlePurityToFluid(FluidStack fluid, @Local(argsOnly = true) ItemStack bottle)
    {
        if (fluid.isEmpty() || !fluid.getFluid().isSame(Fluids.WATER) || !WaterPurity.isWaterFilledContainer(bottle) || !WaterPurity.hasPurity(bottle))
            return fluid;
        return WaterPurity.addPurity(fluid.copy(), WaterPurity.getPurity(bottle));
    }

    @ModifyReturnValue(method = "getBottleFromFluid", at = @At("RETURN"))
    private static ItemStack droplets_of_thirst$fluidPurityToBottle(ItemStack bottle, @Local(argsOnly = true) FluidStack fluid)
    {
        if (WaterPurity.hasPurity(fluid) && WaterPurity.isWaterFilledContainer(bottle))
            WaterPurity.addPurity(bottle, WaterPurity.getPurity(fluid));
        return bottle;
    }
}
