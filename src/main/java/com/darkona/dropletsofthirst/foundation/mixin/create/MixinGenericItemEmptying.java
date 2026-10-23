package com.darkona.dropletsofthirst.foundation.mixin.create;

import com.darkona.dropletsofthirst.purity.WaterPurity;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.simibubi.create.content.fluids.transfer.EmptyingRecipe;
import com.simibubi.create.content.fluids.transfer.GenericItemEmptying;
import net.createmod.catnip.data.Pair;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.fluids.FluidStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value = GenericItemEmptying.class, remap = false)
public class MixinGenericItemEmptying
{
    @WrapOperation(method = "emptyItem", at = @At(value = "INVOKE", target = "Lcom/simibubi/create/content/fluids/potion/PotionFluidHandler;emptyPotion(Lnet/minecraft/world/item/ItemStack;Z)Lnet/createmod/catnip/data/Pair;"))
    private static Pair<FluidStack, ItemStack> droplets_of_thirst$bottlePurity(ItemStack stack, boolean simulate, Operation<Pair<FluidStack, ItemStack>> original,
                                                                         @Local(argsOnly = true) Level level)
    {
        int purity = !level.isClientSide() && WaterPurity.hasPurity(stack) ? WaterPurity.getPurity(stack) : -1;
        Pair<FluidStack, ItemStack> result = original.call(stack, simulate);
        if (purity >= WaterPurity.MIN_PURITY && !result.getFirst().isEmpty())
            WaterPurity.addPurity(result.getFirst(), purity);
        return result;
    }

    @WrapOperation(method = "emptyItem", at = @At(value = "INVOKE", target = "Lcom/simibubi/create/content/fluids/transfer/EmptyingRecipe;getResultingFluid()Lnet/neoforged/neoforge/fluids/FluidStack;"))
    private static FluidStack droplets_of_thirst$recipePurity(EmptyingRecipe recipe, Operation<FluidStack> original,
                                                        @Local(argsOnly = true) Level level, @Local(argsOnly = true) ItemStack stack)
    {
        FluidStack fluid = original.call(recipe);
        if (level.isClientSide() || fluid.isEmpty() || !WaterPurity.hasPurity(stack))
            return fluid;
        return WaterPurity.addPurity(fluid.copy(), WaterPurity.getPurity(stack));
    }
}
