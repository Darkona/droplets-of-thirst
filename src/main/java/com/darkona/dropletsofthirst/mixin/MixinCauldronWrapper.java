package com.darkona.dropletsofthirst.mixin;

import com.darkona.dropletsofthirst.purity.CauldronPurity;
import com.darkona.dropletsofthirst.purity.WaterPurity;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.PatchedDataComponentMap;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.wrappers.CauldronWrapper;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value = CauldronWrapper.class, remap = false)
public abstract class MixinCauldronWrapper
{
    @Shadow @Final private Level level;
    @Shadow @Final private BlockPos pos;

    @ModifyReturnValue(method = "getFluidInTank", at = @At("RETURN"))
    private FluidStack droplets_of_thirst$tankPurity(FluidStack fluid)
    {
        return withPurity(fluid);
    }

    @ModifyReturnValue(method = "drain(Lnet/minecraft/world/level/block/state/BlockState;ILnet/neoforged/neoforge/fluids/capability/IFluidHandler$FluidAction;)Lnet/neoforged/neoforge/fluids/FluidStack;", at = @At("RETURN"))
    private FluidStack droplets_of_thirst$drainPurity(FluidStack drained)
    {
        return withPurity(drained);
    }

    @WrapOperation(method = "drain(Lnet/neoforged/neoforge/fluids/FluidStack;Lnet/neoforged/neoforge/fluids/capability/IFluidHandler$FluidAction;)Lnet/neoforged/neoforge/fluids/FluidStack;",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/core/component/PatchedDataComponentMap;isEmpty()Z"))
    private boolean droplets_of_thirst$acceptOwnPurity(PatchedDataComponentMap components, Operation<Boolean> original, @Local(argsOnly = true) FluidStack resource)
    {
        return original.call(components) || components.size() == 1 && WaterPurity.hasPurity(resource) && resource.is(FluidTags.WATER)
                && WaterPurity.getPurity(resource) == CauldronPurity.at(level, pos);
    }

    private FluidStack withPurity(FluidStack fluid)
    {
        if (!fluid.isEmpty() && fluid.is(FluidTags.WATER))
            WaterPurity.addPurity(fluid, CauldronPurity.at(level, pos));
        return fluid;
    }
}
