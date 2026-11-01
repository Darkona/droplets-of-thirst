package com.darkona.dropletsofthirst.mixin.immersiveengineering;

import blusunrize.immersiveengineering.common.util.Utils;
import com.darkona.dropletsofthirst.content.purity.WaterPurity;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler.FluidAction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value = Utils.class, remap = false)
public abstract class MixinUtils
{
    @ModifyReturnValue(method = "drainFluidBlock", at = @At("RETURN"), remap = false)
    private static FluidStack droplets_of_thirst$addPurity(FluidStack drained, Level level, BlockPos pos, FluidAction action)
    {
        if (WaterPurity.enabled() && !drained.isEmpty() && drained.is(FluidTags.WATER) && !WaterPurity.hasPurity(drained))
            WaterPurity.addPurity(drained, WaterPurity.getWaterPurity(level, pos, true));
        return drained;
    }
}
