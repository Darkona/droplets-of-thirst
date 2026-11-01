package com.darkona.dropletsofthirst.mixin.moonlight;

import com.darkona.dropletsofthirst.content.purity.PurityNbt;
import com.darkona.dropletsofthirst.content.purity.WaterPurity;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.mehvahdjukaar.moonlight.api.fluids.BuiltInSoftFluids;
import net.mehvahdjukaar.moonlight.api.fluids.SoftFluidStack;
import net.mehvahdjukaar.moonlight.api.fluids.forge.SoftFluidStackImpl;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.tags.FluidTags;
import net.minecraftforge.fluids.FluidStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value = SoftFluidStackImpl.class, remap = false)
public abstract class MixinSoftFluidStackImpl
{
    @ModifyReturnValue(
            method = "fromForgeFluid(Lnet/minecraftforge/fluids/FluidStack;)Lnet/mehvahdjukaar/moonlight/api/fluids/SoftFluidStack;",
            at = @At("RETURN"),
            remap = false)
    private static SoftFluidStack droplets_of_thirst$keepPurity(SoftFluidStack converted, FluidStack fluid)
    {
        Integer purity = PurityNbt.get(fluid);
        if (purity != null && !converted.isEmpty() && converted.is(BuiltInSoftFluids.WATER.getHolder()))
            converted.getOrCreateTag().putInt(PurityNbt.PURITY, purity);
        return converted;
    }

    @ModifyReturnValue(
            method = "toForgeFluid(Lnet/mehvahdjukaar/moonlight/api/fluids/SoftFluidStack;)Lnet/minecraftforge/fluids/FluidStack;",
            at = @At("RETURN"),
            remap = false)
    private static FluidStack droplets_of_thirst$exposePurity(FluidStack converted, SoftFluidStack soft)
    {
        CompoundTag tag = soft.getTag();
        if (tag != null && tag.contains(PurityNbt.PURITY) && !converted.isEmpty() && converted.getFluid().is(FluidTags.WATER))
            WaterPurity.addPurity(converted, tag.getInt(PurityNbt.PURITY));
        return converted;
    }
}
