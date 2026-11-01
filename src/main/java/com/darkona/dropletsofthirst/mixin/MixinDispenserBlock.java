package com.darkona.dropletsofthirst.mixin;

import com.darkona.dropletsofthirst.content.purity.WaterPurity;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.block.entity.DispenserBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(DispenserBlock.class)
public abstract class MixinDispenserBlock
{
    @WrapOperation(method = "dispenseFrom", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/entity/DispenserBlockEntity;setItem(ILnet/minecraft/world/item/ItemStack;)V"))
    private void droplets_of_thirst$worldWaterPurity(DispenserBlockEntity dispenser, int slot, ItemStack result, Operation<Void> original,
                                                @Local(argsOnly = true) ServerLevel level, @Local(argsOnly = true) BlockPos pos, @Local(ordinal = 0) ItemStack dispensed)
    {
        if (result.getItem() != dispensed.getItem())
            WaterPurity.dispenserFilled(level, pos, dispenser.getBlockState(), result);
        original.call(dispenser, slot, result);
    }
}
