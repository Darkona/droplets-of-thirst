package com.darkona.dropletsofthirst.foundation.mixin.travelersbackpack;

import com.darkona.dropletsofthirst.purity.WaterPurity;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalIntRef;
import com.tiviacz.travelersbackpack.items.HoseItem;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.fluids.FluidStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value = HoseItem.class, remap = false)
public abstract class MixinHoseItem
{
    @ModifyExpressionValue(method = "use", at = @At(value = "NEW", target = "net/neoforged/neoforge/fluids/FluidStack", ordinal = 0))
    private FluidStack droplets_of_thirst$checkedStack(FluidStack fluid, @Local(argsOnly = true) Level level, @Local(ordinal = 0) BlockPos pos, @Share("purity") LocalIntRef purity)
    {
        purity.set(fluid.is(FluidTags.WATER) && WaterPurity.enabled() ? WaterPurity.getBlockPurity(level, pos) : -1);
        return withPurity(fluid, purity);
    }

    @ModifyExpressionValue(method = "use", at = @At(value = "NEW", target = "net/neoforged/neoforge/fluids/FluidStack", ordinal = 1))
    private FluidStack droplets_of_thirst$filledStack(FluidStack fluid, @Share("purity") LocalIntRef purity)
    {
        return withPurity(fluid, purity);
    }

    private static FluidStack withPurity(FluidStack fluid, LocalIntRef purity)
    {
        return purity.get() >= 0 ? WaterPurity.addPurity(fluid, purity.get()) : fluid;
    }
}
