package com.darkona.dropletsofthirst.mixin;

import com.darkona.dropletsofthirst.content.purity.WaterPurity;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.FluidStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(FluidStack.class)
public abstract class MixinFluidStack
{
    @Inject(method = "<init>(Lnet/minecraft/core/Holder;ILnet/minecraft/core/component/DataComponentPatch;)V", at = @At("TAIL"))
    private void droplets_of_thirst$migrateLegacyPurity(Holder<Fluid> fluid, int amount, DataComponentPatch patch, CallbackInfo ci)
    {
        if (!patch.isEmpty())
            WaterPurity.migrateLegacyPurity((FluidStack) (Object) this);
    }
}
