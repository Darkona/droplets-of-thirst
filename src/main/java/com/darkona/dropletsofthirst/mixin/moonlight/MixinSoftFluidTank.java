package com.darkona.dropletsofthirst.mixin.moonlight;

import com.darkona.dropletsofthirst.content.purity.WaterPurity;
import com.darkona.dropletsofthirst.index.DropletsDataComponents;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalIntRef;
import net.mehvahdjukaar.moonlight.api.fluids.MLBuiltinSoftFluids;
import net.mehvahdjukaar.moonlight.api.fluids.SoftFluidStack;
import net.mehvahdjukaar.moonlight.api.fluids.SoftFluidTank;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = SoftFluidTank.class, remap = false)
public abstract class MixinSoftFluidTank
{
    private static final String FILL_ITEM = "fillItem(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;ZZ)Lnet/minecraft/world/InteractionResultHolder;";
    private static final String DRAIN_ITEM = "drainItem(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;ZZ)Lnet/minecraft/world/InteractionResultHolder;";

    @Inject(method = FILL_ITEM, at = @At("HEAD"), remap = false)
    private void droplets_of_thirst$readTankPurity(ItemStack item, Level level, BlockPos pos, boolean simulate, boolean doFill, CallbackInfoReturnable<InteractionResultHolder<ItemStack>> cir, @Share("purity") LocalIntRef purity)
    {
        SoftFluidStack fluid = ((SoftFluidTank) (Object) this).getFluid();
        Integer stored = fluid.get(DropletsDataComponents.PURITY);
        purity.set(stored != null && fluid.is(MLBuiltinSoftFluids.WATER) ? stored : -1);
    }

    @ModifyReturnValue(method = FILL_ITEM, at = @At("RETURN"), remap = false)
    private InteractionResultHolder<ItemStack> droplets_of_thirst$addItemPurity(InteractionResultHolder<ItemStack> result, ItemStack item, Level level, BlockPos pos, boolean simulate, boolean doFill, @Share("purity") LocalIntRef purity)
    {
        ItemStack filled = result.getObject();
        if (purity.get() >= 0 && result.getResult().consumesAction() && !filled.isEmpty() && !WaterPurity.hasPurity(filled))
            WaterPurity.addPurity(filled, purity.get());
        return result;
    }

    @Inject(method = DRAIN_ITEM, at = @At("HEAD"), remap = false)
    private void droplets_of_thirst$readItemPurity(ItemStack item, Level level, BlockPos pos, boolean simulate, boolean doDrain, CallbackInfoReturnable<InteractionResultHolder<ItemStack>> cir, @Share("purity") LocalIntRef purity)
    {
        boolean tankEmpty = ((SoftFluidTank) (Object) this).isEmpty();
        purity.set(tankEmpty && WaterPurity.hasPurity(item) ? WaterPurity.getPurity(item) : -1);
    }

    @ModifyReturnValue(method = DRAIN_ITEM, at = @At("RETURN"), remap = false)
    private InteractionResultHolder<ItemStack> droplets_of_thirst$addTankPurity(InteractionResultHolder<ItemStack> result, ItemStack item, Level level, BlockPos pos, boolean simulate, boolean doDrain, @Share("purity") LocalIntRef purity)
    {
        SoftFluidStack fluid = ((SoftFluidTank) (Object) this).getFluid();
        if (purity.get() >= 0 && result.getResult().consumesAction() && !fluid.isEmpty() && fluid.is(MLBuiltinSoftFluids.WATER) && !fluid.getComponents().has(DropletsDataComponents.PURITY))
            fluid.set(DropletsDataComponents.PURITY, purity.get());
        return result;
    }
}
