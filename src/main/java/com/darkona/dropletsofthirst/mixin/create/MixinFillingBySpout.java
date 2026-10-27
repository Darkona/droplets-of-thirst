package com.darkona.dropletsofthirst.mixin.create;

import com.darkona.dropletsofthirst.content.purity.WaterPurity;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import com.simibubi.create.content.fluids.spout.FillingBySpout;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.fluids.FluidStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value = FillingBySpout.class, remap = false)
public class MixinFillingBySpout
{
    @ModifyReturnValue(method = "fillItem", at = @At("RETURN"))
    private static ItemStack droplets_of_thirst$keepPurity(ItemStack filled, @Local(argsOnly = true) FluidStack availableFluid)
    {
        if (!filled.isEmpty() && WaterPurity.hasPurity(availableFluid) && !WaterPurity.hasPurity(filled) && WaterPurity.isWaterFilledContainer(filled))
            WaterPurity.addPurity(filled, WaterPurity.getPurity(availableFluid));
        return filled;
    }
}
