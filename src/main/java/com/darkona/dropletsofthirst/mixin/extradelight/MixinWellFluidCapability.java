package com.darkona.dropletsofthirst.mixin.extradelight;

import com.darkona.dropletsofthirst.compat.delight.DelightCompat;
import com.darkona.dropletsofthirst.compat.delight.WaterSourceTank;
import com.lance5057.extradelight.capabilities.WellFluidCapability;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.fluids.FluidStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value = WellFluidCapability.class, remap = false)
public abstract class MixinWellFluidCapability implements WaterSourceTank
{
    @Unique
    private BlockEntity droplets_of_thirst$source;

    @Override
    public void droplets_of_thirst$setSource(BlockEntity source)
    {
        droplets_of_thirst$source = source;
    }

    @ModifyReturnValue(method = {"getFluid", "drain(ILnet/neoforged/neoforge/fluids/capability/IFluidHandler$FluidAction;)Lnet/neoforged/neoforge/fluids/FluidStack;"}, at = @At("RETURN"))
    private FluidStack droplets_of_thirst$worldPurity(FluidStack water)
    {
        return DelightCompat.withSourcePurity(water, droplets_of_thirst$source);
    }
}
