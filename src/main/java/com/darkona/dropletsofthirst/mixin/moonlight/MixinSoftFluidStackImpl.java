package com.darkona.dropletsofthirst.mixin.moonlight;

import com.darkona.dropletsofthirst.index.DropletsDataComponents;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.mehvahdjukaar.moonlight.api.fluids.MLBuiltinSoftFluids;
import net.mehvahdjukaar.moonlight.api.fluids.SoftFluidStack;
import net.mehvahdjukaar.moonlight.api.fluids.platform.SoftFluidStackImpl;
import net.minecraft.core.HolderLookup;
import net.minecraft.tags.FluidTags;
import net.neoforged.neoforge.fluids.FluidStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value = SoftFluidStackImpl.class, remap = false)
public abstract class MixinSoftFluidStackImpl
{
    @ModifyReturnValue(
            method = "fromForgeFluid(Lnet/neoforged/neoforge/fluids/FluidStack;Lnet/minecraft/core/HolderLookup$Provider;)Lnet/mehvahdjukaar/moonlight/api/fluids/SoftFluidStack;",
            at = @At("RETURN"),
            remap = false)
    private static SoftFluidStack droplets_of_thirst$keepPurity(SoftFluidStack converted, FluidStack fluid, HolderLookup.Provider registries)
    {
        Integer purity = fluid.get(DropletsDataComponents.PURITY);
        if (purity != null && !converted.isEmpty() && converted.is(MLBuiltinSoftFluids.WATER))
            converted.set(DropletsDataComponents.PURITY, purity);
        return converted;
    }

    @ModifyReturnValue(
            method = "toForgeFluid(Lnet/mehvahdjukaar/moonlight/api/fluids/SoftFluidStack;)Lnet/neoforged/neoforge/fluids/FluidStack;",
            at = @At("RETURN"),
            remap = false)
    private static FluidStack droplets_of_thirst$exposePurity(FluidStack converted, SoftFluidStack soft)
    {
        Integer purity = soft.get(DropletsDataComponents.PURITY);
        if (purity != null && !converted.isEmpty() && converted.is(FluidTags.WATER))
            converted.set(DropletsDataComponents.PURITY, purity);
        return converted;
    }
}
